package com.sohu.cache.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.sohu.cache.constant.*;
import com.sohu.cache.dao.AppUserDao;
import com.sohu.cache.entity.*;
import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.redis.enums.InstanceAlertCheckCycleEnum;
import com.sohu.cache.redis.enums.InstanceAlertCompareTypeEnum;
import com.sohu.cache.redis.enums.InstanceAlertTypeEnum;
import com.sohu.cache.stats.app.AppDailyDataCenter;
import com.sohu.cache.stats.app.AppDeployCenter;
import com.sohu.cache.stats.app.AppStatsCenter;
import com.sohu.cache.stats.instance.InstanceAlertConfigService;
import com.sohu.cache.stats.instance.InstanceStatsCenter;
import com.sohu.cache.task.constant.ResourceEnum;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.web.chart.model.HighchartDoublePoint;
import com.sohu.cache.web.chart.model.HighchartPoint;
import com.sohu.cache.web.chart.model.SimpleChartData;
import com.sohu.cache.web.enums.SuccessEnum;
import com.sohu.cache.web.service.ModuleService;
import com.sohu.cache.web.service.ResourceService;
import com.sohu.cache.web.service.UserService;
import com.sohu.cache.web.util.AppEmailUtil;
import com.sohu.cache.web.util.DateUtil;
import com.sohu.cache.web.util.Page;
import com.sohu.cache.web.vo.AppDetailVO;
import net.sf.json.JSONArray;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
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
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 应用统计相关
 *
 * @author leifu
 * @Time 2014年8月31日
 */
