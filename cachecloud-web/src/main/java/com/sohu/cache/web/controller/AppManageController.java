package com.sohu.cache.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.sohu.cache.constant.*;
import com.sohu.cache.dao.InstanceReshardProcessDao;
import com.sohu.cache.entity.*;
import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.redis.RedisDeployCenter;
import com.sohu.cache.redis.util.AuthUtil;
import com.sohu.cache.stats.app.AppDailyDataCenter;
import com.sohu.cache.stats.app.AppDeployCenter;
import com.sohu.cache.task.TaskService;
import com.sohu.cache.task.constant.ResourceEnum;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.TypeUtil;
import com.sohu.cache.web.enums.DeployInfoEnum;
import com.sohu.cache.web.enums.NodeEnum;
import com.sohu.cache.web.enums.RedisOperateEnum;
import com.sohu.cache.web.enums.SuccessEnum;
import com.sohu.cache.web.service.AppScrollRestartService;
import com.sohu.cache.web.service.AppService;
import com.sohu.cache.web.util.AppEmailUtil;
import com.sohu.cache.web.util.DateUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 应用后台管理
 *
 * @author leifu
 * @Time 2014年7月3日
 */
@Controller
@RequestMapping("manage/app")
public class AppManageController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(AppManageController.class);

    @Resource(name = "machineCenter")
    private MachineCenter machineCenter;

    @Resource(name = "appEmailUtil")
    private AppEmailUtil appEmailUtil;

    @Resource(name = "appDeployCenter")
    private AppDeployCenter appDeployCenter;

    @Resource(name = "redisCenter")
    private RedisCenter redisCenter;

    @Resource(name = "redisDeployCenter")
    private RedisDeployCenter redisDeployCenter;

    @Resource(name = "appDailyDataCenter")
    private AppDailyDataCenter appDailyDataCenter;

    @Resource(name = "instanceReshardProcessDao")
    private InstanceReshardProcessDao instanceReshardProcessDao;

    @Resource(name = "appService")
    private AppService appService;

    @Autowired
    private AppScrollRestartService appScrollRestartService;

    @Resource
    private TaskService taskService;

    @RequestMapping("/appDaily")
    public ModelAndView appDaily(HttpServletRequest request, HttpServletResponse response, Model model) throws ParseException {
        AppUser userInfo = getUserInfo(request);
        logger.warn("user {} want to send appdaily", userInfo.getName());
        if (ConstUtils.SUPER_MANAGER.contains(userInfo.getName())) {
            Date startDate;
            Date endDate;
            String startDateParam = request.getParameter("startDate");
            String endDateParam = request.getParameter("endDate");
            if (StringUtils.isBlank(startDateParam) || StringUtils.isBlank(endDateParam)) {
                endDate = new Date();
                startDate = DateUtils.addDays(endDate, -1);
            } else {
                startDate = DateUtil.parseYYYY_MM_dd(startDateParam);
                endDate = DateUtil.parseYYYY_MM_dd(endDateParam);
            }
            long appId = NumberUtils.toLong(request.getParameter("appId"));
            if (appId > 0) {
                appDailyDataCenter.sendAppDailyEmail(appId, startDate, endDate);
            } else {
                appDailyDataCenter.sendAppDailyEmail();
            }
            model.addAttribute("msg", "success!");
        } else {
            model.addAttribute("msg", "no power!");
        }
        return new ModelAndView("");
    }

    /**
     * 审核列表
     *
     * @param status 审核状态
     * @param type   申请类型
     */
    @RequestMapping(value = "/auditList")
    public ModelAndView doAppAuditList(HttpServletRequest request, HttpServletResponse response, Model model,
                                       Integer status, Integer type, Long auditId, Long operateId, Long userId,
                                       String startDate, String endDate, Long adminId) {
        AppAuditType[] appAuditTypes = AppAuditType.values();
        model.addAttribute("appAuditTypeMap", Arrays.stream(appAuditTypes).collect(Collectors.toMap(AppAuditType::getValue, Function.identity())));
        List<AppUser> userList = userService.getAllUser();
        model.addAttribute("userMap", userList.stream().collect(Collectors.toMap(AppUser::getId, Function.identity())));
        //获取审核列表
        List<AppAudit> list = appService.getAppAudits(status, type, auditId, userId, operateId);

        //任务汇总
        AppUser currentUser = getUserInfo(request);
        Date startTime = null;
        Date endTime = null;
        if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
            startTime = DateUtil.getDateByFormat(startDate, "yyyy-MM-dd");
            endTime = DateUtil.getDateByFormat(endDate, "yyyy-MM-dd");
        }
        Map<String, Object> statusStatisMap = appService.getStatisticGroupByStatus(null, adminId, startTime, endTime);
        Map<String, Object> typeStatisMap = appService.getStatisticGroupByType(null, adminId, startTime, endTime);

        model.addAttribute("statusStatisMap", statusStatisMap);
        model.addAttribute("typeStatisMap", typeStatisMap);
        model.addAttribute("list", list);

        model.addAttribute("userId", userId);
        model.addAttribute("operateId", operateId);
        model.addAttribute("status", status);
        model.addAttribute("type", type);
        model.addAttribute("auditId", auditId);
        model.addAttribute("checkActive", SuccessEnum.SUCCESS.value());
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("adminId", adminId);

        return new ModelAndView("manage/appAudit/list");
    }


    @RequestMapping(value = "/audit/update")
    public ModelAndView updateAppAudit(HttpServletRequest request, HttpServletResponse response, Model model, Long auditId, int status, int type) {
        AppUser appUser = getUserInfo(request);


        return new ModelAndView("manage/appAudit/list");
    }

    /**
     * 处理应用配置修改
     *
     * @param appAuditId 审批id
     */
    @RequestMapping(value = "/initAppConfigChange")
    public ModelAndView doInitAppConfigChange(HttpServletRequest request,
                                              HttpServletResponse response, Model model, Long appAuditId) {
        // 申请原因
        AppAudit appAudit = appService.getAppAuditById(appAuditId);
        model.addAttribute("appAudit", appAudit);

        // 用第一个参数存实例id
        Long instanceId = NumberUtils.toLong(appAudit.getParam1());
        Map<String, String> redisConfigList = redisCenter.getRedisConfigList(instanceId.intValue());
        model.addAttribute("redisConfigList", redisConfigList);
        model.addAttribute("instanceId", instanceId);

        // 实例列表
        List<InstanceInfo> instanceList = appService.getAppInstanceInfo(appAudit.getAppId());
        model.addAttribute("instanceList", instanceList);
        model.addAttribute("appId", appAudit.getAppId());
        model.addAttribute("appAuditId", appAuditId);

        // 修改配置的键值对
        model.addAttribute("appConfigKey", appAudit.getParam2());
        model.addAttribute("appConfigValue", appAudit.getParam3());

        return new ModelAndView("manage/appAudit/initAppConfigChange");
    }

    /**
     * 添加应用配置修改
     *
     * @param appId          应用id
     * @param appConfigKey   配置项
     * @param appConfigValue 配置值
     * @param appAuditId     审批id
     */
    @RequestMapping(value = "/addAppConfigChange")
    public ModelAndView doAddAppConfigChange(HttpServletRequest request,
                                             HttpServletResponse response, Model model, Long appId,
                                             String appConfigKey, String appConfigValue, Long appAuditId) {
        AppUser appUser = getUserInfo(request);
        logger.warn("user {} change appConfig:appId={};key={};value={},appAuditId:{}", appUser.getName(), appId, appConfigKey, appConfigValue, appAuditId);
        boolean isModify = false;
        if (appId != null && StringUtils.isNotBlank(appConfigKey)) {
            try {
                if(appAuditId != null ){
                    appAuditDao.updateAppAuditOperateUser(appAuditId, appUser.getId());
                }
                isModify = appDeployCenter.modifyAppConfig(appId, appAuditId, appConfigKey, appConfigValue);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        logger.warn("user {} change appConfig:appId={};key={};value={},appAuditId:{},result is:{}", appUser.getName(), appId, appConfigKey, appConfigValue, appAuditId, isModify);
        return new ModelAndView("redirect:/manage/app/auditList");
    }

    /**
     * 初始化水平扩容申请
     */
    @RequestMapping(value = "/initHorizontalScaleApply")
    public ModelAndView doInitHorizontalScaleApply(HttpServletRequest request, HttpServletResponse response, Model model, Long appAuditId) {
        appAuditDao.updateAppAuditOperateUser(appAuditId, getUserInfo(request).getId());
        AppAudit appAudit = appService.getAppAuditById(appAuditId);
        model.addAttribute("appAudit", appAudit);
        model.addAttribute("appId", appAudit.getAppId());
        return new ModelAndView("manage/appAudit/initHorizontalScaleApply");
    }


    /**
     * 添加水平扩容节点
     *
     * @return
     */
    @RequestMapping(value = "/addHorizontalNodes")
    public ModelAndView doAddHorizontalNodes(HttpServletRequest request,
                                             HttpServletResponse response, Model model, String masterSizeSlave,
                                             Long appAuditId) {
        AppUser appUser = getUserInfo(request);
        logger.warn("user {} addHorizontalNodes:{}", appUser.getName(), masterSizeSlave);
        boolean isAdd = false;
        AppAudit appAudit = appService.getAppAuditById(appAuditId);

        String[] nodes = masterSizeSlave.split(ConstUtils.NEXT_LINE);
        for (String node : nodes) {
            if (!StringUtils.isEmpty(node.trim())) {
                // 解析配置
                String[] configArr = node.trim().split(ConstUtils.COLON);
                String masterHost = configArr[0];
                String memSize = configArr[1];
                int memSizeInt = NumberUtils.toInt(memSize);
                String slaveHost = null;
                if (configArr.length >= 3) {
                    slaveHost = configArr[2];
                }
                try {
                    isAdd = appDeployCenter.addHorizontalNodes(appAudit.getAppId(), masterHost, slaveHost, memSizeInt);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        logger.warn("addAppClusterSharding:{}, result is {}", masterSizeSlave, isAdd);
        model.addAttribute("status", isAdd ? 1 : 0);
        return new ModelAndView("");
    }

    /**
     * 检测水平扩容节点
     *
     * @param masterSizeSlave
     * @param appAuditId
     * @return
     */
    @RequestMapping(value = "/checkHorizontalNodes")
    public ModelAndView doCheckHorizontalNodes(HttpServletRequest request,
                                               HttpServletResponse response, Model model, String masterSizeSlave,
                                               Long appAuditId) {
        DataFormatCheckResult dataFormatCheckResult = null;
        try {
            dataFormatCheckResult = appDeployCenter.checkHorizontalNodes(appAuditId, masterSizeSlave);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            dataFormatCheckResult = DataFormatCheckResult.fail(ErrorMessageEnum.INNER_ERROR_MSG.getMessage());
        }
        model.addAttribute("status", dataFormatCheckResult.getStatus());
        model.addAttribute("message", dataFormatCheckResult.getMessage());
        return new ModelAndView("");
    }

    /**
     * 水平扩容初始化
     *
     * @param appAuditId
     */
    @RequestMapping(value = "/handleHorizontalScale")
    public ModelAndView doHandleHorizontalScale(HttpServletRequest request,
                                                HttpServletResponse response, Model model, Long appAuditId) {
        // 1. 审批
        AppAudit appAudit = appService.getAppAuditById(appAuditId);
        model.addAttribute("appAudit", appAudit);
        model.addAttribute("appId", appAudit.getAppId());

        // 2. 进度
        List<InstanceReshardProcess> instanceReshardProcessList = instanceReshardProcessDao.getByAuditId(appAudit.getId());
        model.addAttribute("instanceReshardProcessList", instanceReshardProcessList);

        // 3. 实例列表和统计
        fillAppInstanceStats(appAudit.getAppId(), model);
        // 4. 实例所在机器信息
        fillAppMachineStat(appAudit.getAppId(), model);

        return new ModelAndView("manage/appAudit/handleHorizontalScale");
    }

    /**
     * 显示reshard进度
     */
    @RequestMapping(value = "/showReshardProcess")
    public ModelAndView doShowReshardProcess(HttpServletRequest request, HttpServletResponse response, Model model) {
        long auditId = NumberUtils.toLong(request.getParameter("auditId"));
        List<InstanceReshardProcess> instanceReshardProcessList = instanceReshardProcessDao.getByAuditId(auditId);
        write(response, JSONObject.toJSONString(instanceReshardProcessList));
        return null;
    }

    /**
     * 水平扩容配置检查
     *
     * @param sourceId   源实例ID
     * @param targetId   目标实例ID
     * @param startSlot  开始slot
     * @param endSlot    结束slot
     * @param appId      应用id
     * @param appAuditId 审批id
     * @return
     */
    @RequestMapping(value = "/checkHorizontalScale")
    public ModelAndView doCheckHorizontalScale(HttpServletRequest request, HttpServletResponse response, Model model,
                                               long sourceId, long targetId, int startSlot, int endSlot, long appId, long appAuditId, int migrateType) {
        HorizontalResult horizontalResult = appDeployCenter.checkHorizontal(appId, appAuditId, sourceId, targetId,
                startSlot, endSlot, migrateType);
        model.addAttribute("status", horizontalResult.getStatus());
        model.addAttribute("message", horizontalResult.getMessage());
        return new ModelAndView("");
    }

    /**
     * 开始水平扩容
     *
     * @param sourceId   源实例ID
     * @param targetId   目标实例ID
     * @param startSlot  开始slot
     * @param endSlot    结束slot
     * @param appId      应用id
     * @param appAuditId 审批id
     * @return
     */
    @RequestMapping(value = "/startHorizontalScale")
    public ModelAndView doStartHorizontalScale(HttpServletRequest request, HttpServletResponse response, Model model,
                                               long sourceId, long targetId, int startSlot, int endSlot, long appId, long appAuditId, int migrateType) {
        AppUser appUser = getUserInfo(request);
        logger.warn("user {} horizontalScaleApply appId {} appAuditId {} sourceId {} targetId {} startSlot {} endSlot {}",
                appUser.getName(), appId, appAuditId, sourceId, targetId, startSlot, endSlot);
        HorizontalResult horizontalResult = appDeployCenter.startHorizontal(appId, appAuditId, sourceId, targetId,
                startSlot, endSlot, migrateType);
        model.addAttribute("status", horizontalResult.getStatus());
        model.addAttribute("message", horizontalResult.getMessage());
        return new ModelAndView("");
    }

    /**
     * 重试水平扩容
     *
     * @param instanceReshardProcessId
     * @return
     */
    @RequestMapping(value = "/retryHorizontalScale")
    public ModelAndView retryHorizontalScale(HttpServletRequest request, HttpServletResponse response, Model model, int instanceReshardProcessId) {
        AppUser appUser = getUserInfo(request);
        logger.warn("user {} retryHorizontalScale id {}", appUser.getName(), instanceReshardProcessId);
        HorizontalResult horizontalResult = appDeployCenter.retryHorizontal(instanceReshardProcessId);
        model.addAttribute("status", horizontalResult.getStatus());
        model.addAttribute("message", horizontalResult.getMessage());
        return new ModelAndView("");
    }

    /**
     * 处理应用扩容
     *
     * @param appAuditId 审批id
     */
    @RequestMapping(value = "/initAppScaleApply")
    public ModelAndView doInitAppScaleApply(HttpServletRequest request, HttpServletResponse response, Model model, Long appAuditId) {
        // 申请原因
        AppAudit appAudit = appService.getAppAuditById(appAuditId);
        model.addAttribute("appAudit", appAudit);

        // 实例列表和统计
        fillAppInstanceStats(appAudit.getAppId(), model);
        // 实例所在机器信息
        fillAppMachineStat(appAudit.getAppId(), model);

        long appId = appAudit.getAppId();
        AppDesc appDesc = appService.getByAppId(appId);
        model.addAttribute("appAuditId", appAuditId);
        model.addAttribute("appId", appAudit.getAppId());
        model.addAttribute("appDesc", appDesc);

        return new ModelAndView("manage/appAudit/initAppScaleApply");
    }

    /**
     * 添加扩容配置
     *
     * @param appScaleText 扩容配置
     * @param appAuditId   审批id
     */
    @RequestMapping(value = "/addAppScaleApply")
    public ModelAndView doAddAppScaleApply(HttpServletRequest request,
                                           HttpServletResponse response, Model model, String appScaleText,
                                           Long appAuditId, Long appId) {
        AppUser appUser = getUserInfo(request);
        logger.error("user {} appScaleApplay : appScaleText={},appAuditId:{}", appUser.getName(), appScaleText, appAuditId);
        boolean isSuccess = false;
        int mem = NumberUtils.toInt(appScaleText, 0);
        AppDesc appDesc = appService.getByAppId(appId);
        if (appAuditId != null && StringUtils.isNotBlank(appScaleText) && appDesc != null) {
            try {
                isSuccess = appDeployCenter.verticalExpansion(appId, appAuditId, appUser.getId(), mem);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            logger.error("appScaleApplay error param: appScaleText={},appAuditId:{},appId:{}", appScaleText, appAuditId, appId);
        }
        logger.error("user {} appScaleApplay: appScaleText={},appAuditId:{}, result is {}", appUser.getName(), appScaleText, appAuditId, isSuccess);
        return new ModelAndView("redirect:/manage/app/auditList");
    }

    /**
     * 初始化部署应用
     *
     * @param appAuditId 审批id
     * @return
     */
    @RequestMapping(value = "/initAppDeploy")
    public ModelAndView doInitAppDeploy(HttpServletRequest request, HttpServletResponse response, Model model, Long appAuditId) {
        long appId;
        AppDesc appDesc;
        if (appAuditId == null) {
            appId = NumberUtils.toLong(request.getParameter("appId"));
            appDesc = appService.getByAppId(appId);
        } else {
            // 申请原因
            AppAudit appAudit = appService.getAppAuditById(appAuditId);
            appId = appAudit.getAppId();
            model.addAttribute("appAudit", appAudit);
            appDesc = appService.getByAppId(appId);
        }

        // 获取所有Redis版本
        List<SystemResource> allRedisVersion = resourceService.getResourceList(ResourceEnum.REDIS.getValue());
        // 机器列表
        List<MachineStats> machineList = machineCenter.getMachineStats(null, null, null, null, null, null, null);
        // 获取机器信息
        Map<String, Integer> machineInstanceCountMap = machineCenter.getMachineInstanceCountMap();

        if (appDesc.getVersionId() > 0) {
            model.addAttribute("version", resourceService.getResourceById(appDesc.getVersionId()));
        }
        List<MachineRoom> roomList = machineCenter.getEffectiveRoom();
        model.addAttribute("roomList", roomList);

        model.addAttribute("machineList", machineList);
        model.addAttribute("machineInstanceCountMap", machineInstanceCountMap);
        model.addAttribute("appAuditId", appAuditId);
        model.addAttribute("appId", appId);
        model.addAttribute("md5password", AuthUtil.getAppIdMD5(String.valueOf(appId)));
        model.addAttribute("appDesc", appService.getByAppId(appId));
        model.addAttribute("versionList", allRedisVersion);
        model.addAttribute("importId", request.getParameter("importId"));

        return new ModelAndView("manage/appAudit/deploy/initAppDeploy");
    }

    @RequestMapping(value = "/generateDeployInfo", method = {RequestMethod.POST, RequestMethod.GET})
    public ModelAndView generateDeployInfo(HttpServletResponse response,
                                           int type,
                                           int hasSalve,
                                           int maxMemory,
                                           int redisNum,
                                           int sentinelNum,
                                           int pikaNum,
                                           int twemproxyNum,
                                           String appDeployInfo,
                                           String redisMachines,
                                           String sentinelMachines,
                                           String twemproxyMachines,
                                           String pikaMachines) {
        // 1.根据应用类型获取部署拓扑信息
        logger.info("type:{} ,hasSalve:{} ,maxMemory:{}MB ", type, hasSalve, maxMemory);
        logger.info("redisMachines:{} ,num:{} ", redisMachines, redisNum);
        logger.info("sentinelMachines:{} ,num:{} ", sentinelMachines, sentinelNum);
        logger.info("twemproxyMachines:{} ,num:{} ", twemproxyMachines, twemproxyNum);
        logger.info("pikaMachines:{} ,num:{} ", pikaMachines, pikaNum);

        // 2.根据应用类型获取部署拓扑信息
        List<DeployInfo> deployInfoList = new ArrayList<DeployInfo>();

        Map<String, DeployInfoStat> machineDeployStatMap = new HashMap<>();
        Map<String, Integer> machineInstanceCountMap = new HashMap<>();
        List<MachineMemStatInfo> machineMemStatInfo = new ArrayList<>();
        Set<String> ipList = new HashSet<String>();
        String deployStatus = DeployInfoEnum.SUCCESS.getValue();
        try {
            List<String> redisMachinelist = Arrays.asList(redisMachines.split(";"));
            List<String> sentinelMachinelist = Arrays.asList(sentinelMachines.split(";"));
            List<String> twemproxyMachinelist = Arrays.asList(twemproxyMachines.split(";"));
            List<String> pikaMachinelist = Arrays.asList(pikaMachines.split(";"));

            switch (type) {
                case 2: // redis cluster
                    appService.generateInstanceInfo(redisMachinelist, NodeEnum.REDIS_NODE.getValue(), type, redisMachinelist.size() * redisNum, maxMemory, hasSalve, deployInfoList);
                    ipList.addAll(redisMachinelist);
                    break;
                case 5: // sentinel + redis
                    appService.generateInstanceInfo(redisMachinelist, NodeEnum.REDIS_NODE.getValue(), type, 1, maxMemory, hasSalve, deployInfoList);
                    appService.generateProxyinfo(sentinelMachinelist, NodeEnum.SENTINEL_NODE.getValue(), type, sentinelMachinelist.size() * sentinelNum, deployInfoList);
                    ipList.addAll(redisMachinelist);
                    ipList.addAll(sentinelMachinelist);
                    break;
                case 6:// standalone + redis
                    deployInfoList.add(new DeployInfo(type, redisMachinelist.get(0), maxMemory));
                    ipList.addAll(redisMachinelist);
                    break;
                case 7: //twemproxy + redis
                    appService.generateInstanceInfo(redisMachinelist, NodeEnum.REDIS_NODE.getValue(), type, redisMachinelist.size() * redisNum, maxMemory, hasSalve, deployInfoList);
                    appService.generateProxyinfo(sentinelMachinelist, NodeEnum.SENTINEL_NODE.getValue(), type, sentinelMachinelist.size() * sentinelNum, deployInfoList);
                    appService.generateProxyinfo(twemproxyMachinelist, NodeEnum.TWEMPROXY_NODE.getValue(), type, twemproxyMachinelist.size() * twemproxyNum, deployInfoList);
                    ipList.addAll(redisMachinelist);
                    ipList.addAll(sentinelMachinelist);
                    ipList.addAll(twemproxyMachinelist);
                    break;
                case 8: //sentinel + pika
                    appService.generateInstanceInfo(pikaMachinelist, NodeEnum.PIKA_NODE.getValue(), type, 1, maxMemory, hasSalve, deployInfoList);
                    appService.generateProxyinfo(sentinelMachinelist, NodeEnum.SENTINEL_NODE.getValue(), type, sentinelMachinelist.size() * sentinelNum, deployInfoList);
                    ipList.addAll(pikaMachinelist);
                    ipList.addAll(sentinelMachinelist);
                    break;
                case 9: //twemproxy + pika
                    appService.generateInstanceInfo(pikaMachinelist, NodeEnum.PIKA_NODE.getValue(), type, pikaMachinelist.size() * pikaNum, maxMemory, hasSalve, deployInfoList);
                    appService.generateProxyinfo(sentinelMachinelist, NodeEnum.SENTINEL_NODE.getValue(), type, sentinelMachinelist.size() * sentinelNum, deployInfoList);
                    appService.generateProxyinfo(twemproxyMachinelist, NodeEnum.TWEMPROXY_NODE.getValue(), type, twemproxyMachinelist.size() * twemproxyNum, deployInfoList);
                    ipList.addAll(pikaMachinelist);
                    ipList.addAll(sentinelMachinelist);
                    ipList.addAll(twemproxyMachinelist);
                    break;
                default:
                    break;
            }
            if (CollectionUtils.isEmpty(deployInfoList)) {
                deployStatus = DeployInfoEnum.EMPTY.getValue();
            }

            // 收集机器资源信息
            if (!CollectionUtils.isEmpty(ipList)) {
                machineDeployStatMap = appService.getMachineDeployStat(ipList, deployInfoList);
                machineInstanceCountMap = machineCenter.getMachineInstanceCountMap();
                machineMemStatInfo = machineCenter.getValidMachineMemByIpList(new ArrayList(ipList));
            }
        } catch (Exception e) {
            deployStatus = DeployInfoEnum.EXCEPTION.getValue();
            logger.error(e.getMessage(), e);
        }

        // 3. response
        JSONObject json = new JSONObject();
        json.put("result", deployStatus);
        json.put("deployInfoList", deployInfoList);
        json.put("resMachines", machineMemStatInfo);
        json.put("machineDeployStatMap", machineDeployStatMap);
        json.put("machineInstanceCountMap", machineInstanceCountMap);
        sendMessage(response, json.toString());
        return null;
    }

    /**
     * 应用部署task
     *
     * @param request
     * @param appAuditId
     * @param type
     * @param maxMemory
     * @param redisNum
     * @param sentinelNum
     * @param pikaNum
     * @param twemproxyNum
     * @param redisMachines
     * @param sentinelMachines
     * @param twemproxyMachines
     * @param pikaMachines
     * @return
     */
    @RequestMapping(value = "/addAppDeployTask", method = {RequestMethod.POST})
    public ModelAndView doAddAppDeployTask(HttpServletRequest request,
                                           HttpServletResponse response,
                                           Long appAuditId,
                                           int isSetPasswd,
                                           int versionId,
                                           int importantLevel,
                                           long appid,
                                           int type,
                                           int maxMemory,
                                           int redisNum,
                                           int sentinelNum,
                                           int pikaNum,
                                           int twemproxyNum,
                                           @RequestParam(required = false)  String appDeployInfo,
                                           String redisMachines,
                                           String sentinelMachines,
                                           String twemproxyMachines,
                                           String pikaMachines,
                                           String customPassword
                                        ) {
        JSONObject json = new JSONObject();
        long taskid = -1;//任务流跳转
        AppUser appUser = getUserInfo(request);
        logger.warn("user {} appid:{} ,appAuditId:{}, importantLevel:{} ,isSetPasswd:{},versionId:{},customPassword:{}", appUser.getName(), appid, appAuditId, importantLevel, isSetPasswd, versionId, customPassword);
        logger.info("type:{} ,maxMemory:{} MB ", type, maxMemory);
        logger.info("appDeployInfo :{} ", appDeployInfo);
        logger.info("redisMachines:{} ,num:{} ", redisMachines, redisNum);
        logger.info("sentinelMachines:{} ,num:{} ", sentinelMachines, sentinelNum);
        logger.info("twemproxyMachines:{} ,num:{} ", twemproxyMachines, twemproxyNum);
        logger.info("pikaMachines:{} ,num:{} ", pikaMachines, pikaNum);
        try {
            List<String> appDeployInfolist = new ArrayList<>();
            if(StringUtils.isNotEmpty(appDeployInfo)){
                appDeployInfolist = Arrays.asList(appDeployInfo.split("\n"));
            }
            List<String> redisMachinelist = Arrays.asList(redisMachines.split(";"));
            List<String> sentinelMachinelist = Arrays.asList(sentinelMachines.split(";"));
            List<String> twemproxyMachinelist = Arrays.asList(twemproxyMachines.split(";"));
            List<String> pikaMachinelist = Arrays.asList(pikaMachines.split(";"));

            // 1.保存应用信息
            AppDesc appDesc = appService.getByAppId(appid);
            if (appDesc != null) {
                appDesc.setImportantLevel(importantLevel);
                appDesc.setVersionId(versionId);
                appDesc.setType(type);
                if(StringUtils.isNotBlank(customPassword)){
                    appDesc.setCustomPassword(customPassword);
                }
                appService.updateWithCustomPwd(appDesc);
            } else {
                json.put("status", "fail");
                json.put("message", "部署失败:获取应用信息为空,请检查服务日志!");
                sendMessage(response, json.toString());
                return null;
            }
            // 2.获取Redis版本信息
            SystemResource redisResource = resourceService.getResourceById(versionId);

            if (redisResource == null) {
                json.put("status", "fail");
                json.put("message", "部署失败:redis版本不存在,请检查服务日志!");
                sendMessage(response, json.toString());
                return null;
            }
            if (appAuditId != null) {
                appAuditDao.updateAppAuditOperateUser(appAuditId, appUser.getId());
            } else {
                appAuditId = -1l;
            }

            //2.根据应用类型获取部署拓扑信息
            switch (type) {
                case 2: //  部署task :redis cluster
                    taskid = taskService.addRedisClusterAppTask(appid, appAuditId, maxMemory, appDeployInfolist, redisMachinelist, redisNum, redisResource.getName(), -1);
                    break;
                case 5: //  部署task :sentinel + redis
                    taskid = taskService.addRedisSentinelAppTask(appid, appAuditId, maxMemory, redisMachinelist, sentinelMachinelist, redisNum, sentinelNum, redisResource.getName(), -1);
                    break;
                case 6://   部署task :standalone
                    taskid = taskService.addRedisStandaloneAppTask(appid, appAuditId, maxMemory, redisMachinelist, 1, redisResource.getName(), -1);
                    break;
                case 7: //  部署task :twemproxy + redis
                    taskid = taskService.addTwemproxyAppTask(appid, appAuditId, maxMemory, redisMachinelist, sentinelMachinelist,
                            twemproxyMachinelist, redisNum, sentinelNum, twemproxyNum, false, redisResource.getName(), -1);
                    break;
                case 8: //  部署task :sentinel + pika
                    taskid = taskService.addPikaSentinelAppTask(appid, appAuditId, maxMemory, pikaMachinelist, sentinelMachinelist,
                            pikaNum, sentinelNum, -1);
                    break;
                case 9: //  部署task :twemproxy + pika
                    taskid = taskService.addTwemproxyPikaTask(appid, appAuditId, maxMemory, pikaMachinelist, sentinelMachinelist, twemproxyMachinelist,
                            pikaNum, sentinelNum, twemproxyNum, false, -1);
                    break;
                default:
                    break;
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            json.put("status", "fail");
            json.put("message", "部署失败:系统存在异常,请检查服务日志!");
            sendMessage(response, json.toString());
            return null;
        }
        json.put("status", "success");
        json.put("taskid", taskid); // 用于跳转到任务流页面
        json.put("message", "应用任务流开始构建，即将跳转任务流部署页面!");
        sendMessage(response, json.toString());
        return null;
    }

    /**
     * @Description: 生成部署信息
     * @Author: caoru
     * @CreateDate: 2018/9/25 20:41
     */

    @RequestMapping(value = "/getDeployInfo", method = {RequestMethod.POST, RequestMethod.GET})
    public ModelAndView getDeployInfo(HttpServletResponse response, Model model,
                                      Integer type,
                                      Integer isSalve,
                                      String room,
                                      Double size,
                                      Integer machineNum,
                                      Integer instanceNum,
                                      Integer useType,
                                      String machines,
                                      String excludeMachines,
                                      String sentinelMachines) {

        try {
            JSONObject json = new JSONObject();
            String result;
            //参数校验是否为空
            if (type == null || isSalve == null || room == null || size == null || machineNum == null || instanceNum == null || useType == null) {
                logger.info("参数为空");
                result = "param is error or null";
                json.put("result", result);
                sendMessage(response, json.toString());
                return new ModelAndView("");
            }
            size *= 1024;
            if (TypeUtil.isRedisSentinel(type)) {
                if (sentinelMachines.isEmpty()) {
                    logger.info("sentinel类型，请指定sentinel机器");
                    result = "sentinel类型，请指定sentinel机器";
                    json.put("result", result);
                    sendMessage(response, json.toString());
                    return new ModelAndView("");
                } else {
                    StringBuilder resultBuilder = new StringBuilder();
                    List<MachineMemStatInfo> sentinelMachineList = machineCenter.getValidMachineMemByIpList(Arrays.asList(sentinelMachines.split(",")));
                    for (MachineMemStatInfo sentinelMachine : sentinelMachineList) {
                        if (sentinelMachine.getCpu() - sentinelMachine.getInstanceNum() < 1) {
                            //result += sentinelMachine.getIp() + ",";
                            resultBuilder.append(sentinelMachine.getIp()).append(",");
                        }
                    }
                    result = resultBuilder.toString();
                    if (result != null && !result.isEmpty()) {
                        result = "sentinel机器：" + result;
                        result += "cpu核数不足";
                        json.put("result", result);
                        sendMessage(response, json.toString());
                        return new ModelAndView("");
                    }
                }
            }
            //存储部署信息
            List<DeployInfo> deployInfoList = new ArrayList<DeployInfo>();
            //存储满足条件的机器ip
            List<MachineMemStatInfo> resMachines = new ArrayList<MachineMemStatInfo>();

            result = appService.generateDeployInfo(type, isSalve, room, size, machineNum, instanceNum, useType, machines, excludeMachines, sentinelMachines, deployInfoList, resMachines);
            if (!DeployInfoEnum.SUCCESS.getValue().equals(result)) {
                logger.info("result: {}", result);
                json.put("result", result);
                sendMessage(response, json.toString());
                return new ModelAndView("");
            }
            Set<String> ipList = new HashSet<String>();
            Iterator<MachineMemStatInfo> iterator = resMachines.iterator();
            while (iterator.hasNext()) {
                String ip = iterator.next().getIp();
                if (ipList.contains(ip)) {
                    iterator.remove();
                } else {
                    ipList.add(ip);
                }
            }
            Map<String, DeployInfoStat> machineDeployStatMap = appService.getMachineDeployStat(ipList, deployInfoList);
            Map<String, Integer> machineInstanceCountMap = machineCenter.getMachineInstanceCountMap();

            json.put("result", result);
            json.put("machineInstanceCountMap", machineInstanceCountMap);
            json.put("resMachines", resMachines);
            json.put("deployInfoList", deployInfoList);
            json.put("machineDeployStatMap", machineDeployStatMap);
            sendMessage(response, json.toString());
            return new ModelAndView("");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return new ModelAndView("");
    }

    /**
     * 通过,获取驳回申请
     *
     * @param status       审批状态
     * @param appAuditId   审批id
     * @param refuseReason 应用id
     * @return
     */
    @RequestMapping(value = "/addAuditStatus")
    public ModelAndView doAddAuditStatus(HttpServletRequest request, HttpServletResponse response, Model model, Integer status, Long appAuditId, String refuseReason, Integer type) {
        AppUser appUser = getUserInfo(request);
        logger.warn("user {} addAuditStatus: status={},appAuditId:{},refuseReason:{}", appUser.getName(), status, appAuditId, refuseReason);
        AppAudit appAudit = appService.getAppAuditById(appAuditId);
        Long appId = appAudit.getAppId();

        // 通过或者驳回并记录日志
        appService.updateAppAuditStatus(appAuditId, appId, status, appUser);

        // 记录驳回原因
        if (AppCheckEnum.APP_REJECT.value().equals(status)) {
            appAudit.setRefuseReason(refuseReason);
            appService.updateRefuseReason(appAudit, getUserInfo(request));
        }

        // 发邮件统计
        if (AppCheckEnum.APP_PASS.value().equals(status) || AppCheckEnum.APP_REJECT.value().equals(status)) {
            AppDesc appDesc = appService.getByAppId(appId);
            AppUser applyUser = userService.get(appAudit.getUserId());
            if(appDesc != null){
                appEmailUtil.noticeAppResultWithApplyUser(applyUser, appDesc, appService.getAppAuditById(appAuditId));
            }else{
                appEmailUtil.noticeAuditResult(applyUser, appService.getAppAuditById(appAuditId));
            }
        }
        //没有传入type参数，只有在申请应用时会传入
        if (type == null) {
            // 批准成功直接跳转
            if (AppCheckEnum.APP_PASS.value().equals(status)) {
                return new ModelAndView("redirect:/manage/app/auditList");
            }
        }

        if (AppCheckEnum.APP_ALLOCATE_RESOURCE.value().equals(status) && AppAuditType.APP_DIAGNOSTIC.getValue() == type) {
            return new ModelAndView("redirect:/manage/app/tool/index");
        }

        if (AppCheckEnum.APP_ALLOCATE_RESOURCE.value().equals(status) && AppAuditType.APP_OFFLINE.getValue() == type) {
            return new ModelAndView("redirect:/manage/total/list?appParam=" + appId);
        }

        if (AppCheckEnum.APP_ALLOCATE_RESOURCE.value().equals(status) && AppAuditType.FLUSHALL_DATA.getValue() == type) {
            return new ModelAndView("redirect:/manage/app/tool/index?tabTag=deleteKey");
        }

        if (AppCheckEnum.APP_ALLOCATE_RESOURCE.value().equals(status) && AppAuditType.APP_MIGRATE.getValue() == type) {
            return new ModelAndView("redirect:/data/migrate/init");
        }

        if (AppCheckEnum.APP_ALLOCATE_RESOURCE.value().equals(status) && AppAuditType.APP_IMPORT.getValue() == type) {
            return new ModelAndView("redirect:/import/app/init?importId=" + appAudit.getParam1());
        }

        if (AppCheckEnum.APP_ALLOCATE_RESOURCE.value().equals(status) && AppAuditType.SCAN_CLEAN.getValue() == type) {
            return new ModelAndView("redirect:/manage/app/tool/index?tabTag=scanClean");
        }

        write(response, String.valueOf(SuccessEnum.SUCCESS.value()));
        return null;
    }

    /**
     * 下线应用
     *
     * @param appId
     * @return
     */
    @RequestMapping(value = "/offLine")
    public ModelAndView offLineApp(HttpServletRequest request,
                                   HttpServletResponse response, Model model, Long appId, Long appAuditId) {
        AppUser userInfo = getUserInfo(request);
        logger.warn("user {} hope to offline appId: {}", userInfo.getName(), appId);
        long taskId = appDeployCenter.offLineApp(appId, userInfo, appAuditId);
        if (appAuditId != null) {
            appAuditDao.updateTaskId(appAuditId, taskId);
        }
        model.addAttribute("appId", appId);
        model.addAttribute("taskId", taskId);
        model.addAttribute("message", "下线任务已提交，即将跳转任务流页面， taskId:" + taskId);
        logger.warn("user {} offline appId: {}, taskId is {}", userInfo.getName(), appId, taskId);
        return new ModelAndView();
    }

    /**
     * 实例机器信息
     *
     * @param appId
     * @param model
     */
    private void fillAppMachineStat(Long appId, Model model) {
        List<InstanceInfo> instanceList = appService.getAppInstanceInfo(appId);

        Map<String, MachineStats> machineStatsMap = new HashMap<String, MachineStats>();
        Map<String, Long> machineCanUseMem = new HashMap<String, Long>();

        for (InstanceInfo instanceInfo : instanceList) {
            if (TypeUtil.isRedisSentinel(instanceInfo.getType())) {
                continue;
            }
            String ip = instanceInfo.getIp();
            if (machineStatsMap.containsKey(ip)) {
                continue;
            }
            MachineStats machineStats = machineCenter.getMachineMemoryDetail(ip);
            // 机器ip可能下线，查不到数据
            if (machineStats != null) {
                machineStatsMap.put(ip, machineStats);
                machineCanUseMem.put(ip, machineStats.getMachineMemInfo().getLockedMem());
            }
        }
        model.addAttribute("machineCanUseMem", machineCanUseMem);
        model.addAttribute("machineStatsMap", machineStatsMap);
    }


    /**
     * 应用运维
     *
     * @param appId
     */
    @RequestMapping("/index")
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response, Model model, Long appId, String tabTag) {
        model.addAttribute("appId", appId);
        model.addAttribute("tabTag", tabTag);
        return new ModelAndView("manage/appOps/appOpsIndex");
    }

    /**
     * 应用机器运维
     *
     * @param appId
     */
    @RequestMapping("/machine")
    public ModelAndView appMachine(HttpServletRequest request, HttpServletResponse response, Model model, Long appId) {
        if (appId != null && appId > 0) {
            List<MachineStats> appMachineList = appService.getAppMachineDetail(appId);
            model.addAttribute("appMachineList", appMachineList);
            AppDesc appDesc = appService.getByAppId(appId);
            model.addAttribute("appDesc", appDesc);
        }
        return new ModelAndView("manage/appOps/appMachine");
    }

    /**
     * 应用实例运维
     *
     * @param appId
     */
    @RequestMapping("/instance")
    public ModelAndView appInstance(HttpServletRequest request, HttpServletResponse response, Model model, Long appId) {
        if (appId != null && appId > 0) {
            AppDesc appDesc = appService.getByAppId(appId);
            model.addAttribute("appDesc", appDesc);
            //实例信息和统计
            fillAppInstanceStats(appId, model);

            model.addAttribute("k8sMachineMaps", machineCenter.getK8sMachineMap());
            //只有cluster类型才需要计算slot相关
            if (TypeUtil.isRedisCluster(appDesc.getType())) {
                // 计算丢失的slot区间
                Map<String, String> lossSlotsSegmentMap = redisCenter.getClusterLossSlots(appId);
                model.addAttribute("lossSlotsSegmentMap", lossSlotsSegmentMap);
            }
        }
        return new ModelAndView("manage/appOps/appInstance");
    }

    /**
     * 应用详细信息和各种申请记录
     *
     * @param appId
     */
    @RequestMapping("/detail")
    public ModelAndView appInfoAndAudit(HttpServletRequest request, HttpServletResponse response, Model model, Long appId) {
        if (appId != null && appId > 0) {
            List<AppAudit> appAuditList = appService.getAppAuditListByAppId(appId);
            AppDesc appDesc = appService.getByAppId(appId);
            appDesc.setOfficer(userService.getOfficerName(appDesc.getOfficer()));
            model.addAttribute("appAuditList", appAuditList);
            model.addAttribute("appDesc", appDesc);
        }
        return new ModelAndView("manage/appOps/appInfoAndAudit");
    }

    /**
     * redisCluster节点删除: forget + shutdown
     *
     * @param appId             应用id
     * @param delNodeInstanceId 需要被forget的节点
     * @return
     */
    @RequestMapping("/clusterDelNode")
    public ModelAndView clusterDelNode(HttpServletRequest request, HttpServletResponse response, Model model, Long appId,
                                       int delNodeInstanceId) {
        AppUser appUser = getUserInfo(request);
        logger.warn("user {}, clusterForget: appId:{}, instanceId:{}", appUser.getName(), appId, delNodeInstanceId);
        // 检测forget条件
        ClusterOperateResult checkClusterForgetResult = ClusterOperateResult.fail("checkClusterForget Exception");
        try {
            checkClusterForgetResult = redisDeployCenter.checkClusterForget(appId, delNodeInstanceId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        if (!checkClusterForgetResult.isSuccess()) {
            model.addAttribute("success", checkClusterForgetResult.getStatus());
            model.addAttribute("message", checkClusterForgetResult.getMessage());
            return new ModelAndView("");
        }

        // 执行delnode:forget + shutdown
        ClusterOperateResult delNodeResult = ClusterOperateResult.fail("delNode Exception");
        try {
            delNodeResult = redisDeployCenter.delNode(appId, delNodeInstanceId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        model.addAttribute("success", delNodeResult.getStatus());
        model.addAttribute("message", delNodeResult.getMessage());
        logger.warn("user {}, clusterForget: appId:{}, instanceId:{}, result is {}", appUser.getName(), appId, delNodeInstanceId, delNodeResult.getStatus());

        return new ModelAndView("");

    }

    /**
     * redisCluster从节点failover
     *
     * @param appId           应用id
     * @param slaveInstanceId 从节点instanceId
     * @return
     */
    @RequestMapping("/clusterSlaveFailOver")
    public void clusterSlaveFailOver(HttpServletRequest request, HttpServletResponse response, Model model, Long appId,
                                     int slaveInstanceId) {
        boolean success = false;
        String failoverParam = request.getParameter("failoverParam");
        logger.warn("clusterSlaveFailOver: appId:{}, slaveInstanceId:{}, failoverParam:{}", appId, slaveInstanceId, failoverParam);
        if (appId != null && appId > 0 && slaveInstanceId > 0) {
            try {
                success = redisDeployCenter.clusterFailover(appId, slaveInstanceId, failoverParam);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            logger.error("error param clusterSlaveFailOver: appId:{}, slaveInstanceId:{}, failoverParam:{}", appId, slaveInstanceId, failoverParam);
        }
        logger.warn("clusterSlaveFailOver: appId:{}, slaveInstanceId:{}, failoverParam:{}, result is {}", appId, slaveInstanceId, failoverParam, success);
        write(response, String.valueOf(success == true ? SuccessEnum.SUCCESS.value() : SuccessEnum.FAIL.value()));
    }


    /**
     * @Description: 自动生成slave ip
     * @Author: caoru
     * @CreateDate: 2018/10/11 11:50
     */
    @RequestMapping(value = "/genSlaveIp")
    public void genSlaveIp(HttpServletRequest request, HttpServletResponse response, Model model, long appId,
                           int masterInstanceId) {
        String machineRes;
        String result = "success";
        JSONObject json = new JSONObject();
        if (appId > 0 && masterInstanceId > 0) {
            try {
                machineRes = redisDeployCenter.genSlaveIp(appId, masterInstanceId);
                if (StringUtils.isEmpty(machineRes)) {
                    result = "没有满足条件的机器可用";
                }
                json.put("result", result);
                json.put("machineRes", machineRes);
                sendMessage(response, json.toString());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

    }

    /**
     * 添加slave节点
     *
     * @param appId
     * @param masterInstanceId
     * @param slaveHost
     * @return
     */
    @RequestMapping(value = "/addSlave")
    public void addSlave(HttpServletRequest request, HttpServletResponse response, Model model, long appId,
                         int masterInstanceId, String slaveHost) {
        AppUser appUser = getUserInfo(request);
        logger.warn("user {} addSlave: appId:{},masterInstanceId:{},slaveHost:{}", appUser.getName(), appId, masterInstanceId, slaveHost);
        boolean success = false;
        if (appId > 0 && StringUtils.isNotBlank(slaveHost) && masterInstanceId > 0) {
            try {
                success = redisDeployCenter.addSlave(appId, masterInstanceId, slaveHost);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        logger.warn("user {} addSlave: appId:{},masterInstanceId:{},slaveHost:{} result is {}", appUser.getName(), appId, masterInstanceId, slaveHost, success);
        write(response, String.valueOf(success == true ? SuccessEnum.SUCCESS.value() : SuccessEnum.FAIL.value()));
    }

    /**
     * 添加sentinel节点
     *
     * @param appId
     * @param sentinelHost
     * @return
     */
    @RequestMapping(value = "/addSentinel")
    public void addSentinel(HttpServletRequest request, HttpServletResponse response, Model model, long appId, String sentinelHost) {
        AppUser appUser = getUserInfo(request);
        logger.warn("user {} addSentinel: appId:{}, sentinelHost:{}", appUser.getName(), appId, sentinelHost);
        boolean success = false;
        if (appId > 0 && StringUtils.isNotBlank(sentinelHost)) {
            try {
                success = redisDeployCenter.addSentinel(appId, sentinelHost);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        logger.warn("user {} addSentinel: appId:{}, sentinelHost:{} result is {}", appUser.getName(), appId, sentinelHost, success);
        write(response, String.valueOf(success == true ? SuccessEnum.SUCCESS.value() : SuccessEnum.FAIL.value()));
    }

    /**
     * 为失联的slot添加master节点
     *
     * @param appId
     */
    @RequestMapping(value = "/addFailSlotsMaster")
    public void addFailSlotsMaster(HttpServletRequest request, HttpServletResponse response, Model model, long appId, String failSlotsMasterHost, int instanceId) {
        AppUser appUser = getUserInfo(request);
        logger.warn("user {} addFailSlotsMaster: appId:{}, instanceId {}, newMasterHost:{}", appUser.getName(), appId, instanceId, failSlotsMasterHost);
        RedisOperateEnum redisOperateEnum = RedisOperateEnum.FAIL;
        if (appId > 0 && StringUtils.isNotBlank(failSlotsMasterHost)) {
            try {
                redisOperateEnum = redisDeployCenter.addSlotsFailMaster(appId, instanceId, failSlotsMasterHost);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        logger.warn("user {} addFailSlotsMaster: appId:{}, instanceId {}, newMasterHost:{} result is {}", appUser.getName(), appId, instanceId, failSlotsMasterHost, redisOperateEnum.getValue());
        write(response, String.valueOf(redisOperateEnum.getValue()));
    }


    /**
     * sentinelFailOver操作
     *
     * @param appId
     * @return
     */
    @RequestMapping("/sentinelFailOver")
    public void sentinelFailOver(HttpServletRequest request, HttpServletResponse response, Model model, long appId) {
        AppUser appUser = getUserInfo(request);
        logger.warn("user {} sentinelFailOver, appId:{}", appUser.getName(), appId);
        boolean success = false;
        if (appId > 0) {
            try {
                success = redisDeployCenter.sentinelFailover(appId);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            logger.error("error param, sentinelFailOver: appId:{}", appId);
        }
        logger.warn("user {} sentinelFailOver, appId:{}, result is {}", appUser.getName(), appId, success);
        write(response, String.valueOf(success == true ? SuccessEnum.SUCCESS.value() : SuccessEnum.FAIL.value()));
    }

    @RequestMapping("/sentinelReset")
    public void sentinelReset(HttpServletRequest request, HttpServletResponse response, Model model, long appId) {
        AppUser appUser = getUserInfo(request);
        logger.warn("user {} sentinelReset, appId:{}", appUser.getName(), appId);
        boolean success = false;
        if (appId > 0) {
            try {
                success = redisDeployCenter.sentinelReset(appId);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            logger.error("error param, sentinelReset: appId:{}", appId);
        }
        logger.warn("user {} sentinelReset, appId:{}, result is {}", appUser.getName(), appId, success);
        write(response, String.valueOf(success == true ? SuccessEnum.SUCCESS.value() : SuccessEnum.FAIL.value()));
    }

    /**
     * 应用重要性级别
     */
    @RequestMapping(value = "/updateAppImportantLevel")
    public ModelAndView doUpdateAppImportantLevel(HttpServletRequest request, HttpServletResponse response, Model model) {
        long appId = NumberUtils.toLong(request.getParameter("appId"));
        int importantLevel = NumberUtils.toInt(request.getParameter("importantLevel"));
        SuccessEnum successEnum = SuccessEnum.FAIL;
        if (appId > 0 && importantLevel >= 0) {
            try {
                AppDesc appDesc = appService.getByAppId(appId);
                appDesc.setImportantLevel(importantLevel);
                appService.update(appDesc);
                successEnum = SuccessEnum.SUCCESS;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        model.addAttribute("status", successEnum.value());
        return new ModelAndView("");
    }

    /**
     * 更新应用密码
     */
    @RequestMapping(value = "/updateAppPassword")
    public ModelAndView doUpdateAppPassword(HttpServletRequest request, HttpServletResponse response, Model model) {
        long appId = NumberUtils.toLong(request.getParameter("appId"));
        String password = request.getParameter("password");
        Boolean isSetPasswd = Boolean.valueOf(request.getParameter("isSetPasswd"));
        logger.info("modify appId:{},password:{}", appId, password);
        SuccessEnum successEnum = SuccessEnum.FAIL;
        if (appId > 0) {
            try {
                //增加版本校验，6.0.0-6.0.8不支持清除密码，即password为空的情况
                AppDesc appDesc = appService.getByAppId(appId);
                if(appDesc != null){
                    if(StringUtils.isBlank(password)){
                        // Redis版本信息
                        SystemResource resource = resourceService.getResourceById(appDesc.getVersionId());
                        String name = resource.getName();
                        if(name != null){
                            String[] split = name.split("-");
                            if(split != null && split.length == 2){
                                String version = split[1].replace(".", "");
                                int versionNum = Integer.parseInt(version);
                                if(versionNum <= 608 && versionNum >= 600){
                                    model.addAttribute("status", successEnum.value());
                                    return new ModelAndView("");
                                }
                            }
                        }
                    }
                    // 修改密码逻辑
                    redisDeployCenter.fixPassword(appId, password, isSetPasswd, false);
                }
                successEnum = SuccessEnum.SUCCESS;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        model.addAttribute("status", successEnum.value());
        return new ModelAndView("");
    }

    /**
     * 更新应用 Redis版本
     */
    @RequestMapping(value = "/updateRedisVersion")
    public ModelAndView doUpdateAppRedisVersion(HttpServletRequest request, HttpServletResponse response, Model model) {
        long appId = NumberUtils.toLong(request.getParameter("appId"));
        int versionId = NumberUtils.toInt(request.getParameter("versionId"));
        logger.info("modify appId:{},versionId:{}", appId, versionId);
        SuccessEnum successEnum = SuccessEnum.FAIL;
        if (appId > 0 && versionId > 0) {
            try {
                AppDesc appDesc = appService.getByAppId(appId);
                appDesc.setVersionId(versionId);
                appService.update(appDesc);
                successEnum = SuccessEnum.SUCCESS;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        model.addAttribute("status", successEnum.value());
        return new ModelAndView("");
    }

    /**
     * <p>
     * Description: 应用密码初始化页面
     * </p>
     *
     * @param
     * @return
     * @author chenshi
     * @version 1.0
     * @date 2017/8/2
     */
    @RequestMapping(value = "/initAppPassword")
    public ModelAndView doInitAppPassword(HttpServletRequest request, HttpServletResponse response, Model model, Long appId) {
        if (appId != null && appId > 0) {
            AppDesc appDesc = appService.getByAppId(appId);
            model.addAttribute("appId", appDesc.getAppId());
            model.addAttribute("pkey", appDesc.getPkey());
            model.addAttribute("customPassword", appDesc.getCustomPassword());
        }
        return new ModelAndView("manage/appOps/appCodeInit");
    }

    @RequestMapping(value = "/checkAppPassword")
    public ModelAndView doCheckAppPassword(HttpServletRequest request, HttpServletResponse response, Model model) {

        long appId = NumberUtils.toLong(request.getParameter("appId"));
        logger.info("check appid:{}", appId);
        SuccessEnum successEnum = SuccessEnum.FAIL;
        if (appId > 0) {
            try {
                // 密码校验逻辑
                boolean check = redisDeployCenter.checkAuths(appId);
                // 返回true 则一致
                if (check) {
                    successEnum = SuccessEnum.SUCCESS;
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        model.addAttribute("status", successEnum.value());
        return new ModelAndView("");
    }

    @RequestMapping(value = "/updateAppPersistenceType")
    public void updateAppPersistenceType(HttpServletRequest request, HttpServletResponse response) {
        long appId = NumberUtils.toLong(request.getParameter("appId"), -1);
        Integer persistenceType = Integer.valueOf(request.getParameter("persistenceType"));
        if(AppDescEnum.AppPersistenceType.getByType(persistenceType) == null){
            write(response, String.valueOf(SuccessEnum.FAIL.value()));
            return;
        }
        boolean executeFlag = appService.updateAppPersistenceType(appId, persistenceType);
        write(response, String.valueOf(executeFlag == true ? SuccessEnum.SUCCESS.value() : SuccessEnum.FAIL.value()));
    }

}
