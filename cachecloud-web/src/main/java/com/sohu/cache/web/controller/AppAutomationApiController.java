package com.sohu.cache.web.controller;

import com.sohu.cache.async.AsyncThreadPoolFactory;
import com.sohu.cache.async.KeyCallable;
import com.sohu.cache.constant.*;
import com.sohu.cache.entity.*;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.StringUtil;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.*;

/**
 * Created by chenshi on 2018/10/29.
 */
@RestController
@RequestMapping("/api/automation")
public class AppAutomationApiController extends BaseController {

    // httpcode:500异常信息
    private static String ERROR_EXCEPTION = "系统异常:%s";
    private static String ERROR_APP_NAME_REPEATE = "应用名称:%s重复";
    private static String ERROR_APP_REDISVERSION_NOEXIST = "Redis版本:%s不存在";
    private static String ERROR_APP_USER_NOEXIST = "用户信息不完整:%s";
    private static String ERROR_MACHINE_CLUSTER_NOTENOUGH = "RedisCluster集群机器:%s数量少于3台";
    private static String ERROR_MACHINE_SENTINEL_NOTENOUGH = "RedisSentinel集群机器:%s数量少于2台";
    private static String ERROR_MACHINE_IPINFO = "机器列表iplist:%s,非法机器ip:%s";
    private static String ERROR_RESOURCE_INSUFFICENT = "机器:%s实例资源分配不足";
    private static String ERROR_MEM_ASSIGN = "申请内存:%s M, 机器剩余总内存:%s M,内存分配不足(至少%s M)";
    private static String ERROR_CPU_ASSIGN = "实例数:%s , 机器总核数:%s, cpu分配不足";
    private static String ERROR_REDIS_FORMAT = "部署格式检查异常:%s ";
    private static String ERROR_SETNIENL_NOTENOUGH = "RedisSentinel机器分配不足";
    private static String ERROR_APP_TOKEN = "token校验异常:%s,没有权限调用";
    private static String ERROR_INSTANCE_NUM = "%s应用部署异常，当前实例数:%s";
    // info信息
    private static String ERROR_APP_SAVE_INFO = "应用信息保存异常";
    private static String ERROR_REDIS_DEPLOY_INFO = "Redis应用部署异常";
    private static String ERROR_REDISCLUSTER_INTEGRITY_INFO = "RedisCluster slots不等于16384, slots num=";
    private static String ERROR_APP_SETPASSWD_INFO = "应用设置密码异常";
    private static String SUCCESS_INFO = "创建成功";
    // 内存分配上限
    private static double MEM_MAX_RATIO = 0.85;// 85%留给应用使用
    private static int MAX_INSTANCE_MEM = 2;// 最大分片2G
    // app token
    private static String APP_TOKEN = "appAdd";

