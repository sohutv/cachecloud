package com.sohu.cache.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.sohu.cache.async.AsyncService;
import com.sohu.cache.async.AsyncThreadPoolFactory;
import com.sohu.cache.async.KeyCallable;
import com.sohu.cache.entity.*;
import com.sohu.cache.exception.SSHException;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.ssh.SSHService;
import com.sohu.cache.stats.instance.InstanceDeployCenter;
import com.sohu.cache.stats.instance.InstanceStatsCenter;
import com.sohu.cache.task.constant.InstanceRoleEnum;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.StringUtil;
import com.sohu.cache.web.enums.SuccessEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import redis.clients.jedis.HostAndPort;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 应用后台管理
 *
 * @author leifu
 * @Time 2014年7月3日
 */
@Controller
@RequestMapping("manage/instance")
public class InstanceManageController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(InstanceManageController.class);

    @Resource(name = "instanceDeployCenter")
    private InstanceDeployCenter instanceDeployCenter;

    @Resource(name = "redisCenter")
    private RedisCenter redisCenter;

    @Resource(name = "instanceStatsCenter")
    private InstanceStatsCenter instanceStatsCenter;

    @Resource
    SSHService sshService;

    /**
     * 上线(和下线分开)
     *
     * @param instanceId
     */
    @RequestMapping(value = "/startInstance")
    public ModelAndView doStartInstance(HttpServletRequest request, HttpServletResponse response, Model model, long appId, int instanceId) {
        AppUser appUser = getUserInfo(request);
        logger.warn("user {} startInstance {} ", appUser.getName(), instanceId);
        boolean result = false;
        if (instanceId > 0) {
            try {
                result = instanceDeployCenter.startExistInstance(appId, instanceId);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                model.addAttribute("message", e.getMessage());
            }
        } else {
            logger.error("doStartInstance instanceId:{}", instanceId);
            model.addAttribute("message", "wrong param");
        }
        logger.warn("user {} startInstance {} result is {}", appUser.getName(), instanceId, result);
        if (result) {
            model.addAttribute("success", SuccessEnum.SUCCESS.value());
        } else {
            model.addAttribute("success", SuccessEnum.FAIL.value());
        }
        return new ModelAndView();
    }

    @RequestMapping(value = "/scrollStartInstance")
    public ModelAndView scrollStartInstance(HttpServletRequest request, Model model, String machineIp) {

        AppUser appUser = getUserInfo(request);
        logger.warn("user {} scroll startInstance ip :{} ", appUser.getName(), machineIp);
        try {
            List<InstanceAlertValueResult> instanceAlertValueResults = instanceDeployCenter.checkAndStartExceptionInstance(machineIp, false);
            if (!CollectionUtils.isEmpty(instanceAlertValueResults)) {
                model.addAttribute("message", "滚动重启：恢复实例数量:" + instanceAlertValueResults.size());
            } else {
                model.addAttribute("message", "滚动重启：无实例需要启动!");
            }
            model.addAttribute("success", SuccessEnum.SUCCESS.value());
        } catch (Exception e) {
            logger.error("scrollStartInstance error message :{}", e.getMessage(), e);
            model.addAttribute("success", SuccessEnum.FAIL.value());
            model.addAttribute("message", "滚动重启异常：" + e.getMessage());
        }
        return new ModelAndView();
    }

    /**
     * 下线实例
     *
     * @param instanceId
     */
    @RequestMapping(value = "/shutdownInstance")
    public ModelAndView doShutdownInstance(HttpServletRequest request, HttpServletResponse response, Model model, long appId, int instanceId) {
        AppUser appUser = getUserInfo(request);
        logger.warn("user {} shutdownInstance {} ", appUser.getName(), instanceId);
        boolean result = false;
        if (instanceId > 0) {
            try {
                result = instanceDeployCenter.shutdownExistInstance(appId, instanceId);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                model.addAttribute("message", e.getMessage());
            }
        } else {
            logger.error("doShutdownInstance instanceId:{}", instanceId);
            model.addAttribute("message", "wrong param");
        }
        logger.warn("user {} shutdownInstance {}, result is {}", appUser.getName(), instanceId, result);
        if (result) {
            model.addAttribute("success", SuccessEnum.SUCCESS.value());
        } else {
            model.addAttribute("success", SuccessEnum.FAIL.value());
        }
        return new ModelAndView();
    }

    /**
     * cluster forget instance
     *
     * @param instanceId
     */
    @RequestMapping(value = "/forgetInstance")
    public ModelAndView forgetInstance(HttpServletRequest request, HttpServletResponse response, Model model, long appId, int instanceId) {
        AppUser appUser = getUserInfo(request);
        logger.warn("user {} forgetInstance {} ", appUser.getName(), instanceId);
        boolean result = false;
        if (instanceId > 0) {
            try {
                result = instanceDeployCenter.forgetInstance(appId, instanceId);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                model.addAttribute("message", e.getMessage());
            }
        } else {
            logger.error("doForgetInstance instanceId:{}", instanceId);
            model.addAttribute("message", "wrong param");
        }
        logger.warn("user {} forgetInstance {}, result is {}", appUser.getName(), instanceId, result);
        if (result) {
            model.addAttribute("success", SuccessEnum.SUCCESS.value());
        } else {
            model.addAttribute("success", SuccessEnum.FAIL.value());
            model.addAttribute("message", "请查看日志");
        }
        return new ModelAndView();
    }

    @Resource
    AsyncService asyncService;

    @PostConstruct
    public void init() {
        asyncService.assemblePool(AsyncThreadPoolFactory.TASK_EXECUTE_POOL,
                AsyncThreadPoolFactory.TASK_EXECUTE_THREAD_POOL);
    }

    @RequestMapping("/migrate")
    public ModelAndView doMigrateInstance(HttpServletResponse response, String sourceIp, String targetIp, String instanceIds) {

        Map<String, Object> resultMap = new HashedMap();
        String key = "migrate-instance-" + sourceIp + "-" + targetIp;
        asyncService.submitFuture(AsyncThreadPoolFactory.MACHINE_POOL, new KeyCallable<Boolean>(key) {
            public Boolean execute() {
                try {
                    migrate(sourceIp, targetIp, instanceIds);
                    return true;
                } catch (Exception e) {
                    logger.error("doMigrateInstance ", e.getMessage(), e);
                    return false;
                }
            }
        });

        resultMap.put("status", 1);
        sendMessage(response, JSONObject.toJSONString(resultMap));
        return null;
    }

    public boolean migrate(String sourceIp, String targetIp, String instanceIds) throws SSHException {

        /**
         *  1. 检查目标容器的连通性
         *  2. 获取需要迁移的实例信息
         *  3. 遍历实例:(只对cluster实例迁移)
         *      3.1 standalone/sentinel节点跳过
         *      3.2 如果实例是master：添加新的从节点；获取master节点slave0；执行failover ；下线老节点
         *      3.3 如果是slave节点：添加新从节点；下线老的从节点
         *  4. 输出报告：下线节点数 ，迁移节点数
         */
        //1.检查目标容器的连通性
        String execute = sshService.execute(targetIp, "echo ok");

        //2.获取机器所有实例
        List<InstanceInfo> instanceList = machineCenter.getMachineInstanceInfo(sourceIp);

        //3.如果是部分实例迁移，剔除不需要迁移的实例
        if (!StringUtil.isBlank(instanceIds) && !instanceIds.equals("-1")) {
            List<String> partInstanceIds = Arrays.asList(instanceIds.split(","));
            List<InstanceInfo> partInstanceList = new ArrayList<>();
            // 找出需要迁移的实例
            for (InstanceInfo instanceInfo : instanceList) {
                if (instanceInfo != null && partInstanceIds.contains(String.valueOf(instanceInfo.getId()))) {
                    partInstanceList.add(instanceInfo);
                }
            }
            instanceList = partInstanceList;
        }
        logger.info("container instance migrate instanceIds:{} list size:{}", instanceIds, instanceList.size());

        // 4.开始迁移
        if (!CollectionUtils.isEmpty(instanceList)) {
            for (InstanceInfo instanceInfo : instanceList) {
                if (instanceInfo.isOnline() && instanceInfo.getType() == ConstUtils.CACHE_TYPE_REDIS_CLUSTER) {
                    // a)当前为master节点
                    if (instanceInfo.getRoleDesc() == InstanceRoleEnum.MASTER.getInfo()) {
                        try {
                            //a.1)获取master节点slave0
                            AppDesc appdesc = appService.getByAppId(instanceInfo.getAppId());
                            HostAndPort slave0 = redisCenter.getSlave0(instanceInfo.getIp(), instanceInfo.getPort(), appdesc.getPasswordMd5());
                            // a.2)执行failover
                            if (slave0 == null) {
                                continue;
                            }
                            boolean isFailover = redisDeployCenter.clusterFailover(instanceInfo.getAppId(), slave0, "force");
                            if (!isFailover) {
                                continue;
                            }
                            int times = 0;
                            boolean checkFailover = false;
                            while (!checkFailover && times++ <= 10) {
                                Boolean status = redisCenter.getRedisReplicationStatus(instanceInfo.getAppId(), slave0.getHost(), slave0.getPort());
                                if (status) {
                                    checkFailover = status;
                                } else {
                                    TimeUnit.MILLISECONDS.sleep(6000);
                                    logger.info(" check slave replication status ,waiting 5s ....");
                                }
                            }
                            if (!checkFailover) {
                                // 如果failover失败 ，则不下线源节点，继续轮训下个节点
                                continue;
                            }

                            TimeUnit.SECONDS.sleep(5);

                            //a.3) 添加新的从节点
                            Boolean isSuccess = null;
                            HostAndPort masterInfo = redisCenter.getMaster(instanceInfo.getIp(), instanceInfo.getPort(), appdesc.getPasswordMd5());
                            if (!StringUtils.isEmpty(masterInfo.getHost()) && masterInfo.getPort() > 0) {
                                InstanceInfo masterInst = instanceDao.getInstByIpAndPort(masterInfo.getHost(), masterInfo.getPort());
                                if (masterInst == null) {
                                    continue;
                                }
                                isSuccess = redisDeployCenter.addSlave(instanceInfo.getAppId(), masterInst.getId(), targetIp);
                                if (!isSuccess) {
                                    // 添加从节点 失败，则退出 检查原因
                                    logger.error("migrate add slave {}:{} fail", instanceInfo.getAppId(), masterInst.getId());
                                    break;
                                }
                            }

                            //a.4) 下线节点
                            boolean isOffline = instanceDeployCenter.shutdownExistInstance(instanceInfo.getAppId(), instanceInfo.getId());
                            logger.info("MigrateInstance appid:{} offline node master:{} {}， add new slave :{} {}", instanceInfo.getAppId(), instanceInfo.getHostPort(), isOffline, targetIp, isSuccess);


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    // b).当前为slave节点
                    if (instanceInfo.getRoleDesc() == InstanceRoleEnum.SLAVE.getInfo()) {
                        AppDesc appdesc = appService.getByAppId(instanceInfo.getAppId());
                        HostAndPort masterInfo = redisCenter.getMaster(instanceInfo.getIp(), instanceInfo.getPort(), appdesc.getPasswordMd5());
                        if (!StringUtils.isEmpty(masterInfo.getHost()) && masterInfo.getPort() > 0) {
                            try {
                                InstanceInfo masterInst = instanceDao.getInstByIpAndPort(masterInfo.getHost(), masterInfo.getPort());
                                if (masterInst == null) {
                                    continue;
                                }
                                //添加新的slave节点
                                boolean isSuccess = redisDeployCenter.addSlave(instanceInfo.getAppId(), masterInst.getId(), targetIp);
                                //下线当前slave节点
                                boolean isOffline = instanceDeployCenter.shutdownExistInstance(instanceInfo.getAppId(), instanceInfo.getId());
                                // sleep 5s
                                TimeUnit.SECONDS.sleep(5);
                                logger.info("MigrateInstance appid:{} offline slave:{} {},add slave :{} {}", instanceInfo.getAppId(), instanceInfo.getHostPort(), isOffline, targetIp, isSuccess);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * 查看redis节点日志
     */
    @RequestMapping("/log")
    public ModelAndView doShowLog(HttpServletRequest request, HttpServletResponse response, Model model, int instanceId) {
        int pageSize = NumberUtils.toInt(request.getParameter("pageSize"), 0);
        if (pageSize == 0) {
            pageSize = 100;
        }
        String instanceLogStr = instanceDeployCenter.showInstanceRecentLog(instanceId, pageSize);
        model.addAttribute("instanceLogList", StringUtils.isBlank(instanceLogStr) ? Collections.emptyList() : Arrays.asList(instanceLogStr.split("\n")));
        return new ModelAndView("manage/instance/log");
    }

    /**
     * 处理实例配置修改
     *
     * @param appAuditId 审批id
     */
    @RequestMapping(value = "/initInstanceConfigChange")
    public ModelAndView doInitInstanceConfigChange(HttpServletRequest request,
                                                   HttpServletResponse response, Model model, Long appAuditId) {
        // 申请原因
        AppAudit appAudit = appService.getAppAuditById(appAuditId);
        model.addAttribute("appAudit", appAudit);

        // 用第一个参数存实例id
        Long instanceId = NumberUtils.toLong(appAudit.getParam1());
        Map<String, String> redisConfigList = redisCenter.getRedisConfigList(instanceId.intValue());
        model.addAttribute("redisConfigList", redisConfigList);

        // 实例
        InstanceInfo instanceInfo = instanceStatsCenter.getInstanceInfo(instanceId);
        model.addAttribute("instanceInfo", instanceInfo);
        model.addAttribute("appId", appAudit.getAppId());
        model.addAttribute("appAuditId", appAuditId);

        // 修改配置的键值对
        model.addAttribute("instanceConfigKey", appAudit.getParam2());
        model.addAttribute("instanceConfigValue", appAudit.getParam3());

        return new ModelAndView("manage/appAudit/initInstanceConfigChange");
    }

    /**
     * @param appId               应用id
     * @param host                实例ip
     * @param port                实例端口
     * @param instanceConfigKey   实例配置key
     * @param instanceConfigValue 实例配置value
     * @param appAuditId          审批id
     * @return
     */
    @RequestMapping(value = "/addInstanceConfigChange")
    public ModelAndView doAddAppConfigChange(HttpServletRequest request,
                                             HttpServletResponse response, Model model, Long appId, String host, int port,
                                             String instanceConfigKey, String instanceConfigValue, Long appAuditId) {
        AppUser appUser = getUserInfo(request);
        logger.warn("user {} change instanceConfig:appId={},{}:{};key={};value={},appAuditId:{}", appUser.getName(), appId, host, port, instanceConfigKey, instanceConfigValue, appAuditId);
        boolean isModify = false;
        if (StringUtils.isNotBlank(host) && port > 0 && StringUtils.isNotBlank(instanceConfigKey)) {
            try {
                if (appAuditId != null) {
                    appAuditDao.updateAppAuditOperateUser(appAuditId, appUser.getId());
                }
                isModify = instanceDeployCenter.modifyInstanceConfig(appId, appAuditId, host, port, instanceConfigKey, instanceConfigValue);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        logger.warn("user {} change instanceConfig:appId={},{}:{};key={};value={},appAuditId:{},result is:{}", appUser.getName(), appId, host, port, instanceConfigKey, instanceConfigValue, appAuditId, isModify);
        if (appAuditId != null) {
            return new ModelAndView("redirect:/manage/app/auditList");
        } else {
            JSONObject json = new JSONObject();
            json.put("result", isModify ? 1 : 0);
            sendMessage(response, json.toString());
            return new ModelAndView("");
        }
    }


}
