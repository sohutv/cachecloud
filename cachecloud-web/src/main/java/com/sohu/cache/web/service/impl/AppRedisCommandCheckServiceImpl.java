package com.sohu.cache.web.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sohu.cache.alert.EmailComponent;
import com.sohu.cache.constant.RedisConstant;
import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.dao.MachineDao;
import com.sohu.cache.entity.AppUser;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.entity.MachineInfo;
import com.sohu.cache.exception.SSHException;
import com.sohu.cache.protocol.MachineProtocol;
import com.sohu.cache.redis.AssistRedisService;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.redis.enums.DirEnum;
import com.sohu.cache.ssh.SSHUtil;
import com.sohu.cache.util.StringUtil;
import com.sohu.cache.util.TypeUtil;
import com.sohu.cache.web.enums.BooleanEnum;
import com.sohu.cache.web.service.AppRedisCommandCheckService;
import com.sohu.cache.web.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: zengyizhao
 * @DateTime: 2021/9/29 11:34
 * @Description: redis命令检测
 */
@Slf4j
@Service
public class AppRedisCommandCheckServiceImpl implements AppRedisCommandCheckService {

    @Autowired
    private InstanceDao instanceDao;

    @Autowired
    private RedisCenter redisCenter;

    @Autowired
    private AssistRedisService assistRedisService;

    private static final String REDIS_COMMAND_CHECK_RESULT_SAVE_KEY = "redis:command:check:result";

    private static final String REDIS_COMMAND_CHECK_RESULT_KEY = "redis:command:check:result:";

    @Resource
    private MachineDao machineDao;

    @Resource
    private EmailComponent emailComponent;

    /**
     *
     * @param checkVo
     */
    @Override
    public RedisCommandCheckResult checkRedisCommand(AppUser appUser, AppRedisCommandCheckVo checkVo){
        RedisCommandCheckResult redisCommandCheckResult = new RedisCommandCheckResult();
        BeanUtils.copyProperties(checkVo, redisCommandCheckResult);
        redisCommandCheckResult.setCreateTime(new Date());
        redisCommandCheckResult.setSuccess(true);
        redisCommandCheckResult.setUserName(appUser.getChName() == null ? appUser.getName() : appUser.getChName());
        long beginTime = System.currentTimeMillis();
        StringBuilder stringBuilder = new StringBuilder();
        List<InstanceInfo> allInstanceList = getInstanceListToCheck(checkVo.getMachineIps(), checkVo.getPodIp());
        AppRedisCommandCheckResult checkResult = new AppRedisCommandCheckResult();
        List<InstanceRedisCommandCheckResult> instanceCheckResultList = new ArrayList<>();
        checkResult.setMachineIps(checkVo.getMachineIps());
        checkResult.setCreateTime(new Date());
        checkResult.setPodIp(checkVo.getPodIp());
        checkResult.setCommand(checkVo.getCommand());
        checkResult.setSuccess(false);

        //遍历实例并分别处理
        for (InstanceInfo instanceInfo : allInstanceList){
            BooleanEnum master = redisCenter.isMaster(instanceInfo.getAppId(), instanceInfo.getIp(), instanceInfo.getPort());
            if(master.equals(BooleanEnum.OTHER)){
                continue;
            }
            InstanceRedisCommandCheckResult instanceCheckResult = new InstanceRedisCommandCheckResult();
            instanceCheckResultList.add(instanceCheckResult);
            instanceCheckResult.setInstanceInfo(instanceInfo);
            String s = handleByInstance(instanceInfo, checkVo);
            if(StringUtil.isBlank(s)){
                instanceCheckResult.setSuccess(true);
                continue;
            }
            redisCommandCheckResult.setSuccess(false);
            instanceCheckResult.setSuccess(false);
            instanceCheckResult.setMessage(s);
            stringBuilder.append(s);
            stringBuilder.append("\r\n");
        }
        checkResult.setInstanceCheckList(instanceCheckResultList);
        log.info("耗时：{}ms，结果信息：{}", System.currentTimeMillis() - beginTime, checkResult);
        UUID uuid = UUID.randomUUID();
        redisCommandCheckResult.setKey(uuid.toString());
        this.saveRedisCommandCheckResult(redisCommandCheckResult, checkResult);
        return redisCommandCheckResult;
    }

