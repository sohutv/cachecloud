package com.sohu.cache.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.sohu.cache.constant.InstanceStatusEnum;
import com.sohu.cache.entity.*;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.TypeUtil;
import com.sohu.cache.web.enums.SuccessEnum;
import com.sohu.cache.web.enums.UseTypeEnum;
import com.sohu.cache.web.vo.AppDetailVO;
import com.sohu.cache.web.vo.RedisInfo;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Description: 应用在线迁移
 * </p>
 *
 * @author chenshi
 * @version 1.0
 * @date 2018/9/21
 */
@Controller
@RequestMapping("/manage/app/migrate")
public class AppMigrateController extends BaseController {

    // 实例和日志信息
    private static String INSTANCE_INFO_KEY = "instanceInfo";
    private static String INSTANCE_LOG_KEY = "instanceLog";
    // 下线实例信息
    private static String DOWN_INSTANCE_IDS_KEY = "downInstanceIds";
    private static String DOWN_INSTANCE_INFO_KEY = "downInstanceInfo";

    @RequestMapping(value = "init", method = {RequestMethod.POST, RequestMethod.GET})
    public ModelAndView init(Model model, long appId) {
        // 1.获取应用信息
        AppDetailVO appDetail = appStatsCenter.getAppDetail(appId);
        List<MachineStats> machinelist = machineCenter.getMachineStats(null, null, null, null, null, null, null);
        // 获取机器信息
        Map<String, Integer> machineInstanceCountMap = machineCenter.getMachineInstanceCountMap();
        // 2.获取当前redis实例信息
        List<InstanceInfo> instanceList = appService.getAppInstanceInfo(appId);
        String instanceRedisinfo = getInstanceInfo(instanceList, appId).get(INSTANCE_INFO_KEY);
        // 3.如果是sentinel，获取sentinel实例信息
        String instanceSentinelinfo = getSentinelInstanceInfo(instanceList);
        List<MachineRoom> roomList = machineCenter.getEffectiveRoom();

        model.addAttribute("roomList", roomList);
        model.addAttribute("appDetail", appDetail);
        model.addAttribute("instanceSentinelInfo", instanceSentinelinfo);
        model.addAttribute("instanceSourceInfo", instanceSentinelinfo + instanceRedisinfo);
        model.addAttribute("machinelist", machinelist);
        model.addAttribute("machineInstanceCountMap", machineInstanceCountMap);
        return new ModelAndView("/manage/appOps/appMigrate");
    }

    @RequestMapping(value = "selectMachine", method = {RequestMethod.POST})
    public ModelAndView autoSelectMachine(HttpServletResponse response,
                                          int type,
                                          int useType,
                                          String room,
                                          int machineNum,
                                          long mem,
                                          int masterNum,
                                          int slaveNum) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        if (room == null || machineNum == 0 || masterNum == 0) {
            logger.info("可用机器数不足");
            resultMap.put("status", SuccessEnum.ERROR.value());
            resultMap.put("message", "参数输入错误");
            sendMessage(response, JSONObject.toJSONString(resultMap));
            return null;
        }

        int masterMachineNum;
        Integer isSalve = slaveNum == 0 ? 0 : 1;
        if (TypeUtil.isRedisSentinel(type)) {
            masterMachineNum = 1 + slaveNum;
        } else if (TypeUtil.isRedisStandalone(type)) {
            masterMachineNum = 1;
        } else if (TypeUtil.isRedisCluster(type)) {
            masterMachineNum = machineNum;
        } else {
            resultMap.put("status", SuccessEnum.ERROR.value());
            resultMap.put("message", "type参数错误");
            sendMessage(response, JSONObject.toJSONString(resultMap));
            return null;
        }

        Double reqSize = Math.ceil(mem * 1.0D / masterNum) * Math.ceil(masterNum * 1.0D / masterMachineNum);
        Integer reqCpu = masterNum % masterMachineNum == 0 ? masterNum / masterMachineNum : masterNum / masterMachineNum + 1;
        List<MachineMemStatInfo> machineMemStatInfoList = machineCenter.getAllValidMachineMem(new ArrayList<String>(), room, useType);

        List<MachineMemStatInfo> machineCandi = new ArrayList<MachineMemStatInfo>();
        for (MachineMemStatInfo memStatInfo : machineMemStatInfoList) {
            appService.getMachineCandiList(memStatInfo, reqSize, reqCpu, isSalve, machineCandi);
        }
        List<MachineMemStatInfo> resMachines = new ArrayList<MachineMemStatInfo>();
        appService.getResMachines(machineCandi, masterMachineNum, resMachines);

