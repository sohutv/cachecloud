package com.sohu.cache.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.sohu.cache.constant.InstanceStatusEnum;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.entity.RedisVersionStat;
import com.sohu.cache.entity.SystemResource;
import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.redis.RedisConfigTemplateService;
import com.sohu.cache.task.constant.ResourceEnum;
import com.sohu.cache.web.enums.SuccessEnum;
import com.sohu.cache.web.service.AppService;
import com.sohu.cache.web.service.ResourceService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by chenshi on 2018/9/4.
 */
@Controller
@RequestMapping("manage/redis/upgrade")
public class RedisVersionUpgradeController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(RedisVersionUpgradeController.class);

    @Autowired
    private RedisConfigTemplateService redisConfigTemplateService;
    @Autowired
    private AppService appService;
    @Autowired
    private ResourceService resourceService;

    /**
     * <p>
     * Description: Redis版本对所有机器一键安装
     * </p>
     *
     * @author chenshi
     * @version 1.0
     * @date 2018/9/5
     */
    /*@RequestMapping("install/allmachine")
    public ModelAndView installAllMachines(HttpServletResponse response, Model model, Integer versionId) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        SuccessEnum successEnum = SuccessEnum.SUCCESS;
        // 1.获取需要安装Redis版本信息
//        RedisVersion redisVersion = redisConfigTemplateService.getRedisVersionById(versionId);
        SystemResource redisResource = resourceService.getResourceById(versionId);
        if (redisResource == null) {
            successEnum = SuccessEnum.FAIL;
            resultMap.put("status", successEnum.value());
            resultMap.put("message", ErrorMessageEnum.PARAM_ERROR_MSG.getMessage());
            sendMessage(response, JSONObject.toJSONString(resultMap));
            return null;
        }
        // 2.获取机器列表，执行ssh cmd命令
        List<MachineStats> allMachineStats = machineCenter.getAllMachineStats();
        if (allMachineStats != null && allMachineStats.size() > 0) {
            for (MachineStats machineStats : allMachineStats) {
                try {
                    long start = System.currentTimeMillis();
                    // todo
//                    Boolean install = redisConfigTemplateService.checkAndInstallRedisResource(machineStats.getIp(), redisVersion);
//                    logger.info("install redis result: {} , costtime= {}ms", install, (System.currentTimeMillis() - start));
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    logger.error(" machine ip:{} install redis:{} error !", machineStats.getIp(), redisResource.getName());
                }
            }
            successEnum = SuccessEnum.SUCCESS;
        }
        resultMap.put("status", successEnum.value());
        sendMessage(response, JSONObject.toJSONString(resultMap));
        return null;
    }*/

    /**
     * <p>
     * Description: Redis版本对所有机器一键安装
     * </p>
     *
     * @author chenshi
     * @version 1.0
     * @date 2018/9/5
     */
   /* @RequestMapping("install/machine")
    public ModelAndView installMachine(HttpServletResponse response, Model model, String ip) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        SuccessEnum successEnum = SuccessEnum.FAIL;
        // 获取机器信息
        try {
            MachineInfo machineInfo = machineCenter.getMachineInfoByIp(ip);
            if (machineInfo != null) {
                String result = redisConfigTemplateService.installAllRedisOnMachine(ip);
                redisConfigTemplateService.updateMachineInstallRedis(ip);
                logger.info("machine ip:{} install redis :{}", ip, result);
                successEnum = SuccessEnum.SUCCESS;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            successEnum = SuccessEnum.FAIL;
        }
        resultMap.put("status", successEnum.value());
        sendMessage(response, JSONObject.toJSONString(resultMap));
        return null;
    }*/

    /*@RequestMapping("refresh/machines")
    public ModelAndView refreshMachineStats(HttpServletResponse response, Model model) {

        Map<String, Object> resultMap = new HashMap<String, Object>();
        SuccessEnum successEnum = SuccessEnum.FAIL;
        // 1.获取机器列表
        List<MachineStats> allMachineStats = machineCenter.getAllMachineStats();
        if (allMachineStats != null && allMachineStats.size() > 0) {
            for (MachineStats machineStats : allMachineStats) {
                try {
                    if (machineStats.getInfo().getAvailable() == MachineInfoEnum.AvailableEnum.YES.getValue()) {
                        // 3.更新线上机器version_install版本状态
                        redisConfigTemplateService.updateMachineInstallRedis(machineStats.getIp());
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    logger.error(" refresh all machine stats ip:{} install redis:{} error !", machineStats.getIp());
                }
            }
            successEnum = SuccessEnum.SUCCESS;
        }
        resultMap.put("status", successEnum.value());
        sendMessage(response, JSONObject.toJSONString(resultMap));
        return null;
    }
*/

    /**
     * <p>
     * Description: 实例和配置检查
     * </p>
     *
     * @author chenshi
     * @version 1.0
     * @date 2018/9/11
     */
    @RequestMapping(value = "check/instance", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView checkInstance(HttpServletResponse response, Long appId, Integer upgradeVersionId, String upgradeVersionName) {

        logger.info("--------check instance , appId:{} , upgradeVersionId:{}", appId, upgradeVersionId);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        SuccessEnum successEnum;

        // 1.param check
        if (appId == null || upgradeVersionId == null) {
            // return error
            resultMap.put("status", SuccessEnum.ERROR.value());
            resultMap.put("message", String.format("appId:%s或版本号:%s参数不正确", appId, upgradeVersionName));
            return null;
        }
        // 2.版本有效检查
//        RedisVersion upgradeRedisVerison = redisConfigTemplateService.getRedisVersionById(upgradeVersionId);
        SystemResource redisResource = resourceService.getResourceById(upgradeVersionId);
        if (redisResource == null || redisResource.getStatus() == 0) {
            // 无该版本配置 || 版本配置无效
            resultMap.put("status", SuccessEnum.ERROR.value());
            resultMap.put("message", String.format("升级版本号%s 无效或删除", upgradeVersionName));
            return null;
        }
        // 4. 版本配置项变更预览
        // 5. master是否都有slave节点,所有实例信息
        List<InstanceInfo> instanceList = appService.getAppInstanceInfo(appId);
        Set<String> machineSet = new HashSet<String>();
        String instanceInfo;
        StringBuilder instanceInfoBuilder = new StringBuilder();
        int masterNum = 0;
        int slaveNum = 0;
        if (instanceList != null && instanceList.size() > 0) {
            for (InstanceInfo instance : instanceList) {
                if (instance.getStatus() == InstanceStatusEnum.GOOD_STATUS.getStatus()) {
                    String redisVersion = redisCenter.getRedisVersion(appId, instance.getIp(), instance.getPort());
                    //instanceInfo += instance.getIp() + ":" + instance.getPort() + " " + instance.getRoleDesc() + " version:" + redisVersion + "\n";
                    instanceInfoBuilder.append(instance.getIp())
                            .append(":")
                            .append(instance.getPort())
                            .append(instance.getRoleDesc())
                            .append(" version:")
                            .append(redisVersion)
                            .append("\n");
                    // 主从节点判断
                    if (instance.getRoleDesc().equals("slave")) {
                        slaveNum++;
                    } else if (instance.getRoleDesc().equals("master")) {
                        masterNum++;
                    }
                    machineSet.add(instance.getIp());
                }
            }
        }
        instanceInfo = instanceInfoBuilder.toString();
        // master节点数 <= slave节点数，才能迁移
        if (masterNum > slaveNum || instanceList == null) {
            successEnum = SuccessEnum.ERROR;
            resultMap.put("message", String.format("主(%d)从(%d)实例节点不一致", masterNum, slaveNum));
        } else {
            successEnum = SuccessEnum.SUCCESS;
        }
        // 5.1 实例机器redis资源包安装检查，如果没有redis资源包，自动拉取安装资源包
        String machineInstallInfo = "";
        if (!CollectionUtils.isEmpty(machineSet) && successEnum == SuccessEnum.SUCCESS) {
            for (String machineIp : machineSet) {
                // Redis资源校验&推包
                Boolean installStatus = redisConfigTemplateService.checkAndInstallRedisResource(machineIp, redisResource);
                if (!installStatus) {
                    successEnum = SuccessEnum.ERROR;
                    resultMap.put("message", String.format("%s安装 %s版本失败,请检查日志！", machineIp, redisResource.getName()));
                    break;
                }
            }
        }

        resultMap.put("status", successEnum.value());
        resultMap.put("upgradeVersion", redisResource);
        resultMap.put("instanceInfo", instanceInfo);
        resultMap.put("machineInstallInfo", machineInstallInfo);
        sendMessage(response, JSONObject.toJSONString(resultMap));
        return null;
    }

    /**
     * <p>
     * Description: slave更新配置并重启
     * </p>
     *
     * @author chenshi
     * @version 1.0
     * @date 2018/9/13
     */
    @RequestMapping(value = "slave/update/config", method = RequestMethod.POST)
    public ModelAndView slaveUpdateConfig(HttpServletResponse response, Long appId, Integer upgradeVersionId, String upgradeVersionName) {

        Map<String, Object> resultMap = redisConfigTemplateService.slaveUpdateConfig(appId, upgradeVersionId, upgradeVersionName);
        sendMessage(response, JSONObject.toJSONString(resultMap));
        return null;
    }

    /**
     * <p>
     * Description: slave failover
     * </p>
     *
     * @author chenshi
     * @version 1.0
     * @date 2018/9/13
     */
    @RequestMapping(value = "slave/failover", method = RequestMethod.POST)
    public ModelAndView masterSlavefailover(HttpServletResponse response, Long appId) {

        // 1.failover 状态
        Map<String, Object> resultMap = redisConfigTemplateService.slaveFailover(appId);
        // 2. 输出节点信息及日志
        List<InstanceInfo> instanceList = appService.getAppInstanceInfo(appId);
        StringBuilder instanceInfoBuilder = new StringBuilder();
        StringBuilder instanceLogBuilder = new StringBuilder();
        if (instanceList != null && instanceList.size() > 0) {
            for (InstanceInfo instance : instanceList) {
                if (instance.getStatus() == InstanceStatusEnum.GOOD_STATUS.getStatus()) {
                    String redisVersion = redisCenter.getRedisVersion(appId, instance.getIp(), instance.getPort());
                    //instanceInfo += instance.getIp() + ":" + instance.getPort() + " " + instance.getRoleDesc() + " version:" + redisVersion + " \n";
                    //instanceLog += "<a target='_blank' href=/manage/instance/log?instanceId=" + instance.getId() + ">日志</a><br/>";
                    instanceInfoBuilder.append(instance.getIp())
                            .append(":")
                            .append(instance.getPort())
                            .append(" ")
                            .append(instance.getRoleDesc())
                            .append(" version:")
                            .append(redisVersion)
                            .append(" \n");
                    instanceLogBuilder.append("<a target='_blank' href=/manage/instance/log?instanceId=" + instance.getId() + ">日志</a><br/>");
                }
            }
        }
        resultMap.put("instanceInfo", instanceInfoBuilder.toString());
        resultMap.put("instanceLog", instanceLogBuilder.toString());

        sendMessage(response, JSONObject.toJSONString(resultMap));
        return null;
    }

    /**
     * <p>
     * Description: 更新应用信息，返回最终实例信息
     * </p>
     *
     * @author chenshi
     * @version 1.0
     * @date 2018/9/13
     */
    @RequestMapping(value = "complete/check", method = {RequestMethod.POST, RequestMethod.POST})
    public ModelAndView completeCheck(HttpServletResponse response, Long appId, Integer upgradeVersionId) {

        Map<String, Object> resultMap = new HashMap<String, Object>();
        // 1.更新应用为升级的版本
        AppDesc appDesc = appService.getByAppId(appId);
        appDesc.setVersionId(upgradeVersionId);
        appService.update(appDesc);
        // 2.遍历当前节点状态
        List<InstanceInfo> instancelist = appService.getAppInstanceInfo(appId);
        StringBuilder instanceInfoBuilder = new StringBuilder();
        if (instancelist != null && instancelist.size() > 0) {
            for (InstanceInfo instance : instancelist) {
                if (instance.getStatus() == InstanceStatusEnum.GOOD_STATUS.getStatus()) {
                    String redisVersion = redisCenter.getRedisVersion(appId, instance.getIp(), instance.getPort());
                    //instanceInfo += instance.getIp() + ":" + instance.getPort() + " " + instance.getRoleDesc() + " version:" + redisVersion + " \n";
                    instanceInfoBuilder.append(instance.getIp())
                            .append(":")
                            .append(instance.getPort())
                            .append(" ")
                            .append(instance.getRoleDesc())
                            .append(" version:")
                            .append(redisVersion)
                            .append(" \n");
                }
            }
        }

        resultMap.put("status", SuccessEnum.SUCCESS.value());
        resultMap.put("instanceInfo", instanceInfoBuilder.toString());
        resultMap.put("instanceLog", "");
        sendMessage(response, JSONObject.toJSONString(resultMap));
        return null;
    }

    public void setRedisConfigTemplateService(RedisConfigTemplateService redisConfigTemplateService) {
        this.redisConfigTemplateService = redisConfigTemplateService;
    }

}