    //machineIps
    //podIp
    //命令
    private List<InstanceInfo> getInstanceListToCheck(String machineIps, String podIp){
        List<InstanceInfo> allInstanceList = new ArrayList<>();
        if(StringUtils.isNotEmpty(podIp)){
            allInstanceList = getInstanceInfoList(podIp);
        }else if(machineIps != null){
            List<String> machineList = getMachineIpist(machineIps);
            //遍历machine list，分别获取machine下 pod list
            List<MachineInfo> allMachineList =  new ArrayList<>();
            for (String machineIp: machineList) {
                List<MachineInfo> vmPodList = getVmPodList(machineIp);
                if(!org.springframework.util.CollectionUtils.isEmpty(vmPodList)){
                    allMachineList.addAll(vmPodList);
                }
            }
            //分别获取 pod下 redis intance list
            for (MachineInfo machineInfo : allMachineList) {
                List<InstanceInfo> instanceInfoList = getInstanceInfoList(machineInfo.getIp());
                if(!CollectionUtils.isEmpty(instanceInfoList)){
                    allInstanceList.addAll(instanceInfoList);
                }
            }
        }
        return allInstanceList;
    }

    /**
     * 获取机器ip列表
     * @return
     */
    private List<String> getMachineIpist(String machineIps){
        String[] split = machineIps.split(";");
        List<String> ipList = Arrays.asList(split);
        return ipList;
    }

    /**
     * 获取机器pod列表
     * @return
     */
    private List<MachineInfo> getVmPodList(String realIp){
        List<MachineInfo> machineInfoList = machineDao.getMachineInfoByCondition(null, -1, -1, null , -1, realIp);
        return machineInfoList;
    }

    /**
     * 根据机器ip获取instance列表
     * @param ip
     * @return
     */
    private List<InstanceInfo> getInstanceInfoList(String ip){
        //获取一台机器的所有实例
        List<InstanceInfo> instListByIp = instanceDao.getInstListByIp(ip);
        return instListByIp;
    }

    /**
     * 根据实例进行处理
     * @param instanceInfo
     * @return
     */
    private String handleByInstance(InstanceInfo instanceInfo, AppRedisCommandCheckVo checkVo){
        long startTime = 0L;
        //执行命令
        if("bgsave".equals(checkVo.getCommand())){
            startTime = sendBgsaveCommand(instanceInfo);
        }else if("bgrewriteaof".equals(checkVo.getCommand())) {
            startTime = sendBgrewriteaofCommand(instanceInfo);
        }
        if(startTime == 0L){
            return String.format("实例appId：%s, ip:%s, port:%s，处理失败：发送命令失败", instanceInfo.getAppId(), instanceInfo.getIp(), instanceInfo.getPort());
        }
        //等待命令执行结束或定时等待
        String checkInfo = null;
        if("bgsave".equals(checkVo.getCommand())){
            checkInfo = checkByInfo(instanceInfo, startTime, checkVo.getMaxTry(), RedisConstant.Persistence, "rdb_bgsave_in_progress", "0");
        }else if("bgrewriteaof".equals(checkVo.getCommand())) {
            checkInfo = checkByInfo(instanceInfo, startTime, checkVo.getMaxTry(), RedisConstant.Persistence, "aof_rewrite_in_progress", "0");
        }
        log.info("实例appId：{}, ip:{}, port:{}，执行checkByInfo用时：{}ms，结果：{}", instanceInfo.getAppId(), instanceInfo.getIp(), instanceInfo.getPort(), System.currentTimeMillis() - startTime, checkInfo);
        if(checkInfo != null){
            return checkInfo;
        }
        //检测校验
        checkInfo = checkByLog(instanceInfo, "crashed by signal", 10);
        if(checkInfo == null){
            log.info("实例appId：{}, ip:{}, port:{}，未出现指定信息", instanceInfo.getAppId(), instanceInfo.getIp(), instanceInfo.getPort());
        }else{
            log.info("实例appId：{}, ip:{}, port:{}，出现错误信息：{}", instanceInfo.getAppId(), instanceInfo.getIp(), instanceInfo.getPort(), checkInfo);
        }
        return checkInfo;
    }

