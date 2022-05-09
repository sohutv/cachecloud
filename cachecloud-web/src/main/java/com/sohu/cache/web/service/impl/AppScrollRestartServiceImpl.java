package com.sohu.cache.web.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sohu.cache.constant.InstanceStatusEnum;
import com.sohu.cache.dao.ConfigRestartRecordDao;
import com.sohu.cache.dao.InstanceConfigDao;
import com.sohu.cache.dao.MachineDao;
import com.sohu.cache.entity.*;
import com.sohu.cache.exception.SSHException;
import com.sohu.cache.protocol.MachineProtocol;
import com.sohu.cache.redis.AssistRedisService;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.redis.RedisDeployCenter;
import com.sohu.cache.redis.enums.DirEnum;
import com.sohu.cache.ssh.SSHUtil;
import com.sohu.cache.stats.instance.InstanceDeployCenter;
import com.sohu.cache.task.constant.InstanceRoleEnum;
import com.sohu.cache.util.IdempotentConfirmer;
import com.sohu.cache.util.StringUtil;
import com.sohu.cache.util.TypeUtil;
import com.sohu.cache.web.enums.AppTypeEnum;
import com.sohu.cache.web.enums.BooleanEnum;
import com.sohu.cache.web.enums.MasterSlaveExistEnum;
import com.sohu.cache.web.enums.RestartStatusEnum;
import com.sohu.cache.web.service.AppScrollRestartService;
import com.sohu.cache.web.service.AppService;
import com.sohu.cache.web.util.DateUtil;
import com.sohu.cache.web.util.Page;
import com.sohu.cache.web.vo.AppRedisConfigVo;
import com.sohu.cache.web.vo.ExecuteResult;
import com.sohu.cache.web.vo.MasterSlaveGroupBo;
import com.sohu.cache.web.vo.RedisConfigVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author: zengyizhao
 * @DateTime: 2021/9/13 16:10
 * @Description:
 */
@Slf4j
@Service
public class AppScrollRestartServiceImpl implements AppScrollRestartService {

    @Autowired
    private MachineDao machineDao;

    @Autowired
    private InstanceConfigDao instanceConfigDao;

    @Autowired
    private RedisCenter redisCenter;

    @Autowired
    private RedisDeployCenter redisDeployCenter;

    @Autowired
    protected InstanceDeployCenter instanceDeployCenter;

    @Autowired
    private ConfigRestartRecordDao configRestartRecordDao;

    @Autowired
    protected AppService appService;

    @Autowired
    private AssistRedisService assistRedisService;

    @Value("${cachecloud.redis.config-test.appid:#{null}}")
    private Long TEST_APP_ID;

    private static final String RESTART_LOG_KEY = "restart:log:";

    private final static String RESTART_CONFIG_KEY = "restart:config:";

    private final static String RESTART_STOP_KEY = "restart:stop:";

    private final static String INTERRUPT_STOP = ">>>>> 被中断 >>>>>";