@Controller
@RequestMapping("/admin/app")
public class AppController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(AppController.class);

    @Resource(name = "appStatsCenter")
    private AppStatsCenter appStatsCenter;

    @Resource(name = "appEmailUtil")
    private AppEmailUtil appEmailUtil;

    @Resource(name = "appDeployCenter")
    private AppDeployCenter appDeployCenter;

    @Resource(name = "instanceStatsCenter")
    private InstanceStatsCenter instanceStatsCenter;

    @Resource(name = "appDailyDataCenter")
    private AppDailyDataCenter appDailyDataCenter;

    @Resource(name = "instanceAlertConfigService")
    private InstanceAlertConfigService instanceAlertConfigService;

    @Autowired
    private ResourceService resourceService;

    @Resource
    private MachineCenter machineCenter;

    @Resource
    private UserService userService;
    @Autowired
    private AppUserDao appUserDao;
    @Autowired
    private ModuleService moduleService;

    /**
     * 初始化贡献者页面
     *
     * @return
     */
    @RequestMapping("/initBecomeContributor")
    public ModelAndView doInitBecomeContributor(HttpServletRequest request,
                                                HttpServletResponse response, Model model) {
        model.addAttribute("currentUser", getUserInfo(request));
        return new ModelAndView("app/initBecomeContributor");
    }

    /**
     * 成为cachecloud贡献者
     *
     * @param groupName   项目组
     * @param applyReason 申请理由
     * @return
     */
    @RequestMapping("/addBecomeContributor")
    public ModelAndView doAddBecomeContributor(HttpServletRequest request,
                                               HttpServletResponse response, Model model, String groupName, String applyReason) {
        appEmailUtil.noticeBecomeContributor(groupName, applyReason, getUserInfo(request));
        model.addAttribute("success", SuccessEnum.SUCCESS.value());
        return new ModelAndView("");
    }

    /**
     * 单个应用首页
     *
     * @param appId
     * @param tabTag       标签名
     * @param firstCommand 第一条命令
     * @return
     * @throws ParseException
     */
    @RequestMapping("/index")
    public ModelAndView index(HttpServletRequest request,
                              HttpServletResponse response, Model model, Long appId, String tabTag, String firstCommand, String condition)
            throws ParseException {
        // 如果应用id为空，取第一个应用id
        if (appId == null) {
            return new ModelAndView("redirect:/admin/app/list");
        }

        // 日期转换
        String searchDate = request.getParameter("searchDate");

        String startDateParam = request.getParameter("startDate");
        String endDateParam = request.getParameter("endDate");
        if (StringUtils.isBlank(startDateParam) || StringUtils.isBlank(endDateParam)) {
            Date startDate = new Date();
            startDateParam = DateUtil.formatDate(startDate, "yyyy-MM-dd");
            endDateParam = DateUtil.formatDate(DateUtils.addDays(startDate, 1), "yyyy-MM-dd");
        }

        //慢查询
        String slowLogStartDateParam = request.getParameter("slowLogStartDate");
        String slowLogEndDateParam = request.getParameter("slowLogEndDate");
        if (StringUtils.isBlank(slowLogStartDateParam) || StringUtils.isBlank(slowLogEndDateParam)) {
            Date startDate = new Date();
            slowLogStartDateParam = DateUtil.formatDate(startDate, "yyyy-MM-dd");
            slowLogEndDateParam = DateUtil.formatDate(DateUtils.addDays(startDate, 1), "yyyy-MM-dd");
        }

        //日报
        String dailyDateParam = request.getParameter("dailyDate");
        if (StringUtils.isBlank(dailyDateParam)) {
            dailyDateParam = DateUtil.formatDate(DateUtils.addDays(new Date(), -1), "yyyy-MM-dd");
        }

        int conditionInt = StringUtils.isEmpty(condition) ? 0 : Integer.parseInt(condition);
        model.addAttribute("condition", conditionInt);

        model.addAttribute("startDate", startDateParam);
        model.addAttribute("endDate", endDateParam);
        model.addAttribute("searchDate", searchDate);
        model.addAttribute("slowLogStartDate", slowLogStartDateParam);
        model.addAttribute("slowLogEndDate", slowLogEndDateParam);
        model.addAttribute("dailyDate", dailyDateParam);
        model.addAttribute("appId", appId);
        model.addAttribute("tabTag", tabTag);
        model.addAttribute("firstCommand", firstCommand);

        return new ModelAndView("app/userAppsIndex");

    }

    /**
     * 应用统计相关
     */
    @RequestMapping("/stat")
    public ModelAndView appStat(HttpServletRequest request,
                                HttpServletResponse response, Model model, Long appId) throws ParseException {
        // 1.获取app的VO
        AppDetailVO appDetail = appStatsCenter.getAppDetail(appId);
        model.addAttribute("appDetail", appDetail);

        // 2. 时间
        TimeBetween timeBetween = getTimeBetween(request, model, "startDate", "endDate");
        long beginTime = timeBetween.getStartTime();
        long endTime = timeBetween.getEndTime();

        // 3.是否超过1天
        if (endTime - beginTime > TimeUnit.DAYS.toMillis(1)) {
            model.addAttribute("betweenOneDay", 0);
        } else {
            model.addAttribute("betweenOneDay", 1);
        }

        // 4. top5命令
        List<AppCommandStats> top5Commands = appStatsCenter
                .getTopLimitAppCommandStatsList(appId, beginTime, endTime, 5);
        model.addAttribute("top5Commands", top5Commands);

        // 5.峰值
        List<AppCommandStats> top5ClimaxList = new ArrayList<AppCommandStats>();
        if (CollectionUtils.isNotEmpty(top5Commands)) {
            for (AppCommandStats appCommandStats : top5Commands) {
                AppCommandStats temp = appStatsCenter
                        .getCommandClimax(appId, beginTime, endTime, appCommandStats.getCommandName());
                if (temp != null) {
                    top5ClimaxList.add(temp);
                }
            }
        }
        model.addAttribute("top5ClimaxList", top5ClimaxList);
        if (appDetail != null && StringUtils.isNotBlank(appDetail.getAppDesc().getAppPassword())) {
            model.addAttribute("md5password", appDetail.getAppDesc().getAppPassword());
        }

        model.addAttribute("appId", appId);
        return new ModelAndView("app/appStat");
    }


    /**
     * 命令曲线
     *
     * @param firstCommand 第一条命令
     */
    @RequestMapping("/commandAnalysis")
    public ModelAndView appCommandAnalysis(HttpServletRequest request,
                                           HttpServletResponse response, Model model, Long appId, String firstCommand) throws ParseException {
        // 1.获取app的VO
        AppDetailVO appDetail = appStatsCenter.getAppDetail(appId);
        model.addAttribute("appDetail", appDetail);

        // 2.返回日期
        TimeBetween timeBetween = getTimeBetween(request, model, "startDate", "endDate");

        // 3.是否超过1天
        if(timeBetween.getStartDate() != null && timeBetween.getEndDate() != null
                && (timeBetween.getEndDate().getTime() - timeBetween.getStartDate().getTime() > TimeUnit.DAYS.toMillis(1))){
            model.addAttribute("betweenOneDay", 0);
        } else {
            model.addAttribute("betweenOneDay", 1);
        }

        // 4.获取top命令
        List<AppCommandStats> allCommands = appStatsCenter
                .getTopLimitAppCommandStatsList(appId, timeBetween.getStartTime(), timeBetween.getEndTime(), 20);
        model.addAttribute("allCommands", allCommands);
        if (StringUtils.isBlank(firstCommand) && CollectionUtils.isNotEmpty(allCommands)) {
            model.addAttribute("firstCommand", allCommands.get(0).getCommandName());
        } else {
            model.addAttribute("firstCommand", firstCommand);
        }
        model.addAttribute("appId", appId);
        // 返回标签名
        return new ModelAndView("app/appCommandAnalysis");
    }

    /**
     * 应用故障
     */
    @RequestMapping("/fault")
    public ModelAndView appFault(HttpServletRequest request,
                                 HttpServletResponse response, Model model) {

        return new ModelAndView("app/appFault");
    }

    /**
     * 应用拓扑图
     *
     * @param appId
     * @return
     */
    @RequestMapping("/topology")
    public ModelAndView statTopology(HttpServletRequest request,
                                     HttpServletResponse response, Long appId, Model model) {
        //应用信息
        AppDesc appDesc = appService.getByAppId(appId);
        model.addAttribute("appDesc", appDesc);
        //实例相关信息(包含统计)
        fillAppInstanceStats(appId, model);
        return new ModelAndView("app/appTopology");
    }

    @RequestMapping("/appDesc")
    public ModelAndView appDesc(HttpServletRequest request,
                                HttpServletResponse response, Long appId, Model model) {
        JSONObject json = new JSONObject();
        AppDesc appDesc = appService.getByAppId(appId);
        if (appDesc == null) {
            json.put("status", String.valueOf(SuccessEnum.FAIL.value()));
        } else {
            json.put("appDesc", appDesc);
            json.put("status", String.valueOf(SuccessEnum.SUCCESS.value()));
        }
        sendMessage(response, json.toString());
        return null;
    }

    /**
     * 应用机器拓扑图
     *
     * @param appId
     * @return
     */
    @RequestMapping("/machineInstancesTopology")
    public ModelAndView machineInstancesTopology(HttpServletRequest request,
                                                 HttpServletResponse response, Long appId, Model model) {
        //应用信息
        AppDesc appDesc = appService.getByAppId(appId);
        model.addAttribute("appDesc", appDesc);
        //拓扑
        fillAppMachineInstanceTopology(appId, model);
        return new ModelAndView("app/appMachineInstancesTopology");
    }

    /**
     * 应用基本信息
     *
     * @param appId 应用id
     */
    @RequestMapping("/detail")
    public ModelAndView appDetail(HttpServletRequest request,
                                  HttpServletResponse response, Model model, Long appId) {
        // 获取应用vo
        AppDetailVO appDetail = appStatsCenter.getAppDetail(appId);
        model.addAttribute("appDetail", appDetail);
        //增加全局监控
        model.addAttribute("instanceAlertAllList",
                instanceAlertConfigService.getByType(InstanceAlertTypeEnum.ALL_ALERT.getValue()));
        model.addAttribute("instanceAlertCheckCycleEnumList",
                InstanceAlertCheckCycleEnum.getInstanceAlertCheckCycleEnumList());
        model.addAttribute("instanceAlertCompareTypeEnumList",
                InstanceAlertCompareTypeEnum.getInstanceAlertCompareTypeEnumList());
        if (!StringUtils.isEmpty(appDetail.getAppDesc().getAppPassword())) {
            model.addAttribute("password", appDetail.getAppDesc().getAppPassword());
        } else {
            model.addAttribute("password", "");
        }
        // 用户管理权限
        List<AppUser> userList = userService.getAllUser();
        Map<Long, AppUser> userMap = userList.stream().collect(Collectors.toMap(AppUser::getId, Function.identity()));
        model.addAttribute("userMap", userMap);
        AppUser appUser = getUserInfo(request);
        Boolean hasAuth = false;
        String officer = appDetail.getAppDesc().getOfficer();
        List<String> officers = new ArrayList<String>();
        if (!StringUtils.isEmpty(officer)) {
            officers.addAll(Arrays.asList(officer.split(",")));
        }
        if (appUser != null &&
                (appUser.getType() == AppUserTypeEnum.ADMIN_USER.value() || officers.contains(String.valueOf(appUser.getId())))) {
            hasAuth = true;
        }
        model.addAttribute("hasAuth", hasAuth);

        return new ModelAndView("app/appDetail");
    }

    /**
     * 应用客户端连接
     *
     * @param request
     * @param response
     * @param model
     * @param appId
     * @return
     */
    @RequestMapping("/clientList")
    public ModelAndView clientList(HttpServletRequest request,
                                   HttpServletResponse response,
                                   Model model,
                                   Long appId, int condition) {
        //应用信息
        AppDesc appDesc = appService.getByAppId(appId);
        model.addAttribute("appDesc", appDesc);

        model.addAttribute("condition", condition);

        List<InstanceInfo> instanceInfoList = appService.getAppOnlineInstanceInfo(appId);
        Map<Integer, String> instanceMap = instanceInfoList.stream().collect(Collectors.toMap(
                InstanceInfo::getId,
                InstanceInfo::getHostPort));
        model.addAttribute("instanceMap", instanceMap);

        List<Map<String, Object>> addrInstanceList = redisCenter.getAppClientList(appId, condition);

        model.addAttribute("addrInstanceList", addrInstanceList);

        return new ModelAndView("app/appClientList");
    }

    /**
     * 获取某个命令时间分布图
     *
     * @param appId 应用id
     * @throws ParseException
     */
    @RequestMapping("/getCommandStats")
    public ModelAndView getCommandStats(HttpServletRequest request,
                                        HttpServletResponse response, Model model, Long appId) throws ParseException {
        TimeBetween timeBetween = getJsonTimeBetween(request);
        long beginTime = timeBetween.getStartTime();
        long endTime = timeBetween.getEndTime();
        // 命令参数
        String commandName = request.getParameter("commandName");
        List<AppCommandStats> appCommandStatsList;
        if (StringUtils.isNotBlank(commandName)) {
            appCommandStatsList = appStatsCenter.getCommandStatsList(appId, beginTime, endTime, commandName);
        } else {
            appCommandStatsList = appStatsCenter.getCommandStatsList(appId, beginTime, endTime);
        }
        String result = assembleJson(appCommandStatsList);
        write(response, result);
        return null;
    }

    /**
     * 获取某个命令时间分布图
     *
     * @param appId 应用id
     * @throws ParseException
     */
    @RequestMapping("/getMutiDatesCommandStats")
    public ModelAndView getMutiDatesCommandStats(HttpServletRequest request,
                                                 HttpServletResponse response, Model model, Long appId) throws ParseException {
        TimeBetween timeBetween = getJsonTimeBetween(request);
        // 命令参数
        String commandName = request.getParameter("commandName");
        List<AppCommandStats> appCommandStatsList;
        if (StringUtils.isNotBlank(commandName)) {
            appCommandStatsList = appStatsCenter
                    .getCommandStatsListV2(appId, timeBetween.getStartTime(), timeBetween.getEndTime(),
                            TimeDimensionalityEnum.MINUTE, commandName);
        } else {
            appCommandStatsList = appStatsCenter
                    .getCommandStatsListV2(appId, timeBetween.getStartTime(), timeBetween.getEndTime(),
                            TimeDimensionalityEnum.MINUTE);
        }
        String result = assembleMutilDateAppCommandJsonMinute(appCommandStatsList, timeBetween.getStartDate(),
                timeBetween.getEndDate());
        model.addAttribute("data", result);
        return new ModelAndView("");
    }

    /**
     * 获取命中率、丢失率等分布
     *
     * @param appId    应用id
     * @param statName 统计项(hit,miss等)
     * @throws ParseException
     */
    @RequestMapping("/getAppStats")
    public ModelAndView getAppStats(HttpServletRequest request,
                                    HttpServletResponse response, Model model, Long appId,
                                    String statName) throws ParseException {
        TimeBetween timeBetween = getJsonTimeBetween(request);
        List<AppStats> appStats = appStatsCenter
                .getAppStatsListByMinuteTime(appId, timeBetween.getStartTime(), timeBetween.getEndTime());
        String result = assembleAppStatsJson(appStats, statName);
        write(response, result);
        return null;
    }

    /**
     * 多命令
     *
     * @param appId
     * @return
     * @throws ParseException
     */
    @RequestMapping("/getMutiStatAppStats")
    public ModelAndView getMutiStatAppStats(HttpServletRequest request,
                                            HttpServletResponse response, Model model, Long appId) throws ParseException {
        String statNames = request.getParameter("statName");
        List<String> statNameList = Arrays.asList(statNames.split(ConstUtils.COMMA));
        TimeBetween timeBetween = getJsonTimeBetween(request);
        List<AppStats> appStats = appStatsCenter
                .getAppStatsList(appId, timeBetween.getStartTime(), timeBetween.getEndTime(),
                        TimeDimensionalityEnum.MINUTE);
        String result = assembleMutiStatAppStatsJsonMinute(appStats, statNameList, timeBetween.getStartDate());
        model.addAttribute("data", result);
        return new ModelAndView("");
    }

    /**
     * 获取命中率、丢失率等分布
     *
     * @param appId    应用id
     * @param statName 统计项(hit,miss等)
     * @throws ParseException
     */
    @RequestMapping("/getMutiDatesAppStats")
    public ModelAndView getMutiDatesAppStats(HttpServletRequest request,
                                             HttpServletResponse response, Model model, Long appId,
                                             String statName, Integer addDay) throws ParseException {
        TimeBetween timeBetween = getJsonTimeBetween(request);
        List<AppStats> appStats = appStatsCenter
                .getAppStatsList(appId, timeBetween.getStartTime(), timeBetween.getEndTime(),
                        TimeDimensionalityEnum.MINUTE);
        String result = assembleMutilDateAppStatsJsonMinute(appStats, statName, timeBetween.getStartDate(),
                timeBetween.getEndDate());
        model.addAttribute("data", result);
        return new ModelAndView("");
    }

    /**
     * 获取指定时间内某个应用全部实例的统计信息
     *
     * @param appId
     */
    @RequestMapping("/appInstanceNetStat")
    public ModelAndView appInstanceNetStat(HttpServletRequest request, HttpServletResponse response, Model model,
                                           Long appId) throws ParseException {
        // 应用基本信息
        AppDesc appDesc = appService.getByAppId(appId);
        model.addAttribute("appDesc", appDesc);
        model.addAttribute("appId", appId);

        // 日期格式转换
        getTimeBetween(request, model, "startDate", "endDate");

        return new ModelAndView("app/appInstanceNetStat");
    }

    /**
     * 获取指定时间内某个应用全部实例的CPU统计信息
     *
     * @param appId
     */
    @RequestMapping("/appInstanceCpuStat")
    public ModelAndView appInstanceCpuStat(HttpServletRequest request, Model model,
                                           Long appId) throws ParseException {
        // 应用基本信息
        AppDesc appDesc = appService.getByAppId(appId);
        model.addAttribute("appDesc", appDesc);
        model.addAttribute("appId", appId);

        // 日期格式转换
        getTimeBetween(request, model, "startDate", "endDate");

        return new ModelAndView("app/appInstanceCpuStat");
    }

    /**
     * 获取指定时间内某个应用全部实例的统计信息
     *
     * @param appId 应用流量
     */
    @RequestMapping("/getAppInstancesCpuStat")
    public void getAppInstancesCpuStat(HttpServletRequest request, HttpServletResponse response, Model model,
                                       Long appId) throws ParseException {
        //时间转换
        TimeBetween timeBetween = getJsonTimeBetween(request);

        //缩减字段
        String cpuSysCommand = "used_cpu_sys";
        String cpuUserCommand = "used_cpu_user";
        String cpuSysChildCommand = "used_cpu_sys_children";
        String cpuUserChildCommand = "used_cpu_user_children";
        Map<String, String> commandMap = Maps.newHashMap();
        commandMap.put(cpuSysCommand, "cs");
        commandMap.put(cpuUserCommand, "cu");
        commandMap.put(cpuSysChildCommand, "cs_child");
        commandMap.put(cpuUserChildCommand, "cu_child");

        //获取应用下所有实例网络流量统计
        Map<Integer, Map<String, List<InstanceCommandStats>>> appInstancesNetStat = instanceStatsCenter
                .getStandardStatsList(appId, timeBetween.getStartTime(), timeBetween.getEndTime(),
                        Arrays.asList(cpuSysCommand, cpuUserCommand, cpuSysChildCommand, cpuUserChildCommand));

        //解析成json数组
        List<Map<String, Object>> appInstancesNetStatList = new ArrayList<Map<String, Object>>();
        for (Entry<Integer, Map<String, List<InstanceCommandStats>>> entry : appInstancesNetStat.entrySet()) {
            Integer instanceId = entry.getKey();

            //实例基本信息
            Map<String, Object> instanceStatMap = new HashMap<String, Object>();
            instanceStatMap.put("instanceId", instanceId);
            InstanceInfo instanceInfo = instanceStatsCenter.getInstanceInfo(instanceId);
            instanceStatMap.put("instanceInfo", instanceInfo.getIp() + ":" + instanceInfo.getPort());

            //每个实例的统计信息
            List<Map<String, Object>> instanceCpuStatList = new ArrayList<Map<String, Object>>();
            instanceStatMap.put("instanceCpuStatMapList", instanceCpuStatList);
            appInstancesNetStatList.add(instanceStatMap);

            //记录输入和输出流量
            Map<String, List<InstanceCommandStats>> map = entry.getValue();
            List<InstanceCommandStats> instanceCommandStatsList = new ArrayList<InstanceCommandStats>();
            instanceCommandStatsList.addAll(map.get(cpuSysCommand));
            instanceCommandStatsList.addAll(map.get(cpuUserCommand));
            instanceCommandStatsList.addAll(map.get(cpuSysChildCommand));
            instanceCommandStatsList.addAll(map.get(cpuUserChildCommand));

            Map<Long, Map<String, Object>> total = new HashMap<Long, Map<String, Object>>();
            for (InstanceCommandStats instanceCommandStat : instanceCommandStatsList) {
                //用timestamp作为key,保证输入和输出流量在一个Map统计里
                long timestamp = instanceCommandStat.getTimeStamp();
                long commandCount = instanceCommandStat.getCommandCount();
                String command = instanceCommandStat.getCommandName();
                //精简字段
                command = commandMap.get(command);
                if (total.containsKey(timestamp)) {
                    Map<String, Object> tmpMap = total.get(timestamp);
                    tmpMap.put(command, commandCount);
                } else {
                    Map<String, Object> tmpMap = new HashMap<String, Object>();
                    tmpMap.put("t", timestamp);
                    tmpMap.put(command, commandCount);
                    total.put(timestamp, tmpMap);
                    instanceCpuStatList.add(tmpMap);
                }
            }
        }

        String result = JSONObject.toJSONString(appInstancesNetStatList);
        write(response, result);
    }

    /**
     * 获取指定时间内某个应用全部实例的CPU统计信息
     *
     * @param appId
     */
    @RequestMapping("/appInstanceMemFragRatioStat")
    public ModelAndView appInstanceMemFragRatioStat(HttpServletRequest request, Model model,
                                                    Long appId) throws ParseException {
        // 应用基本信息
        AppDesc appDesc = appService.getByAppId(appId);
        model.addAttribute("appDesc", appDesc);
        model.addAttribute("appId", appId);

        // 日期格式转换
        getTimeBetween(request, model, "startDate", "endDate");

        return new ModelAndView("app/appInstanceMemFragRatioStat");
    }

    /**
     * 获取指定时间内某个应用全部实例的统计信息
     *
     * @param appId 应用流量
     */
    @RequestMapping("/getAppInstancesMemFragRatioStat")
    public void getAppInstancesMemFragRatioStat(HttpServletRequest request, HttpServletResponse response, Model model,
                                                Long appId) throws ParseException {
        //时间转换
        TimeBetween timeBetween = getJsonTimeBetween(request);

        //缩减字段
        String ratioCommand = "mem_fragmentation_ratio";
        Map<String, String> commandMap = Maps.newHashMap();
        commandMap.put(ratioCommand, "ratio");

        long start = System.currentTimeMillis();
        //获取应用下所有实例碎片率统计
        Table<Integer, String, Map<String, List<InstanceMinuteStats>>> table = instanceStatsCenter
                .getInstanceMinuteStatsList(appId, timeBetween.getStartTime(), timeBetween.getEndTime(),
                        Arrays.asList(ratioCommand));
        logger.warn("getInstanceMinuteStatsList cost:{} ms", (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();

        //解析成json数组
        List<Map<String, Object>> appInstancesMemFragRatioStatList = new ArrayList<Map<String, Object>>();
        for (Table.Cell<Integer, String, Map<String, List<InstanceMinuteStats>>> cell : table.cellSet()) {
            Integer instanceId = cell.getRowKey();

            //实例基本信息
            Map<String, Object> instanceStatMap = new HashMap<String, Object>();
            instanceStatMap.put("instanceId", instanceId);
            instanceStatMap.put("instanceInfo", cell.getColumnKey());

            //每个实例的统计信息
            List<Map<String, Object>> instanceMemFragRatioStatList = new ArrayList<Map<String, Object>>();
            instanceStatMap.put("instanceMemFragRatioStatMapList", instanceMemFragRatioStatList);
            appInstancesMemFragRatioStatList.add(instanceStatMap);

            //记录碎片率
            Map<String, List<InstanceMinuteStats>> map = cell.getValue();
            List<InstanceMinuteStats> instanceCommandStatsList = new ArrayList<>();
            instanceCommandStatsList.addAll(map.get(ratioCommand));


            Map<Long, Map<String, Object>> total = new HashMap<Long, Map<String, Object>>();
            for (InstanceMinuteStats instanceMinuteStats : instanceCommandStatsList) {
                //用timestamp作为key,保证在一个Map统计里
                long timestamp = instanceMinuteStats.getTimeStamp();
                double memFragRatio = instanceMinuteStats.getMemFragmentationRatio();
                String command = instanceMinuteStats.getCommandName();
                //精简字段
                command = commandMap.get(command);
                if (total.containsKey(timestamp)) {
                    Map<String, Object> tmpMap = total.get(timestamp);
                    tmpMap.put(command, memFragRatio);
                } else {
                    Map<String, Object> tmpMap = new HashMap<String, Object>();
                    tmpMap.put("t", timestamp);
                    tmpMap.put(command, memFragRatio);
                    total.put(timestamp, tmpMap);
                    instanceMemFragRatioStatList.add(tmpMap);
                }
            }
        }

        String result = JSONObject.toJSONString(appInstancesMemFragRatioStatList);
        logger.warn("parse Json cost:{} ms", (System.currentTimeMillis() - start));
        write(response, result);
    }

    /**
     * 获取指定时间内某个应用全部实例的过期/淘汰键统计信息
     *
     * @param appId
     */
    @RequestMapping("/appInstanceExpiredEvictedKeysStat")
    public ModelAndView appInstanceExpiredEvictedKeysStat(HttpServletRequest request, Model model,
                                                    Long appId) throws ParseException {
        // 应用基本信息
        AppDesc appDesc = appService.getByAppId(appId);
        model.addAttribute("appDesc", appDesc);
        model.addAttribute("appId", appId);
        // 日期格式转换
        getTimeBetween(request, model, "startDate", "endDate");
        return new ModelAndView("app/appInstanceExpiredEvictedKeysStat");
    }

    /**
     * 获取指定时间内某个应用全部实例的过期/淘汰键统计信息
     * @param appId 应用流量
     */
    @RequestMapping("/getAppInstancesExpiredEvictedKeysStat")
    public void getAppInstancesExpiredEvictedKeysStat(HttpServletRequest request, HttpServletResponse response, Model model,
                                                Long appId) throws ParseException {
        //时间转换
        TimeBetween timeBetween = getJsonTimeBetween(request);

        //缩减字段
        String expiredKeysCommand = "expired_keys";
        String evictedKeysCommand = "evicted_keys";
        Map<String, String> commandMap = Maps.newHashMap();
        commandMap.put(expiredKeysCommand, "exkey");
        commandMap.put(evictedKeysCommand, "evkey");

        long start = System.currentTimeMillis();
        //获取应用下所有实例过期/淘汰键
        Table<Integer, String, Map<String, List<InstanceMinuteStats>>> table = instanceStatsCenter
                .getInstanceMinuteStatsList(appId, timeBetween.getStartTime(), timeBetween.getEndTime(),
                        Arrays.asList(expiredKeysCommand, evictedKeysCommand));
        logger.warn("getInstanceMinuteStatsList cost:{} ms", (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();

        //解析成json数组
        List<Map<String, Object>> appInstancesExpiredEvictedKeysStatList = new ArrayList<Map<String, Object>>();
        for (Table.Cell<Integer, String, Map<String, List<InstanceMinuteStats>>> cell : table.cellSet()) {
            Integer instanceId = cell.getRowKey();

            //实例基本信息
            Map<String, Object> instanceStatMap = new HashMap<String, Object>();
            instanceStatMap.put("instanceId", instanceId);
            instanceStatMap.put("instanceInfo", cell.getColumnKey());

            //每个实例的统计信息
            List<Map<String, Object>> instanceExpiredEvictedKeysStatList = new ArrayList<Map<String, Object>>();
            instanceStatMap.put("instanceExpiredEvictedKeysStatMapList", instanceExpiredEvictedKeysStatList);
            appInstancesExpiredEvictedKeysStatList.add(instanceStatMap);

            //记录过期、淘汰键
            Map<String, List<InstanceMinuteStats>> map = cell.getValue();
            List<InstanceMinuteStats> instanceCommandStatsList = new ArrayList<>();
            instanceCommandStatsList.addAll(map.get(expiredKeysCommand));
            instanceCommandStatsList.addAll(map.get(evictedKeysCommand));

            Map<Long, Map<String, Object>> total = new HashMap<Long, Map<String, Object>>();
            for (InstanceMinuteStats instanceMinuteStats : instanceCommandStatsList) {
                //用timestamp作为key,保证在一个Map统计里
                long timestamp = instanceMinuteStats.getTimeStamp();
                double memFragRatio = instanceMinuteStats.getMemFragmentationRatio();
                String command = instanceMinuteStats.getCommandName();
                //精简字段
                command = commandMap.get(command);
                if (total.containsKey(timestamp)) {
                    Map<String, Object> tmpMap = total.get(timestamp);
                    tmpMap.put(command, memFragRatio);
                } else {
                    Map<String, Object> tmpMap = new HashMap<String, Object>();
                    tmpMap.put("t", timestamp);
                    tmpMap.put(command, memFragRatio);
                    total.put(timestamp, tmpMap);
                    instanceExpiredEvictedKeysStatList.add(tmpMap);
                }
            }
        }

        String result = JSONObject.toJSONString(appInstancesExpiredEvictedKeysStatList);
        logger.warn("parse Json cost:{} ms", (System.currentTimeMillis() - start));
        write(response, result);
    }

    /**
     * 获取指定时间内某个应用全部实例的统计信息
     *
     * @param appId 应用流量
     */
    @RequestMapping("/getAppInstancesNetStat")
    public ModelAndView getAppInstancesNetStat(HttpServletRequest request, HttpServletResponse response, Model model,
                                               Long appId) throws ParseException {
        //时间转换
        TimeBetween timeBetween = getJsonTimeBetween(request);

        //缩减字段
        String netInCommand = "total_net_input_bytes";
        String netOutCommand = "total_net_output_bytes";
        Map<String, String> commandMap = new HashMap<String, String>();
        commandMap.put(netInCommand, "i");
        commandMap.put(netOutCommand, "o");

        //获取应用下所有实例网络流量统计
        Map<Integer, Map<String, List<InstanceCommandStats>>> appInstancesNetStat = instanceStatsCenter
                .getStandardStatsList(appId, timeBetween.getStartTime(), timeBetween.getEndTime(),
                        Arrays.asList(netInCommand, netOutCommand));

        //解析成json数组
        List<Map<String, Object>> appInstancesNetStatList = new ArrayList<Map<String, Object>>();
        for (Entry<Integer, Map<String, List<InstanceCommandStats>>> entry : appInstancesNetStat.entrySet()) {
            Integer instanceId = entry.getKey();

            //实例基本信息
            Map<String, Object> instanceStatMap = new HashMap<String, Object>();
            instanceStatMap.put("instanceId", instanceId);
            InstanceInfo instanceInfo = instanceStatsCenter.getInstanceInfo(instanceId);
            instanceStatMap.put("instanceInfo", instanceInfo.getIp() + ":" + instanceInfo.getPort());

            //每个实例的统计信息
            List<Map<String, Object>> instanceNetStatMapList = new ArrayList<Map<String, Object>>();
            instanceStatMap.put("instanceNetStatMapList", instanceNetStatMapList);
            appInstancesNetStatList.add(instanceStatMap);

            //记录输入和输出流量
            Map<String, List<InstanceCommandStats>> map = entry.getValue();
            List<InstanceCommandStats> instanceCommandStatsList = new ArrayList<InstanceCommandStats>();
            instanceCommandStatsList.addAll(map.get(netInCommand));
            instanceCommandStatsList.addAll(map.get(netOutCommand));

            Map<Long, Map<String, Object>> total = new HashMap<Long, Map<String, Object>>();
            for (InstanceCommandStats instanceCommandStat : instanceCommandStatsList) {
                //用timestamp作为key,保证输入和输出流量在一个Map统计里
                long timestamp = instanceCommandStat.getTimeStamp();
                long commandCount = instanceCommandStat.getCommandCount();
                String command = instanceCommandStat.getCommandName();
                //精简字段
                command = commandMap.get(command);
                if (total.containsKey(timestamp)) {
                    Map<String, Object> tmpMap = total.get(timestamp);
                    tmpMap.put(command, commandCount);
                } else {
                    Map<String, Object> tmpMap = new HashMap<String, Object>();
                    tmpMap.put("t", timestamp);
                    tmpMap.put(command, commandCount);
                    total.put(timestamp, tmpMap);
                    instanceNetStatMapList.add(tmpMap);
                }
            }
        }

        String result = JSONObject.toJSONString(appInstancesNetStatList);
        write(response, result);
        return null;
    }

    /**
     * @param appId
     * @throws ParseException
     */
    @RequestMapping("/getTop5Commands")
    public ModelAndView getAppTop5Commands(HttpServletRequest request,
                                           HttpServletResponse response, Model model, Long appId) throws ParseException {
        TimeBetween timeBetween = getJsonTimeBetween(request);
        List<AppCommandStats> appCommandStats = appStatsCenter
                .getTop5AppCommandStatsList(appId, timeBetween.getStartTime(), timeBetween.getEndTime());
        String result = assembleJson(appCommandStats);
        write(response, result);
        return null;
    }

    /**
     * 应用各个命令分布情况
     *
     * @param appId 应用id
     * @throws ParseException
     */
    @RequestMapping("/appCommandDistribute")
    public ModelAndView appCommandDistribute(HttpServletRequest request,
                                             HttpServletResponse response, Model model, Long appId) throws ParseException {
        TimeBetween timeBetween = getJsonTimeBetween(request);
        List<AppCommandGroup> appCommandGroupList = appStatsCenter
                .getAppCommandGroup(appId, timeBetween.getStartTime(), timeBetween.getEndTime());
        String result = assembleGroupJson(appCommandGroupList);
        write(response, result);
        return null;
    }

    /**
     * 应用列表
     */
    @RequestMapping(value = "/list")
    public ModelAndView doAppList(HttpServletRequest request,
                                  HttpServletResponse response, Model model, String appParam, AppSearch appSearch, String userId) {
        // 1.获取该用户能够读取的应用列表,没有返回申请页面
        AppUser currentUser = getUserInfo(request);
        model.addAttribute("currentUser", currentUser);
        int userAppCount = appService.getUserAppCount(currentUser.getId());
        if (userAppCount == 0 && !AppUserTypeEnum.ADMIN_USER.value().equals(currentUser.getType())) {
            return new ModelAndView("redirect:/admin/app/init");
        }
        // 2.0 默认只出运行中的
        if (appSearch.getAppStatus() == null) {
            appSearch.setAppStatus(AppStatusEnum.STATUS_PUBLISHED.getStatus());
        }
        // 2.1 查询指定时间客户端异常
        if (!StringUtils.isEmpty(appParam)) {
            if (StringUtils.isNumeric(appParam)) {
                appSearch.setAppId(Long.parseLong(appParam));
            } else {
                appSearch.setAppName(appParam);
            }
        }
        if (StringUtils.isNotEmpty(userId)) {
            appSearch.setUserId(NumberUtils.toLong(userId));
        }
        // 2.2 分页相关
        int totalCount = appService.getAppDescCount(currentUser, appSearch);
        int pageNo = NumberUtils.toInt(request.getParameter("pageNo"), 1);
        int pageSize = NumberUtils.toInt(request.getParameter("pageSize"), 10);
        Page page = new Page(pageNo, pageSize, totalCount);
        model.addAttribute("page", page);
        appSearch.setPage(page);

        List<AppDesc> apps = appService.getAppDescList(currentUser, appSearch);
        // 2.3 应用列表
        List<AppDetailVO> appDetailList = new ArrayList<AppDetailVO>();
        model.addAttribute("appDetailList", appDetailList);

        // 3.Redis版本信息
        List<SystemResource> resourcelist = resourceService.getResourceList(ResourceEnum.REDIS.getValue());

        // 4. 全局统计
        long totalApplyMem = 0;
        long totalUsedMem = 0;
        long totalApps = 0;
        if (apps != null && apps.size() > 0) {
            for (AppDesc appDesc : apps) {
                AppDetailVO appDetail = appStatsCenter.getAppDetail(appDesc.getAppId());
                appDetailList.add(appDetail);
                totalApplyMem += appDetail.getMem();
                totalUsedMem += appDetail.getMemUsePercent() * appDetail.getMem() / 100.0;
                totalApps++;
            }
        }

        List<AppUser> userList = userService.getAllUser();

        model.addAttribute("userList", userList);
        model.addAttribute("appParam", appParam);
        model.addAttribute("resourcelist", resourcelist);
        model.addAttribute("totalApps", totalApps);
        model.addAttribute("totalApplyMem", totalApplyMem);
        model.addAttribute("totalUsedMem", totalUsedMem);

        return new ModelAndView("app/appList");
    }

    /**
     * 初始化应用申请
     */
    @RequestMapping(value = "/init")
    public ModelAndView doAppInit(HttpServletRequest request,
                                  HttpServletResponse response, Model model) {
        List<AppUser> userList = userService.getAllUser();
        List<MachineRoom> roomList = machineCenter.getEffectiveRoom();
        List<SystemResource> versionList = resourceService.getResourceList(ResourceEnum.REDIS.getValue());
        //获取插件信息
        List<ModuleInfo> allModules = moduleService.getAllModules();
        model.addAttribute("userList", userList);
        model.addAttribute("roomList", roomList);
        model.addAttribute("versionList", versionList);
        model.addAttribute("allModules", allModules);

        return new ModelAndView("app/jobIndex/appInitIndex");
    }

    @RequestMapping(value = "/import")
    public ModelAndView doAppImport(HttpServletRequest request,
                                    HttpServletResponse response, Model model) {
        List<AppUser> userList = userService.getAllUser();
        List<MachineRoom> roomList = machineCenter.getEffectiveRoom();
        List<SystemResource> versionList = resourceService.getResourceList(ResourceEnum.REDIS.getValue());
        model.addAttribute("userList", userList);
        model.addAttribute("roomList", roomList);
        model.addAttribute("versionList", versionList);

        return new ModelAndView("app/jobIndex/appImportIndex");
    }

    @RequestMapping(value = "/jobs")
    public ModelAndView doAppJobs(HttpServletRequest request,
                                  HttpServletResponse response, Model model, Long appId,
                                  Integer status, Integer type) {
        model.addAttribute("appId", appId);
        AppUser currentUser = getUserInfo(request);
        List<AppAudit> jobList = appService.getAppAudits(4, type, null, currentUser.getId(), null);
        Map<String, Object> statusStatisMap = appService.getStatisticGroupByStatus(currentUser.getId(), null, null, null);
        Map<String, Object> typeStatisMap = appService.getStatisticGroupByType(currentUser.getId(), null, null, null);
        List<AppUser> adminList = appUserDao.getAdminList();
        model.addAttribute("adminMap", adminList.stream().collect(Collectors.toMap(AppUser::getId, Function.identity())));

        AppAuditType[] appAuditTypes = AppAuditType.values();
        model.addAttribute("appAuditTypeMap", Arrays.stream(appAuditTypes).collect(Collectors.toMap(AppAuditType::getValue, Function.identity())));

        model.addAttribute("statusStatisMap", statusStatisMap);
        model.addAttribute("typeStatisMap", typeStatisMap);
        model.addAttribute("jobList", jobList);
        model.addAttribute("status", status);
        model.addAttribute("type", type);
        return new ModelAndView("app/jobIndex/index");
    }


    @RequestMapping(value = "/appKeyAnalysis")
    public ModelAndView doKeyAnalysis(HttpServletRequest request,
                                      HttpServletResponse response, Model model, Long appId,
                                      Integer status, Integer type) {
        model.addAttribute("appId", appId);
        if (appId != null) {
            List<InstanceInfo> instanceInfos = appService.getAppOnlineInstanceInfo(appId);
            model.addAttribute("appInstanceList", instanceInfos);
        }

        AppUser currentUser = getUserInfo(request);
        List<AppDesc> appDescList = appService.getAppDescList(currentUser, new AppSearch());
        appDescList.forEach(appDesc -> {
            String versionName = Optional.ofNullable(resourceService.getResourceById(appDesc.getVersionId())).map(ver -> ver.getName()).orElse("");
            appDesc.setVersionName(versionName);
        });
        Map<Long, AppDesc> appDescMap = appDescList.stream().collect(Collectors.toMap(AppDesc::getAppId, Function.identity()));
        model.addAttribute("appDescMap", appDescMap);

        return new ModelAndView("app/jobIndex/appKeyAnalysisIndex");
    }


    @RequestMapping(value = "/appScale")
    public ModelAndView doAppScale(HttpServletRequest request,
                                   HttpServletResponse response, Model model, Long appId) {
        model.addAttribute("appId", appId);

        AppUser currentUser = getUserInfo(request);
        List<AppDesc> appDescList = appService.getAppDescList(currentUser, new AppSearch());
        appDescList.forEach(appDesc -> {
            String versionName = Optional.ofNullable(resourceService.getResourceById(appDesc.getVersionId())).map(ver -> ver.getName()).orElse("");
            appDesc.setVersionName(versionName);
        });
        Map<Long, AppDesc> appDescMap = appDescList.stream().collect(Collectors.toMap(AppDesc::getAppId, Function.identity()));
        model.addAttribute("appDescMap", appDescMap);

        return new ModelAndView("app/jobIndex/appScaleIndex");
    }


    @RequestMapping(value = "/appDiagnostic")
    public ModelAndView doAppDiagnostic(HttpServletRequest request,
                                        HttpServletResponse response, Model model, Long appId) {
        model.addAttribute("appId", appId);

        AppUser currentUser = getUserInfo(request);
        List<AppDesc> appDescList = appService.getAppDescList(currentUser, new AppSearch());
        appDescList.forEach(appDesc -> {
            String versionName = Optional.ofNullable(resourceService.getResourceById(appDesc.getVersionId())).map(ver -> ver.getName()).orElse("");
            appDesc.setVersionName(versionName);
        });
        Map<Long, AppDesc> appDescMap = appDescList.stream().collect(Collectors.toMap(AppDesc::getAppId, Function.identity()));
        model.addAttribute("appDescMap", appDescMap);

        Map<Integer, String> diagnosticTypeMap = new HashMap<>();
        for (DiagnosticTypeEnum diagnosticType : DiagnosticTypeEnum.values()) {
            diagnosticTypeMap.put(diagnosticType.getType(), diagnosticType.getDesc() + ": " + diagnosticType.getMore());
        }
        model.addAttribute("diagnosticTypeMap", diagnosticTypeMap);

        return new ModelAndView("app/jobIndex/appDiagnosticIndex");
    }

    @RequestMapping(value = "/appDel")
    public ModelAndView doAppDel(HttpServletRequest request,
                                 HttpServletResponse response, Model model, Long appId) {
        model.addAttribute("appId", appId);

        AppUser currentUser = getUserInfo(request);
        List<AppDesc> appDescList = appService.getAppDescList(currentUser, new AppSearch());
        appDescList.forEach(appDesc -> {
            String versionName = Optional.ofNullable(resourceService.getResourceById(appDesc.getVersionId())).map(ver -> ver.getName()).orElse("");
            appDesc.setVersionName(versionName);
        });
        Map<Long, AppDesc> appDescMap = appDescList.stream().collect(Collectors.toMap(AppDesc::getAppId, Function.identity()));
        model.addAttribute("appDescMap", appDescMap);

        return new ModelAndView("app/jobIndex/appCleanIndex");
    }

    @RequestMapping(value = "/appOffline")
    public ModelAndView doAppOffline(HttpServletRequest request,
                                     HttpServletResponse response, Model model, Long appId) {
        model.addAttribute("appId", appId);

        AppUser currentUser = getUserInfo(request);
        List<AppDesc> appDescList = appService.getAppDescList(currentUser, new AppSearch());
        appDescList.forEach(appDesc -> {
            String versionName = Optional.ofNullable(resourceService.getResourceById(appDesc.getVersionId())).map(ver -> ver.getName()).orElse("");
            appDesc.setVersionName(versionName);
        });

        Map<Long, AppDesc> appDescMap = appDescList.stream().collect(Collectors.toMap(AppDesc::getAppId, Function.identity()));
        model.addAttribute("appDescMap", appDescMap);

        return new ModelAndView("app/jobIndex/appOfflineIndex");
    }


    @RequestMapping(value = "/appDataMigrate")
    public ModelAndView doAppDataMigrate(HttpServletRequest request,
                                         HttpServletResponse response, Model model, Long appId) {
        model.addAttribute("appId", appId);

        AppUser currentUser = getUserInfo(request);
        List<AppDesc> appDescList = appService.getAppDescList(currentUser, new AppSearch());
        appDescList.forEach(appDesc -> {
            String versionName = Optional.ofNullable(resourceService.getResourceById(appDesc.getVersionId())).map(ver -> ver.getName()).orElse("");
            appDesc.setVersionName(versionName);
        });

        Map<Long, AppDesc> appDescMap = appDescList.stream().collect(Collectors.toMap(AppDesc::getAppId, Function.identity()));
        model.addAttribute("appDescMap", appDescMap);

        return new ModelAndView("app/jobIndex/appDataMigrateIndex");
    }

    @RequestMapping(value = "/appConfig")
    public ModelAndView doAppConfig(HttpServletRequest request,
                                    HttpServletResponse response, Model model, Long appId, Long instanceId) {
        model.addAttribute("appId", appId);
        int first_instanceId = -1;
        if (appId != null) {
            List<InstanceInfo> instanceInfos = appService.getAppOnlineInstanceInfo(appId);
            model.addAttribute("appInstanceList", instanceInfos);
            first_instanceId = CollectionUtils.isNotEmpty(instanceInfos) && instanceInfos.size() > 0 ? instanceInfos.get(0).getId() : -1;
        }
        if (instanceId != null) {
            model.addAttribute("instanceId", instanceId);
            first_instanceId = instanceId.intValue();
        }
        Map<String, String> redisConfigMap = redisCenter.getRedisConfigList(first_instanceId);
        model.addAttribute("redisConfigMap", redisConfigMap);

        AppUser currentUser = getUserInfo(request);
        List<AppDesc> appDescList = appService.getAppDescList(currentUser, new AppSearch());
        appDescList.forEach(appDesc -> {
            String versionName = Optional.ofNullable(resourceService.getResourceById(appDesc.getVersionId())).map(ver -> ver.getName()).orElse("");
            appDesc.setVersionName(versionName);
        });
        Map<Long, AppDesc> appDescMap = appDescList.stream().collect(Collectors.toMap(AppDesc::getAppId, Function.identity()));
        model.addAttribute("appDescMap", appDescMap);

        return new ModelAndView("app/jobIndex/appConfigIndex");
    }

    @RequestMapping("/redisConfig")
    public ModelAndView redisConfig(HttpServletRequest request,
                                    HttpServletResponse response, Long appId, Integer instanceId, Model model) {
        JSONObject json = new JSONObject();
        if (instanceId == null) {
            List<InstanceInfo> instanceInfos = appService.getAppOnlineInstanceInfo(appId);
            instanceId = CollectionUtils.isNotEmpty(instanceInfos) && instanceInfos.size() > 0 ? instanceInfos.get(0).getId() : -1;
        }
        Map<String, String> redisConfigMap = redisCenter.getRedisConfigList(instanceId.intValue());


        if (MapUtils.isEmpty(redisConfigMap)) {
            json.put("status", String.valueOf(SuccessEnum.FAIL.value()));
        } else {
            json.put("redisConfigMap", redisConfigMap);
            json.put("status", String.valueOf(SuccessEnum.SUCCESS.value()));
        }
        sendMessage(response, json.toString());
        return null;
    }

    @RequestMapping(value = "/appAlterConfig")
    public ModelAndView doAppAlterConfig(HttpServletRequest request,
                                         HttpServletResponse response, Model model, Long appId) {
        model.addAttribute("appId", appId);

        AppUser currentUser = getUserInfo(request);
        List<AppDesc> appDescList = appService.getAppDescList(currentUser, new AppSearch());
        appDescList.forEach(appDesc -> {
            String versionName = Optional.ofNullable(resourceService.getResourceById(appDesc.getVersionId())).map(ver -> ver.getName()).orElse("");
            appDesc.setVersionName(versionName);
        });
        Map<Long, AppDesc> appDescMap = appDescList.stream().collect(Collectors.toMap(AppDesc::getAppId, Function.identity()));
        model.addAttribute("appDescMap", appDescMap);

        return new ModelAndView("app/jobIndex/appAlterConfigIndex");
    }


    @RequestMapping(value = "/appScanClean")
    public ModelAndView doAppScanClean(HttpServletRequest request,
                                         HttpServletResponse response, Model model, Long appId) {
        model.addAttribute("appId", appId);

        AppUser currentUser = getUserInfo(request);
        List<AppDesc> appDescList = appService.getAppDescList(currentUser, new AppSearch());
        appDescList.forEach(appDesc -> {
            String versionName = Optional.ofNullable(resourceService.getResourceById(appDesc.getVersionId())).map(ver -> ver.getName()).orElse("");
            appDesc.setVersionName(versionName);
        });

        Map<Long, AppDesc> appDescMap = appDescList.stream().collect(Collectors.toMap(AppDesc::getAppId, Function.identity()));
        model.addAttribute("appDescMap", appDescMap);

        return new ModelAndView("app/jobIndex/appScanCleanIndex");
    }



    @RequestMapping(value = "/job/submit")
    public ModelAndView submitJobApplication(HttpServletRequest request,
                                             HttpServletResponse response, Model model,
                                             Long appId, String nodeInfos, int jobType, String reason, String param) {
        try {
            AppUser appUser = getUserInfo(request);
            AppDesc appDesc = appService.getByAppId(appId);

            AppAudit appAudit = new AppAudit();
            appAudit.setAppId(appDesc.getAppId());
            appAudit.setUserId(appUser.getId());
            appAudit.setUserName(appUser.getName());
            appAudit.setModifyTime(new Date());
            String info = "申请原因: " + reason;
            if (StringUtils.isNotBlank(nodeInfos)) {
                info += "，节点: " + nodeInfos;
                appAudit.setParam1(nodeInfos);
            }
            if (StringUtils.isNotBlank(param)) {
                info += "，其他: " + param;
                appAudit.setParam2(param);
            }
            appAudit.setInfo(info);
            appAudit.setStatus(AppCheckEnum.APP_WATING_CHECK.value());
            appAudit.setType(jobType);
            Date now = new Date();
            appAudit.setCreateTime(now);
            appAudit.setModifyTime(now);
            appAuditDao.insertAppAudit(appAudit);

            // 保存日志
//            AppAuditLog appAuditLog = AppAuditLog.generate(appDesc, appUser, appAudit.getId(),
//                    AppAuditLogTypeEnum.KEY_VALUE_ANALYSIS);
//            if (appAuditLog != null) {
//                appAuditLogDao.save(appAuditLog);
//            }

            write(response, String.valueOf(SuccessEnum.SUCCESS.value()));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            write(response, String.valueOf(SuccessEnum.FAIL.value()));
        }
        return null;
    }


    @RequestMapping(value = "import/submit", method = RequestMethod.POST)
    public ModelAndView doAppImportSubmit(HttpServletRequest request,
                                          HttpServletResponse response, Model model, AppDesc appDesc, String memSize) {
        AppUser appUser = getUserInfo(request);

        //创建appImport
        AppImport appImport = new AppImport();
        //appDesc入库
        if (appDesc != null) {
            Timestamp now = new Timestamp(new Date().getTime());
            appDesc.setCreateTime(now);
            appDesc.setPassedTime(now);
            appDesc.setVerId(1);
            appDesc.setStatus((short) AppStatusEnum.STATUS_INITIALIZE.getStatus());
            appDesc.setHitPrecentAlertValue(0);
            appDesc.setIsAccessMonitor(AppUserAlertEnum.NO.value());
            appService.save(appDesc);
            // 保存应用和用户的关系
            String officers = appDesc.getOfficer();
            if (!StringUtils.isEmpty(officers)) {
                for (String officerId : officers.split(",")) {
                    if (!StringUtils.isEmpty(officerId)) {
                        appService.saveAppToUser(appDesc.getAppId(), Long.parseLong(officerId));
                    }
                }
            }
            // 更新appKey
            long appId = appDesc.getAppId();
            appService.updateAppKey(appId);
        }
        appImport.setAppId(appDesc.getAppId());
        appImport.setMemSize(NumberUtils.toInt(memSize));
        appImport.setSourceType(NumberUtils.toInt(request.getParameter("sourceType")));
        appImport.setInstanceInfo(request.getParameter("appInstanceInfo"));
        appImport.setRedisPassword(request.getParameter("password"));
        appImport.setStatus(0);
        appImportDao.save(appImport);

        //appAudit入库
        AppAudit appAudit = new AppAudit();
        appAudit.setAppId(appDesc.getAppId());
        appAudit.setUserId(appUser.getId());
        appAudit.setUserName(appUser.getName());
        appAudit.setInfo("迁移到应用：" + appDesc.getAppId() + " " + appDesc.getName());
        appAudit.setModifyTime(new Date());
        appAudit.setStatus(AppCheckEnum.APP_WATING_CHECK.value());
        appAudit.setType(AppAuditType.APP_IMPORT.getValue());
        appAudit.setParam1(String.valueOf(appImport.getId()));
        Date now = new Date();
        appAudit.setCreateTime(now);
        appAudit.setModifyTime(now);
        appAuditDao.insertAppAudit(appAudit);

        return new ModelAndView("redirect:/admin/app/jobs");
    }

    /**
     * 添加应用
     *
     * @param appDesc 应用实体
     * @param memSize 申请容量(G)
     * @return
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public ModelAndView doAppAdd(HttpServletRequest request,
                                 HttpServletResponse response, Model model, AppDesc appDesc, String memSize, String isInstall, String moduleInfo) {
        AppUser appUser = getUserInfo(request);
        logger.info("isInstall:{} moduleInfo:{}", isInstall, moduleInfo);
        if (appDesc != null) {
            Timestamp now = new Timestamp(new Date().getTime());
            appDesc.setCreateTime(now);
            appDesc.setPassedTime(now);
            appDesc.setVerId(1);
            appDesc.setStatus((short) AppStatusEnum.STATUS_ALLOCATED.getStatus());
            // 设置命中率报警0,默认不监控
            appDesc.setHitPrecentAlertValue(0);
            // 客户端默认关闭监控
            appDesc.setIsAccessMonitor(AppUserAlertEnum.NO.value());
            appDeployCenter.createApp(appDesc, appUser, memSize, isInstall, moduleInfo);
        }
        return new ModelAndView("redirect:/admin/app/jobs");
    }

    /**
     * 查看应用名是否存在
     *
     * @param appName
     * @return
     */
    @RequestMapping(value = "/checkAppNameExist")
    public ModelAndView doCheckAppNameExist(HttpServletRequest request,
                                            HttpServletResponse response, Model model, String appName) {
        AppDesc appDesc = appService.getAppByName(appName);
        if (appDesc != null) {
            write(response, String.valueOf(SuccessEnum.SUCCESS.value()));
        } else {
            write(response, String.valueOf(SuccessEnum.FAIL.value()));
        }
        return null;
    }

    /**
     * 应用命令查询
     *
     * @param appId
     * @return
     */
    @RequestMapping("/command")
    public ModelAndView command(HttpServletRequest request, HttpServletResponse response, Model model, Long appId) {
        if (appId != null && appId > 0) {
            model.addAttribute("appId", appId);
        }
        return new ModelAndView("app/appCommand");
    }

    /**
     * 执行应用命令
     *
     * @param appId
     * @return
     */
    @RequestMapping("/commandExecute")
    public ModelAndView commandExecute(HttpServletRequest request, HttpServletResponse response, Model model,
                                       Long appId) {
        AppUser currentUser = getUserInfo(request);
        if (appId != null && appId > 0) {
            model.addAttribute("appId", appId);
            String command = request.getParameter("command");
            String result = appStatsCenter.executeCommand(appId, command, currentUser != null ? currentUser.getName() : null);
            model.addAttribute("result", result);
        } else {
            model.addAttribute("result", "error");
        }
        return new ModelAndView("app/commandExecute");
    }

    /**
     * 删除应用下的指定用户
     *
     * @param userId
     * @param appId
     * @return
     */
    @RequestMapping(value = "/deleteAppToUser")
    public ModelAndView doDeleteAppToUser(HttpServletRequest request,
                                          HttpServletResponse response, Model model, Long userId, Long appId) {
        if (userId != null && appId != null) {
            // 验证删除权限
            AppUser currentUser = getUserInfo(request);
            List<AppToUser> appToUsers = appService.getAppToUserList(appId);
            if (CollectionUtils.isNotEmpty(appToUsers)) {
                for (AppToUser appToUser : appToUsers) {
                    if (appToUser.getUserId().equals(currentUser.getId())) {
                        write(response, String.valueOf(SuccessEnum.FAIL.value()));
                    }
                }
            }
            appService.deleteAppToUser(appId, userId);
            write(response, String.valueOf(SuccessEnum.SUCCESS.value()));
        } else {
            write(response, String.valueOf(SuccessEnum.FAIL.value()));
        }
        return null;
    }

    /**
     * 更新用户
     *
     * @param name
     * @param chName
     * @param email
     * @param mobile
     * @param weChat
     * @param type
     * @param userId
     * @return
     */
    @RequestMapping(value = "/changeAppUserInfo")
    public ModelAndView doAddUser(HttpServletRequest request,
                                  HttpServletResponse response, Model model, String name, String chName, String email, String mobile,
                                  String weChat,
                                  Integer type, Integer isAlert, Long userId, String company, String purpose) {
        // 后台暂时不对参数进行验证
        AppUser appUser = AppUser.buildFrom(userId, name, chName, email, mobile, weChat, type, isAlert, company, purpose);
        try {
            if (userId == null) {
                appUser.setPassword(ConstUtils.DEFAULT_USER_PASSWORD);
                userService.save(appUser);
            } else {
                userService.update(appUser);
            }
            write(response, String.valueOf(SuccessEnum.SUCCESS.value()));
        } catch (Exception e) {
            write(response, String.valueOf(SuccessEnum.FAIL.value()));
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 扩容申请
     *
     * @param appId          应用id
     * @param applyMemSize   申请容量
     * @param appScaleReason 申请原因
     * @return
     */
    @RequestMapping(value = "/scale")
    public ModelAndView doScaleApp(HttpServletRequest request,
                                   HttpServletResponse response, Model model, Long appId, String applyMemSize, String appScaleReason) {
        AppUser appUser = getUserInfo(request);
        AppDesc appDesc = appService.getByAppId(appId);
        AppAudit appAudit = appService
                .saveAppScaleApply(appDesc, appUser, applyMemSize, appScaleReason, AppAuditType.APP_SCALE);
        appEmailUtil.noticeAppResult(appDesc, appAudit);
        write(response, String.valueOf(SuccessEnum.SUCCESS.value()));
        return null;
    }

    /**
     * 应用修改配置申请
     *
     * @param appId          应用id
     * @param appConfigKey   配置项
     * @param appConfigValue 配置值
     * @return
     */
    @RequestMapping(value = "/changeAppConfig")
    public ModelAndView doChangeAppConfig(HttpServletRequest request,
                                          HttpServletResponse response, Model model, Long appId, Long instanceId, String appConfigKey,
                                          String appConfigValue, String appConfigReason) {
        AppUser appUser = getUserInfo(request);
        AppDesc appDesc = appService.getByAppId(appId);
        AppAudit appAudit = appService
                .saveAppChangeConfig(appDesc, appUser, instanceId, appConfigKey, appConfigValue, appConfigReason,
                        AppAuditType.APP_MODIFY_CONFIG);
        appEmailUtil.noticeAppResult(appDesc, appAudit);
        write(response, String.valueOf(SuccessEnum.SUCCESS.value()));
        return null;
    }

    /**
     * <p>
     * Description: 全局报警项修改
     * </p>
     *
     * @param
     * @return
     * @author chenshi
     * @version 1.0
     * @date 2017/9/25
     */
    @RequestMapping(value = "/changeAppMonitorConfig")
    public ModelAndView doChangeMonitorAppConfig(HttpServletRequest request,
                                                 HttpServletResponse response, Model model, Long appId, Long instanceId, String appConfigKey,
                                                 String appConfigValue, String appConfigReason) {
        AppUser appUser = getUserInfo(request);
        AppDesc appDesc = appService.getByAppId(appId);
        AppAudit appAudit = appService
                .saveAppChangeConfig(appDesc, appUser, instanceId, appConfigKey, appConfigValue, appConfigReason,
                        AppAuditType.APP_MODIFY_CONFIG);
        appEmailUtil.noticeAppResult(appDesc, appAudit);
        write(response, String.valueOf(SuccessEnum.SUCCESS.value()));
        return null;
    }

    /**
     * 实例修改配置申请
     *
     * @param appId               应用id
     * @param instanceConfigKey   配置项
     * @param instanceConfigValue 配置值
     * @return
     */
    @RequestMapping(value = "/changeInstanceConfig")
    public ModelAndView doChangeInstanceConfig(HttpServletRequest request,
                                               HttpServletResponse response, Model model, Long appId, Long instanceId, String instanceConfigKey,
                                               String instanceConfigValue, String instanceConfigReason) {
        AppUser appUser = getUserInfo(request);
        AppDesc appDesc = appService.getByAppId(appId);
        AppAudit appAudit = appService
                .saveInstanceChangeConfig(appDesc, appUser, instanceId, instanceConfigKey, instanceConfigValue,
                        instanceConfigReason, AppAuditType.INSTANCE_MODIFY_CONFIG);
        appEmailUtil.noticeAppResult(appDesc, appAudit);
        write(response, String.valueOf(SuccessEnum.SUCCESS.value()));
        return null;
    }

    /**
     * 添加应用和用户对应关系
     *
     * @param appId 应用id
     * @param users 用户id(邮箱前缀)
     * @returns
     */
    @RequestMapping(value = "/addAppToUser")
    public ModelAndView doAddAppToUser(HttpServletRequest request,
                                       HttpServletResponse response, Model model, Long appId, String users) {
        if (StringUtils.isNotBlank(users)) {
            List<String> userIdList = Arrays.asList(users.split(","));
            for (String userIdStr : userIdList) {
                if (!appService.saveAppToUser(appId, NumberUtils.toLong(userIdStr, -1l))) {
                    write(response, String.valueOf(SuccessEnum.FAIL.value()));
                }
            }
            write(response, String.valueOf(SuccessEnum.SUCCESS.value()));
        }
        return null;
    }

    /**
     * 修改应用报警配置
     */
    @RequestMapping(value = "/changeAppAlertConfig")
    public ModelAndView doChangeAppAlertConfig(HttpServletRequest request,
                                               HttpServletResponse response, Model model) {

        long appId = NumberUtils.toLong(request.getParameter("appId"), -1);
        int memAlertValue = NumberUtils.toInt(request.getParameter("memAlertValue"), -1);
        int clientConnAlertValue = NumberUtils.toInt(request.getParameter("clientConnAlertValue"), -1);
        int hitPrecentAlertValue = NumberUtils.toInt(request.getParameter("hitPrecentAlertValue"), 0);
        int isAccessMonitor = NumberUtils.toInt(request.getParameter("isAccessMonitor"), 0);
        SuccessEnum result = appService
                .changeAppAlertConfig(appId, memAlertValue, clientConnAlertValue, hitPrecentAlertValue, isAccessMonitor,
                        getUserInfo(request));
        write(response, String.valueOf(result.value()));
        return null;
    }

    /**
     * 修改应用信息
     */
    @RequestMapping(value = "/updateAppDetail")
    public ModelAndView doUpdateAppDetail(HttpServletRequest request,
                                          HttpServletResponse response, Model model) {
        long appId = NumberUtils.toLong(request.getParameter("appId"), 0);
        AppUser appUser = getUserInfo(request);
        logger.warn("{} want to update appId={} info!", appUser.getName(), appId);
        String appDescName = request.getParameter("appDescName");
        String appDescIntro = request.getParameter("appDescIntro");
        String officer = request.getParameter("officer");
        SuccessEnum successEnum = SuccessEnum.SUCCESS;
        if (appId <= 0 || StringUtils.isBlank(appDescName) || StringUtils.isBlank(appDescIntro) || StringUtils
                .isBlank(officer)) {
            successEnum = SuccessEnum.FAIL;
        } else {
            try {
                AppDesc appDesc = appService.getByAppId(appId);
                appDesc.setName(appDescName);
                appDesc.setIntro(appDescIntro);
                appDesc.setOfficer(officer);
                appService.update(appDesc);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                successEnum = SuccessEnum.FAIL;
            }
        }
        write(response, String.valueOf(successEnum.value()));
        return null;
    }

    /**
     * 应用日报查询
     */
    @RequestMapping("/daily")
    public ModelAndView appDaily(HttpServletRequest request,
                                 HttpServletResponse response, Model model, Long appId) throws ParseException {
        // 1. 应用信息
        AppDesc appDesc = appService.getByAppId(appId);
        model.addAttribute("appDesc", appDesc);

        // 2. 日期
        String dailyDateParam = request.getParameter("dailyDate");
        Date date;
        if (StringUtils.isBlank(dailyDateParam)) {
            date = DateUtils.addDays(new Date(), -1);
        } else {
            date = DateUtil.parseYYYY_MM_dd(dailyDateParam);
        }
        model.addAttribute("dailyDate", dailyDateParam);

        // 3. 日报
        AppDailyData appDailyData = appDailyDataCenter.getAppDailyData(appId, date);
        model.addAttribute("appDailyData", appDailyData);

        return new ModelAndView("app/appDaily");
    }

    /**
     * 应用历史慢查询
     *
     * @param appId
     * @return
     * @throws ParseException
     */
    @RequestMapping("/slowLog")
    public ModelAndView appSlowLog(HttpServletRequest request,
                                   HttpServletResponse response, Model model, Long appId) throws ParseException {
        // 应用基本信息
        AppDesc appDesc = appService.getByAppId(appId);
        model.addAttribute("appDesc", appDesc);

        // 开始和结束日期
        TimeBetween timeBetween = getTimeBetween(request, model, "slowLogStartDate", "slowLogEndDate");
        Date startDate = timeBetween.getStartDate();
        Date endDate = timeBetween.getEndDate();

        // 应用慢查询日志
        Map<String, Long> appInstanceSlowLogCountMap = appStatsCenter
                .getInstanceSlowLogCountMapByAppId(appId, startDate, endDate);
        model.addAttribute("appInstanceSlowLogCountMap", appInstanceSlowLogCountMap);
        List<InstanceSlowLog> appInstanceSlowLogList = appStatsCenter
                .getInstanceSlowLogByAppId(appId, startDate, endDate);
        model.addAttribute("appInstanceSlowLogList", appInstanceSlowLogList);

        // 各个实例对应的慢查询日志
        Map<String, List<InstanceSlowLog>> instaceSlowLogMap = new HashMap<String, List<InstanceSlowLog>>();
        Map<String, Long> instanceHostPortIdMap = new HashMap<String, Long>();
        for (InstanceSlowLog instanceSlowLog : appInstanceSlowLogList) {
            String hostPort = instanceSlowLog.getIp() + ":" + instanceSlowLog.getPort();
            instanceHostPortIdMap.put(hostPort, instanceSlowLog.getInstanceId());
            if (instaceSlowLogMap.containsKey(hostPort)) {
                instaceSlowLogMap.get(hostPort).add(instanceSlowLog);
            } else {
                List<InstanceSlowLog> list = new ArrayList<InstanceSlowLog>();
                list.add(instanceSlowLog);
                instaceSlowLogMap.put(hostPort, list);
            }
        }
        model.addAttribute("instaceSlowLogMap", instaceSlowLogMap);
        model.addAttribute("instanceHostPortIdMap", instanceHostPortIdMap);

        return new ModelAndView("app/slowLog");
    }

    @RequestMapping("/latencyMonitor")
    public ModelAndView appLatencyMonitor(HttpServletRequest request,
                                          HttpServletResponse response, Model model, Long appId) {
        // 应用基本信息
        AppDesc appDesc = appService.getByAppId(appId);
        model.addAttribute("appDesc", appDesc);

        // 获取时间区间
        String searchDate = request.getParameter("searchDate");
        TimeBetween timeBetween = new TimeBetween();
        try {
            timeBetween = DateUtil.fillWithDateFormat(searchDate);
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
        }
        long startTime = timeBetween.getStartTime();
        long endTime = timeBetween.getEndTime();
        searchDate = timeBetween.getFormatStartDate();
        model.addAttribute("searchDate", searchDate);

        // 应用延迟监控数据
        Map<String, List<Map<String, Object>>> appLatencyStats = appStatsCenter.getAppLatencyStats(appId, startTime, endTime);
        model.addAttribute("appLatencyStatsJson", JSONObject.toJSONString(appLatencyStats));

        Map<String, Long> appLatencyStatsGroupByInstance = appStatsCenter.getAppLatencyStatsGroupByInstance(appId, startTime, endTime);
        model.addAttribute("appLatencyStatsGroupByInstance", appLatencyStatsGroupByInstance);

        Set<String> instanceSet = new HashSet<>();
        instanceSet.addAll(appLatencyStatsGroupByInstance.keySet());

        // 应用慢查询日志
        Map<String, Long> appInstanceSlowLogCountMap = appStatsCenter
                .getInstanceSlowLogCountMapByAppId(appId, timeBetween.getStartDate(), timeBetween.getEndDate());
        model.addAttribute("appInstanceSlowLogCountMap", appInstanceSlowLogCountMap);
        instanceSet.addAll(appInstanceSlowLogCountMap.keySet());
        model.addAttribute("instanceSet", instanceSet);

        List<InstanceSlowLog> appInstanceSlowLogList = appStatsCenter
                .getInstanceSlowLogByAppId(appId, timeBetween.getStartDate(), timeBetween.getEndDate());
        model.addAttribute("appInstanceSlowLogList", appInstanceSlowLogList);

        // 各个实例对应的慢查询日志
        Map<String, List<InstanceSlowLog>> instaceSlowLogMap = new HashMap<String, List<InstanceSlowLog>>();
        Map<String, Long> instanceHostPortIdMap = new HashMap<String, Long>();
        for (InstanceSlowLog instanceSlowLog : appInstanceSlowLogList) {
            String hostPort = instanceSlowLog.getIp() + ":" + instanceSlowLog.getPort();
            instanceHostPortIdMap.put(hostPort, instanceSlowLog.getInstanceId());
            if (instaceSlowLogMap.containsKey(hostPort)) {
                instaceSlowLogMap.get(hostPort).add(instanceSlowLog);
            } else {
                List<InstanceSlowLog> list = new ArrayList<InstanceSlowLog>();
                list.add(instanceSlowLog);
                instaceSlowLogMap.put(hostPort, list);
            }
        }
        model.addAttribute("instaceSlowLogMap", instaceSlowLogMap);
        model.addAttribute("instanceHostPortIdMap", instanceHostPortIdMap);
        return new ModelAndView("app/slowLog");
    }

    @RequestMapping(value = "/latencyInfoDetails")
    public ModelAndView getLatencyInfoDetails(HttpServletRequest request, HttpServletResponse response, Model model) {
        long appId = NumberUtils.toLong(request.getParameter("appId"));
        model.addAttribute("appId", appId);

        long timestamp = NumberUtils.toLong(request.getParameter("searchTime")); //毫秒
        Date startDate = new Date(timestamp);
        long startTime = NumberUtils.toLong(DateUtil.formatDate(startDate, "yyyyMMddHHmm00"));
        Date endDate = DateUtils.addMinutes(startDate, 1);
        long endTime = NumberUtils.toLong(DateUtil.formatDate(endDate, "yyyyMMddHHmm00"));
        model.addAttribute("searchTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:00").format(startDate));

        Map<String, Long> sumInstanceLatencyStatMap = appStatsCenter.getAppLatencyStatsGroupByInstance(appId, startTime, endTime);
        model.addAttribute("sumInstanceLatencyStatMap", sumInstanceLatencyStatMap);

        Map<String, List<Map<String, Object>>> latencyInfoDetailMap = appStatsCenter.getAppLatencyInfo(appId, startTime, endTime);
        model.addAttribute("latencyInfoDetailMap", latencyInfoDetailMap);

        return new ModelAndView("/app/appLatencyInfoDetail");
    }

    @RequestMapping(value = "/latencyRelatedSlowLog")
    public ModelAndView getLatencyRelatedSlowLog(HttpServletRequest request, HttpServletResponse response, Model model) {
        long instanceId = NumberUtils.toLong(request.getParameter("instanceId"));
        String executeDate = request.getParameter("executeDate"); //秒
        List<InstanceSlowLog> instanceSlowLogList = appStatsCenter.getByInstanceExecuteTime(instanceId, executeDate);
        model.addAttribute("instanceSlowLogList", instanceSlowLogList);

        return new ModelAndView("");
    }


    /**
     * 清理应用数据
     */
    @RequestMapping(value = "/cleanAppData")
    public ModelAndView doCleanAppData(HttpServletRequest request, HttpServletResponse response, Model model,
                                       long appId) {
        AppUser appUser = getUserInfo(request);
        logger.warn("{} start to clean appId={} data!", appUser.getName(), appId);
        SuccessEnum successEnum = SuccessEnum.FAIL;
        if (appId > 0) {
            //验证用户对应用的权限 以及数据清理的结果
            if (checkAppUserProvilege(request, appId) && appDeployCenter.cleanAppData(appId, getUserInfo(request))) {
                successEnum = SuccessEnum.SUCCESS;
            }
        }
        logger.warn("{} end to clean appId={} data, result is {}", appUser.getName(), appId, successEnum.info());
        write(response, String.valueOf(successEnum.value()));
        return null;
    }

    /**
     * AppCommandGroup列表组装成json串
     */
    private String assembleGroupJson(List<AppCommandGroup> appCommandGroupList) {
        if (appCommandGroupList == null || appCommandGroupList.isEmpty()) {
            return "[]";
        }
        List<SimpleChartData> list = new ArrayList<SimpleChartData>();
        for (AppCommandGroup appCommandGroup : appCommandGroupList) {
            SimpleChartData chartData = SimpleChartData
                    .getFromAppCommandGroup(appCommandGroup);
            list.add(chartData);
        }
        JSONArray jsonArray = JSONArray.fromObject(list);
        return jsonArray.toString();
    }

    /**
     * AppStats列表组装成json串
     */
    private String assembleAppStatsJson(List<AppStats> appStats, String statName) {
        if (appStats == null || appStats.isEmpty()) {
            return "[]";
        }
        List<SimpleChartData> list = new ArrayList<SimpleChartData>();
        for (AppStats stat : appStats) {
            try {
                SimpleChartData chartData = SimpleChartData.getFromAppStats(stat, statName);
                list.add(chartData);
            } catch (ParseException e) {
                logger.info(e.getMessage(), e);
            }
        }
        JSONArray jsonArray = JSONArray.fromObject(list);
        return jsonArray.toString();
    }

    private String assembleMutilDateAppCommandJsonMinute(List<AppCommandStats> appCommandStats, Date startDate,
                                                         Date endDate) {
        if (appCommandStats == null || appCommandStats.isEmpty()) {
            return "[]";
        }
        Map<String, List<HighchartPoint>> map = new HashMap<String, List<HighchartPoint>>();
        Date currentDate = DateUtils.addDays(endDate, -1);
        int diffDays = 0;
        while (currentDate.getTime() >= startDate.getTime()) {
            List<HighchartPoint> list = new ArrayList<HighchartPoint>();
            for (AppCommandStats stat : appCommandStats) {
                try {
                    HighchartPoint highchartPoint = HighchartPoint.getFromAppCommandStats(stat, currentDate, diffDays);
                    if (highchartPoint == null) {
                        continue;
                    }
                    list.add(highchartPoint);
                } catch (ParseException e) {
                    logger.info(e.getMessage(), e);
                }
            }
            String formatDate = DateUtil.formatDate(currentDate, "yyyy-MM-dd");
            map.put(formatDate, list);
            currentDate = DateUtils.addDays(currentDate, -1);
            diffDays++;
        }
        net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject.fromObject(map);
        return jsonObject.toString();
    }

    /**
     * 多命令组装
     *
     * @param appStats
     * @param statNameList
     * @param startDate
     * @return
     */
    private String assembleMutiStatAppStatsJsonMinute(List<AppStats> appStats, List<String> statNameList,
                                                      Date startDate) {
        if (appStats == null || appStats.isEmpty()) {
            return "[]";
        }
        Map<String, List<HighchartPoint>> map = new HashMap<String, List<HighchartPoint>>();
        for (String statName : statNameList) {
            List<HighchartPoint> list = new ArrayList<HighchartPoint>();
            for (AppStats stat : appStats) {
                try {
                    HighchartPoint highchartPoint = HighchartPoint.getFromAppStats(stat, statName, startDate, 0);
                    if (highchartPoint == null) {
                        continue;
                    }
                    list.add(highchartPoint);
                } catch (ParseException e) {
                    logger.info(e.getMessage(), e);
                }
            }
            map.put(statName, list);
        }
        net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject.fromObject(map);
        return jsonObject.toString();
    }

    /**
     * 多时间组装
     *
     * @param appStats
     * @param statName
     * @param startDate
     * @param endDate
     * @return
     */
    private String assembleMutilDateAppStatsJsonMinute(List<AppStats> appStats, String statName, Date startDate,
                                                       Date endDate) {
        if (appStats == null || appStats.isEmpty()) {
            return "[]";
        }
        Map<String, List<HighchartPoint>> map = new HashMap<String, List<HighchartPoint>>();
        Date currentDate = DateUtils.addDays(endDate, -1);
        int diffDays = 0;
        while (currentDate.getTime() >= startDate.getTime()) {
            List<HighchartPoint> list = new ArrayList<HighchartPoint>();
            for (AppStats stat : appStats) {
                try {
                    HighchartPoint highchartPoint = HighchartPoint
                            .getFromAppStats(stat, statName, currentDate, diffDays);
                    if (highchartPoint == null) {
                        continue;
                    }
                    list.add(highchartPoint);
                } catch (ParseException e) {
                    logger.info(e.getMessage(), e);
                }
            }
            String formatDate = DateUtil.formatDate(currentDate, "yyyy-MM-dd");
            map.put(formatDate, list);
            currentDate = DateUtils.addDays(currentDate, -1);
            diffDays++;
        }
        net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject.fromObject(map);
        return jsonObject.toString();
    }

    /**
     * 多时间组装
     *
     * @param appStats
     * @param statName
     * @param startDate
     * @param endDate
     * @return
     */
    private String assembleMutilDateAppStatsJsonMinuteDoublePoint(List<AppStats> appStats, String statName, Date startDate,
                                                                  Date endDate) {
        if (appStats == null || appStats.isEmpty()) {
            return "[]";
        }
        Map<String, List<HighchartDoublePoint>> map = new HashMap<>();
        Date currentDate = DateUtils.addDays(endDate, -1);
        int diffDays = 0;
        while (currentDate.getTime() >= startDate.getTime()) {
            List<HighchartDoublePoint> list = new ArrayList<>();
            for (AppStats stat : appStats) {
                try {
                    HighchartDoublePoint highchartPoint = HighchartDoublePoint
                            .getFromAppStats(stat, statName, currentDate, diffDays);
                    if (highchartPoint == null) {
                        continue;
                    }
                    list.add(highchartPoint);
                } catch (ParseException e) {
                    logger.info(e.getMessage(), e);
                }
            }
            String formatDate = DateUtil.formatDate(currentDate, "yyyy-MM-dd");
            map.put(formatDate, list);
            currentDate = DateUtils.addDays(currentDate, -1);
            diffDays++;
        }
        net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject.fromObject(map);
        return jsonObject.toString();
    }

    /**
     * AppCommandStats列表组装成json串
     */
    private String assembleJson(List<AppCommandStats> appCommandStatsList) {
        return assembleJson(appCommandStatsList, null);
    }

    private String assembleJson(List<AppCommandStats> appCommandStatsList, Integer addDay) {
        if (appCommandStatsList == null || appCommandStatsList.isEmpty()) {
            return "[]";
        }
        List<SimpleChartData> list = new ArrayList<SimpleChartData>();
        for (AppCommandStats stat : appCommandStatsList) {
            try {
                SimpleChartData chartData = SimpleChartData
                        .getFromAppCommandStats(stat, addDay);
                list.add(chartData);
            } catch (ParseException e) {
                logger.info(e.getMessage(), e);
            }
        }
        JSONArray jsonArray = JSONArray.fromObject(list);
        return jsonArray.toString();
    }

}