    /**
     * 发送bgrewriteaof命令并获取返回信息
     * @param instanceInfo
     * @return
     */
    private long sendBgrewriteaofCommand(InstanceInfo instanceInfo){
        String host = instanceInfo.getIp();
        int port = instanceInfo.getPort();
        long appId = instanceInfo.getAppId();
        StringBuilder infoBuilder = new StringBuilder();
        Jedis jedis = null;
        long start = 0L;
        try {
            jedis = redisCenter.getAdminJedis(appId, host, port);
            start = System.currentTimeMillis();
            String info = jedis.bgrewriteaof();
            log.info("实例appId：{}, ip:{}, port:{}，bgrewriteaof结果：{}", appId, host, port, info);
        } catch (Exception e) {
            start = 0L;
            log.error("实例appId：{}, host:{}, port:{}, redis-bgrewriteaof errorMsg:{}", appId, host, port, e.getMessage());
        } finally {
            if (jedis != null){
                jedis.close();
            }
        }
        return start;
    }

    /**
     * 发送命令并获取返回信息
     * @param instanceInfo
     * @return
     */
    private long sendBgsaveCommand(InstanceInfo instanceInfo){
        String host = instanceInfo.getIp();
        int port = instanceInfo.getPort();
        long appId = instanceInfo.getAppId();
        StringBuilder infoBuilder = new StringBuilder();
        Jedis jedis = null;
        long start = 0L;
        try {
            jedis = redisCenter.getAdminJedis(appId, host, port);
            start = System.currentTimeMillis();
            String info = jedis.bgsave();
            log.info("实例appId：{}, ip:{}, port:{}，bgrewriteaof结果：{}", appId, host, port, info);
        } catch (Exception e) {
            start = 0L;
            log.error("实例appId：{}, host:{}, port:{}, redis-bgrewriteaof errorMsg:{}", appId, host, port, e.getMessage());
        } finally {
            if (jedis != null){
                jedis.close();
            }
        }
        return start;
    }

    private String getMethod(String command) {
        return null;
    }

    /**
     * 发送info命令查看相关指标信息,进行监测
     * @param instanceInfo 实例信息
     * @param startTime 开始时间戳
     * @param maxTry 最大重试次数
     * @param redisConstant redis info中一级指标常量
     * @param indicateName 配置项
     * @param expectValue 期望值
     * @return
     */
    private String checkByInfo(InstanceInfo instanceInfo, long startTime, Integer maxTry, RedisConstant redisConstant, String indicateName, String expectValue){
        String result = null;
        String host = instanceInfo.getIp();
        int port = instanceInfo.getPort();
        long appId = instanceInfo.getAppId();
        Jedis jedis = null;
        boolean reConfirmFlag = true;
        long costTime = 0L;
        try {
            jedis = redisCenter.getAdminJedis(appId, host, port);
            int retryTimes = 1;
            long sleepTime = 500L;
            boolean printMemory = true;
            String configValue = null;
            while(reConfirmFlag && retryTimes < maxTry){
                try {
                    Thread.sleep(retryTimes * sleepTime > 3000L ? 3000L : retryTimes * sleepTime );
                } catch (InterruptedException e) {

                }
                String info = jedis.info("all");
                Map<RedisConstant, Map<String, Object>> infoStats = processRedisStats(info);
                Map<String, Object> persistenceMap = infoStats.get(redisConstant);
                if(printMemory){
                    log.info("实例appId：{}, ip:{}, port:{}，retryTimes:{}, info : {}", appId, host, port, retryTimes, info);
                    printMemory = false;
                }
                configValue = MapUtils.getString(persistenceMap, indicateName);
                if(expectValue.equals(configValue)){
                    reConfirmFlag = false;
                    costTime = System.currentTimeMillis() - startTime;
                }
                retryTimes++;
            }
            if(reConfirmFlag){
                result = String.format("实例appId：%s, ip:%s, port:%s, redis-checkByInfo, configName:%s, expectValue:%s, configValue:%s", appId, host, port, indicateName, expectValue, configValue);
            }
            log.info("实例appId：{}, ip:{}, port:{}，重试次数：{}，配置项：{}，期望值：{}，实际值：{} 最终结果(结束false/未结束true)：{}，耗时：{}ms",
                    instanceInfo.getAppId(), instanceInfo.getIp(), instanceInfo.getPort(), retryTimes - 1, indicateName, expectValue, configValue, reConfirmFlag, costTime);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(String.format("实例appId：%s, ip:%s, port:%s, redis-checkByInfo errorMsg:", appId, host, port), e);
            result = String.format("实例appId：%s, ip:%s, port:%s, redis-checkByInfo errorMsg:%s", appId, host, port, e.getMessage());
        } finally {
            if (jedis != null)
                jedis.close();
        }
        return result;
    }