    /**
     * <p>
     * Description: 应用状态检测接口
     * </p>
     *
     * @author chenshi
     * @version 1.0
     * @date 2018/11/14
     */
    @RequestMapping(value = "/app/inspect", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<List<Map<String, Object>>> appInspect(
            @RequestHeader(value = "token") String token,
            @RequestBody List<String> appIds) {

        // 1.token 校验
        if (token.isEmpty() || !token.equals(APP_TOKEN)) {
            logger.error("error token:{}", String.format(ERROR_APP_TOKEN, token));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        // 2.appids 校验
        if (CollectionUtils.isEmpty(appIds)) {
            logger.error("error param: appIds:{}", appIds);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        // 3.获取appid 状态
        try {
            List<AppDesc> appDescs = appService.checkAppStatus(appIds);
            List<Map<String, Object>> lists = new ArrayList<Map<String, Object>>();
            for (AppDesc appDesc : appDescs) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("appid", appDesc.getAppId());
                map.put("status", appDesc.getStatus());
                if (appDesc.getStatus() == 4) {
                    List<AppAudit> appAudits = appService.getAppAudits(appDesc.getAppId(), AppAuditType.APP_AUDIT.getValue());
                    if (!CollectionUtils.isEmpty(appAudits)) {
                        map.put("message", appAudits.get(0).getRefuseReason());
                    }
                }
                lists.add(map);
            }
            return ResponseEntity.status(HttpStatus.OK).body(lists);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * <p>
     * Description: api自动创建集群
     * </p>
     *
     * @author chenshi
     * @version 1.0
     * @date 2018/11/14
     */
    @RequestMapping(value = "/app", method = {RequestMethod.POST})
    public ResponseEntity<String> addApp(
            @RequestHeader(value = "token") String token,
            @RequestBody AppInfoApi appInfoApi) {

        // 接口执行时间
        long startTime = System.currentTimeMillis();
        JSONObject response = new JSONObject();
        /**
         * 1.token权限校验
         */
        if (token.isEmpty() || !token.equals(APP_TOKEN)) {
            logger.error("error:{}", String.format(ERROR_APP_TOKEN, token));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.format(ERROR_APP_TOKEN, token));
        }
        /**
         * 2.默认属性设置
         */
        setDefaultConfig(appInfoApi);

        /**
         * 3. 基础信息检查
         */
        RedisVersion redisVersion = null;
        AppUser appUser = null;
        try {
            //1.1 应用名
            AppDesc appByName = appService.getAppByName(appInfoApi.getName());
            if (appByName != null && appByName.getAppId() > 0) {
                logger.error("error:{}", String.format(ERROR_APP_NAME_REPEATE, appInfoApi.getName()));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.format(ERROR_APP_NAME_REPEATE, appInfoApi.getName()));
            }
            // 1.2 redisVersion
            SystemResource redisResource = redisConfigTemplateService.getRedisVersionByName(appInfoApi.getRedisVersion());
            if (redisResource != null && redisResource.getId() > 0) {
                logger.error("error:{}", String.format(ERROR_APP_REDISVERSION_NOEXIST, appInfoApi.getRedisVersion()));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.format(ERROR_APP_REDISVERSION_NOEXIST, appInfoApi.getRedisVersion()));
            }
            logger.info("redisVersion:{}", redisVersion);
            // 1.3 获取用户信息
            AppUser user = appInfoApi.getUser();
            if (user == null || StringUtils.isEmpty(user.getName())) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.format(ERROR_APP_USER_NOEXIST, appInfoApi.getUser()));
            }
            if (!StringUtils.isEmpty(user.getName())) {
                appUser = userService.getByName(appInfoApi.getUser().getName());
                logger.info("get user:{}", appUser);
            }
            // 如果不是cc用户 自动创建
            if (appUser == null) {
                appUser = new AppUser(user.getName(), user.getChName(), user.getEmail(), user.getMobile(), user.getWeChat(), AppUserTypeEnum.REGULAR_USER.value(), AppUserAlertEnum.YES.value());
                userService.save(appUser);
                logger.info("create new appUser:{}", appUser);
            }
            if (appUser.getId() == null) {
                logger.error("error:{}", String.format(ERROR_APP_USER_NOEXIST, appInfoApi.getUser().getName()));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.format(ERROR_APP_USER_NOEXIST, appInfoApi.getUser().getName()));
            }

