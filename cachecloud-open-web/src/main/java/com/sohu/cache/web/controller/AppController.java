package com.sohu.cache.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.sohu.cache.constant.AppAuditType;
import com.sohu.cache.constant.AppStatusEnum;
import com.sohu.cache.constant.AppUserTypeEnum;
import com.sohu.cache.constant.TimeDimensionalityEnum;
import com.sohu.cache.entity.*;
import com.sohu.cache.stats.app.AppDeployCenter;
import com.sohu.cache.stats.app.AppStatsCenter;
import com.sohu.cache.stats.instance.InstanceStatsCenter;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.DemoCodeUtil;
import com.sohu.cache.web.vo.AppDetailVO;
import com.sohu.cache.web.chart.model.HighchartPoint;
import com.sohu.cache.web.chart.model.SimpleChartData;
import com.sohu.cache.web.enums.SuccessEnum;
import com.sohu.cache.web.util.AppEmailUtil;
import com.sohu.cache.web.util.DateUtil;
import com.sohu.cache.web.util.Page;

import net.sf.json.JSONArray;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

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
    
    /**
     * 取命令的条数
     */
    private final static int commandsCount = 20;

    /**
     * 初始化贡献者页面
     * @return
     */
    @RequestMapping("/initBecomeContributor")
    public ModelAndView doInitBecomeContributor(HttpServletRequest request,
                        HttpServletResponse response, Model model){
        model.addAttribute("currentUser", getUserInfo(request));
        return new ModelAndView("app/initBecomeContributor");
    }
    
    /**
     * 成为cachecloud贡献者
     * @param groupName 项目组
     * @param applyReason 申请理由
     * @return
     */
    @RequestMapping("/addBecomeContributor")
    public ModelAndView doAddBecomeContributor(HttpServletRequest request,
                        HttpServletResponse response, Model model, String groupName, String applyReason){
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
                              HttpServletResponse response, Model model, Long appId, String tabTag, String firstCommand)
            throws ParseException {
        // 如果应用id为空，取第一个应用id
        if (appId == null) {
            return new ModelAndView("redirect:/admin/app/list");
        }
        
        // 日期转换
        String startDateParam = request.getParameter("startDate");
        String endDateParam = request.getParameter("endDate");
        if (StringUtils.isBlank(startDateParam) || StringUtils.isBlank(endDateParam)) {
            Date startDate = new Date();
            startDateParam = DateUtil.formatDate(startDate, "yyyy-MM-dd");
            endDateParam = DateUtil.formatDate(DateUtils.addDays(startDate, 1), "yyyy-MM-dd");
        }
        String slowLogStartDateParam = request.getParameter("slowLogStartDate");
        String slowLogEndDateParam = request.getParameter("slowLogEndDate");
        if (StringUtils.isBlank(slowLogStartDateParam) || StringUtils.isBlank(slowLogEndDateParam)) {
            Date startDate = new Date();
            slowLogStartDateParam = DateUtil.formatDate(startDate, "yyyy-MM-dd");
            slowLogEndDateParam = DateUtil.formatDate(DateUtils.addDays(startDate, 1), "yyyy-MM-dd");
        }
        
        model.addAttribute("startDate", startDateParam);
        model.addAttribute("endDate", endDateParam);
        model.addAttribute("slowLogStartDate", slowLogStartDateParam);
        model.addAttribute("slowLogEndDate", slowLogEndDateParam);
        model.addAttribute("appId", appId);
        model.addAttribute("tabTag", tabTag);
        model.addAttribute("firstCommand", firstCommand);
        

        return new ModelAndView("app/userAppsIndex");

    }

    
    /**
     * 应用统计相关
     *
     * @param appId 应用id
     * @return
     * @throws ParseException
     */
    @RequestMapping("/stat")
    public ModelAndView appStat(HttpServletRequest request,
                                HttpServletResponse response, Model model, Long appId) throws ParseException {
        String startDateParam = request.getParameter("startDate");
        String endDateParam = request.getParameter("endDate");

        /** 1.获取app的VO */
        AppDetailVO appDetail = appStatsCenter.getAppDetail(appId);
        model.addAttribute("appDetail", appDetail);

        /** 2. 时间. */
        Date startDate;
        Date endDate;
        if (StringUtils.isBlank(startDateParam) || StringUtils.isBlank(endDateParam)) {
            startDate = new Date();
            endDate = DateUtils.addDays(startDate, 1);
        } else {
            endDate = DateUtil.parseYYYY_MM_dd(endDateParam);
            startDate = DateUtil.parseYYYY_MM_dd(startDateParam);
        }
        Date yesterDay = DateUtils.addDays(startDate, -1);

        long beginTime = NumberUtils.toLong(DateUtil.formatYYYYMMddHHMM(startDate));
        long endTime = NumberUtils.toLong(DateUtil.formatYYYYMMddHHMM(endDate));
        model.addAttribute("startDate", startDateParam);
        model.addAttribute("endDate", endDateParam);
        model.addAttribute("yesterDay", DateUtil.formatDate(yesterDay, "yyyy-MM-dd"));

        // 3.是否超过1天
        if (endDate.getTime() - startDate.getTime() > TimeUnit.DAYS.toMillis(1)) {
            model.addAttribute("betweenOneDay", 0);
        } else {
            model.addAttribute("betweenOneDay", 1);
        }

        // 4. top5命令
        List<AppCommandStats> top5Commands = appStatsCenter.getTopLimitAppCommandStatsList(appId, beginTime, endTime, 5);
        model.addAttribute("top5Commands", top5Commands);
        
        // 5.峰值
        List<AppCommandStats> top5ClimaxList = new ArrayList<AppCommandStats>();
        if (CollectionUtils.isNotEmpty(top5Commands)) {
            for (AppCommandStats appCommandStats : top5Commands) {
                AppCommandStats temp = appStatsCenter.getCommandClimax(appId, beginTime, endTime, appCommandStats.getCommandName());
                if (temp != null) {
                    top5ClimaxList.add(temp);
                }
            }
        }

        model.addAttribute("top5ClimaxList", top5ClimaxList);

        model.addAttribute("appId", appId);
        return new ModelAndView("app/appStat");
    }
    
    /**
     * 命令曲线
     *
     * @param appId
     * @param firstCommand 第一条命令
     * @throws ParseException
     */
    @RequestMapping("/commandAnalysis")
    public ModelAndView appCommandAnalysis(HttpServletRequest request,
                                           HttpServletResponse response, Model model, Long appId, String firstCommand) throws ParseException {
        String startDateParam = request.getParameter("startDate");
        String endDateParam = request.getParameter("endDate");

        /** 1.获取app的VO */
        AppDetailVO appDetail = appStatsCenter.getAppDetail(appId);
        model.addAttribute("appDetail", appDetail);

        /** 2.返回日期 */
        Date endDate;
        Date startDate;
        if (StringUtils.isBlank(startDateParam) || StringUtils.isBlank(endDateParam)) {
            startDate = new Date();
            endDate = DateUtils.addDays(startDate, 1);
            startDateParam = DateUtil.formatDate(startDate, "yyyy-MM-dd");
            endDateParam = DateUtil.formatDate(endDate, "yyyy-MM-dd");
        } else {
            endDate = DateUtil.parseYYYY_MM_dd(endDateParam);
            startDate = DateUtil.parseYYYY_MM_dd(startDateParam);
        }
        Date yesterDay = DateUtils.addDays(startDate, -1);
        model.addAttribute("yesterDay", DateUtil.formatDate(yesterDay, "yyyy-MM-dd"));

        model.addAttribute("startDate", startDateParam);
        model.addAttribute("endDate", endDateParam);
        long beginTime = NumberUtils.toLong(DateUtil.formatYYYYMMddHHMM(startDate));
        long endTime = NumberUtils.toLong(DateUtil.formatYYYYMMddHHMM(endDate));

        // 3.是否超过1天
        if (endDate.getTime() - startDate.getTime() > TimeUnit.DAYS.toMillis(1)) {
            model.addAttribute("betweenOneDay", 0);
        } else {
            model.addAttribute("betweenOneDay", 1);
        }

        /** 3.获取top5命令 */
        List<AppCommandStats> allCommands = appStatsCenter.getTopLimitAppCommandStatsList(appId, beginTime, endTime, commandsCount);
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
        return new ModelAndView("app/appDetail");
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
        String startDateParam = request.getParameter("startDate");
        String endDateParam = request.getParameter("endDate");
        Date startDate = DateUtil.parseYYYY_MM_dd(startDateParam);
        Date endDate = DateUtil.parseYYYY_MM_dd(endDateParam);
        String result = "[]";
        if (appId != null) {
            long beginTime = NumberUtils.toLong(DateUtil.formatYYYYMMddHHMM(startDate));
            long endTime = NumberUtils.toLong(DateUtil.formatYYYYMMddHHMM(endDate));

            // 命令参数
            String commandName = request.getParameter("commandName");
            List<AppCommandStats> appCommandStatsList;
            if (StringUtils.isNotBlank(commandName)) {
                appCommandStatsList = appStatsCenter.getCommandStatsList(appId, beginTime, endTime, commandName);
            } else {
                appCommandStatsList = appStatsCenter.getCommandStatsList(appId, beginTime, endTime);
            }
            result = assembleJson(appCommandStatsList);
        }
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
        String startDateParam = request.getParameter("startDate");
        String endDateParam = request.getParameter("endDate");
        Date startDate = DateUtil.parseYYYY_MM_dd(startDateParam);
        Date endDate = DateUtil.parseYYYY_MM_dd(endDateParam);
        String result = "[]";
        if (appId != null) {
            long beginTime = NumberUtils.toLong(DateUtil.formatYYYYMMddHHMM(startDate));
            long endTime = NumberUtils.toLong(DateUtil.formatYYYYMMddHHMM(endDate));

            // 命令参数
            String commandName = request.getParameter("commandName");
            List<AppCommandStats> appCommandStatsList;
            if (StringUtils.isNotBlank(commandName)) {
                appCommandStatsList = appStatsCenter.getCommandStatsListV2(appId, beginTime, endTime, TimeDimensionalityEnum.MINUTE, commandName);
            } else {
                appCommandStatsList = appStatsCenter.getCommandStatsListV2(appId, beginTime, endTime, TimeDimensionalityEnum.MINUTE);
            }
            result = assembleMutilDateAppCommandJsonMinute(appCommandStatsList, startDate, endDate);
        }
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
        String startDateParam = request.getParameter("startDate");
        String endDateParam = request.getParameter("endDate");
        Date startDate = DateUtil.parseYYYY_MM_dd(startDateParam);
        Date endDate = DateUtil.parseYYYY_MM_dd(endDateParam);
        String result = "[]";
        if (appId != null) {
            long beginTime = NumberUtils.toLong(DateUtil.formatYYYYMMddHHMM(startDate));
            long endTime = NumberUtils.toLong(DateUtil.formatYYYYMMddHHMM(endDate));
            List<AppStats> appStats = appStatsCenter.getAppStatsListByMinuteTime(appId, beginTime, endTime);
            result = assembleAppStatsJson(appStats, statName);
        }
        write(response, result);
        return null;
    }

    /**
     * 多命令
     * @param appId
     * @param statName
     * @return
     * @throws ParseException
     */
    @RequestMapping("/getMutiStatAppStats")
    public ModelAndView getMutiStatAppStats(HttpServletRequest request,
                                    HttpServletResponse response, Model model, Long appId) throws ParseException {
        String statNames = request.getParameter("statName");
        List<String> statNameList = Arrays.asList(statNames.split(ConstUtils.COMMA));
        
        String startDateParam = request.getParameter("startDate");
        String endDateParam = request.getParameter("endDate");
        Date startDate = DateUtil.parseYYYY_MM_dd(startDateParam);
        Date endDate = DateUtil.parseYYYY_MM_dd(endDateParam);
        String result = "[]";
        if (appId != null) {
            long beginTime = NumberUtils.toLong(DateUtil.formatYYYYMMddHHMM(startDate));
            long endTime = NumberUtils.toLong(DateUtil.formatYYYYMMddHHMM(endDate));
            List<AppStats> appStats = appStatsCenter.getAppStatsList(appId, beginTime, endTime, TimeDimensionalityEnum.MINUTE);
            result = assembleMutiStatAppStatsJsonMinute(appStats, statNameList, startDate);
        }
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
        
        String startDateParam = request.getParameter("startDate");
        String endDateParam = request.getParameter("endDate");
        Date startDate = DateUtil.parseYYYY_MM_dd(startDateParam);
        Date endDate = DateUtil.parseYYYY_MM_dd(endDateParam);
        String result = "[]";
        if (appId != null) {
            long beginTime = NumberUtils.toLong(DateUtil.formatYYYYMMddHHMM(startDate));
            long endTime = NumberUtils.toLong(DateUtil.formatYYYYMMddHHMM(endDate));
            List<AppStats> appStats = appStatsCenter.getAppStatsList(appId, beginTime, endTime, TimeDimensionalityEnum.MINUTE);
            result = assembleMutilDateAppStatsJsonMinute(appStats, statName, startDate, endDate);
        }
        model.addAttribute("data", result);
        return new ModelAndView("");
    }
    
    /**
     * 获取指定时间内某个应用全部实例的统计信息
     * @param appId
     */
    @RequestMapping("/appInstanceNetStat")
    public ModelAndView appInstanceNetStat(HttpServletRequest request, HttpServletResponse response, Model model, Long appId) throws ParseException {
        // 应用基本信息
        AppDesc appDesc = appService.getByAppId(appId);
        model.addAttribute("appDesc", appDesc);
        model.addAttribute("appId", appId);

        // 日期格式转换
        Date endDate;
        Date startDate;
        String startDateParam = request.getParameter("startDate");
        String endDateParam = request.getParameter("endDate");
        if (StringUtils.isBlank(startDateParam) || StringUtils.isBlank(endDateParam)) {
            startDate = new Date();
            endDate = DateUtils.addDays(startDate, 1);
            startDateParam = DateUtil.formatDate(startDate, "yyyy-MM-dd");
            endDateParam = DateUtil.formatDate(endDate, "yyyy-MM-dd");
        } else {
            endDate = DateUtil.parseYYYY_MM_dd(endDateParam);
            startDate = DateUtil.parseYYYY_MM_dd(startDateParam);
        }
        model.addAttribute("startDate", startDateParam);
        model.addAttribute("endDate", endDateParam);
        return new ModelAndView("app/appInstanceNetStat");
    }
    
    
    
    /**
     * 获取指定时间内某个应用全部实例的统计信息
     * @param appId 应用流量
     */
    @RequestMapping("/getAppInstancesNetStat")
    public ModelAndView getAppInstancesNetStat(HttpServletRequest request, HttpServletResponse response, Model model, Long appId) throws ParseException {
        //时间转换
        String startDateParam = request.getParameter("startDate");
        String endDateParam = request.getParameter("endDate");
        Date startDate = DateUtil.parseYYYY_MM_dd(startDateParam);
        Date endDate = DateUtil.parseYYYY_MM_dd(endDateParam);
        long beginTime = NumberUtils.toLong(DateUtil.formatYYYYMMddHHMM(startDate));
        long endTime = NumberUtils.toLong(DateUtil.formatYYYYMMddHHMM(endDate));
        
        //缩减字段
        String netInCommand = "total_net_input_bytes";
        String netOutCommand = "total_net_output_bytes";
        Map<String,String> commandMap = new HashMap<String, String>();
        commandMap.put(netInCommand, "i");
        commandMap.put(netOutCommand, "o");
        
        //获取应用下所有实例网络流量统计
        Map<Integer, Map<String, List<InstanceCommandStats>>> appInstancesNetStat = instanceStatsCenter
                .getStandardStatsList(appId, beginTime, endTime,
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
                    Map<String,Object> tmpMap = total.get(timestamp);
                    tmpMap.put(command, commandCount);
                } else {
                    Map<String,Object> tmpMap = new HashMap<String, Object>();
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
     *
     * @param appId
     * @throws ParseException
     */
    @RequestMapping("/getTop5Commands")
    public ModelAndView getAppTop5Commands(HttpServletRequest request,
                                           HttpServletResponse response, Model model, Long appId) throws ParseException {
        String startDateParam = request.getParameter("startDate");
        String endDateParam = request.getParameter("endDate");
        Date startDate = DateUtil.parseYYYY_MM_dd(startDateParam);
        Date endDate = DateUtil.parseYYYY_MM_dd(endDateParam);
        String result = "[]";
        if (appId != null) {
            long beginTime = NumberUtils.toLong(DateUtil.formatYYYYMMddHHMM(startDate));
            long endTime = NumberUtils.toLong(DateUtil.formatYYYYMMddHHMM(endDate));
            List<AppCommandStats> appCommandStats = appStatsCenter
                    .getTop5AppCommandStatsList(appId, beginTime, endTime);
            result = assembleJson(appCommandStats);
        }
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
        String startDateParam = request.getParameter("startDate");
        String endDateParam = request.getParameter("endDate");
        Date startDate = DateUtil.parseYYYY_MM_dd(startDateParam);
        Date endDate = DateUtil.parseYYYY_MM_dd(endDateParam);
        String result = "[]";
        if (appId != null) {
            long beginTime = NumberUtils.toLong(DateUtil.formatYYYYMMddHHMM(startDate));
            long endTime = NumberUtils.toLong(DateUtil.formatYYYYMMddHHMM(endDate));
            List<AppCommandGroup> appCommandGroupList = appStatsCenter
                    .getAppCommandGroup(appId, beginTime, endTime);
            result = assembleGroupJson(appCommandGroupList);
        }
        write(response, result);
        return null;
    }
    
    
    /**
     * 应用列表
     *
     * @param userId
     * @return
     */
    @RequestMapping(value = "/list")
    public ModelAndView doAppList(HttpServletRequest request,
                                  HttpServletResponse response, Model model, AppSearch appSearch) {
        // 1.获取该用户能够读取的应用列表,没有返回申请页面
        AppUser currentUser = getUserInfo(request);
        model.addAttribute("currentUser", currentUser);
        int userAppCount = appService.getUserAppCount(currentUser.getId());
        if (userAppCount == 0 && !AppUserTypeEnum.ADMIN_USER.value().equals(currentUser.getType())) {
            return new ModelAndView("redirect:/admin/app/init");
        }
        
        // 2.1 分页相关
        int totalCount = appService.getAppDescCount(currentUser, appSearch);
        int pageNo = NumberUtils.toInt(request.getParameter("pageNo"), 1);
        int pageSize = NumberUtils.toInt(request.getParameter("pageSize"), 10);
        Page page = new Page(pageNo,pageSize, totalCount);
        model.addAttribute("page", page);

        // 2.2 查询指定时间客户端异常
        appSearch.setPage(page);
        List<AppDesc> apps = appService.getAppDescList(currentUser, appSearch);
        // 2.3 应用列表
        List<AppDetailVO> appDetailList = new ArrayList<AppDetailVO>();
        model.addAttribute("appDetailList", appDetailList);

        // 3. 全局统计
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
        return new ModelAndView("app/appInit");
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
                                 HttpServletResponse response, Model model, AppDesc appDesc, String memSize) {
        AppUser appUser = getUserInfo(request);
        if (appDesc != null) {
            Timestamp now = new Timestamp(new Date().getTime());
            appDesc.setCreateTime(now);
            appDesc.setPassedTime(now);
            appDesc.setVerId(1);
            appDesc.setStatus((short) AppStatusEnum.STATUS_ALLOCATED.getStatus());
            appDeployCenter.createApp(appDesc, appUser, memSize);
        }
        return new ModelAndView("redirect:/admin/app/list");
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
    public ModelAndView commandExecute(HttpServletRequest request, HttpServletResponse response, Model model, Long appId) {
        if (appId != null && appId > 0) {
            model.addAttribute("appId", appId);
            String command = request.getParameter("command");
            String result = appStatsCenter.executeCommand(appId, command);
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
     * @param name
     * @param chName
     * @param email
     * @param mobile
     * @param type
     * @param userId
     * @return
     */
    @RequestMapping(value = "/changeAppUserInfo")
    public ModelAndView doAddUser(HttpServletRequest request,
            HttpServletResponse response, Model model, String name, String chName, String email, String mobile,
            Integer type, Long userId) {
        // 后台暂时不对参数进行验证
        AppUser appUser = AppUser.buildFrom(userId, name, chName, email, mobile, type);
        try {
            if (userId == null) {
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
        AppAudit appAudit = appService.saveAppScaleApply(appDesc, appUser, applyMemSize, appScaleReason, AppAuditType.APP_SCALE);
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
                                          HttpServletResponse response, Model model, Long appId, Long instanceId, String appConfigKey, String appConfigValue, String appConfigReason) {
        AppUser appUser = getUserInfo(request);
        AppDesc appDesc = appService.getByAppId(appId);
        AppAudit appAudit = appService.saveAppChangeConfig(appDesc, appUser, instanceId, appConfigKey, appConfigValue,appConfigReason, AppAuditType.APP_MODIFY_CONFIG);
        appEmailUtil.noticeAppResult(appDesc, appAudit);
        write(response, String.valueOf(SuccessEnum.SUCCESS.value()));
        return null;
    }
    
    /**
     * 实例修改配置申请
     *
     * @param appId          应用id
     * @param appConfigKey   配置项
     * @param appConfigValue 配置值
     * @return
     */
    @RequestMapping(value = "/changeInstanceConfig")
    public ModelAndView doChangeInstanceConfig(HttpServletRequest request,
                                          HttpServletResponse response, Model model, Long appId, Long instanceId, String instanceConfigKey, String instanceConfigValue, String instanceConfigReason) {
        AppUser appUser = getUserInfo(request);
        AppDesc appDesc = appService.getByAppId(appId);
        AppAudit appAudit = appService.saveInstanceChangeConfig(appDesc, appUser, instanceId, instanceConfigKey, instanceConfigValue, instanceConfigReason, AppAuditType.INSTANCE_MODIFY_CONFIG);
        appEmailUtil.noticeAppResult(appDesc, appAudit);
        write(response, String.valueOf(SuccessEnum.SUCCESS.value()));
        return null;
    }

    /**
     * 添加应用和用户对应关系
     *
     * @param appId    应用id
     * @param userName 用户名(邮箱前缀)
     * @return
     */
    @RequestMapping(value = "/addAppToUser")
    public ModelAndView doAddAppToUser(HttpServletRequest request,
                                       HttpServletResponse response, Model model, Long appId, String userName) {
        if (StringUtils.isNotBlank(userName)) {
            AppUser needAddAppUser = userService.getByName(userName);
            if (needAddAppUser != null) {
                appService.saveAppToUser(appId, needAddAppUser.getId());
                write(response, String.valueOf(SuccessEnum.SUCCESS.value()));
            } else {
                write(response, String.valueOf(SuccessEnum.FAIL.value()));
            }
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
        int memAlertValue =  NumberUtils.toInt(request.getParameter("memAlertValue"), -1);
        int clientConnAlertValue =  NumberUtils.toInt(request.getParameter("clientConnAlertValue"), -1);
        SuccessEnum result = appService.changeAppAlertConfig(appId, memAlertValue,clientConnAlertValue, getUserInfo(request));
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
        String appDescName =  request.getParameter("appDescName");
        String appDescIntro =  request.getParameter("appDescIntro");
        String officer = request.getParameter("officer");
        SuccessEnum successEnum = SuccessEnum.SUCCESS;
        if (appId <= 0 || StringUtils.isBlank(appDescName) || StringUtils.isBlank(appDescIntro) || StringUtils.isBlank(officer)) {
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
    

    @RequestMapping(value = "/demo")
    public ModelAndView doDemo(HttpServletRequest request, HttpServletResponse response, Long appId, Model model) {
        if (appId != null && appId > 0) {
            AppDesc appDesc = appService.getByAppId(appId);
            List<String> code = DemoCodeUtil.getCode(appDesc.getType(), appDesc.getAppId());
            List<String> dependency = DemoCodeUtil.getDependencyRedis();
            List<String> springConfig = DemoCodeUtil.getSpringConfig(appDesc.getType(), appDesc.getAppId());
            String restApi = DemoCodeUtil.getRestAPI(appDesc.getType(), appDesc.getAppId());
            
            if(CollectionUtils.isNotEmpty(springConfig) && springConfig.size() > 0){
                model.addAttribute("springConfig", springConfig);
            }
            model.addAttribute("dependency",dependency);
            model.addAttribute("code", code);
            model.addAttribute("status", 1);
            model.addAttribute("restApi", restApi);
        } else {
            model.addAttribute("status", 0);
        }
        return new ModelAndView("app/appDemo");
    }

    /**
     * 应用历史慢查询
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
        String slowLogStartDateParam = request.getParameter("slowLogStartDate");
        String slowLogEndDateParam = request.getParameter("slowLogEndDate");
        Date startDate;
        Date endDate;
        if (StringUtils.isBlank(slowLogStartDateParam) || StringUtils.isBlank(slowLogEndDateParam)) {
            startDate = new Date();
            endDate = DateUtils.addDays(startDate, 1);
        } else {
            startDate = DateUtil.parseYYYY_MM_dd(slowLogStartDateParam);
            endDate = DateUtil.parseYYYY_MM_dd(slowLogEndDateParam);
        }
        model.addAttribute("slowLogStartDate", slowLogStartDateParam);
        model.addAttribute("slowLogEndDate", slowLogEndDateParam);
        
        // 应用慢查询日志
        Map<String,Long> appInstanceSlowLogCountMap = appStatsCenter.getInstanceSlowLogCountMapByAppId(appId, startDate, endDate);
        model.addAttribute("appInstanceSlowLogCountMap", appInstanceSlowLogCountMap);
        List<InstanceSlowLog> appInstanceSlowLogList = appStatsCenter.getInstanceSlowLogByAppId(appId, startDate, endDate);
        model.addAttribute("appInstanceSlowLogList", appInstanceSlowLogList);
        
        // 各个实例对应的慢查询日志
        Map<String, List<InstanceSlowLog>> instaceSlowLogMap = new HashMap<String, List<InstanceSlowLog>>();
        Map<String, Long> instanceHostPortIdMap = new HashMap<String, Long>();
        for(InstanceSlowLog instanceSlowLog : appInstanceSlowLogList) {
            String hostPort = instanceSlowLog.getIp() + ":" + instanceSlowLog.getPort();
            instanceHostPortIdMap.put(hostPort, instanceSlowLog.getInstanceId());
            if(instaceSlowLogMap.containsKey(hostPort)) {
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
    
    /**
     * 清理应用数据
     */
    @RequestMapping(value = "/cleanAppData")
    public ModelAndView doCleanAppData(HttpServletRequest request, HttpServletResponse response, Model model, long appId) {
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
    
    private String assembleMutilDateAppCommandJsonMinute(List<AppCommandStats> appCommandStats, Date startDate, Date endDate) {
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
     * @param appStats
     * @param statNameList
     * @param startDate
     * @return
     */
    private String assembleMutiStatAppStatsJsonMinute(List<AppStats> appStats, List<String> statNameList, Date startDate) {
        if (appStats == null || appStats.isEmpty()) {
            return "[]";
        }
        Map<String, List<HighchartPoint>> map = new HashMap<String, List<HighchartPoint>>();
        for(String statName : statNameList) {
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
     * @param appStats
     * @param statName
     * @param startDate
     * @param endDate
     * @return
     */
    private String assembleMutilDateAppStatsJsonMinute(List<AppStats> appStats, String statName, Date startDate, Date endDate) {
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
                    HighchartPoint highchartPoint = HighchartPoint.getFromAppStats(stat, statName, currentDate, diffDays);
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