    /**
     * 查看实例日志中，aof rewrite是否被terminated 或 redis是否crashed by signal: 11
     * @param instanceInfo
     * @return
     */
    private String checkByLog(InstanceInfo instanceInfo, String expectValue, int minuteInternalAllow){
        String recentLog = getRecentLog(instanceInfo);
        if(StringUtil.isBlank(recentLog)){
            return "实例appId:" + instanceInfo.getAppId() + "-ip:" + instanceInfo.getIp() + "-port:" + instanceInfo.getPort() + "-结果：无日志，请确认实例是否正常并人工判定";
        }
        String[] logArray = recentLog.split(System.getProperty("line.separator"));
        if(logArray != null && logArray.length > 1){
            for(int i = logArray.length - 1; i >= 0; i--){
                String log = logArray[i];
                if(log != null && log.contains(expectValue)){
                    if(!checkLogTimeMeet(log, minuteInternalAllow)){
                        continue;
                    }
                    return  "实例appId:" + instanceInfo.getAppId() + "-ip:" + instanceInfo.getIp() + "-port:" + instanceInfo.getPort() + "-结果log: " + log;
                }
            }
        }
        return null;
    }

    private boolean checkLogTimeMeet(String log, int minuteInternalAllow) {
        boolean flag = false;
        Calendar calendar = Calendar.getInstance(Locale.US);
        int year = 0;
        try{
            calendar.add(Calendar.MINUTE, -minuteInternalAllow);
            year = calendar.get(Calendar.YEAR);
        }catch (Exception e){
            return flag;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm:ss.SSS", Locale.US);
        String[] strArray = log.split(" ");
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

            }
        }
        return flag;
    }

    /**
     * 发送info命令查看aof rewrite是否结束
     * @param instanceInfo
     * @return
     */
    private boolean checkInProgress(InstanceInfo instanceInfo, long startTime){
        String host = instanceInfo.getIp();
        int port = instanceInfo.getPort();
        long appId = instanceInfo.getAppId();
        Jedis jedis = null;
        boolean reConfirmFlag = true;
        long costTime = 0L;
        try {
            jedis = redisCenter.getAdminJedis(appId, host, port);
            int retryTimes = 1;
            long sleepTime = 500L;
            boolean printMemory = true;
            while(reConfirmFlag && retryTimes < 40){
                try {
                    Thread.sleep(retryTimes * sleepTime > 3000L ? 3000L : retryTimes * sleepTime );
                } catch (InterruptedException e) {

                }
                String info = jedis.info("all");
                Map<RedisConstant, Map<String, Object>> infoStats = processRedisStats(info);
                Map<String, Object> persistenceMap = infoStats.get(RedisConstant.Persistence);
                if(printMemory){
                    Map<String, Object> memoryMap = infoStats.get(RedisConstant.Memory);
                    String used_memory_human = MapUtils.getString(memoryMap, "used_memory_human");
                    log.info("实例appId：{}, ip:{}, port:{}，retryTimes:{}, info used_memory_human: {}", appId, host, port, retryTimes, used_memory_human);
                    printMemory = false;
                }
                String aof_rewrite_in_progress = MapUtils.getString(persistenceMap, "aof_rewrite_in_progress");
                if("0".equals(aof_rewrite_in_progress)){
                    reConfirmFlag = false;
                    long lastCostTime = MapUtils.getLong(persistenceMap, "aof_last_rewrite_time_sec");
                    if(lastCostTime != 0L && lastCostTime != -1L){
                        costTime = lastCostTime * 1000;
                    }else{
                        costTime = System.currentTimeMillis() - startTime;
                    }
                }
                retryTimes++;
            }
            log.info("实例appId：{}, ip:{}, port:{}，重试次数：{}， 最终结果(结束false/未结束true)：{}，耗时：{}ms", instanceInfo.getAppId(), instanceInfo.getIp(), instanceInfo.getPort(), retryTimes - 1, reConfirmFlag, costTime);
        } catch (Exception e) {
            log.error("实例appId：{}, host:{}, port:{}, redis-checkInProgress errorMsg:{}", appId, host, port, e.getMessage());
        } finally {
            if (jedis != null)
                jedis.close();
        }
        return reConfirmFlag;
    }

    /**
     * 处理redis统计信息
     *
     * @param statResult 统计结果串
     */
    private Map<RedisConstant, Map<String, Object>> processRedisStats(String statResult) {
        Map<RedisConstant, Map<String, Object>> redisStatMap = new HashMap<RedisConstant, Map<String, Object>>();
        String[] data = statResult.split("\r\n");
        String key;
        int i = 0;
        int length = data.length;
        while (i < length) {
            if (data[i].contains("#")) {
                int index = data[i].indexOf('#');
                key = data[i].substring(index + 1);
                ++i;
                RedisConstant redisConstant = RedisConstant.value(key.trim());
                if (redisConstant == null) {
                    continue;
                }
                Map<String, Object> sectionMap = new LinkedHashMap<String, Object>();
                while (i < length && data[i].contains(":")) {
                    String[] pair = StringUtils.splitByWholeSeparator(data[i], ":");
                    sectionMap.put(pair[0], pair[1]);
                    i++;
                }
                redisStatMap.put(redisConstant, sectionMap);
            } else {
                i++;
            }
        }
        return redisStatMap;
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
        command.append("/usr/bin/tail -n").append(1000).append(" ").append(remoteFilePath);
        try {
            return SSHUtil.execute(host, command.toString());
        } catch (SSHException e) {
            log.error("实例appId：{}, host:{}, port:{}, redis-checkInProgress errorMsg:{}", instanceInfo.getAppId(), host, port, e.getMessage());
            return null;
        }
    }

    /**
     * 获取日志相对路径
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

    @Override
    public void saveRedisCommandCheckResult(RedisCommandCheckResult redisCommandCheckResult, AppRedisCommandCheckResult checkResult) {
        if(redisCommandCheckResult != null){
            Long llen = assistRedisService.llen(REDIS_COMMAND_CHECK_RESULT_SAVE_KEY);
            if(llen >= 20){
                String configResult = assistRedisService.lpop(REDIS_COMMAND_CHECK_RESULT_SAVE_KEY);
                RedisCommandCheckResult toRemoveResult = JSONObject.parseObject(configResult, RedisCommandCheckResult.class);
                assistRedisService.remove(REDIS_COMMAND_CHECK_RESULT_KEY + toRemoveResult.getKey());
            }
            assistRedisService.rpush(REDIS_COMMAND_CHECK_RESULT_SAVE_KEY, JSONObject.toJSONString(redisCommandCheckResult));
            if(checkResult != null && CollectionUtils.isNotEmpty(checkResult.getInstanceCheckList())){
                assistRedisService.set(REDIS_COMMAND_CHECK_RESULT_KEY + redisCommandCheckResult.getKey(), JSONObject.toJSONString(checkResult));
            }
        }
    }

    @Override
    public List<RedisCommandCheckResult> getRedisCommandCheckResult() {
        List<RedisCommandCheckResult> lists = new ArrayList<>();
        List<String> lrange = assistRedisService.lrange(REDIS_COMMAND_CHECK_RESULT_SAVE_KEY, 0, 20);
        if(CollectionUtils.isNotEmpty(lrange)){
            for (String str : lrange) {
                lists.add(JSONObject.parseObject(str, RedisCommandCheckResult.class));
            }
        }
        lists = lists.stream().sorted(Comparator.comparing(RedisCommandCheckResult::getCreateTime).reversed()).collect(Collectors.toList());
        return lists;
    }

    @Override
    public AppRedisCommandCheckResult getRedisCommandCheckDetailResult(String uuid) {
        AppRedisCommandCheckResult checkResult = null;
        String resultStr = assistRedisService.get(REDIS_COMMAND_CHECK_RESULT_KEY + uuid);
        if(StringUtils.isNotEmpty(resultStr)){
            checkResult = JSONObject.parseObject(resultStr, AppRedisCommandCheckResult.class);
        }
        return checkResult;
    }

}