            // 1.4 机器ip合法性验证
            if (!CollectionUtils.isEmpty(appInfoApi.getIplist())) {
                // 非法机器ip信息
                List<String> invalidIps = new ArrayList<String>();
                for (String ip : appInfoApi.getIplist()) {
                    if (!StringUtils.isEmpty(ip)) {
                        MachineInfo machineinfo = machineCenter.getMachineInfoByIp(ip);
                        if (machineinfo == null) {
                            invalidIps.add(ip);
                        }
                    }
                }
                // 存在非法机器信息 返回异常信息
                if (!CollectionUtils.isEmpty(invalidIps) && invalidIps.size() > 0) {
                    logger.error("iplist:{} exist invalid ipinfo:{}", appInfoApi.getIplist(), invalidIps);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.format(ERROR_MACHINE_IPINFO, appInfoApi.getIplist(), invalidIps));
                }

            } else {
                logger.error("iplist is empty ,error:{}", appInfoApi.getIplist());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.format(ERROR_MACHINE_IPINFO, appInfoApi.getIplist(), "无可用机器ip"));
            }

            // 1.5 集群 机器数>=3  sentinel 机器数>=2 sentinel单独分配
            if (appInfoApi.getType() == ConstUtils.CACHE_TYPE_REDIS_CLUSTER && appInfoApi.getIplist().size() < 3) {
                logger.error("error:{}", String.format(ERROR_MACHINE_CLUSTER_NOTENOUGH, appInfoApi.getIplist()));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.format(ERROR_MACHINE_CLUSTER_NOTENOUGH, appInfoApi.getIplist()));
            } else if (appInfoApi.getType() == ConstUtils.CACHE_REDIS_SENTINEL && appInfoApi.getIplist().size() < 2) {
                logger.error("error:{}", String.format(ERROR_MACHINE_SENTINEL_NOTENOUGH, appInfoApi.getIplist()));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.format(ERROR_MACHINE_SENTINEL_NOTENOUGH, appInfoApi.getIplist()));
            }
        } catch (Exception e) {
            // exception  系统异常 500
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.format(ERROR_EXCEPTION, e.getMessage()));
        }

        /**
         * 4.自动分配实例
         *      4.1 自动计算实例个数 instanceNum
         *      4.2 实例分配完成合理性验证
         */
        List<DeployInfo> deployInfoList = new ArrayList<DeployInfo>();
        List<MachineMemStatInfo> machineMems = new ArrayList<MachineMemStatInfo>();
        String sentinelMachines = "";//setnienl节点列表
        String appDeployText = "";//部署实例信息
        StringBuilder appDeployTextBuilder = new StringBuilder();
        try {
            String ips = String.join(",", appInfoApi.getIplist());
            logger.info("ips :" + ips);
            if (appInfoApi.getType() == ConstUtils.CACHE_REDIS_SENTINEL) {
                //从机器列表获取sentinel节点
                sentinelMachines = autoSelectSentinel(appInfoApi);
                if (sentinelMachines == null) {
                    logger.error(String.format(ERROR_SETNIENL_NOTENOUGH));
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.format(ERROR_SETNIENL_NOTENOUGH));
                }
            }

            String result = appService.generateDeployInfo(appInfoApi.getType(), appInfoApi.getHasSlave(), appInfoApi.getRoom(), Double.parseDouble(appInfoApi.getMemTotalSize() * 1024 + ""),
                    appInfoApi.getIplist().size(), appInfoApi.getInstanceNum(), 0, ips,
                    "", sentinelMachines, deployInfoList, machineMems);
            logger.info("assign redis instance result :" + result);

            // 4.2 拆解实例
            if (!CollectionUtils.isEmpty(deployInfoList)) {
                for (DeployInfo deployInfo : deployInfoList) {
                    if (appInfoApi.getHasSlave() == 1) {
                        if (deployInfo.getMasterIp() != null) {
                            //appDeployText += deployInfo.getMasterIp() + ConstUtils.COLON + deployInfo.getMemSize() + ConstUtils.COLON + deployInfo.getSlaveIp() + "\n";
                            appDeployTextBuilder.append(deployInfo.getMasterIp())
                                    .append(ConstUtils.COLON)
                                    .append(deployInfo.getMemSize())
                                    .append(ConstUtils.COLON)
                                    .append(deployInfo.getSlaveIp())
                                    .append("\n");
                        }
                        if (deployInfo.getSentinelIp() != null) {
                            //appDeployText += deployInfo.getSentinelIp() + "\n";
                            appDeployTextBuilder.append(deployInfo.getSentinelIp()).append("\n");
                        }
                    } else {
                        if (deployInfo.getMasterIp() != null) {
                            //appDeployText += deployInfo.getMasterIp() + ConstUtils.COLON + deployInfo.getMemSize() + "\n";
                            appDeployTextBuilder.append(deployInfo.getMasterIp())
                                    .append(ConstUtils.COLON)
                                    .append(deployInfo.getMemSize())
                                    .append("\n");
                        }
                        if (deployInfo.getSentinelIp() != null) {
                            //appDeployText += deployInfo.getSentinelIp() + "\n";
                            appDeployTextBuilder.append(deployInfo.getSentinelIp()).append("\n");
                        }
                    }
                }
            } else {
                logger.error(String.format(ERROR_RESOURCE_INSUFFICENT, ips));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.format(ERROR_RESOURCE_INSUFFICENT, ips));
            }
            appDeployText = appDeployTextBuilder.toString();
            logger.info("deployInfo : {}", appDeployText);
        } catch (Exception e) {
            // exception  系统异常 500
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.format(ERROR_EXCEPTION, e.getMessage()));
        }

        /**
         * 5. 应用信息部署前 实例格式检查
         *    5.1 内存/cpu核数检查
         *    5.2 实例格式检查
         */
        try {
            // cpu和mem检查
            Map<String, Integer> machineInstanceCountMap = machineCenter.getMachineInstanceCountMap();
            if (!CollectionUtils.isEmpty(machineMems)) {
                long remainMem = 0;
                int remianCpu = 0;
                for (MachineMemStatInfo machineMemStatInfo : machineMems) {
                    remainMem += (machineMemStatInfo.getMem() * 1024 - machineMemStatInfo.getApplyMem() / 1024 / 1024);
                    int machineCpu = machineInstanceCountMap.get(machineMemStatInfo.getIp()) == null ? 0 : machineInstanceCountMap.get(machineMemStatInfo.getIp());
                    remianCpu += machineMemStatInfo.getCpu() - machineCpu;
                }
                // 1)机器剩余内存量
                long applyMem = appInfoApi.getHasSlave() == 0 ? appInfoApi.getMemTotalSize() * 1024 : appInfoApi.getMemTotalSize() * 2 * 1024;
                if (remainMem * MEM_MAX_RATIO < applyMem) {
                    logger.error(String.format(ERROR_MEM_ASSIGN, applyMem, remainMem, applyMem * 1.25));
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.format(ERROR_MEM_ASSIGN, applyMem, remainMem, applyMem * 1.25));
                }
                // 2).机器剩余cpu核数，应用部署实例数,需要判断是否有主从
                int instanceNumber = 1; //standalone
                if (appInfoApi.getType() == ConstUtils.CACHE_REDIS_SENTINEL) {
                    instanceNumber = 2;
                } else if (appInfoApi.getType() == ConstUtils.CACHE_TYPE_REDIS_CLUSTER) {
                    instanceNumber = appInfoApi.getHasSlave() == 1 ? appInfoApi.getInstanceNum() * 2 + 1 : appInfoApi.getInstanceNum() + 1;
                }
                if (instanceNumber > remianCpu) {
                    // cpu核数不够
                    logger.error(String.format(ERROR_CPU_ASSIGN, instanceNumber, remianCpu));
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.format(ERROR_CPU_ASSIGN, instanceNumber, remianCpu));
                }
                logger.info("remain cpuNum:{},app instanceNum:{},applyMemTotal:{}M, remian machineMemTotal:{}M", remianCpu, instanceNumber, applyMem, remainMem);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.format(ERROR_EXCEPTION, e.getMessage()));
        }

        // 实例格式检查
        try {
            DataFormatCheckResult dataFormatCheckResult = appDeployCenter.checkAppDeployDetail4Api(appInfoApi, appDeployText, redisVersion);
            if (!dataFormatCheckResult.isSuccess()) {
                //检查异常
                logger.error(String.format(ERROR_REDIS_FORMAT, dataFormatCheckResult.getMessage()));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.format(ERROR_REDIS_FORMAT, dataFormatCheckResult.getMessage()));
            }
        } catch (Exception e) {
            // exception  系统异常 500
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.format(ERROR_EXCEPTION, e.getMessage()));
        }

        /**
         * 6.appDesc 和 appAudit 应用信息保存
         */
        AppAudit appAudit = saveAppInfoAndLog(redisVersion, appUser, appInfoApi);
        if (appAudit == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.format(ERROR_EXCEPTION, ERROR_APP_SAVE_INFO));
        }
        Long appId = appAudit.getAppId();
        Long appAuditId = appAudit.getId();
        if (appId == null || appAuditId == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.format(ERROR_EXCEPTION, ERROR_APP_SAVE_INFO));
        }
        // 获取应用信息
        AppDesc appDesc = appService.getByAppId(appId);

        /**
         * 7.异步提交创建应用任务
         */
        asyncExecuteAppDeployTask(appId, appAuditId, appDeployText, appUser, appInfoApi);

        /**
         * 8.发邮件通知管理员
         */
        if (appDesc != null) {
            appEmailUtil.noticeAppResultByApi(appDesc, appAudit);
        }
        logger.info("api create appdesc cost time :{} ms", (System.currentTimeMillis() - startTime));
        response.put("cost", System.currentTimeMillis() - startTime);
        response.put("status", appDesc.getStatus());
        response.put("appId", appId);
        return ResponseEntity.status(HttpStatus.OK).body(response.toString());
    }

    /**
     * <p>
     * Description: 创建集群异步任务
     * </p>
     *
     * @author chenshi
     * @version 1.0
     * @date 2018/11/14
     */
    public void asyncExecuteAppDeployTask(final Long appId, final Long appAuditId, final String appDeployText, final AppUser appUser, final AppInfoApi appInfoApi) {

        String key = "app-automation-" + appId;
        asyncService.submitFuture(AsyncThreadPoolFactory.APP_POOL, new KeyCallable<Boolean>(key) {
            public Boolean execute() {
                try {
                    long start = System.currentTimeMillis();
                    ResponseEntity<String> result = deployApp(appId, appAuditId, appDeployText, appUser, appInfoApi);
                    logger.info("async execute task :{} , cost time:{} ms", result, System.currentTimeMillis() - start);
                    if (result.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                        // 记录异常原因
                        appAuditDao.updateRefuseReason(appAuditId, result.getBody());
                    }
                    return true;
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return false;
                }
            }
        });
    }

    /**
     * <p>
     * Description: 部署应用
     * </p>
     *
     * @author chenshi
     * @version 1.0
     * @date 2018/11/14
     */
    public ResponseEntity<String> deployApp(Long appId, Long appAuditId, String appDeployText, AppUser appUser, AppInfoApi appInfoApi) {

        /**
         * 1.开始部署redis
         */
        try {
            boolean isSuccess = false;
            if (appAuditId != null && StringUtils.isNotBlank(appDeployText)) {
                String[] appDetails = appDeployText.split("\n");
                // 部署service
                isSuccess = appDeployCenter.allocateResourceApp(appAuditId, Arrays.asList(appDetails), appUser);
            }
            // 部署失败 直接返回
            if (!isSuccess) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.format(ERROR_EXCEPTION, ERROR_REDIS_DEPLOY_INFO));
            }
            logger.info("deploy rediscluster :" + isSuccess);
        } catch (Exception e) {
            // exception  系统异常 500
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.format(ERROR_EXCEPTION, e.getMessage()));
        }

        /**
         * 2.集群完整性验证
         */
        int slotsNum = 0;
        try {
            if (appInfoApi.getType() == ConstUtils.CACHE_TYPE_REDIS_CLUSTER) {
                //等待集群握手,有时api获取会不完整
                Thread.sleep(2000);
                logger.info("wait 2 seocnds for cluster meet success!");

                Map<String, InstanceSlotModel> clusterSlotsMap = redisCenter.getClusterSlotsMap(appId);
                logger.info("==================clusterSlots validate ==================");
                for (Map.Entry<String, InstanceSlotModel> InstanceSlot : clusterSlotsMap.entrySet()) {
                    slotsNum += InstanceSlot.getValue().getSlotList().size();
                }
                logger.info("cluster slots assign num =" + slotsNum);
                if (slotsNum != 16384) {
                    logger.info("==================cluster is not ok ! ==================");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.format(ERROR_EXCEPTION, ERROR_REDISCLUSTER_INTEGRITY_INFO + slotsNum));
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.format(ERROR_EXCEPTION, e.getMessage()));
        }

        /**
         * 3.部署节点个数验证
         */
        AppDesc appDesc = appService.getByAppId(appId);
        try {
            // 部署节点数验证
            List<InstanceInfo> instancelist = instanceDao.getEffectiveInstListByAppId(appId);
            if (CollectionUtils.isEmpty(instancelist)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.format(ERROR_INSTANCE_NUM, appDesc.getTypeDesc(), 0));
            }
            logger.info("appid :{} ,type:{}, redis instanceNum ={}", appId, appDesc.getTypeDesc(), instancelist.size());
            // cluster/standalone/sentinel节点数验证
            if (appInfoApi.getType() == ConstUtils.CACHE_TYPE_REDIS_CLUSTER && instancelist.size() < 3) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.format(ERROR_INSTANCE_NUM, appDesc.getTypeDesc(), instancelist.size()));
            } else if (appInfoApi.getType() == ConstUtils.CACHE_REDIS_SENTINEL && instancelist.size() < 5) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.format(ERROR_INSTANCE_NUM, appDesc.getTypeDesc(), instancelist.size()));
            } else if (appInfoApi.getType() == ConstUtils.CACHE_REDIS_STANDALONE && instancelist.size() != 1) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.format(ERROR_INSTANCE_NUM, appDesc.getTypeDesc(), instancelist.size()));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.format(ERROR_EXCEPTION, e.getMessage()));
        }

        /**
         * 4.设置验证密码
         */
        try {
            if (appId > 0) {
                // 设置密码
                redisDeployCenter.fixPassword(appId, null, null, true);
                // 密码校验逻辑
                boolean checkFlag = redisDeployCenter.checkAuths(appId);
                logger.info("check app passwd:" + checkFlag);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.format(ERROR_EXCEPTION, ERROR_APP_SETPASSWD_INFO));
        }
        return ResponseEntity.status(HttpStatus.OK).body("应用创建成功");
    }

    /**
     * 保存应用和审核信息
     *
     * @param redisVersion 版本信息
     * @param appUser      用户信息
     * @return 审核对象
     */
    public AppAudit saveAppInfoAndLog(RedisVersion redisVersion, AppUser appUser, AppInfoApi appInfoApi) {

        try {
            AppDesc appDesc = new AppDesc();
            appDesc.setName(appInfoApi.getName());
            appDesc.setIntro(appInfoApi.getDesc() == null ? "" : appInfoApi.getDesc());
            appDesc.setClientMachineRoom(appInfoApi.getRoom() == null ? "" : appInfoApi.getRoom());
            appDesc.setType(appInfoApi.getType());
            appDesc.setIsTest(appInfoApi.getIsTest());
            appDesc.setTypeDesc(appDesc.getTypeDesc());
            appDesc.setVersionId(redisVersion.getId());
            appDesc.setUserId(appUser.getId());
            appDesc.setOfficer(String.valueOf(appUser.getId()));
            appDesc.setClientConnAlertValue(2000);
            appDesc.setForecaseQps(1000);
            appDesc.setForecastObjNum(100000);
            appDesc.setMemAlertValue(85);
            appDesc.setVerId(1);//获取版本id
            appDesc.setStatus((short) AppStatusEnum.STATUS_ALLOCATED.getStatus());
            // 设置命中率报警0,默认不监控
            appDesc.setHitPrecentAlertValue(0);
            // 客户端默认关闭监控
            appDesc.setIsAccessMonitor(AppUserAlertEnum.NO.value());
            Timestamp now = new Timestamp(new Date().getTime());
            appDesc.setCreateTime(now);
            appDesc.setPassedTime(now);

            appService.save(appDesc);
            // 保存应用和用户的关系
            appService.saveAppToUser(appDesc.getAppId(), appDesc.getUserId());
            // 更新appKey
            long appId = appDesc.getAppId();
            appService.updateAppKey(appId);

            // 保存应用审批信息
            AppAudit appAudit = new AppAudit();
            appAudit.setAppId(appId);
            appAudit.setUserId(appUser.getId());
            appAudit.setUserName(appUser.getName());
            appAudit.setModifyTime(new Date());
            appAudit.setParam1(String.valueOf(appInfoApi.getMemTotalSize()));
            appAudit.setParam2(appDesc.getTypeDesc());
            appAudit.setInfo("类型:" + appDesc.getTypeDesc() + ";初始申请空间:" + appInfoApi.getMemTotalSize() + "G (<a style='color:red'>API创建</a>)");
            appAudit.setStatus(AppCheckEnum.APP_WATING_CHECK.value());
            appAudit.setType(AppAuditType.APP_AUDIT.getValue());
            appAuditDao.insertAppAudit(appAudit);

            logger.info("appAudit :" + appAudit);
            // 保存申请日志
            AppAuditLog appAuditLog = AppAuditLog.generate(appDesc, appUser, appAudit.getId(),
                    AppAuditLogTypeEnum.APP_DESC_APPLY);
            if (appAuditLog != null) {
                appAuditLogDao.save(appAuditLog);
            }
            return appAudit;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * <p>
     * Description:初始化设置
     * </p>
     *
     * @author chenshi
     * @version 1.0
     * @date 2018/10/31
     */
    public void setDefaultConfig(AppInfoApi appInfoApi) {
        //1.实例数量
        if (appInfoApi.getType() == ConstUtils.CACHE_TYPE_REDIS_CLUSTER) {
            int memTotalSize = appInfoApi.getMemTotalSize();
            int maxInstanceMem = MAX_INSTANCE_MEM;//2G
            int machineNum = appInfoApi.getIplist().size();
            int instanceNum = 3;
            // 1.正式应用 /测试计算实例数量
            if (machineNum < 3) {
                logger.info(" machine is not enough !");
            }
            if (maxInstanceMem * 3 <= memTotalSize) {
                instanceNum = memTotalSize / maxInstanceMem;  //取整
            }
            if (maxInstanceMem * 3 > memTotalSize) {
                instanceNum = machineNum >= 3 ? machineNum : 3;    //取默认值
            }
            Double instanceMem = Double.parseDouble(memTotalSize + "") / instanceNum * 1024;
            logger.info("集群申请内存量:{}G,机器数:{},分配实例数:{},实例内存:{}", memTotalSize, machineNum, instanceNum, instanceMem.intValue());
            appInfoApi.setInstanceNum(instanceNum);
        } else if (appInfoApi.getType() == ConstUtils.CACHE_REDIS_SENTINEL) {
            appInfoApi.setInstanceNum(2);
        } else if (appInfoApi.getType() == ConstUtils.CACHE_REDIS_STANDALONE) {
            appInfoApi.setInstanceNum(1);
        }
        // 2.默认值 sentinel 有slave | 设置sentinel数量
        if (appInfoApi.getType() == ConstUtils.CACHE_REDIS_SENTINEL) {
            appInfoApi.setHasSlave(1);//sentinel节点需要设置主从
            if (appInfoApi.getSentinelNum() < 3 || appInfoApi.getSentinelNum() % 2 == 0) {
                appInfoApi.setSentinelNum(3);//设置默认sentinel节点数量
            }
        }
        if (appInfoApi.getType() == ConstUtils.CACHE_REDIS_STANDALONE) {
            appInfoApi.setHasSlave(0);//standalone节点不设置从节点
        }
        // 3.版本默认值 redis-3.0.7
        if (StringUtil.isBlank(appInfoApi.getRedisVersion())) {
            appInfoApi.setRedisVersion("redis-3.0.7");
        }
    }

    /**
     * <p>
     * Description:自动获取sentinel节点
     * </p>
     *
     * @author chenshi
     * @version 1.0
     * @date 2018/11/1
     */
    public String autoSelectSentinel(AppInfoApi appInfoApi) {

        String sentinelMachines = "";
        StringBuilder sentinelMachinesBuilder = new StringBuilder();
        try {
            //1. 默认sentinel节点数
            int sentinelNum = appInfoApi.getSentinelNum();
            List<MachineMemStatInfo> machineMemStatInfoList = machineCenter.getAllValidMachineMem(new ArrayList<String>(), null, 3);
            List<MachineMemStatInfo> machineCandi = new ArrayList<MachineMemStatInfo>();
            for (MachineMemStatInfo memStatInfo : machineMemStatInfoList) {
                memStatInfo.setInstanceNum(instanceDao.getInstListByIp(memStatInfo.getIp()).size());
                appService.getMachineCandiList(memStatInfo, 0d, 1, 0, machineCandi);
            }
            // 2.优先挑选空闲机器，否则走随机挑选
            if (machineCandi.size() < sentinelNum) {
                logger.warn("machineCandi size = {},default random sentinel ip!", machineCandi.size());
                machineCandi = machineMemStatInfoList;
            }
            if (machineCandi.size() < 3) {
                logger.warn("machineCandi size = {} is not enough!", machineCandi.size());
                return null;
            }
            // 3.按机房分类 (跨机房部署)
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
            // 4.for select
            while (sentinelMachineSet.size() < sentinelNum && sentinelMachineMap.size() >= 3) {
                for (Map.Entry<String, List<MachineMemStatInfo>> entry : sentinelMachineMap.entrySet()) {
                    getResMachines(entry.getValue(), 1, sentinelMachineSet);
                    if (sentinelMachineSet.size() == sentinelNum) {
                        break;
                    }
                }
            }
            logger.info("sentinelMachineSet:{}", sentinelMachineSet);
            if (sentinelMachineSet.size() >= 3) {
                for (MachineMemStatInfo sentinelMachine : sentinelMachineSet) {
                    //sentinelMachines += sentinelMachine.getIp() + ",";
                    sentinelMachinesBuilder.append(sentinelMachine.getIp()).append(",");
                }
                sentinelMachines = sentinelMachinesBuilder.toString();
                sentinelMachines = sentinelMachines.substring(0, sentinelMachines.lastIndexOf(","));
                logger.info("sentinelMachines:{}", sentinelMachines);
                return sentinelMachines;
            }
            return null;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
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

}