        if (resMachines.size() == 0) {
            if (useType == UseTypeEnum.Machine_test.getValue()) {
                appService.getResMachines(machineMemStatInfoList, masterMachineNum, resMachines);
            }
            if (resMachines.size() == 0) {
                logger.info("可用机器数不足");
                resultMap.put("status", SuccessEnum.ERROR.value());
                resultMap.put("message", "可用机器数不足");
                sendMessage(response, JSONObject.toJSONString(resultMap));
                return null;
            }
        }

        resultMap.put("status", SuccessEnum.SUCCESS.value());
        resultMap.put("resMachineList", resMachines);
        sendMessage(response, JSONObject.toJSONString(resultMap));
        return null;
    }


    @RequestMapping(value = "selectSentinelMachine", method = {RequestMethod.POST})
    public ModelAndView selectSentinelMachine(HttpServletResponse response, long appId) {
        Map<String, Object> resultMap = new HashMap<String, Object>();

        List<InstanceInfo> instanceList = appService.getAppInstanceInfo(appId);
        String instanceSentinelInfo = getSentinelInstanceInfo(instanceList);
        if (instanceSentinelInfo.isEmpty()) {
            resultMap.put("status", SuccessEnum.ERROR.value());
            resultMap.put("message", "sentinel信息为空");
            sendMessage(response, JSONObject.toJSONString(resultMap));
            return null;
        }
        List<String> sentinelInfoList = Arrays.asList(instanceSentinelInfo.split("\n"));
        int sentinelNum = sentinelInfoList.size();
        if (sentinelNum < 3 || sentinelNum % 2 == 0) {
            resultMap.put("message", "sentinel机器数为" + sentinelNum + "不满足条件，系统自动进行调整");
            sentinelNum = sentinelNum < 3 ? 3 : sentinelNum + 1;
        }

        Integer reqCpu = 1;
        List<MachineMemStatInfo> machineMemStatInfoList = machineCenter.getAllValidMachineMem(new ArrayList<String>(), null, 3);
        List<MachineMemStatInfo> machineCandi = new ArrayList<MachineMemStatInfo>();
        for (MachineMemStatInfo memStatInfo : machineMemStatInfoList) {
            memStatInfo.setInstanceNum(instanceDao.getInstListByIp(memStatInfo.getIp()).size());
            appService.getMachineCandiList(memStatInfo, 0d, reqCpu, 0, machineCandi);
        }
        if (machineCandi.size() < sentinelNum) {
            /*resultMap.put("status", SuccessEnum.ERROR.value());
            resultMap.put("message", "可用sentinel机器数不足");
            sendMessage(response, JSONObject.toJSONString(resultMap));
            return null;*/
            // 可用sentinel机器不足 则默认挑选
            logger.warn("machineCandi size = {},default random sentinel ip!", machineCandi.size());
            machineCandi = machineMemStatInfoList;
        }
        //按机房分类
        List<MachineRoom> roomList = machineCenter.getEffectiveRoom();
        Map<String, List<MachineMemStatInfo>> sentinelMachineMap = new HashMap<String, List<MachineMemStatInfo>>();
        for (MachineRoom room : roomList) {
            List<MachineMemStatInfo> list = new ArrayList<MachineMemStatInfo>();
            sentinelMachineMap.put(room.getName(), list);
        }
        for (MachineMemStatInfo memStatInfo : machineCandi) {
            sentinelMachineMap.get(memStatInfo.getRoom()).add(memStatInfo);
        }
        Set<MachineMemStatInfo> sentinelMachineSet = new HashSet<MachineMemStatInfo>();
        //select
        while (sentinelMachineSet.size() < sentinelNum) {
            for (Map.Entry<String, List<MachineMemStatInfo>> entry: sentinelMachineMap.entrySet()) {
                getResMachines(entry.getValue(), 1, sentinelMachineSet);
                if (sentinelMachineSet.size() == sentinelNum) {
                    break;
                }
            }
        }
        resultMap.put("status", SuccessEnum.SUCCESS.value());
        resultMap.put("sentinelMachineList", new ArrayList<MachineMemStatInfo>(sentinelMachineSet));
        sendMessage(response, JSONObject.toJSONString(resultMap));
        return null;
    }

    private void getResMachines(List<MachineMemStatInfo> machineCandi, Integer machineNum, Set<MachineMemStatInfo> resMachines) {
        if (machineCandi == null) {
            return;
        }
        Map map = new HashMap();
        if (machineCandi.size() < machineNum) {
            return;
        } else {
            while (map.size() < machineNum) {
                int random = (int) (Math.random() * machineCandi.size());
                if (!map.containsKey(random)) {
                    map.put(random, "");
                    resMachines.add(machineCandi.get(random));
                }
            }
        }
    }

    @RequestMapping(value = "generateMachineInfo", method = {RequestMethod.POST})
    public ModelAndView autoSelectMachine(HttpServletResponse response,
                                          String machineIps) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        if (machineIps == null || machineIps.isEmpty()) {
            logger.info("所选sentinel机器为空");
            resultMap.put("status", SuccessEnum.ERROR.value());
            resultMap.put("message", "请选择sentinel机器");
            sendMessage(response, JSONObject.toJSONString(resultMap));
            return null;
        }
        List<MachineMemStatInfo> resMachines = machineCenter.getValidMachineMemByIpList(Arrays.asList(machineIps.split(",")));
        resultMap.put("status", SuccessEnum.SUCCESS.value());
        resultMap.put("resMachineList", resMachines);
        sendMessage(response, JSONObject.toJSONString(resultMap));
        return null;
    }

    @RequestMapping(value = "checkPlan", method = {RequestMethod.POST})
    public ModelAndView checkPlan(HttpServletResponse response, long appId, String machineInfo, String machineSentinelInfo, int type) {

        Map<String, Object> resultMap = new HashMap<String, Object>();
        SuccessEnum successEnum = SuccessEnum.SUCCESS;

        logger.info(" appId:{},type:{},machineInfo:{},machineSentinelInfo:{}", appId, type, machineInfo, machineSentinelInfo);
        // param check
        if (StringUtils.isEmpty(machineInfo)) {
            resultMap.put("status", SuccessEnum.ERROR.value());
            resultMap.put("message", "迁移机器参数异常,machineInfo:" + machineInfo);
            sendMessage(response, JSONObject.toJSONString(resultMap));
            return null;
        }

        AppDesc appDesc = appService.getByAppId(appId);
        List<InstanceInfo> instanceList = appService.getAppInstanceInfo(appId);
        // 1.迁移机器ip连通性,系统环境安装env检查
        // 2.迁移机器安装redis版本检查
//        RedisVersion redisVersion = redisConfigTemplateService.getRedisVersionById(appDesc.getVersionId());
        SystemResource redisResource = resourceService.getResourceById(appDesc.getVersionId());
        try {
            Boolean flag = true;
            for (String machineIp : machineInfo.split(ConstUtils.SEMICOLON)) {
                if (!StringUtils.isEmpty(machineIp)) {
                    // todo
//                    flag = redisConfigTemplateService.checkMachineRedisVersion(machineIp, redisVersion);
//                    if (!flag) {
//                        resultMap.put("status", SuccessEnum.ERROR.value());
//                        resultMap.put("message", "" +
//                                "迁移机器:" + machineIp + "未安装" + redisVersion.getName() + "版本,请先安装!");
//                        sendMessage(response, JSONObject.toJSONString(resultMap));
//                        return null;
//                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.put("status", SuccessEnum.ERROR.value());
            resultMap.put("message", "迁移机器Redis版本检查异常，请查看日志!");
            sendMessage(response, JSONObject.toJSONString(resultMap));
            return null;
        }
        // 3.获取下线节点
        Map<String, String> mapInfo = getDownInstanceInfo(instanceList, appId, "slave");
        // 4.获取新实例节点
        List<InstanceInfo> masterNodes = new ArrayList<InstanceInfo>();
        List<InstanceInfo> sentinelNodes = new ArrayList<InstanceInfo>();
        if (instanceList != null && instanceList.size() > 0) {
            for (InstanceInfo instance : instanceList) {
                if (instance.getStatus() == InstanceStatusEnum.GOOD_STATUS.getStatus() && instance.getRoleDesc().equals("master")) {
                    masterNodes.add(instance);
                }
                if (instance.getStatus() == InstanceStatusEnum.GOOD_STATUS.getStatus() && instance.getRoleDesc().equals("sentinel")) {
                    sentinelNodes.add(instance);
                }
            }
        }
        Map<String, RedisInfo> redisInfoMap = getRedisInfo(masterNodes, machineInfo);
        String newInstanceInfo;
        StringBuilder newInstanceInfoBuilder = new StringBuilder("新增实例信息:\n");
        for (Map.Entry<String, RedisInfo> redisInfo : redisInfoMap.entrySet()) {
            RedisInfo redisNode = redisInfo.getValue();
            //newInstanceInfo += redisNode.getRedisInfo(redisNode);
            newInstanceInfoBuilder.append(redisNode.getRedisInfo(redisNode));
        }
        // 5.如果是sentinel集群
        if (type == ConstUtils.CACHE_REDIS_SENTINEL) {
            Map<String, RedisInfo> sentinelInfoMap = getSentinelInfo(sentinelNodes, machineSentinelInfo);
            for (Map.Entry<String, RedisInfo> sentinelInfo : sentinelInfoMap.entrySet()) {
                RedisInfo redisNode = sentinelInfo.getValue();
                //newInstanceInfo += redisNode.getRedisInfo(redisNode);
                newInstanceInfoBuilder.append(redisNode.getRedisInfo(redisNode));
            }
            // 获取需要下线的sentinel
            Map<String, String> sentinelInfo = getDownInstanceInfo(instanceList, appId, "sentinel");
            resultMap.put("downSentinelIds", sentinelInfo.get(DOWN_INSTANCE_IDS_KEY));
        }
        newInstanceInfo = newInstanceInfoBuilder.toString();
        resultMap.put("status", successEnum.value());
        resultMap.put("downInstanceInfo", "下线实例信息:\n" + mapInfo.get(DOWN_INSTANCE_INFO_KEY));
        resultMap.put("downInstanceIds", mapInfo.get(DOWN_INSTANCE_IDS_KEY));
        resultMap.put("newInstanceInfo", newInstanceInfo);
        sendMessage(response, JSONObject.toJSONString(resultMap));
        return null;
    }

    @RequestMapping(value = "nodeReplace", method = {RequestMethod.POST})
    public ModelAndView nodeReplace(HttpServletResponse response, long appId, String machineInfo, String machineSentinelInfo, String downInstanceIds, int type) {

        Map<String, Object> resultMap = new HashMap<String, Object>();
        SuccessEnum successEnum = SuccessEnum.SUCCESS;
        logger.info("appid:{}, downInstanceIds:{}", appId, downInstanceIds);

        // 1.获取应用信息
        List<InstanceInfo> instanceList = appService.getAppInstanceInfo(appId);
        // 2.关闭指定下线slave节点
        Boolean shutdownFlag = shutdownInstance(downInstanceIds, appId);
        if (!shutdownFlag) {
            resultMap.put("status", SuccessEnum.ERROR.value());
            resultMap.put("message", "关闭slave节点异常，请查看日志!");
            sendMessage(response, JSONObject.toJSONString(resultMap));
            return null;
        }
        // 3.启动新Redis实例
        startInstance(instanceList, machineInfo, appId);
        if (type == ConstUtils.CACHE_REDIS_SENTINEL) {
            // 启动sentinel实例
            startSentinelInstance(instanceList, machineSentinelInfo, appId);
        }
        // 4.获取迁移后最新实例信息
        instanceList = appService.getAppInstanceInfo(appId);
        Map<String, String> instanceInfo = getInstanceInfo(instanceList, appId);
        // 5.如果是sentinel，获取sentinel实例信息
        String instanceSentinelinfo = "";
        if (type == ConstUtils.CACHE_REDIS_SENTINEL) {
            instanceSentinelinfo = getSentinelInstanceInfo(instanceList);
        }
        resultMap.put("status", successEnum.value());
        resultMap.put("instanceTargetInfo", instanceInfo.get(INSTANCE_INFO_KEY) + instanceSentinelinfo);
        resultMap.put("instanceTargetLog", instanceInfo.get(INSTANCE_LOG_KEY));
        sendMessage(response, JSONObject.toJSONString(resultMap));

        return null;
    }

    @RequestMapping(value = "msFailover", method = {RequestMethod.POST})
    public ModelAndView msFailover(HttpServletResponse response, Model model, long appId, int type) {

        // 1.从节点failover
        Map<String, Object> resultMap = redisConfigTemplateService.slaveFailover(appId);
        // 2.获取最新实例信息
        List<InstanceInfo> instanceList = appService.getAppInstanceInfo(appId);
        Map<String, String> instanceInfo = getInstanceInfo(instanceList, appId);
        // 3.需要下线的slave实例信息
        Map<String, String> mapInfo = getDownInstanceInfo(instanceList, appId, "slave");
        // 4.如果是sentinel，获取sentinel实例信息
        String instanceSentinelinfo = "";
        if (type == ConstUtils.CACHE_REDIS_SENTINEL) {
            instanceSentinelinfo = getSentinelInstanceInfo(instanceList);
        }
        resultMap.put("instanceTargetInfo", instanceInfo.get(INSTANCE_INFO_KEY) + instanceSentinelinfo);
        resultMap.put("instanceTargetLog", instanceInfo.get(INSTANCE_LOG_KEY));
        resultMap.put("downInstanceInfo", "下线实例信息:\n" + mapInfo.get(DOWN_INSTANCE_INFO_KEY));
        resultMap.put("downInstanceIds", mapInfo.get(DOWN_INSTANCE_IDS_KEY));
        sendMessage(response, JSONObject.toJSONString(resultMap));
        return null;
    }

    @RequestMapping(value = "addSlave", method = {RequestMethod.POST})
    public ModelAndView addNewSlave(HttpServletResponse response, long appId, String machineInfo, int type) {

        Map<String, Object> resultMap = new HashMap<String, Object>();
        // 1.参数验证
        if (StringUtils.isEmpty(machineInfo) && appId < 0) {
            resultMap.put("status", SuccessEnum.ERROR.value());
            resultMap.put("message", "参数异常:machineInfo:{" + machineInfo + "},appId:{" + appId + "}");
        }
        // 2.获取实例信息
        List<InstanceInfo> instanceList = appService.getAppInstanceInfo(appId);
        // 3.添加新从节点
        startInstance(instanceList, machineInfo, appId);
        // 4.日志
        instanceList = appService.getAppInstanceInfo(appId);
        // 5.如果是sentinel，获取sentinel实例信息
        String instanceSentinelinfo = "";
        if (type == ConstUtils.CACHE_REDIS_SENTINEL) {
            instanceSentinelinfo = getSentinelInstanceInfo(instanceList);
        }
        Map<String, String> instanceInfo = getInstanceInfo(instanceList, appId);
        resultMap.put("status", SuccessEnum.SUCCESS.value());
        resultMap.put("instanceTargetInfo", instanceInfo.get(INSTANCE_INFO_KEY) + instanceSentinelinfo);
        resultMap.put("instanceTargetLog", instanceInfo.get(INSTANCE_LOG_KEY));
        sendMessage(response, JSONObject.toJSONString(resultMap));
        return null;
    }

    @RequestMapping(value = "appCheck", method = {RequestMethod.POST})
    public ModelAndView appStatusCheck(HttpServletResponse response, long appId) {

        Map<String, Object> resultMap = new HashMap<String, Object>();

        // 1.连接数检测

        // 2.实例运行状态


        resultMap.put("status", SuccessEnum.SUCCESS.value());
        sendMessage(response, JSONObject.toJSONString(resultMap));
        return null;
    }

    @RequestMapping(value = "downSlave", method = {RequestMethod.POST})
    public ModelAndView downSlave(HttpServletResponse response, long appId, String downInstanceIds, int type) {

        Map<String, Object> resultMap = new HashMap<String, Object>();
        logger.info("appid:{}, downInstanceIds:{},downSentinelIds:{} ", appId, downInstanceIds);
        // 1.关闭指定下线slave节点
        Boolean shutdownFlag = shutdownInstance(downInstanceIds, appId);

        if (!shutdownFlag) {
            resultMap.put("status", SuccessEnum.ERROR.value());
            resultMap.put("message", "关闭slave节点异常，请查看日志!");
        } else {
            List<InstanceInfo> instanceList = appService.getAppInstanceInfo(appId);
            Map<String, String> instanceInfo = getInstanceInfo(instanceList, appId);

            // 2.如果是sentinel，获取sentinel实例信息
            String instanceSentinelinfo = "";
            if (type == ConstUtils.CACHE_REDIS_SENTINEL) {
                instanceSentinelinfo = getSentinelInstanceInfo(instanceList);
            }
            resultMap.put("status", SuccessEnum.SUCCESS.value());
            resultMap.put("instanceTargetInfo", instanceInfo.get(INSTANCE_INFO_KEY) + instanceSentinelinfo);
            resultMap.put("instanceTargetLog", instanceInfo.get(INSTANCE_LOG_KEY));
        }
        sendMessage(response, JSONObject.toJSONString(resultMap));
        return null;
    }

    @RequestMapping(value = "complete", method = {RequestMethod.POST})
    public ModelAndView migrateComplete(HttpServletResponse response, long appId) {

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("status", SuccessEnum.SUCCESS.value());
        sendMessage(response, JSONObject.toJSONString(resultMap));
        return null;
    }

    /**
     * <p>
     * Description: 获取实例信息
     * </p>
     *
     * @author chenshi
     * @version 1.0
     * @date 2018/10/8
     */
    public Map<String, String> getDownInstanceInfo(List<InstanceInfo> instanceList, long appId, String role) {
        Map<String, String> instanceMap = new HashMap<String, String>();
        String downInstanceInfo = "";
        List<String> downInstanceIds = new ArrayList<String>();
        if (instanceList != null && instanceList.size() > 0) {
            // 遍历获取slave节点
            for (InstanceInfo instance : instanceList) {
                if (instance.getStatus() == InstanceStatusEnum.GOOD_STATUS.getStatus() && instance.getRoleDesc().equals(role)) {
                    String redisVersion = redisCenter.getRedisVersion(appId, instance.getIp(), instance.getPort());
                    RedisInfo slaveInfo = new RedisInfo(instance.getIp(), instance.getPort(), redisVersion, instance.getRoleDesc());
                    downInstanceInfo += slaveInfo.getInfo(slaveInfo);
                    downInstanceIds.add(instance.getId() + "#" + instance.getIp() + ":" + instance.getPort());
                }
            }
        }
        instanceMap.put(DOWN_INSTANCE_INFO_KEY, downInstanceInfo);
        instanceMap.put(DOWN_INSTANCE_IDS_KEY, StringUtils.join(downInstanceIds, ","));
        return instanceMap;
    }

    /**
     * <p>
     * Description: 下线实例
     * </p>
     *
     * @author chenshi
     * @version 1.0
     * @date 2018/10/8
     */
    public Boolean shutdownInstance(String downInstanceIds, long appId) {
        if (!StringUtils.isEmpty(downInstanceIds)) {
            try {
                // 遍历获取slave节点,关闭
                for (String downInstance : downInstanceIds.split(",")) {
                    int instanceId = Integer.parseInt(downInstance.split("#")[0]);
                    String hostInfo = String.valueOf(downInstance.split("#")[1]);
                    boolean closeOp = instanceDeployCenter.shutdownExistInstance(appId, instanceId);
                    logger.info("appid:{} shutdown slave instance:[] :{}", appId, instanceId, hostInfo);
                }
            } catch (Exception e) {
                logger.info(e.getMessage(), e);
                return false;
            }
        } else {
            // 没有slave 可下线的slave节点
            logger.info(" appid:{} ,has no slave node need to shutdown ,downInstanceIds:{}", appId, downInstanceIds);
        }
        return true;
    }

    /**
     * <p>
     * Description:添加实例
     * </p>
     *
     * @param instanceList 实例列表
     * @param machineInfo  机器信息
     * @author chenshi
     * @version 1.0
     * @date 2018/10/8
     */
    public void startInstance(List<InstanceInfo> instanceList, String machineInfo, long appId) {

        // 1 当前master节点信息
        List<InstanceInfo> masterNodes = new ArrayList<InstanceInfo>();
        if (instanceList != null && instanceList.size() > 0) {
            // 遍历获取slave节点,关闭slave节点
            for (InstanceInfo instance : instanceList) {
                if (instance.getStatus() == InstanceStatusEnum.GOOD_STATUS.getStatus() && instance.getRoleDesc().equals("master")) {
                    masterNodes.add(instance);
                }
            }
        }
        // 2 启动新的slave实例
        Map<String, RedisInfo> redisInfoMap = getRedisInfo(masterNodes, machineInfo);
        if (!CollectionUtils.isEmpty(masterNodes)) {
            for (InstanceInfo masterInstance : masterNodes) {
                if (masterInstance.getRoleDesc().equals("master")) {
                    // 获取新slave节点
                    RedisInfo slaveNode = redisInfoMap.get(masterInstance.getHostPort());
                    boolean success = false;
                    if (appId > 0 && StringUtils.isNotBlank(slaveNode.getIp())) {
                        try {
                            success = redisDeployCenter.addSlave(appId, masterInstance.getId(), slaveNode.getIp());
                            // sleep 15s for master psync
                            TimeUnit.SECONDS.sleep(15);
                            // todo 检测slave节点同步数据状态成功才发起下一个节点优化
                            long start = System.currentTimeMillis();
                            Boolean psyncFlag = redisConfigTemplateService.slaveIsPsync(appId, masterInstance.getIp(), masterInstance.getPort());
                            logger.info("appid:{} add slave wait psync cost :{}ms ,psyncFlag:{}", appId, (System.currentTimeMillis() - start), psyncFlag);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                            success = false;
                        }
                    }
                    logger.warn("migrate addSlave: appId:{},masterInstanceId:{},slaveHost:{} result is {}", appId, masterInstance.getId(), slaveNode.getIp(), success);
                }
            }
        }
    }

    /**
     * <p>
     * Description: sentinel实例启动
     * </p>
     *
     * @author chenshi
     * @version 1.0
     * @date 2018/10/11
     */
    public void startSentinelInstance(List<InstanceInfo> instanceList, String machineSentinelInfo, long appId) {

        // 1 当前sentinel节点信息
        List<InstanceInfo> sentinelNodes = new ArrayList<InstanceInfo>();
        if (instanceList != null && instanceList.size() > 0) {
            // 遍历获取slave节点,关闭slave节点
            for (InstanceInfo instance : instanceList) {
                if (instance.getStatus() == InstanceStatusEnum.GOOD_STATUS.getStatus() && instance.getRoleDesc().equals("sentinel")) {
                    sentinelNodes.add(instance);
                }
            }
        }
        // 2 启动新的slave实例
        Map<String, RedisInfo> redisInfoMap = getSentinelInfo(sentinelNodes, machineSentinelInfo);
        if (!CollectionUtils.isEmpty(sentinelNodes) && !MapUtils.isEmpty(redisInfoMap)) {
            for (InstanceInfo sentnelNode : sentinelNodes) {
                // 获取新slave节点
                RedisInfo sentinelInfo = redisInfoMap.get(sentnelNode.getHostPort());
                boolean success = false;
                if (appId > 0 && StringUtils.isNotBlank(sentinelInfo.getIp())) {
                    try {
                        success = redisDeployCenter.addSentinel(appId, sentinelInfo.getIp());
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        success = false;
                    }
                }
                logger.warn("migrate addSentinel: appId:{},sentinelHost:{} result is {}", appId, sentnelNode.getHostPort(), success);
            }
        }
    }

    /**
     * <p>
     * Description: 获取迁移实例对应关系
     * </p>
     *
     * @version 1.0
     * @date 2018/10/9
     */
    public Map<String, RedisInfo> getRedisInfo(List<InstanceInfo> masterNodes, String machineInfo) {

        Map<String, RedisInfo> redisInfoMap = new HashMap<String, RedisInfo>();
        try {
            List<String> machineIps = new ArrayList<String>();
            if (!StringUtils.isEmpty(machineInfo)) {
                machineIps = Arrays.asList(machineInfo.split(ConstUtils.SEMICOLON));
            }
            logger.info("masterNodes num=" + masterNodes.size() + " ,machineNums =" + machineIps.size() + ",machine ips:" + machineIps);

            String role = "slave";
            int pos = 0;
            for (InstanceInfo masterNode : masterNodes) {
                int tag = pos % machineIps.size();
                int retryTimes = 0;
                // 挑选主从节点，如果只有重复则可以重复
                while (masterNode.getIp().equals(machineIps.get(tag)) && retryTimes++ <= 3) {
                    if (masterNodes.size() % machineIps.size() != 0) {
                        tag = (tag + masterNodes.size() % machineIps.size()) % machineIps.size();
                    } else {
                        tag = (tag + masterNodes.size() % machineIps.size() + 1) % machineIps.size();
                    }
                }
                redisInfoMap.put(masterNode.getHostPort(), new RedisInfo(machineIps.get(tag), role));
                logger.info("masterNode:{} => {}", masterNode.getHostPort(), machineIps.get(tag));
                pos++;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("redisInfoMap:{}", redisInfoMap);
        return redisInfoMap;
    }

    public Map<String, RedisInfo> getSentinelInfo(List<InstanceInfo> sentinelNodes, String machineSentinelInfo) {

        Map<String, RedisInfo> sentinelInfoMap = new HashMap<String, RedisInfo>();
        try {
            /**
             * 规则:
             * 1. 主从尽量在不同节点 (物理机节点)
             * 2. 如果节点不够可部署在同一节点
             */
            List<String> machineIps = new ArrayList<String>();
            if (!StringUtils.isEmpty(machineSentinelInfo)) {
                machineIps = Arrays.asList(machineSentinelInfo.split(ConstUtils.SEMICOLON));
            }
            String role = "sentinel";
            for (InstanceInfo sentinelNode : sentinelNodes) {
                String host = sentinelNode.getIp();
                // 获取随机节点
                List<String> ips = new ArrayList<String>();
                // 1.去重sentinelIp
                for (String ip : machineIps) {
                    if (!ip.equals(host)) {
                        ips.add(ip);
                    }
                }
                // 2.获取随机节点
                if (ips.size() > 0) {
                    int random = Math.abs((sentinelNode.getHostPort()).hashCode()) % ips.size();
                    sentinelInfoMap.put(sentinelNode.getHostPort(), new RedisInfo(ips.get(random), role));
                } else {
                    sentinelInfoMap.put(sentinelNode.getHostPort(), new RedisInfo(host, role));
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        logger.info("sentinelInfoMap:{}", sentinelInfoMap);
        return sentinelInfoMap;
    }

    private Map<String, String> getInstanceInfo(List<InstanceInfo> instanceList, long appId) {

        Map<String, String> resultMap = new HashMap<String, String>();
        //实例信息和日志信息
        String instanceInfo = "";
        String instanceLog = "";
        StringBuilder instanceInfoBuilder = new StringBuilder();
        StringBuilder instanceLogBuilder = new StringBuilder();
        //建立slave-master实例关系
        Map<String, RedisInfo> instanceMap = new HashMap<String, RedisInfo>();
        if (instanceList != null && instanceList.size() > 0) {
            // 遍历获取slave节点
            for (InstanceInfo instance : instanceList) {
                if (instance.getStatus() == InstanceStatusEnum.GOOD_STATUS.getStatus() && instance.getRoleDesc().equals("slave")) {
                    String redisVersion = redisCenter.getRedisVersion(appId, instance.getIp(), instance.getPort());
                    RedisInfo slaveInfo = new RedisInfo(instance.getId(), instance.getIp(), instance.getPort(), redisVersion, instance.getRoleDesc());
                    RedisInfo masterInfo = new RedisInfo(instance.getMasterHost(), instance.getMasterPort());
                    instanceMap.put(masterInfo.getIpAndPortInfo(masterInfo) + "#" + slaveInfo.getIpAndPortInfo(slaveInfo), slaveInfo);
                }
            }
            // 遍历获取master节点
            for (InstanceInfo instance : instanceList) {
                if (instance.getStatus() == InstanceStatusEnum.GOOD_STATUS.getStatus() && instance.getRoleDesc().equals("master")) {
                    String redisVersion = redisCenter.getRedisVersion(appId, instance.getIp(), instance.getPort());
                    RedisInfo masterInfo = new RedisInfo(instance.getIp(), instance.getPort(), redisVersion, instance.getRoleDesc());
                    //instanceInfo += masterInfo.getInfo(masterInfo);
                    //instanceLog += "<br/>";
                    instanceInfoBuilder.append(masterInfo.getInfo(masterInfo));
                    instanceLogBuilder.append("<br/>");
                    // 遍历map
                    for (Map.Entry<String, RedisInfo> redisInfo : instanceMap.entrySet()) {
                        if (redisInfo.getKey().indexOf(masterInfo.getIpAndPortInfo(masterInfo)) > -1) {
                            //instanceInfo += "------" + redisInfo.getValue().getInfo(redisInfo.getValue());
                            //instanceLog += "<a target='_blank' href=/manage/instance/log?instanceId=" + redisInfo.getValue().getSid() + ">日志</a><br/>";
                            instanceInfoBuilder.append("------")
                                    .append(redisInfo.getValue().getInfo(redisInfo.getValue()));
                            instanceLogBuilder.append("<a target='_blank' href=/manage/instance/log?instanceId=")
                                    .append(redisInfo.getValue().getSid())
                                    .append(">日志</a><br/>");
                        }
                    }
                }
            }
        }
        instanceLog = instanceLogBuilder.toString();
        instanceInfo = instanceInfoBuilder.toString();
        // 返回实例和日志信息
        resultMap.put(INSTANCE_INFO_KEY, instanceInfo);
        resultMap.put(INSTANCE_LOG_KEY, instanceLog);
        return resultMap;
    }

    /**
     * <p>
     * Description:获取sentinel实例信息
     * </p>
     *
     * @version 1.0
     * @date 2018/10/11
     */
    private String getSentinelInstanceInfo(List<InstanceInfo> instanceList) {
        //String instanceSentinelinfo = "";
        StringBuilder instanceSentinelInfoBuilder = new StringBuilder();
        List<InstanceInfo> sentinelNodes = new ArrayList<InstanceInfo>();
        if (instanceList != null && instanceList.size() > 0) {
            for (InstanceInfo instance : instanceList) {
                if (instance.getStatus() == InstanceStatusEnum.GOOD_STATUS.getStatus() && instance.getRoleDesc().equals("sentinel")) {
                    sentinelNodes.add(instance);
                }
            }
            if (sentinelNodes != null && sentinelNodes.size() > 0) {
                for (InstanceInfo sentinelNode : sentinelNodes) {
                    //instanceSentinelinfo += sentinelNode.getHostPort() + " " + sentinelNode.getTypeDesc() + " \n";
                    instanceSentinelInfoBuilder.append(sentinelNode.getHostPort())
                            .append(" ")
                            .append(sentinelNode.getTypeDesc())
                            .append(" \n");
                }
            }
        }
        return instanceSentinelInfoBuilder.toString();
    }
}