    /**
     *
     * @param instanceInfoList
     * @param appDesc
     * @return
     */
    @Override
    public boolean handleAppInstanceInfo(List<InstanceInfo> instanceInfoList, AppDesc appDesc) {
        boolean resultFlag =  true;
        if (instanceInfoList != null && instanceInfoList.size() > 0) {
            for (InstanceInfo instanceInfo : instanceInfoList) {
                int type = instanceInfo.getType();
                if (instanceInfo.getStatus() != InstanceStatusEnum.GOOD_STATUS.getStatus()) {
                    continue;
                }
                if (TypeUtil.isRedisType(type)) {
                    if (TypeUtil.isRedisSentinel(type)) {
                        continue;
                    }
                    String host = instanceInfo.getIp();
                    int port = instanceInfo.getPort();
                    // 幂等操作
                    BooleanEnum isMaster = BooleanEnum.OTHER;
                    int retryTime = 3;
                    while (BooleanEnum.OTHER.equals(isMaster) && retryTime-- > 0){
                        isMaster = redisCenter.isMaster(instanceInfo.getAppId(), host, port);
                    }
                    if(BooleanEnum.OTHER.equals(isMaster)){
                        return false;
                    }
                    instanceInfo.setRoleDesc(isMaster);
                    if (BooleanEnum.FALSE == isMaster) {
                        retryTime = 3;
                        HostAndPort hap = null;
                        while (hap == null && retryTime-- > 0){
                            hap = redisCenter.getMaster(host, port, appDesc.getPasswordMd5());
                        }
                        if(hap == null){
                            return false;
                        }
                        if (hap != null) {
                            instanceInfo.setMasterHost(hap.getHost());
                            instanceInfo.setMasterPort(hap.getPort());
                            for (InstanceInfo innerInfo : instanceInfoList) {
                                if (innerInfo.getIp().equals(hap.getHost())
                                        && innerInfo.getPort() == hap.getPort()) {
                                    instanceInfo.setMasterInstanceId(innerInfo.getId());
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return resultFlag;
    }

    @Override
    public Map<Integer, List<InstanceInfo>> instanceGroupByMaster(List<InstanceInfo> instanceList) {
        Map<Integer, List<InstanceInfo>> resultMap = new HashMap<Integer, List<InstanceInfo>>();
        for (InstanceInfo info : instanceList) {
            String roleDesc = info.getRoleDesc();
            if (roleDesc != null && roleDesc.equals("master")) {
                List<InstanceInfo> list = (ArrayList<InstanceInfo>) MapUtils.getObject(resultMap, info.getId(), new ArrayList<InstanceInfo>());
                list.add(info);
                resultMap.put(info.getId(), list);
            } else if (roleDesc != null && roleDesc.equals("slave")) {
                List<InstanceInfo> list = (ArrayList<InstanceInfo>) MapUtils.getObject(resultMap, info.getMasterInstanceId(), new ArrayList<InstanceInfo>());
                list.add(info);
                resultMap.put(info.getMasterInstanceId(), list);
            }
        }
        return resultMap;
    }

    @Override
    public long generateAndSaveConfigRestartRecord(AppUser appUser, AppDesc appDesc, AppRedisConfigVo appRedisConfigVo, Integer opertateType, List<InstanceInfo> instanceInfoList){
        ConfigRestartRecord configRestartRecord = new ConfigRestartRecord();
        configRestartRecord.setAppId(appDesc.getAppId());
        configRestartRecord.setAppName(appDesc.getName());
        configRestartRecord.setParam(JSONObject.toJSONString(appRedisConfigVo));
        if(opertateType == null){
            configRestartRecord.setOperateType(appRedisConfigVo.isConfigFlag() == true ? 1 : 0);
        }else{
            configRestartRecord.setOperateType(opertateType);
        }
        List<Integer> instanceIdList = new ArrayList<>();
        if(instanceInfoList != null && instanceInfoList.size() > 0){
            instanceInfoList.forEach(instanceInfo -> instanceIdList.add(instanceInfo.getId()));
        }
        configRestartRecord.setInstances(JSONObject.toJSONString(instanceIdList));
        Date date = new Date();
        configRestartRecord.setCreateTime(date);
        configRestartRecord.setEndTime(date);
        configRestartRecord.setStartTime(date);
        configRestartRecord.setUpdateTime(date);
        configRestartRecord.setStatus(RestartStatusEnum.RUNNING.getValue());
        configRestartRecord.setUserId(appUser.getId());
        configRestartRecord.setUserName(appUser.getChName());
        this.saveConfigRestartRecord(configRestartRecord);
        return configRestartRecord.getId();
    }

    @Override
    public void saveConfigRestartRecord(ConfigRestartRecord configRestartRecord) {
        configRestartRecordDao.save(configRestartRecord);
    }

    @Override
    public void updateConfigRestartRecord(ConfigRestartRecord configRestartRecord) {
        configRestartRecordDao.updateByCondition(configRestartRecord);
    }

    @Override
    public ConfigRestartRecord getConfigRestartRecord(long id) {
        ConfigRestartRecord configRestartRecord = configRestartRecordDao.getById(id);
        if(RestartStatusEnum.WAITING.getValue() == configRestartRecord.getStatus()
                || RestartStatusEnum.RUNNING.getValue() == configRestartRecord.getStatus()){
            List<String> logList = assistRedisService.lrange(RESTART_LOG_KEY + id, 0, -1);
            configRestartRecord.setLog(JSONObject.toJSONString(logList));
        }
        return configRestartRecord;
    }

    @Override
    public List<ConfigRestartRecord> getConfigRestartRecordByCondition(Model model, ConfigRestartRecord configRestartRecordParam, int pageNo, int pageSize) {
        int totalCount = configRestartRecordDao.getCountByCondition(configRestartRecordParam);

        Page page = new Page(pageNo, pageSize, totalCount);
        model.addAttribute("page", page);
        // 分页相关:list
        configRestartRecordParam.setPage(page);
        List<ConfigRestartRecord> listByCondition = configRestartRecordDao.getListByCondition(configRestartRecordParam);
        listByCondition.stream().forEach(configRestartRecord -> {
            if(RestartStatusEnum.WAITING.getValue() == configRestartRecord.getStatus()
                    || RestartStatusEnum.RUNNING.getValue() == configRestartRecord.getStatus()
                    || RestartStatusEnum.NEED_RESTART.getValue() == configRestartRecord.getStatus()
                    || RestartStatusEnum.RESTART_AFTER_CONFIG.getValue() == configRestartRecord.getStatus()
            ){
                List<String> logList = assistRedisService.lrange(RESTART_LOG_KEY + configRestartRecord.getId(), 0, -1);
                configRestartRecord.setLog(JSONObject.toJSONString(logList));
            }
        });
        return listByCondition;
    }

    @Override
    public void saveConfigRestartLog(long id, String log) {
        assistRedisService.rpush(RESTART_LOG_KEY + id, log);
    }

    @Override
    public List<String> getAndDeleteConfigRestartLog(long id){
        List<String> logList = assistRedisService.lrange(RESTART_LOG_KEY + id, 0, -1);
        assistRedisService.remove(RESTART_LOG_KEY + id);
        return logList;
    }

    @Override
    public void deleteConfigRestartLog(long id){
        assistRedisService.remove(RESTART_LOG_KEY + id);
    }

    /**
     * 更新修改配置、重启记录
     * @param recordId
     * @param restartStatusEnum
     * @param lastLog
     */
    @Override
    public void updateConfigRestartRecord(long recordId, RestartStatusEnum restartStatusEnum, String... lastLog) {
        ConfigRestartRecord configRestartRecord = new ConfigRestartRecord();
        configRestartRecord.setId(recordId);
        configRestartRecord.setStatus(restartStatusEnum.getValue());
        List<String> logList = new ArrayList<>();
        if(!RestartStatusEnum.NEED_RESTART.equals(restartStatusEnum)
            && !RestartStatusEnum.RESTART_AFTER_CONFIG.equals(restartStatusEnum)){
            logList = this.getAndDeleteConfigRestartLog(recordId);
        }
        if(lastLog != null && lastLog.length > 0){
            logList.addAll(Arrays.asList(lastLog));
        }
        if(!CollectionUtils.isEmpty(logList)){
            configRestartRecord.setLog(JSONObject.toJSONString(logList));
        }
        configRestartRecord.setEndTime(new Date());
        this.updateConfigRestartRecord(configRestartRecord);
    }

    /**
     * 添加停止滚动重启标志
     * @param appId
     * @return
     */
    @Override
    public boolean addStopRestartFlag(Long appId) {
        boolean result = false;
        result = assistRedisService.set(RESTART_STOP_KEY + appId, 1);
        return result;
    }

    /**
     * 删除停止滚动重启标志
     * @param appId
     * @return
     */
    @Override
    public boolean deleteStopRestartFlag(Long appId) {
        boolean result = false;
        result = assistRedisService.del(RESTART_STOP_KEY + appId);
        return result;
    }

    /**
     * 判断是否有停止滚动重启标志
     * @param appId
     * @return
     */
    @Override
    public boolean existsStopRestartFlag(Long appId){
        boolean result = false;
        result = assistRedisService.exists(RESTART_STOP_KEY + appId);
        return result;
    }

    /**
     * 滚动重启处理
     * @param appDesc
     * @param instanceInfoList
     * @param appRedisConfigVo
     * @return
     */
    @Override
    public ExecuteResult handleRestart(AppUser appUser, AppDesc appDesc, List<InstanceInfo> instanceInfoList, AppRedisConfigVo appRedisConfigVo){
        if(assistRedisService.get(RESTART_CONFIG_KEY + appRedisConfigVo.getAppId()) != null){
            return ExecuteResult.error("滚动重启/修改配置正在执行中，不允许重复操作。");
        }
        log.info(String.format("restart info, appId: %s, instanceInfoList: %s, paramInfo: %s", appDesc.getAppId(), instanceInfoList, appRedisConfigVo));
        //拆分实例为，主从分组
        Map<Integer, List<InstanceInfo>> groupMap = this.instanceGroupByMaster(instanceInfoList);
        //筛选出需要处理的实例
        List<Integer> instanceIdList = appRedisConfigVo.getInstanceList();
        List<InstanceInfo> pointedInstanceList = this.filterPointedInstance(instanceInfoList, instanceIdList);

        //走重启逻辑, 校验相关的实例是否为主从备份
        boolean check = this.checkIsMasterSlavePair(pointedInstanceList, groupMap);
        if(assistRedisService.get(RESTART_CONFIG_KEY + appRedisConfigVo.getAppId()) != null){
            return ExecuteResult.error("滚动重启/修改配置正在执行中，不允许重复操作。");
        }
        if(!check){
            String tipInfo = "集群主从实例不满足操作";
            return ExecuteResult.error(tipInfo);
        }

        if(assistRedisService.get(RESTART_CONFIG_KEY + appRedisConfigVo.getAppId()) != null){
            return ExecuteResult.error("滚动重启/修改配置正在执行中，不允许重复操作。");
        }
        //按实例主从分组处理——重启实例, 异步
        AsyncRestartService asyncRestartService = new AsyncRestartService(appUser, appDesc, appRedisConfigVo, instanceInfoList, pointedInstanceList, groupMap);
        asyncRestartService.start();
        return ExecuteResult.ok("重启进行中，请前往查看进程记录。");
    }

    /**
     * 处理修改配置
     * @param appUser
     * @param appDesc
     * @param instanceInfoList
     * @param appRedisConfigVo
     * @return
     */
    @Override
    public Map<String,Object> handleConfig(AppUser appUser, AppDesc appDesc, List<InstanceInfo> instanceInfoList, AppRedisConfigVo appRedisConfigVo){
        Map<String, Object> map = new HashMap<>();
        map.put("commandSet", false);
        //拆分实例为，主从分组
        Map<Integer, List<InstanceInfo>> groupMap = this.instanceGroupByMaster(instanceInfoList);
        //处理配置项，将相同配置名的归为一组进行处理
        Map<String, Set<String>> redisConfigMap = this.groupRedisConfigByName(appRedisConfigVo.getConfigList());

        //校验配置项合法性
        boolean isSupport = this.checkConfigIsSupport(appDesc, redisConfigMap);
        if(!isSupport){
            map.put("errorInfo", String.format("配置模板中无此配置项：%s", redisConfigMap));
            return map;
        }
        //筛选出需要处理的实例
        List<Integer> instanceIdList = appRedisConfigVo.getInstanceList();
        List<InstanceInfo> pointedInstanceList = this.filterPointedInstance(instanceInfoList, instanceIdList);
        //获取一个实例用于check config set是否可用
        InstanceInfo toCheckInstance = this.getInstanceToCheckConfigSet(instanceInfoList, instanceIdList);
        //通过一个实例运行config set 判断是否成功，成功则全部通过进行处理。
        Map<String, String> toCheckInstanceConfig = this.getConfigByCommand(appDesc, toCheckInstance, redisConfigMap);
        int checkResult = this.checkConfigSetAvailable(appDesc, toCheckInstance, redisConfigMap);

        //保存重启记录
        Integer operateType = 1;
        if(checkResult == 1 || checkResult == 2){
            operateType = 2;
        }
        long recordId = this.generateAndSaveConfigRestartRecord(appUser, appDesc, appRedisConfigVo, operateType, instanceInfoList);
        appRedisConfigVo.setRecordId(recordId);
        map.put("recordId", recordId);
        // 仅修改相关实例的配置，无需重启
        String errorInfo = null;
        if(checkResult == 1){
            this.saveConfigRestartLog(recordId, this.generateLog("修改配置 >>>> 开始，共%s个实例， 配置信息为：%s", pointedInstanceList.size(), redisConfigMap));
            //获取原有配置
            List<Map<String, String>> pointedInstanceConfigList = new ArrayList<>();
            for(int i = 0; i < pointedInstanceList.size(); i++){
                InstanceInfo instanceInfo = pointedInstanceList.get(i);
                if(!InstanceRoleEnum.SLAVE.getInfo().equals(instanceInfo.getRoleDesc())
                        && !InstanceRoleEnum.MASTER.getInfo().equals(instanceInfo.getRoleDesc())){
                    continue;
                }
            }
            for(int i = 0; i < pointedInstanceList.size(); i++){
                InstanceInfo instanceInfo = pointedInstanceList.get(i);
                if(!InstanceRoleEnum.SLAVE.getInfo().equals(instanceInfo.getRoleDesc())
                        && !InstanceRoleEnum.MASTER.getInfo().equals(instanceInfo.getRoleDesc())){
                    continue;
                }
                //获取原配置信息
                Map<String, String> configByCommand = null;
                if(instanceInfo.equals(toCheckInstance)){
                    configByCommand = toCheckInstanceConfig;
                }else{
                    configByCommand = this.getConfigByCommand(appDesc, instanceInfo, redisConfigMap);
                }
                long startTime = System.currentTimeMillis();
                boolean configResult = this.configSetAndRewrite(appDesc, instanceInfo, redisConfigMap);
                long endTime = System.currentTimeMillis();
                this.saveConfigRestartLog(recordId, this.generateLog("第%s个实例， 实例信息：%s， 原配置信息:%s，结果：%s，耗时：%s", (i + 1), instanceInfo.getHostPort(), configByCommand, (configResult ? "成功" : "失败"), this.getTimeBetween(endTime, startTime)));
                if(!configResult){
                    errorInfo = String.format("修改配置config set/rewrite失败， 请人工确认。节点信息如下：appId:%s，instanceId:%s，hostPort:%s", instanceInfo.getAppId(), instanceInfo.getId(), instanceInfo.getHostPort());
                    break;
                }
            }
            map.put("commandSet", true);
            map.put("errorInfo", errorInfo);
            if(StringUtil.isBlank(errorInfo)){
                this.updateConfigRestartRecord(recordId, RestartStatusEnum.SUCCESS, this.generateLog("修改配置 >>>> 结束"));
            }else{
                this.updateConfigRestartRecord(recordId, RestartStatusEnum.FAIL, this.generateLog(errorInfo));
            }
            return map;
        }
        if(checkResult == 2){
            errorInfo = String.format("配置值不合法，配置：%s", redisConfigMap);
            map.put("commandSet", false);
            map.put("errorInfo", errorInfo);
            this.updateConfigRestartRecord(recordId, RestartStatusEnum.FAIL, this.generateLog(errorInfo));
            return map;
        }
        //走重启逻辑, 校验相关的实例是否为主从备份
        boolean check = this.checkIsMasterSlavePair(pointedInstanceList, groupMap);
        if(!check){
            errorInfo = "集群主从实例不满足操作";
            map.put("errorInfo", errorInfo);
            this.updateConfigRestartRecord(recordId, RestartStatusEnum.FAIL, this.generateLog(errorInfo));
            return map;
        }

        //在测试机上进行测试
        boolean testFlag = this.testUpdateConfigFileAndRestart(appDesc.getAppId(), redisConfigMap);
        if(!testFlag){
            errorInfo = String.format("配置有严重错误或不支持，配置： %s", redisConfigMap);
            map.put("commandSet", false);
            map.put("errorInfo", errorInfo);
            this.updateConfigRestartRecord(recordId, RestartStatusEnum.FAIL, this.generateLog(errorInfo));
            return map;
        }

        //按实例主从分组处理——修改配置实例
        errorInfo = this.handleConfigByGroup(appDesc, appRedisConfigVo, pointedInstanceList, groupMap, redisConfigMap);
        map.put("errorInfo", errorInfo);
        return map;
    }

    /**
     * 校验相关的实例是否为主从备份
     * @param pointedInstanceList 指定的某些节点, 可为空
     * @param groupMap 已根据主从进行分组，参见#AppScrollRestartService.instanceGroupByMaster
     * @return false:不满足主从，true满足
     */
    private boolean checkIsMasterSlavePair(List<InstanceInfo> pointedInstanceList, Map<Integer, List<InstanceInfo>> groupMap){
        if(CollectionUtils.isEmpty(pointedInstanceList)){
            //校验是否均为主从备份
            Set<Map.Entry<Integer, List<InstanceInfo>>> entries = groupMap.entrySet();
            for (Map.Entry<Integer, List<InstanceInfo>> entry : entries){
                List<InstanceInfo> instanceInfos = entry.getValue();
                if(instanceInfos.size() < 2){
                    return false;
                }
            }
        }else{
            for(InstanceInfo instanceInfo : pointedInstanceList){
                if(InstanceRoleEnum.MASTER.getInfo().equals(instanceInfo.getRoleDesc())){
                    List<InstanceInfo> instanceInfos = groupMap.get(instanceInfo.getId());
                    if(instanceInfos.size() < 2){
                        return false;
                    }
                }
                if(InstanceRoleEnum.SLAVE.getInfo().equals(instanceInfo.getRoleDesc())){
                    List<InstanceInfo> instanceInfos = groupMap.get(instanceInfo.getMasterInstanceId());
                    if(instanceInfos.size() < 2){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 从实例中获取一个实例去进行config set 校验
     * @param instanceInfoList
     * @param instanceIdList
     * @return
     */
    private InstanceInfo getInstanceToCheckConfigSet(List<InstanceInfo> instanceInfoList, List<Integer> instanceIdList){
        InstanceInfo toCheckInstance = null;
        if(CollectionUtils.isEmpty(instanceIdList)){
            for(InstanceInfo instanceInfo : instanceInfoList){
                if(InstanceRoleEnum.SLAVE.getInfo().equals(instanceInfo.getRoleDesc())){
                    toCheckInstance = instanceInfo;
                    break;
                }
            }
            if(toCheckInstance == null){
                toCheckInstance = instanceInfoList.get(0);
            }
        }else{
            for(InstanceInfo instanceInfo : instanceInfoList){
                if(instanceInfo.getId() == instanceIdList.get(0)){
                    toCheckInstance = instanceInfo;
                    break;
                }
            }
        }
        return toCheckInstance;
    }

    /**
     * 筛选出指定实例
     * @param instanceInfoList
     * @param instanceIdList
     * @return
     */
    private List<InstanceInfo> filterPointedInstance(List<InstanceInfo> instanceInfoList, List<Integer> instanceIdList){
        if(CollectionUtils.isEmpty(instanceIdList)){
            return instanceInfoList;
        }
        List<InstanceInfo> filterInstanceList = new ArrayList<>();
        for(Integer instanceId : instanceIdList){
            for(InstanceInfo instanceInfo : instanceInfoList){
                if(instanceId == instanceInfo.getId()){
                    filterInstanceList.add(instanceInfo);
                    break;
                }
            }
        }
        return filterInstanceList;
    }

    class AsyncRestartService extends Thread{

        private AppUser appUser;

        private AppDesc appDesc;

        private AppRedisConfigVo appRedisConfigVo;

        private List<InstanceInfo> instanceInfoList;

        private List<InstanceInfo> pointedInstanceList;

        private Map<Integer, List<InstanceInfo>> groupMap;

        public AsyncRestartService(){

        }

        public AsyncRestartService(AppUser appUser, AppDesc appDesc, AppRedisConfigVo appRedisConfigVo, List<InstanceInfo> instanceInfoList, List<InstanceInfo> pointedInstanceList, Map<Integer, List<InstanceInfo>> groupMap){
            this.appUser = appUser;
            this.appDesc = appDesc;
            this.appRedisConfigVo = appRedisConfigVo;
            this.instanceInfoList = instanceInfoList;
            this.pointedInstanceList = pointedInstanceList;
            this.groupMap = groupMap;
        }

        @Override
        public void run() {
            handleRestartByGroup(appUser, appDesc, appRedisConfigVo, instanceInfoList, pointedInstanceList, groupMap);
        }
    }

    /**
     * 按实例主从分组重启, 异步
     * @param appDesc
     * @param groupMap
     * @return
     */
    @Override
    public void handleRestartByGroup(AppUser appUser, AppDesc appDesc, AppRedisConfigVo appRedisConfigVo, List<InstanceInfo> instanceInfoList, List<InstanceInfo> pointedInstanceList, Map<Integer, List<InstanceInfo>> groupMap){
        Long recordId = null;
        boolean restartAfterConfig = false;
        //保存重启记录
        if(appRedisConfigVo.getRecordId() == null){
            recordId = this.generateAndSaveConfigRestartRecord(appUser, appDesc, appRedisConfigVo, null, instanceInfoList);
            appRedisConfigVo.setRecordId(recordId);
        }else{
            recordId = appRedisConfigVo.getRecordId();
            restartAfterConfig = true;
        }

        if (!assistRedisService.setNx(RESTART_CONFIG_KEY + appRedisConfigVo.getAppId(), "1")) {
            this.updateConfigRestartRecord(recordId, RestartStatusEnum.FAIL, this.generateLog("滚动重启/修改配置正在执行中，不允许重复操作。"));
            return;
        }
        if(assistRedisService.exists(RESTART_STOP_KEY + appRedisConfigVo.getAppId())){
            assistRedisService.del(RESTART_STOP_KEY + appRedisConfigVo.getAppId());
        }
        try{
            if(restartAfterConfig){
                this.updateConfigRestartRecord(recordId, RestartStatusEnum.RESTART_AFTER_CONFIG);
            }
            log.info(String.format("restart by group, start, appId: %s, paramInfo: %s", appDesc.getAppId(), appRedisConfigVo));
            ExecuteResult executeResult =  new ExecuteResult();
            executeResult.setSuccess(false);
            boolean transferFlag = appRedisConfigVo.isTransferFlag();
            Set<Map.Entry<Integer, List<InstanceInfo>>> entries = groupMap.entrySet();
            //------------------------
            int groupCount = 0;
            for(Map.Entry<Integer, List<InstanceInfo>> map : entries) {
                MasterSlaveGroupBo masterSlaveGroupBo = this.getOneMasterSlaveGroup(pointedInstanceList, map);
                MasterSlaveExistEnum masterSlaveExistEnum = masterSlaveGroupBo.getMasterSlaveExistEnum();//1:only master;2:only slave;3:master and slave
                if (MasterSlaveExistEnum.NONE.equals(masterSlaveExistEnum)) {
                    continue;
                }
                groupCount++;
            }
            this.saveConfigRestartLog(recordId, this.generateLog("滚动重启 >>>>>> 开始，主从分组共%s个", groupCount));
            long initialStartTime = System.currentTimeMillis();
            //-------------------
            int runGroup = 0;
            for(Map.Entry<Integer, List<InstanceInfo>> map : entries){
                MasterSlaveGroupBo masterSlaveGroupBo = this.getOneMasterSlaveGroup(pointedInstanceList, map);
                MasterSlaveExistEnum masterSlaveExistEnum = masterSlaveGroupBo.getMasterSlaveExistEnum();//1:only master;2:only slave;3:master and slave
                InstanceInfo oneGroupMaster = masterSlaveGroupBo.getMaster();
                List<InstanceInfo> oneGroupSlave = masterSlaveGroupBo.getSlaveList();
                if(MasterSlaveExistEnum.NONE.equals(masterSlaveExistEnum)){
                    continue;
                }
                runGroup++;
                //处理当前某一分组
                List<String> slaveStrList = new ArrayList<>();
                oneGroupSlave.forEach(slaveInfo -> slaveStrList.add(slaveInfo.getHostPort()));
                if(this.existsStopRestartFlag(appRedisConfigVo.getAppId())){
                    if(restartAfterConfig){
                        this.saveConfigRestartLog(recordId, this.generateLog("滚动重启 >>>>>> 被中断，请注意配置文件已修改，需人工判定生效范围及影响"));
                    }else{
                        this.saveConfigRestartLog(recordId, this.generateLog("滚动重启 >>>>>> 被中断，请确认影响"));
                    }
                    this.updateConfigRestartRecord(recordId, RestartStatusEnum.INTERUPT);
                    return;
                }
                this.saveConfigRestartLog(recordId, this.generateLog("处理第%s个主从分组开始，主节点信息：%s，从节点信息：%s，主从存在标志：%s", runGroup, oneGroupMaster.getHostPort(), slaveStrList, masterSlaveExistEnum.getInfo()));
                long startTime = System.currentTimeMillis();
                executeResult = this.restartOneGroup(recordId, runGroup, appDesc, oneGroupMaster, oneGroupSlave, masterSlaveExistEnum, transferFlag);
                long endTime = System.currentTimeMillis();
                this.saveConfigRestartLog(recordId,
                        this.generateLog("处理第%s个主从分组结束。结果为：%s，耗时：%s", runGroup,
                                (executeResult.isSuccess() ? "成功" : executeResult.getMessage()), this.getTimeBetween(endTime, startTime)));
                if(!executeResult.isSuccess()){
                    //中断
                    if(executeResult.getMessage() != null && executeResult.getMessage().contains(INTERRUPT_STOP)){
                        long finalEndTime = System.currentTimeMillis();
                        if(restartAfterConfig){
                            this.saveConfigRestartLog(recordId, this.generateLog("滚动重启 >>>>>> 结束，被中断，请注意配置文件已修改，需人工判定生效范围及影响，主从分组共%s个，耗时：%s", groupCount, this.getTimeBetween(finalEndTime, initialStartTime)));
                        }else{
                            this.saveConfigRestartLog(recordId, this.generateLog("滚动重启 >>>>>> 结束，被中断，主从分组共%s个，耗时：%s", groupCount, this.getTimeBetween(finalEndTime, initialStartTime)));
                        }
                        this.updateConfigRestartRecord(recordId, RestartStatusEnum.INTERUPT);
                        return;
                    }else{
                        //异常结束
                        long finalEndTime = System.currentTimeMillis();
                        this.saveConfigRestartLog(recordId, this.generateLog("滚动重启 >>>>>> 结束，失败，耗时：%s", groupCount, this.getTimeBetween(finalEndTime, initialStartTime)));
                        this.updateConfigRestartRecord(recordId, RestartStatusEnum.FAIL);
                        return;
                    }
                }
            }
            long finalEndTime = System.currentTimeMillis();
            this.saveConfigRestartLog(recordId, this.generateLog("滚动重启 >>>>>> 结束，主从分组共%s个，耗时：%s", groupCount, this.getTimeBetween(finalEndTime, initialStartTime)));
            //正常结束
            this.updateConfigRestartRecord(recordId, RestartStatusEnum.SUCCESS);
            return;
        }catch (Exception e){
            log.error("handleRestartByGroup error: ", e);
        }finally {
            assistRedisService.remove(RESTART_CONFIG_KEY + appRedisConfigVo.getAppId());
            this.deleteStopRestartFlag(appRedisConfigVo.getAppId());
        }
    }

    /**
     * 按实例主从分组配置
     * @param appDesc
     * @param groupMap
     * @return
     */
    private String handleConfigByGroup(AppDesc appDesc, AppRedisConfigVo appRedisConfigVo, List<InstanceInfo> pointedInstanceList, Map<Integer, List<InstanceInfo>> groupMap, Map<String, Set<String>> redisConfigMap){
        Long recordId = appRedisConfigVo.getRecordId();
        boolean transferFlag = appRedisConfigVo.isTransferFlag();
        StringBuilder  sb = new StringBuilder();
        if(CollectionUtils.isEmpty(groupMap)){
            return null;
        }
        Set<Map.Entry<Integer, List<InstanceInfo>>> entries = groupMap.entrySet();
        //------------------------
        int groupCount = 0;
        for(Map.Entry<Integer, List<InstanceInfo>> map : entries) {
            MasterSlaveGroupBo masterSlaveGroupBo = this.getOneMasterSlaveGroup(pointedInstanceList, map);
            MasterSlaveExistEnum masterSlaveExistEnum = masterSlaveGroupBo.getMasterSlaveExistEnum();//1:only master;2:only slave;3:master and slave
            if (MasterSlaveExistEnum.NONE.equals(masterSlaveExistEnum)) {
                continue;
            }
            groupCount++;
        }
        this.saveConfigRestartLog(recordId, this.generateLog("强制修改配置 >>>> 开始，待处理主从分组共%s个，配置信息为：%s", groupCount, redisConfigMap));
        long initialStartTime = System.currentTimeMillis();
        //-------------------
        int runGroup =  0;
        for(Map.Entry<Integer, List<InstanceInfo>> map : entries){
            MasterSlaveGroupBo masterSlaveGroupBo = this.getOneMasterSlaveGroup(pointedInstanceList, map);
            MasterSlaveExistEnum masterSlaveExistEnum = masterSlaveGroupBo.getMasterSlaveExistEnum();//1:only master;2:only slave;3:master and slave
            InstanceInfo oneGroupMaster = masterSlaveGroupBo.getMaster();
            List<InstanceInfo> oneGroupSlave = masterSlaveGroupBo.getSlaveList();
            if(MasterSlaveExistEnum.NONE.equals(masterSlaveExistEnum)){
                continue;
            }
            runGroup++;
            //处理当前某一分组
            long startTime = System.currentTimeMillis();
//            this.saveConfigRestartLog(recordId, this.generateLog("强制修改配置，处理第%s个主从分组开始", runGroup));
            String oneGroupError = this.configOneGroup(recordId, runGroup, appDesc, oneGroupMaster, oneGroupSlave, redisConfigMap, masterSlaveExistEnum, transferFlag);
            long endTime = System.currentTimeMillis();
            this.saveConfigRestartLog(recordId, this.generateLog("第%s个主从分组结束。结果为：%s，耗时：%s" , runGroup, (StringUtil.isBlank(oneGroupError) ? "成功" : oneGroupError), this.getTimeBetween(endTime, startTime)));
            if(StringUtils.isNotEmpty(oneGroupError)){
                sb.append(oneGroupError);
                sb.append(";\n");
                this.updateConfigRestartRecord(recordId, RestartStatusEnum.FAIL);
                return sb.toString();
            }
        }
        long finalEndTime = System.currentTimeMillis();
        if(StringUtil.isBlank(sb.toString())){
            this.saveConfigRestartLog(recordId, this.generateLog("强制修改配置 >>>> 结束，结果为：%s，耗时：%s", (StringUtil.isBlank(sb.toString()) ? "成功" : sb.toString()), this.getTimeBetween(finalEndTime, initialStartTime)));
            this.saveConfigRestartLog(recordId, this.generateLog(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"));
            this.updateConfigRestartRecord(recordId, RestartStatusEnum.NEED_RESTART);
        }else{
            this.updateConfigRestartRecord(recordId, RestartStatusEnum.FAIL, this.generateLog("强制修改配置 >>>> 结束，结果为：失败，失败信息：%s，耗时：%s", (StringUtil.isBlank(sb.toString()) ? "成功" : sb.toString()), this.getTimeBetween(finalEndTime, initialStartTime)));
            this.saveConfigRestartLog(recordId, this.generateLog(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"));
        }
        return sb.toString();
    }

    /**
     * （根据指定实例），拆分出主节点，从节点（多个），及主从节点是否均包含
     * @param pointedInstanceList
     * @param map
     * @return
     */
    private MasterSlaveGroupBo getOneMasterSlaveGroup(List<InstanceInfo> pointedInstanceList, Map.Entry<Integer, List<InstanceInfo>> map){
        MasterSlaveGroupBo masterSlaveGroupBo = new MasterSlaveGroupBo();
        int masterSlaveFlag = 0;//1:only master;2:only slave;3:master and slave
        InstanceInfo oneGroupMaster = null;
        List<InstanceInfo> oneGroupSlave = new ArrayList<>();
        if(CollectionUtils.isEmpty(pointedInstanceList)){
            List<InstanceInfo> oneGroupList = map.getValue();
            //获取当前分组主节点、从节点列表
            for (InstanceInfo instanceInfo : oneGroupList) {
                if(InstanceRoleEnum.MASTER.getInfo().equals(instanceInfo.getRoleDesc())){
                    oneGroupMaster = instanceInfo;
                }
                if(InstanceRoleEnum.SLAVE.getInfo().equals(instanceInfo.getRoleDesc())){
                    oneGroupSlave.add(instanceInfo);
                }
            }
            masterSlaveFlag = 3;
        }else{
            List<InstanceInfo> oneGroupList = map.getValue();
            //从指定的实例中，获取当前分组待处理的主节点，从节点列表
            for(int i = 0; i < pointedInstanceList.size(); i++){
                InstanceInfo instanceInfo = pointedInstanceList.get(i);
                if(InstanceRoleEnum.MASTER.getInfo().equals(instanceInfo.getRoleDesc()) && instanceInfo.getId() == map.getKey()){
                    oneGroupMaster = instanceInfo;
                    masterSlaveFlag = masterSlaveFlag > 0 ? 3 : 1;
                }else{
                    if(InstanceRoleEnum.SLAVE.getInfo().equals(instanceInfo.getRoleDesc()) && instanceInfo.getMasterInstanceId() == map.getKey()){
                        oneGroupSlave.add(instanceInfo);
                        masterSlaveFlag = masterSlaveFlag > 0 ? (masterSlaveFlag == 2 ? 2: 3) : 2;

                    }
                }
                if(i == pointedInstanceList.size() - 1 && masterSlaveFlag == 1){
                    for (InstanceInfo oneGroupInstance : oneGroupList) {
                        if(InstanceRoleEnum.SLAVE.getInfo().equals(oneGroupInstance.getRoleDesc())){
                            oneGroupSlave.add(oneGroupInstance);
                            break;
                        }
                    }
                }
                if(i == pointedInstanceList.size() - 1 && masterSlaveFlag == 2){
                    for (InstanceInfo oneGroupInstance : oneGroupList) {
                        if(InstanceRoleEnum.MASTER.getInfo().equals(oneGroupInstance.getRoleDesc())){
                            oneGroupMaster = oneGroupInstance;
                            break;
                        }
                    }
                }
            }
        }
        masterSlaveGroupBo.setMaster(oneGroupMaster);
        masterSlaveGroupBo.setMasterSlaveExistEnum(MasterSlaveExistEnum.getByType(masterSlaveFlag));
        masterSlaveGroupBo.setSlaveList(oneGroupSlave);
        return masterSlaveGroupBo;
    }



    /**
     * 滚动重启：重启从节点，选某个从节点failover，重启原主节点，根据条件（在原主节点failover）
     * @param appDesc
     * @param master
     * @param slaveList
     * @param masterSlaveFlag
     * @param transferFlag
     * @return
     */
    private ExecuteResult restartOneGroup(long recordId, int runGroup, AppDesc appDesc, InstanceInfo master, List<InstanceInfo> slaveList, MasterSlaveExistEnum masterSlaveFlag , boolean transferFlag){
        ExecuteResult executeResult = ExecuteResult.error();
        log.info(String.format("restart by group one group, recordId: %s, master: %s, slaveList: %s, masterSlaveFlag: %s", recordId, master.getHostPort(), slaveList.stream().map(slaveInfo -> slaveInfo.getHostPort()).collect(Collectors.joining(",")), masterSlaveFlag));
        if(this.existsStopRestartFlag(appDesc.getAppId())){
            return ExecuteResult.error(String.format("本分组未开始时%s", INTERRUPT_STOP));
        }
        if(MasterSlaveExistEnum.MASTRE_SLAVE.equals(masterSlaveFlag) || MasterSlaveExistEnum.SLAVE.equals(masterSlaveFlag)){
            //重启并修改从节点
            executeResult = this.restartSlaveNodes(recordId, appDesc, slaveList, master);
            if (!executeResult.isSuccess()) {
                return executeResult;
            }
        }

        if(this.existsStopRestartFlag(appDesc.getAppId())){
            return ExecuteResult.error(String.format("从节点重启后，从节点failover未开始，主节点重启未开始时%s", INTERRUPT_STOP));
        }
        if(!MasterSlaveExistEnum.SLAVE.equals(masterSlaveFlag)){
            //failover到从节点
            Optional<String> failOverResult = this.clusterFailoverSlaveInstanceAndCheck(recordId, slaveList.get(0), master, appDesc);
            if (failOverResult.isPresent()) {
                return ExecuteResult.error(String.format("对从节点执行failover失败，信息如下：%s", failOverResult.get()));
            }
            //failover后，休息30s
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
            }

            if(this.existsStopRestartFlag(appDesc.getAppId())){
                return ExecuteResult.error(String.format("从节点重启后，从节点failover后，主节点重启未开始时%s，请注意是否允许主从切换，并人工处理。", INTERRUPT_STOP));
            }
            //重启原主节点
            Optional<String> restartResult = this.restartSlaveNode(recordId, appDesc, master, slaveList.get(0));
            if (restartResult.isPresent()) {
                return ExecuteResult.error(restartResult.get());
            }
            if (!transferFlag) {
                if(this.existsStopRestartFlag(appDesc.getAppId())){
                    return ExecuteResult.error(String.format("从节点重启后，从节点failover后，主节点重启后，主节点failover未开始时%s，请注意是否允许主从切换，并人工处理。", INTERRUPT_STOP));
                }
                //重启后，failover前，休息30s
                //对原主节点进行failover
                failOverResult = this.clusterFailoverSlaveInstanceAndCheck(recordId, master, slaveList.get(0), appDesc);
                if (failOverResult.isPresent()) {
                    return ExecuteResult.error(String.format("对原主节点执行failover失败，信息如下：%s", failOverResult.get()));
                }
            }
        }
        return ExecuteResult.ok();
    }

    /**
     * 修改配置
     * 修改配置：//只修改从节点（关闭从节点，修改配置文件，重启从节点）
     * 修改配置：//只修改主节点（选某个从节点failover, 关闭原主节点，修改配置文件，重启原主节点，在原主节点failover）
     * 修改配置：//修改主和从节点（关闭从节点，修改从节点配置，重启从节点，选某个从节点failover, 关闭原主节点，修改原主节点配置，重启原主节点，根据条件（在原主节点failover））
     * @param appDesc
     * @param master
     * @param slaveList
     * @param masterSlaveExistEnum
     * @param transferFlag
     * @return
     */
    private String configOneGroup(long recordId, int runGroup, AppDesc appDesc, InstanceInfo master, List<InstanceInfo> slaveList, Map<String, Set<String>> redisConfigMap, MasterSlaveExistEnum masterSlaveExistEnum , boolean transferFlag){
        if(MasterSlaveExistEnum.MASTRE_SLAVE.equals(masterSlaveExistEnum) || CollectionUtils.isEmpty(redisConfigMap) || MasterSlaveExistEnum.SLAVE.equals(masterSlaveExistEnum)){
            //重启并修改从节点
            String res = this.configNodes(appDesc, recordId, runGroup, slaveList, redisConfigMap);
            if (StringUtils.isNotEmpty(res)) {
                return res;
            }
        }
        if(!MasterSlaveExistEnum.SLAVE.equals(masterSlaveExistEnum)){
            //重启并修改原主节点
            long statTime = System.currentTimeMillis();
            Map<String, String> configByCommand = this.getConfigByCommand(appDesc, master, redisConfigMap);
            String res = this.configNode(appDesc, master, redisConfigMap);
            long endTime = System.currentTimeMillis();
            this.saveConfigRestartLog(recordId, this.generateLog("第%s分组，主节点信息：%s，原配置信息:%s，结果：%s，耗时：%s", runGroup, master.getHostPort(), configByCommand, (StringUtil.isBlank(res) ? "成功" : "失败"), this.getTimeBetween(endTime, statTime)));
            if (StringUtils.isNotEmpty(res)) {
                return res;
            }
        }
        return null;
    }

    /**
     * 修改配置从节点
     * @param instanceInfoList
     * @param redisConfigMap
     * @return
     */
    private String configNodes(AppDesc appDesc, long recordId, int runGroup, List<InstanceInfo> instanceInfoList, Map<String, Set<String>> redisConfigMap) {
        StringBuilder sb = new StringBuilder();
        for(InstanceInfo instanceInfo : instanceInfoList){
            long statTime = System.currentTimeMillis();
            Map<String, String> configByCommand = this.getConfigByCommand(appDesc, instanceInfo, redisConfigMap);
            String res = this.configNode(appDesc, instanceInfo, redisConfigMap);
            long endTime = System.currentTimeMillis();
            //获取原配置信息
            this.saveConfigRestartLog(recordId, this.generateLog("第%s分组，从节点信息:%s，原配置信息:%s，结果：%s，耗时：%s", runGroup, instanceInfo.getHostPort(), configByCommand, (StringUtil.isBlank(res) ? "成功" : "失败"), this.getTimeBetween(endTime, statTime)));
            if(StringUtils.isNotEmpty(res)){
                sb.append(res);
                break;
            }
        }
        return sb.toString();
    }

    /**
     * 修改配置从节点
     * @param instanceInfo
     * @param redisConfigMap
     * @return
     */
    private String configNode(AppDesc appDesc, InstanceInfo instanceInfo, Map<String, Set<String>> redisConfigMap) {
        boolean executeFlag = false;
        if(CollectionUtils.isEmpty(redisConfigMap)) {
            return null;
        }
        //修改配置
        executeFlag = this.updateConfigFile(instanceInfo, redisConfigMap);
        if(!executeFlag){
            return String.format("关闭节点后，修改配置文件失败，需进行手动重启，节点信息如下：appId:%s，instanceId:%s，hostPort:%s", instanceInfo.getAppId(), instanceInfo.getId(), instanceInfo.getHostPort());
        }
        return null;
    }

    /**
     * 依次重启从节点，一旦有一个失败，则返回
     * @param appDesc
     * @param slaveList
     * @param currentMaster
     * @return
     */
    private ExecuteResult restartSlaveNodes(long recordId, AppDesc appDesc, List<InstanceInfo> slaveList, InstanceInfo currentMaster) {
        ExecuteResult executeResult = ExecuteResult.error();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < slaveList.size(); i++){
            InstanceInfo instanceInfo = slaveList.get(i);
            Optional<String> restartResult = this.restartSlaveNode(recordId, appDesc, instanceInfo, currentMaster);
            log.info(String.format("重启第%s个从节点，从节点信息：%s，结果为：%s", (i + 1), instanceInfo.getHostPort(), (restartResult.isPresent() ? restartResult.get() : "成功")));
//            appScrollRestartService.saveConfigRestartLog(recordId, this.generateLog("重启第%s个从节点，从节点信息：%s，结果为：%s", (i + 1), instanceInfo.getHostPort(), (restartResult.isPresent() ? restartResult.get() : "成功")));
            if(restartResult.isPresent()){
                sb.append(String.format("重启从节点失败，instance:%s，失败信息：%s", instanceInfo.getHostPort(), restartResult.get()));
                sb.append("\n");
                return executeResult.error(sb.toString());
            }else{
                sb.append(String.format("重启从节点成功，instance:%s", instanceInfo.getAppId(), instanceInfo.getHostPort()));
                sb.append("\n");
            }
        }
        return ExecuteResult.ok(sb.toString());
    }

    /**
     * 重启节点
     * @param appDesc
     * @param instanceInfo
     * @return
     */
    private Optional<String> restartNode(Long recordId, AppDesc appDesc, InstanceInfo instanceInfo) {
        log.info(String.format("restart slave node, shutdown instance, start, recordId: %s, slaveInstance: %s", recordId, instanceInfo.getHostPort()));
        boolean executeFlag = this.shutdownInstanceIdempotent(3, recordId, appDesc, instanceInfo);
        log.info(String.format("restart slave node, shutdown instance, end, recordId: %s, slaveInstance: %s, result: %s", recordId, instanceInfo.getHostPort(), executeFlag));
        if(!executeFlag){
            return Optional.of(String.format("重启从节点，关闭从节点失败，节点信息：%s", instanceInfo.getHostPort()));
        }
        log.info(String.format("restart slave node, start instance, start, recordId: %s, slaveInstance: %s", recordId, instanceInfo.getHostPort()));
        executeFlag = startInstance(instanceInfo);
        log.info(String.format("restart slave node, start instance, end, recordId: %s, slaveInstance: %s, result: %s", recordId, instanceInfo.getHostPort(), executeFlag));
        if(!executeFlag){
            return Optional.of(String.format("重启从节点，关闭从节点后，重启从节点失败，节点信息：%s", instanceInfo.getHostPort()));
        }
        return Optional.empty();
    }

    /**
     * 关闭实例，幂等操作，目前最大支持3次
     * @return
     */
    private boolean shutdownInstanceIdempotent(int retryTime, Long recordId, AppDesc appDesc, InstanceInfo instanceInfo){
        boolean executeFlag = false;
        long beginShutdown = System.currentTimeMillis();
        executeFlag = shutdownInstance(instanceInfo);
        log.info(String.format("restart slave node, shutdown instance, recordId: %s, slaveInstance: %s, result: %s", recordId, instanceInfo.getHostPort(), executeFlag));
        if(!executeFlag){
            return false;
        }
        //2. check the slave node original process has release config file
        int tryTimes = 200;
        long sleepTime = 2L;
        while(tryTimes-- > 0){
            try{
                executeFlag = this.checkFileReleaseAfterShutdown(appDesc, instanceInfo);
                if(executeFlag){
                    break;
                }
                TimeUnit.SECONDS.sleep(sleepTime);
                if(tryTimes == 195){
//                    this.saveConfigRestartLog(recordId, this.generateLog("重启从节点, shutdown and check，持续10s仍未完成，请等待留意，节点信息：%s。", instanceInfo.getHostPort()));
                }
                if(tryTimes % 5 == 0){
                    boolean shutdownFailFlag = this.checkByLog(instanceInfo, "can't exit", beginShutdown);
                    log.info(String.format("restart slave node, shutdown instance fail and retry check, recordId: %s, slaveInstance: %s, result: %s", recordId, instanceInfo.getHostPort(), shutdownFailFlag));
                    if(shutdownFailFlag && retryTime-- > 0){
                        log.info(String.format("restart slave node, shutdown instance fail and retry, recordId: %s, slaveInstance: %s, retryTime left : %s", recordId, instanceInfo.getHostPort(), retryTime));
                        TimeUnit.SECONDS.sleep(20 * (3 - retryTime) > 40 ? 40 : 20 * (3 - retryTime));
                        return this.shutdownInstanceIdempotent(retryTime, recordId, appDesc, instanceInfo);
                    }
                }
            }catch (Exception e){
                log.error("shutdown Instance error: ", e);
                continue;
            }
        }
        return executeFlag;
    }

    /**
     * 重启从节点
     * @param appDesc
     * @param slaveInstance
     * @param currentMaster
     * @return
     */
    private Optional<String> restartSlaveNode(long recordId, AppDesc appDesc, InstanceInfo slaveInstance, InstanceInfo currentMaster) {
        //判断当前实例是否为从节点，且存在主节点，且master_link_status  up
        BooleanEnum slaveAndMasterUp = BooleanEnum.FALSE;
        int tryTimes = 3;
        while (tryTimes-- > 0) {
            try {
                slaveAndMasterUp = redisCenter.isSlaveAndPointedMasterUp(appDesc, slaveInstance, currentMaster);
                if (BooleanEnum.TRUE.equals(slaveAndMasterUp)){
                    break;
                }
                TimeUnit.SECONDS.sleep(2L);
            } catch (Exception e) {
                log.error("restartSlaveNode check is Slave and master is up, error: ", e);
                continue;
            }
        }
        log.info(String.format("restart slave node, isSlaveAndPointedMasterUp check, recordId: %s, slaveInstance: %s, currentMaster: %s, slaveAndMasterUp: %s", recordId, slaveInstance.getHostPort(), currentMaster.getHostPort(), slaveAndMasterUp));
        if(BooleanEnum.FALSE.equals(slaveAndMasterUp)){
            return Optional.of("重启从节点，主从状态校验失败");
        }
        return this.restartNode(recordId, appDesc, slaveInstance);
    }

    /**
     * 执行failover并检查是否成功
     * @param slaveInstance
     * @param currentMaster
     * @param appDesc
     * @return
     */
    private Optional<String> clusterFailoverSlaveInstanceAndCheck(long recordId, InstanceInfo slaveInstance, InstanceInfo currentMaster, AppDesc appDesc){
        try{
            //判断当前实例是否为从节点，且存在主节点，且master_link_status  up
            BooleanEnum slaveAndMasterUp = BooleanEnum.FALSE;
            int tryTimes = 300;
            while (tryTimes-- > 0) {
                try {
                    BooleanEnum result = redisCenter.isSlaveAndPointedMasterUp(appDesc, slaveInstance, currentMaster);
                    if (BooleanEnum.TRUE.equals(result)){
                        slaveAndMasterUp = result;
                        break;
                    }
                    TimeUnit.SECONDS.sleep(2L);
                } catch (Exception e) {
                    log.error("clusterFailoverSlaveInstanceAndCheck is Slave and master is pointed master error: ", e);
                    continue;
                }
            }
            log.info(String.format("restart slave node, failover pre check, isSlaveAndPointedMasterUp, recordId: %s, slaveInstance: %s, currentMaster: %s, result: %s", recordId, slaveInstance.getHostPort(), currentMaster.getHostPort(), slaveAndMasterUp));

            if(BooleanEnum.FALSE.equals(slaveAndMasterUp)){
                return Optional.of(String.format("执行failover and check， 检验为从节点及与主节点连接正常，失败，从节点信息：%s，主节点信息：%s", slaveInstance.getHostPort(), currentMaster.getHostPort()));
            }
            log.info(String.format("restart slave node, failoverAndCheckIdempotent, start, recordId: %s, slaveInstance: %s, currentMaster: %s", recordId, slaveInstance.getHostPort(), currentMaster.getHostPort()));
            boolean isFailover = this.failoverAndCheckIdempotent(3, recordId, slaveInstance, currentMaster, appDesc);
            log.info(String.format("restart slave node, failoverAndCheckIdempotent, end, recordId: %s, slaveInstance: %s, currentMaster: %s, result: %s", recordId, slaveInstance.getHostPort(), currentMaster.getHostPort(), isFailover));
            if(!isFailover){
                return Optional.of(String.format("执行failover and check失败，从节点信息：%s，主节点信息：%s", slaveInstance.getHostPort(), currentMaster.getHostPort()));
            }
            return Optional.empty();
        }catch (Exception e){
            log.error("clusterFailoverSlaveInstanceAndCheck is Slave and master is pointed master error: ", e);
            return Optional.of(String.format("执行failover and check，出现异常，异常信息：%s，从节点信息：%s，主节点信息：%s", e.getMessage(), slaveInstance.getHostPort(), currentMaster.getHostPort()));
        }
    }


    /**
     * 关闭实例，幂等操作，目前最大支持3次
     * @return
     */
    private boolean failoverAndCheckIdempotent(int retryTime, long recordId, InstanceInfo slaveInstance, InstanceInfo currentMaster, AppDesc appDesc){
        long beginShutdown = System.currentTimeMillis();
        boolean executeFlag = false;
        try{
            if(AppTypeEnum.REDIS_CLUSTER.getType() == appDesc.getType()){
                executeFlag = redisDeployCenter.clusterFailover(slaveInstance.getAppId(), slaveInstance.getId(), null);
            }
        }catch (Exception e){
            log.error("cluster failover异常，异常信息：", e);
        }
        log.info(String.format("restart slave node, clusterFailover, recordId: %s, slaveInstance: %s, currentMaster: %s, result: %s", recordId, slaveInstance.getHostPort(), currentMaster.getHostPort(), executeFlag));
        if (!executeFlag) {
            return executeFlag;
        }
        int tryTimes = 200;
        long sleepTime = 2L;
        while(tryTimes-- > 0){
            try{
                executeFlag = redisCenter.getRedisReplicationStatus(slaveInstance.getAppId(), slaveInstance.getIp(), slaveInstance.getPort());
                if (executeFlag) {
                    break;
                }
                TimeUnit.SECONDS.sleep(sleepTime);
                if(tryTimes == 195){
//                    this.saveConfigRestartLog(recordId, this.generateLog("failover and check, 持续10s仍未完成，请等待留意，节点信息：%s。", slaveInstance.getHostPort()));
                }
                if(tryTimes % 5 == 0){
                    boolean shutdownFailFlag = this.checkByLog(slaveInstance, "Manual failover timed out", beginShutdown);
                    log.info(String.format("restart slave node, clusterFailover fail and retry check, recordId: %s, slaveInstance: %s, currentMaster: %s, result: %s", recordId, slaveInstance.getHostPort(), currentMaster.getHostPort(), shutdownFailFlag));
                    if(shutdownFailFlag && retryTime-- > 0){
                        log.info(String.format("restart slave node, clusterFailover fail and retry, recordId: %s, slaveInstance: %s, currentMaster: %s", recordId, slaveInstance.getHostPort(), currentMaster.getHostPort()));
                        TimeUnit.SECONDS.sleep(20 * (3 - retryTime) > 40 ? 40 : 20 * (3 - retryTime));
                        return this.failoverAndCheckIdempotent(retryTime, recordId, slaveInstance, currentMaster, appDesc);
                    }
                }
            }catch (Exception e){
                log.error("failover and check error: ", e);
                continue;
            }
        }
        return executeFlag;
    }

    /**
     * 下线实例
     * @param instanceInfo
     * @return
     */
    public boolean shutdownInstance(InstanceInfo instanceInfo) {
        if (instanceInfo != null) {
            try {
                boolean operationFlag = instanceDeployCenter.shutdownExistInstance(instanceInfo.getAppId(), instanceInfo.getId());
                log.info("shutdown slave, appId:${}, instance:${}, result:${}", instanceInfo.getAppId(), instanceInfo.getHostPort(), operationFlag);
                return operationFlag;
            } catch (Exception e) {
                log.error("shutdown slave, appId: " + instanceInfo.getAppId() +", instance:" + instanceInfo.getHostPort() + ",异常：", e);
                return false;
            }
        }
        return true;
    }

    /**
     * 启动实例
     * @param instanceInfo
     */
    public boolean startInstance(InstanceInfo instanceInfo) {
        if (instanceInfo != null) {
            try {
                boolean operationFlag = instanceDeployCenter.startExistInstanceWithoutResourceCheck(instanceInfo.getAppId(), instanceInfo.getId());
                log.info("restart slave node, start instance, appId:${}, instance:${}, result:${}", instanceInfo.getAppId(), instanceInfo.getHostPort(), operationFlag);
                return operationFlag;
            } catch (Exception e) {
                log.error("restart slave node, start instance, error, appId: " + instanceInfo.getAppId() +", instance:" + instanceInfo.getHostPort() + ",异常：", e);
                return false;
            }
        }
        return false;
    }

    /**
     * check whether config set is ok with this to updated config name
     * just support check one config name, if more than one, update this logic code
     * @param instanceInfo
     * @return 0:失败；1：成功；2：配置值错误
     */
    private int checkConfigSetAvailable(AppDesc appDesc, InstanceInfo instanceInfo, Map<String, Set<String>> redisConfigMap) {
        int checkResult = 0;
        // 1.获取连接
        final Jedis jedis = redisCenter.getJedis(appDesc.getAppId(), instanceInfo.getIp(), instanceInfo.getPort(), 5000, 5000);
        try {
            int retry = 3;
            while (retry-- > 0) {
                Set<Map.Entry<String, Set<String>>> entrySet = redisConfigMap.entrySet();
                for (Map.Entry<String, Set<String>> entry : entrySet) {
                    String configName = entry.getKey();
                    Set<String> configValueSet = entry.getValue();
                    StringBuilder sb = new StringBuilder();
                    for (String configValue : configValueSet) {
                        sb.append(configValue);
                        sb.append(" ");
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    try {
                        String result = jedis.configSet(configName, sb.toString());
                        if (result != null) {
                            log.info(String.format("check config set available, config set, appId:%s, instance:%s, configName:%s, configValue:%s, result=%s", instanceInfo.getAppId(), instanceInfo.getHostPort(), configName, sb, result));
                            if (result.equalsIgnoreCase("OK")) {
                                checkResult = 1;
                                continue;
                            } else if (result.equalsIgnoreCase("Invalid argument")) {
                                checkResult = 2;
                                continue;
                            }
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        if (e instanceof JedisDataException && e.getMessage() != null && e.getMessage().contains("Invalid argument")) {
                            checkResult = 2;
                            continue;
                        }
                    }
                }
                if(checkResult != 0){
                    break;
                }
            }
            return checkResult;
        } catch (Exception e) {
            log.error(String.format("check config set available, appId:%s, instance:%s, 异常：", instanceInfo.getAppId(), instanceInfo.getHostPort()), e);
        } finally {
            if (jedis != null)
                jedis.close();
        }
        return checkResult;
    }

    /**
     * config set and rewrite
     * @param instanceInfo
     * @return
     */
    private boolean configSetAndRewrite(AppDesc appDesc, InstanceInfo instanceInfo, Map<String, Set<String>> redisConfigMap) {
        // 1.获取连接
        final Jedis jedis = redisCenter.getJedis(appDesc.getAppId(), instanceInfo.getIp(), instanceInfo.getPort(), 5000, 5000);
        try {
            boolean isConfig = new IdempotentConfirmer() {
                @Override
                public boolean execute() {
                    boolean isRun = redisCenter.isRun(appDesc.getAppId(), instanceInfo.getIp(), instanceInfo.getPort());
                    if (!isRun) {
                        log.warn("config set and rewrite, check is run fail, instance is not run, appId:${}, instance:${}", instanceInfo.getAppId(), instanceInfo.getHostPort());
                        return false;
                    }
                    Set<Map.Entry<String, Set<String>>> entrySet = redisConfigMap.entrySet();
                    boolean isConfig =false;
                    for (Map.Entry<String, Set<String>> entry : entrySet){
                        String configName = entry.getKey();
                        Set<String> configValueSet = entry.getValue();
                        StringBuilder sb = new StringBuilder();
                        for (String configValue : configValueSet) {
                            sb.append(configValue);
                            sb.append(" ");
                        }
                        sb.deleteCharAt(sb.length() - 1);
                        String result = jedis.configSet(configName, sb.toString());
                        isConfig = result != null && result.equalsIgnoreCase("OK");
                        if (!isConfig) {
                            log.error(String.format("config set and rewrite, config set fail, appId:%s, instance:%s, configName:%s, configValue:%s, result=%s", instanceInfo.getAppId(), instanceInfo.getHostPort(), configName, sb.toString(), result));
                            return false;
                        }
                    }
                    return isConfig;
                }
            }.run();
            String response = jedis.configRewrite();
            boolean isRewrite = response != null && response.equalsIgnoreCase("OK");
            if (!isRewrite) {
                log.error(String.format("config set and rewrite, config rewrite fail, appId:%s, instance:%s", instanceInfo.getAppId(), instanceInfo.getHostPort()));
            }
            return isConfig;
        } catch (Exception e) {
            log.error(String.format("config set and rewrite, occur error, appId:%s, instance:%s, error:", instanceInfo.getAppId(), instanceInfo.getHostPort()), e);
        } finally {
            if (jedis != null)
                jedis.close();
        }
        return false;
    }

    /**
     * 通过config get获取配置项的值
     * @param appDesc
     * @param instanceInfo
     * @param redisConfigMap
     * @return
     */
    private Map<String, String> getConfigByCommand(AppDesc appDesc, InstanceInfo instanceInfo, Map<String, Set<String>> redisConfigMap){
        Map<String, String> configNameValueMap = new HashMap<>();
        Set<String> configNameSet = redisConfigMap.keySet();
        if(configNameSet.size() > 0) {
            String configValue = null;
            // 1.获取连接
            final Jedis jedis = redisCenter.getJedis(appDesc.getAppId(), instanceInfo.getIp(), instanceInfo.getPort(), 5000, 5000);
            try {
                for (String configName : configNameSet) {
                    try {
                        List<String> strings = jedis.configGet(configName);
                        if (strings != null && strings.size() > 1) {
                            configValue = strings.get(1);
                        }
                    } catch (Exception e) {
                        if (e instanceof JedisConnectionException) {
                            configValue = "连接失败，未取到值";
                        } else if (e instanceof JedisDataException) {
                            if (e.getMessage().contains("ERR unknown command `CONFIG`, with args beginning with: `get`, ")) {
                                configValue = "无此配置，未取到值";
                            }
                        }
                        if (configValue == null) {
                            configValue = "异常，未取到值";
                        }
                        log.error("getConfigByCommand", e);
                    }
                    configNameValueMap.put(configName, configValue);
                }
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return configNameValueMap;
    }

    /**
     * 將redis config 进行合并
     * @param redisConfigList
     * @return
     */
    private Map<String, Set<String>> groupRedisConfigByName(List<RedisConfigVo> redisConfigList){
        Map<String, Set<String>> map = new HashMap<>();
        for (RedisConfigVo redisConfigVo : redisConfigList) {
            if(map.containsKey(redisConfigVo.getConfigName())){
                map.get(redisConfigVo.getConfigName()).add(redisConfigVo.getConfigValue());
            }else{
                HashSet<String> valueSet = new HashSet<>();
                valueSet.add(redisConfigVo.getConfigValue());
                map.put(redisConfigVo.getConfigName(), valueSet);
            }
        }
        return map;
    }

    /**
     * config redis.conf file directly
     * @param instanceInfo
     * @return
     */
    private boolean updateConfigFile(InstanceInfo instanceInfo, Map<String, Set<String>> redisConfigMap){
        if(CollectionUtils.isEmpty(redisConfigMap)){
            return true;
        }
        String host = instanceInfo.getIp();
        int port = instanceInfo.getPort();
        int type = instanceInfo.getType();
        String confType = "";
        if (TypeUtil.isRedisCluster(type)) {
            confType = "redis-cluster-";
        } else if (TypeUtil.isRedisSentinel(type)) {
            confType = "redis-sentinel-";
        } else if (TypeUtil.isRedisStandalone(type)){
            confType = "redis-sentinel-";
        }
        String remoteFilePath = getMachineRelativeDir(host, DirEnum.CONF_DIR.getValue()) + confType + port + ".conf";
        String backFilePath = remoteFilePath + ".program.bak";
        //拷贝副本，命名统一 program.bak
        StringBuilder command = new StringBuilder();
        command.append("cp ").append(remoteFilePath).append(" ").append(backFilePath);
        log.info(String.format("update config file, copy file, appId:%s, instance:%s, command:%s", instanceInfo.getAppId(), instanceInfo.getHostPort(), command));
        boolean executeFlag = true;
        try {
            SSHUtil.execute(host, command.toString());
        } catch (SSHException e) {
            executeFlag = false;
            log.error(String.format("update config file, copy file, appId:%s, instance:%s, command:%s, error: ", instanceInfo.getAppId(), instanceInfo.getHostPort(), command), e);
        }

        Set<Map.Entry<String, Set<String>>> configSet = redisConfigMap.entrySet();
        for (Map.Entry<String, Set<String>> entry : configSet) {
            String configName = entry.getKey();
            Set<String> configValues = entry.getValue();
            if(executeFlag){
                //删除原有配置项
                StringBuilder removeConfigCommand = new StringBuilder();
                removeConfigCommand.append("sed -i '/^").append(configName).append(" /d' ").append(remoteFilePath);
                log.info(String.format("update config file, delete config name, appId:%s, instance:%s, command:%s", instanceInfo.getAppId(), instanceInfo.getHostPort(), removeConfigCommand));
                //sed -i '/save /d' redis-test-111.conf
                try {
                    SSHUtil.execute(host, removeConfigCommand.toString());
                } catch (SSHException e) {
                    executeFlag = false;
                    log.error(String.format("update config file, delete config name, appId:%s, instance:%s, command:%s, error: ", instanceInfo.getAppId(), instanceInfo.getHostPort(), removeConfigCommand), e);
                }
            }
            if(executeFlag){
                //新增新的配置项
                StringBuilder addConfigCommand = new StringBuilder();
                addConfigCommand.append("echo -e '").append("\n");
                for(String configValue : configValues){
                    addConfigCommand.append(configName).append(" ").append(configValue).append("\n");
                }
                addConfigCommand.append("' >> ").append(remoteFilePath);
                log.info(String.format("update config file, add config name value, appId:%s, instance:%s, command:%s", instanceInfo.getAppId(), instanceInfo.getHostPort(), addConfigCommand));
                try {
                    SSHUtil.execute(host, addConfigCommand.toString());
                } catch (SSHException e) {
                    executeFlag = false;
                    log.error(String.format("update config file, add config name value, appId:%s, instance:%s, command:%s, error: ", instanceInfo.getAppId(), instanceInfo.getHostPort(), addConfigCommand), e);
                }
            }
        }
        return executeFlag;
    }

    /**
     * rollback redis.conf file to last version
     * @param instanceInfo
     * @return
     */
    private boolean rollbackUpdateConfigFile(InstanceInfo instanceInfo){
        String host = instanceInfo.getIp();
        int port = instanceInfo.getPort();
        int type = instanceInfo.getType();
        String confType = "";
        if (TypeUtil.isRedisCluster(type)) {
            confType = "redis-cluster-";
        } else if (TypeUtil.isRedisSentinel(type)) {
            confType = "redis-sentinel-";
        } else if (TypeUtil.isRedisStandalone(type)) {
            confType = "redis-sentinel-";
        }
        String remoteFilePath = getMachineRelativeDir(host, DirEnum.CONF_DIR.getValue()) + confType + port + ".conf";
        String backFilePath = remoteFilePath + ".program.bak";
        //拷贝副本，命名统一 program.bak
        StringBuilder command = new StringBuilder();
        command.append("mv ").append(backFilePath).append(" ").append(remoteFilePath);
        log.info(String.format("rollback config file to last version, appId:%s, instance:%s, command:%s", instanceInfo.getAppId(), instanceInfo.getHostPort(), command));
        boolean executeFlag = true;
        try {
            String execute = SSHUtil.execute(host, command.toString());

            if(StringUtil.isBlank(execute) || (execute != null && execute.contains("No such file"))){
                executeFlag = true;
            }else{
                executeFlag = false;
            }
            log.info(String.format("rollback config file to last version, appId:%s, instance:%s, command:%s, resultStr:%s, result:%s", instanceInfo.getAppId(), instanceInfo.getHostPort(), command, execute, executeFlag));
        } catch (SSHException e) {
            executeFlag = false;
            log.error(String.format("rollback config file to last version, appId:%s, instance:%s, command:%s, error: ", instanceInfo.getAppId(), instanceInfo.getHostPort(), command), e);
        }
        return executeFlag;
    }

    /**
     * 关闭节点后，判断配置文件句柄是否释放
     * @param appDesc
     * @param instanceInfo
     * @return
     */
    private boolean checkFileReleaseAfterShutdown(AppDesc appDesc, InstanceInfo instanceInfo) {
        String host = instanceInfo.getIp();
        int port = instanceInfo.getPort();
        StringBuilder command = new StringBuilder();
        command.append("ps -ef | grep redis | grep redis-server | grep :").append(port).append("  | grep -v \"grep\"");
        log.info(String.format("check config file release, appId:%s, instance:%s, command:%s", instanceInfo.getAppId(), instanceInfo.getHostPort(), command));
        boolean executeFlag = false;
        try {
            String execute = SSHUtil.execute(host, command.toString());
            log.info(String.format("check config file release, appId:%s, instance:%s, command:%s, result:%s", instanceInfo.getAppId(), instanceInfo.getHostPort(), command, execute));
            if(StringUtils.isEmpty(execute)){
                executeFlag = true;
            }
        } catch (SSHException e) {
            executeFlag = false;
            log.error(String.format("check config file release, appId:%s, instance:%s, command:%s, error: ", instanceInfo.getAppId(), instanceInfo.getHostPort(), command), e);
        }
        return executeFlag;
    }


    /**
     * 获取相对路径
     * @param host
     * @param dirType
     * @return
     */
    public String getMachineRelativeDir(String host, int dirType) {
        MachineInfo machineInfo = machineDao.getMachineInfoByIp(host);
        if (machineInfo != null && machineInfo.isK8sMachine(machineInfo.getK8sType())) {
            return MachineProtocol.getK8sDir(host, dirType);
        }
        return MachineProtocol.getDir(dirType);
    }

    /**
     * 校验当前版本的配置模板中的配置项，是否支持这些配置
     * @param appDesc 应用信息
     * @param redisConfigMap 根据配置名分组的配置信息，value为配置值的set
     * @return
     */
    public boolean checkConfigIsSupport(AppDesc appDesc, Map<String, Set<String>> redisConfigMap){
        int type = appDesc.getType();
        int versionId = appDesc.getVersionId();
        //根据redis version获取配置模板列表
        List<InstanceConfig> instanceConfigList = instanceConfigDao.getByVersion(versionId);
        //过滤配置，有效配置，仅根据应用类型保留：2.cluster节点特殊配置（cluster类型是保留）, 5:sentinel节点配置（删除）, 6:redis普通节点（保留）
        instanceConfigList = instanceConfigList.stream().filter(instanceConfig -> instanceConfig.getType() != 5 && instanceConfig.getStatus() == 1).collect(Collectors.toList());
        if(!AppTypeEnum.REDIS_CLUSTER.equals(AppTypeEnum.getByType(type))){
            instanceConfigList = instanceConfigList.stream().filter(instanceConfig -> instanceConfig.getType() != 2).collect(Collectors.toList());
        }
        Set<String> configNames = redisConfigMap.keySet();
        for(String configName : configNames){
            for (int i = 0; i < instanceConfigList.size(); i++){
                InstanceConfig instanceConfig = instanceConfigList.get(i);
                String configKey = instanceConfig.getConfigKey();
                if(configName.equals(configKey)){
                    break;
                }else{
                    if(configKey.contains(" ")){
                        String[] s = configKey.split(" ");
                        if(s != null && s.length > 1 && configName.equals(s[0])){
                            break;
                        }
                    }
                    if(i == instanceConfigList.size() - 1){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 校验当前版本的配置模板中的配置项，是否支持这些配置
     * @param redisConfigMap 根据配置名分组的配置信息，value为配置值的set
     * @return
     */
    public boolean testUpdateConfigFileAndRestart(Long appId, Map<String, Set<String>> redisConfigMap){
        //专用于测试的应用信息
        AppDesc appDesc = null;
        List<InstanceInfo> instanceInfoList = null;
        if(TEST_APP_ID != null){
            appDesc = appService.getByAppId(TEST_APP_ID);
            instanceInfoList = appService.getAppInstanceInfo(TEST_APP_ID);
        }else{
            appDesc = appService.getByAppId(appId);
            instanceInfoList = appService.getAppInstanceInfo(appId);
        }

        if(CollectionUtils.isEmpty(instanceInfoList)){
            return false;
        }
        //获取一个实例用于check config set是否可用
        InstanceInfo instanceInfo = this.getInstanceToCheckConfigSet(instanceInfoList, null);

        //更新配置文件
        this.updateConfigFile(instanceInfo, redisConfigMap);
        //重启节点
        Optional<String> s1 = this.restartNode(null, appDesc, instanceInfo);
        log.info(String.format("test update config file and restart, appId:%s, instance:%s, resultMsg:%s", instanceInfo.getAppId(), instanceInfo.getHostPort(), s1));
        if(s1.isPresent()){
            log.error(String.format("test update config file and restart, error, rollback config and restart begin, appId:%s, instance:%s", instanceInfo.getAppId(), instanceInfo.getHostPort()));
            //失败时，还原配置并重启
            boolean executeFlag = this.rollbackUpdateConfigFile(instanceInfo);
            log.error(String.format("test update config file and restart error, rollback config and restart, rollback config fail, appId:%s, instance:%s", instanceInfo.getAppId(), instanceInfo.getHostPort()));
            if(executeFlag == true){
                //重启节点
                Optional<String> s2 = this.restartNode(null, appDesc, instanceInfo);
                if(s2.isPresent()){
                    log.error(String.format("test update config file and restart, error, rollback config and restart, rollback config success, restart fail, appId:%s, instance:%s, restartResult:%s", instanceInfo.getAppId(), instanceInfo.getHostPort(), s2));
                }
            }
            return false;
        }
        return true;
    }

    private String generateLog(String format, Object... objects){
        List<Object> objectsList = new ArrayList<>();
        if(objects != null){
            for(int i = 0; i < objects.length; i++){
                objectsList.add(objects[i]);
            }
        }
        objectsList.add(0, DateUtil.formatYYYYMMddHHMMSS(new Date()));
        return String.format("%s " + format, objectsList.toArray());
    }

    private String getTimeBetween(long endTime, long startTime){
        long l = (endTime - startTime);
        return l + "ms";
    }


    /**
     * 查看实例日志中，aof rewrite是否被terminated 或 redis是否crashed by signal: 11
     * @param instanceInfo
     * @return
     */
    private boolean checkByLog(InstanceInfo instanceInfo, String expectValue, long startTime){
        String recentLog = getRecentLog(instanceInfo);
        if(StringUtil.isBlank(recentLog)){
            return false;
        }
        String[] logArray = recentLog.split(System.getProperty("line.separator"));
        if(logArray != null && logArray.length > 1){
            for(int i = logArray.length - 1; i >= 0; i--){
                String logInfo = logArray[i];
                if(logInfo != null && logInfo.contains(expectValue)){
                    log.info(String.format("check by log, log info : %s, time: %s", logInfo, startTime));
                    if(!checkLogTimeMeet(logInfo, startTime)){
                        continue;
                    }else{
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 查看实例日志中，aof rewrite是否被terminated 或 redis是否crashed by signal: 11
     * @param instanceInfo
     * @return
     */
    private String getRecentLog(InstanceInfo instanceInfo){
        String host = instanceInfo.getIp();
        int port = instanceInfo.getPort();
        int type = instanceInfo.getType();
        String logType = "";
        if (TypeUtil.isRedisDataType(type)) {
            logType = "redis-";
        } else if (TypeUtil.isRedisSentinel(type)) {
            logType = "redis-sentinel-";
        }
        String remoteFilePath = getMachineRelativeDir(host, DirEnum.LOG_DIR.getValue()) + logType + port + "-*.log";
        StringBuilder command = new StringBuilder();
        command.append("/usr/bin/tail -n").append(500).append(" ").append(remoteFilePath);
        try {
            return SSHUtil.execute(host, command.toString());
        } catch (SSHException e) {
            log.error("实例appId：{}, host:{}, port:{}, getRecentLog errorMsg:{}", instanceInfo.getAppId(), host, port, e.getMessage());
            return null;
        }
    }

    private boolean checkLogTimeMeet(String logInfo, long beginTime) {
        //allow machine clock 1s difference
        beginTime = beginTime - 1 * 1000;
        boolean flag = false;
        Calendar calendar = Calendar.getInstance(Locale.US);
        int year = 0;
        try{
            calendar.setTimeInMillis(beginTime);
            year = calendar.get(Calendar.YEAR);
        }catch (Exception e){
            return flag;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm:ss.SSS", Locale.US);
        String[] strArray = logInfo.split(" ");

        if (strArray != null && strArray.length > 5) {
            StringBuilder sb = new StringBuilder();
            int strSize = 5;
            for (int i = 1; i < strSize; i++) {
                String str = strArray[i];
                if(i == 3 && !str.equals(String.valueOf(year))){
                    sb.append(year);
                    sb.append(" ");
                    strSize = 4;
                }
                sb.append(str);
                sb.append(" ");
            }
            sb.deleteCharAt(sb.length() - 1);
            try {
                Date parse = sdf.parse(sb.toString());
                flag = !parse.before(calendar.getTime());
            } catch (ParseException e) {
                log.error("log time parse error : ", e);
            }
        }
        return flag;
    }

}
