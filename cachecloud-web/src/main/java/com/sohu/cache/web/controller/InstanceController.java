package com.sohu.cache.web.controller;

import com.sohu.cache.alert.InstanceAlertService;
import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.entity.*;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.stats.app.AppStatsCenter;
import com.sohu.cache.stats.instance.InstanceStatsCenter;
import com.sohu.cache.web.chart.key.ChartKeysUtil;
import com.sohu.cache.web.chart.model.SplineChartEntity;
import com.sohu.cache.web.service.AppService;
import com.sohu.cache.web.service.WebClientComponent;
import com.sohu.cache.web.util.DateUtil;
import com.sohu.cache.web.vo.RedisSlowLog;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by hym on 14-7-27.
 */
@Controller
@RequestMapping("/admin/instance")
public class InstanceController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource(name = "instanceStatsCenter")
    private InstanceStatsCenter instanceStatsCenter;

    @Resource(name = "appStatsCenter")
    private AppStatsCenter appStatsCenter;

    @Resource(name = "redisCenter")
    private RedisCenter redisCenter;

    @Resource
    private InstanceAlertService instanceAlertService;

    @RequestMapping("/index")
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response, Model model, Integer admin, Long instanceId, Long appId, String tabTag) {

        String startDateParam = request.getParameter("startDate");
        String endDateParam = request.getParameter("endDate");

        if (StringUtils.isBlank(startDateParam) || StringUtils.isBlank(endDateParam)) {
            Date endDate = new Date();
            Date startDate = DateUtils.addDays(endDate, -1);
            startDateParam = DateUtil.formatDate(startDate, "yyyyMMdd");
            endDateParam = DateUtil.formatDate(endDate, "yyyyMMdd");
        }
        model.addAttribute("startDate", startDateParam);
        model.addAttribute("endDate", endDateParam);

        String condition = request.getParameter("condition");
        int conditionInt = StringUtils.isEmpty(condition) ? 0 : Integer.parseInt(condition);
        model.addAttribute("condition", conditionInt);

        if (instanceId != null && instanceId > 0) {
            model.addAttribute("instanceId", instanceId);
            InstanceInfo instanceInfo = instanceStatsCenter.getInstanceInfo(instanceId);

            if (instanceInfo == null) {
                model.addAttribute("type", -1);
            } else {
                if (appId != null && appId > 0) {
                    model.addAttribute("appId", appId);
                } else {
                    model.addAttribute("appId", instanceInfo.getAppId());
                }
                model.addAttribute("type", instanceInfo.getType());
            }
        } else {

        }
        if (tabTag != null) {
            model.addAttribute("tabTag", tabTag);
        }
        return new ModelAndView("instance/instanceIndex");
    }

    @RequestMapping("/stat")
    public ModelAndView stat(HttpServletRequest request, HttpServletResponse response, Model model, Integer admin, Long instanceId) {

        String startDateParam = request.getParameter("startDate");
        String endDateParam = request.getParameter("endDate");

        if (StringUtils.isBlank(startDateParam) || StringUtils.isBlank(endDateParam)) {
            Date endDate = new Date();
            Date startDate = DateUtils.addDays(endDate, -1);
            startDateParam = DateUtil.formatDate(startDate, "yyyyMMdd");
            endDateParam = DateUtil.formatDate(endDate, "yyyyMMdd");
        }
        model.addAttribute("startDate", startDateParam);
        model.addAttribute("endDate", endDateParam);

        if (instanceId != null && instanceId > 0) {
            model.addAttribute("instanceId", instanceId);
            InstanceInfo instanceInfo = instanceStatsCenter.getInstanceInfo(instanceId);
            model.addAttribute("instanceInfo", instanceInfo);
            model.addAttribute("appId", instanceInfo.getAppId());
            model.addAttribute("appDetail", appStatsCenter.getAppDetail(instanceInfo.getAppId()));
            InstanceStats instanceStats = instanceStatsCenter.getInstanceStats(instanceId);
            model.addAttribute("instanceStats", instanceStats);
            List<AppCommandStats> topLimitAppCommandStatsList = appStatsCenter.getTopLimitAppCommandStatsList(instanceInfo.getAppId(), Long.parseLong(startDateParam) * 10000, Long.parseLong(endDateParam) * 10000, 5);
            model.addAttribute("appCommandStats", topLimitAppCommandStatsList);
        }
        return new ModelAndView("instance/instanceStat");
    }

    @RequestMapping("/advancedAnalysis")
    public ModelAndView advancedAnalysis(HttpServletRequest request, HttpServletResponse response, Model model, Integer admin, Long instanceId) {

        String startDateParam = request.getParameter("startDate");
        String endDateParam = request.getParameter("endDate");
        String formatStartDate = null;
        String formatEndDate = null;

        if (StringUtils.isBlank(startDateParam) || StringUtils.isBlank(endDateParam)) {
            Date endDate = new Date();
            Date startDate = DateUtils.addDays(endDate, -1);
            startDateParam = DateUtil.formatDate(startDate, "yyyyMMdd");
            endDateParam = DateUtil.formatDate(endDate, "yyyyMMdd");
            formatStartDate = startDateParam;
            formatEndDate = endDateParam;
        }else{
            if(startDateParam.contains("-") && endDateParam.contains("-")){
                try{
                    formatStartDate = DateUtil.formatDate(DateUtil.parseYYYY_MM_dd(startDateParam), "yyyyMMdd");
                    formatEndDate = DateUtil.formatDate(DateUtil.parseYYYY_MM_dd(endDateParam), "yyyyMMdd");
                }catch (Exception e){
                    logger.error("date format error", e);
                }
            }else{
                formatStartDate = startDateParam;
                formatEndDate = endDateParam;
            }
        }
        model.addAttribute("startDate", startDateParam);
        model.addAttribute("endDate", endDateParam);

        if (instanceId != null && instanceId > 0) {
            model.addAttribute("instanceId", instanceId);
            InstanceInfo instanceInfo = instanceStatsCenter.getInstanceInfo(instanceId);
            model.addAttribute("instanceInfo", instanceInfo);
            model.addAttribute("appId", instanceInfo.getAppId());
            List<AppCommandStats> topLimitAppCommandStatsList = appStatsCenter.getTopLimitAppCommandStatsList(instanceInfo.getAppId(), Long.parseLong(formatStartDate) * 10000, Long.parseLong(formatEndDate) * 10000, 5);
            model.addAttribute("appCommandStats", topLimitAppCommandStatsList);
        } else {

        }
        return new ModelAndView("instance/instanceAdvancedAnalysis");
    }

    /**
     * 获取某个命令时间分布图
     *
     * @param instanceId  实例id
     * @param commandName 命令名称
     * @throws java.text.ParseException
     */
    @RequestMapping("/getCommandStats")
    public ModelAndView getCommandStats(HttpServletRequest request,
                                        HttpServletResponse response, Model model, Long instanceId,
                                        String commandName) throws ParseException {
        String startDateParam = request.getParameter("startDate");
        String endDateParam = request.getParameter("endDate");

        if (StringUtils.isBlank(startDateParam) || StringUtils.isBlank(endDateParam)) {
            Date endDate = new Date();
            Date startDate = DateUtils.addDays(endDate, -1);
            startDateParam = DateUtil.formatDate(startDate, "yyyyMMdd");
            endDateParam = DateUtil.formatDate(endDate, "yyyyMMdd");
        }
        model.addAttribute("startDate", startDateParam);
        model.addAttribute("endDate", endDateParam);

        Date startDate = DateUtil.parseYYYYMMdd(startDateParam);
        Date endDate = DateUtil.parseYYYYMMdd(endDateParam);
        if (instanceId != null) {
            long firstDayBegin = NumberUtils.toLong(DateUtil.formatYYYYMMdd(startDate) + "0000");
            long firstDayEnd = NumberUtils.toLong(DateUtil.formatYYYYMMdd(startDate) + "2359");
            long secondDayBegin = NumberUtils.toLong(DateUtil.formatYYYYMMdd(endDate) + "0000");
            long secondDayEnd = NumberUtils.toLong(DateUtil.formatYYYYMMdd(endDate) + "2359");
            List<InstanceCommandStats> instanceCommandStatsListFirst = instanceStatsCenter
                    .getCommandStatsList(instanceId, firstDayBegin, firstDayEnd, commandName);
            List<InstanceCommandStats> instanceCommandStatsListSecond = instanceStatsCenter
                    .getCommandStatsList(instanceId, secondDayBegin, secondDayEnd, commandName);
            Map<String, InstanceCommandStats> cmdStatsFirst = new HashMap<String, InstanceCommandStats>();
            Map<String, InstanceCommandStats> cmdStatsSecond = new HashMap<String, InstanceCommandStats>();

            for (InstanceCommandStats first : instanceCommandStatsListFirst) {
                cmdStatsFirst.put(first.getCollectTime() + "", first);
            }
            for (InstanceCommandStats second : instanceCommandStatsListSecond) {
                cmdStatsSecond.put(second.getCollectTime() + "", second);
            }

            SplineChartEntity splineChartEntity = new SplineChartEntity();
            String container = request.getParameter("container");
            if (container != null) {
                splineChartEntity.renderTo(container);
            }
            model.addAttribute("chart", splineChartEntity);
            splineChartEntity.putTitle(ChartKeysUtil.TitleKey.TEXT.getKey(), "命令:" + commandName + " 的比较曲线【" + startDateParam + "】-【" + endDateParam + "】");
            splineChartEntity.setYAxisTitle("y");
            List<Long> data1 = new ArrayList<Long>();
            List<Long> data2 = new ArrayList<Long>();
            Map<String, Object> serie1 = new HashMap<String, Object>();
            serie1.put("name", startDateParam);
            serie1.put("data", data1);
//            serie1.put("type", "area");
            Map<String, Object> serie2 = new HashMap<String, Object>();
            serie2.put("name", endDateParam);
            serie2.put("data", data2);
//            serie2.put("type", "area");
            splineChartEntity.putSeries(serie1);
            splineChartEntity.putSeries(serie2);
            List<Object> x = new LinkedList<Object>();
            for (int i = 0; i < 1440; i += 1) {
                Date date = DateUtils.addMinutes(startDate, i);
                String s = DateUtil.formatHHMM(date);
                if (cmdStatsFirst.containsKey(startDateParam + s)) {
                    data1.add(cmdStatsFirst.get(startDateParam + s).getCommandCount());
                } else {
                    data1.add(0l);
                }
                if (cmdStatsSecond.containsKey(endDateParam + s)) {
                    data2.add(cmdStatsSecond.get(endDateParam + s).getCommandCount());
                } else {
                    data2.add(0l);
                }

                x.add(s);
            }
            splineChartEntity.setXAxisCategories(x);
        }
        return new ModelAndView("");
    }

    /**
     * 获取某个命令时间分布图
     *
     * @param instanceId  实例id
     * @param commandName 命令名称
     * @throws java.text.ParseException
     */
    @RequestMapping("/getCommandStatsV2")
    public ModelAndView getCommandStatsV2(HttpServletRequest request,
                                          HttpServletResponse response, Model model, Long instanceId,
                                          String commandName) throws ParseException {
        String startDateParam = request.getParameter("startDate");
        String endDateParam = request.getParameter("endDate");

        if (StringUtils.isBlank(startDateParam) || StringUtils.isBlank(endDateParam)) {
            Date endDate = new Date();
            Date startDate = DateUtils.addDays(endDate, -1);
            startDateParam = DateUtil.formatDate(startDate, "yyyyMMdd");
            endDateParam = DateUtil.formatDate(endDate, "yyyyMMdd");
        }
        model.addAttribute("startDate", startDateParam);
        model.addAttribute("endDate", endDateParam);

        Date startDate = DateUtil.parseYYYYMMdd(startDateParam);
        Date endDate = DateUtil.parseYYYYMMdd(endDateParam);
        if (instanceId != null) {
            long firstDayBegin = NumberUtils.toLong(DateUtil.formatYYYYMMdd(startDate) + "0000");
            long firstDayEnd = NumberUtils.toLong(DateUtil.formatYYYYMMdd(startDate) + "2359");
            long secondDayBegin = NumberUtils.toLong(DateUtil.formatYYYYMMdd(endDate) + "0000");
            long secondDayEnd = NumberUtils.toLong(DateUtil.formatYYYYMMdd(endDate) + "2359");
            List<InstanceCommandStats> instanceCommandStatsListFirst = instanceStatsCenter
                    .getCommandStatsList(instanceId, firstDayBegin, firstDayEnd, commandName);
            List<InstanceCommandStats> instanceCommandStatsListSecond = instanceStatsCenter
                    .getCommandStatsList(instanceId, secondDayBegin, secondDayEnd, commandName);
            Map<String, InstanceCommandStats> cmdStatsFirst = new HashMap<String, InstanceCommandStats>();
            Map<String, InstanceCommandStats> cmdStatsSecond = new HashMap<String, InstanceCommandStats>();

            for (InstanceCommandStats first : instanceCommandStatsListFirst) {
                cmdStatsFirst.put(first.getCollectTime() + "", first);
            }
            for (InstanceCommandStats second : instanceCommandStatsListSecond) {
                cmdStatsSecond.put(second.getCollectTime() + "", second);
            }

            SplineChartEntity splineChartEntity = new SplineChartEntity();
            String container = request.getParameter("container");
            if (container != null) {
                splineChartEntity.renderTo(container);
            }
            model.addAttribute("chart", splineChartEntity);
            splineChartEntity.putTitle(ChartKeysUtil.TitleKey.TEXT.getKey(), "命令:" + commandName + " 的比较曲线【" + startDateParam + "】-【" + endDateParam + "】");
            splineChartEntity.setYAxisTitle("y");
            List<Long> data1 = new ArrayList<Long>();
            List<Long> data2 = new ArrayList<Long>();
            Map<String, Object> marker = new HashMap<String, Object>();
            marker.put("radius", 1);
            Map<String, Object> serie1 = new HashMap<String, Object>();
            serie1.put("name", startDateParam);
            serie1.put("data", data1);
            serie1.put("marker", marker);
            Map<String, Object> serie2 = new HashMap<String, Object>();
            serie2.put("name", endDateParam);
            serie2.put("data", data2);
            serie2.put("marker", marker);
            splineChartEntity.putSeries(serie1);
            splineChartEntity.putSeries(serie2);
            List<Object> x = new LinkedList<Object>();
            for (int i = 0; i < 1440; i += 1) {
                Date date = DateUtils.addMinutes(startDate, i);
                String s = DateUtil.formatHHMM(date);
                if (cmdStatsFirst.containsKey(startDateParam + s)) {
                    data1.add(cmdStatsFirst.get(startDateParam + s).getCommandCount());
                } else {
                    data1.add(0l);
                }
                if (cmdStatsSecond.containsKey(endDateParam + s)) {
                    data2.add(cmdStatsSecond.get(endDateParam + s).getCommandCount());
                } else {
                    data2.add(0l);
                }

                x.add(s);
            }
            splineChartEntity.setXAxisCategories(x);
        }
        return new ModelAndView("");
    }

    @RequestMapping("/fault")
    public ModelAndView fault( Model model, Integer instanceId) {
        List<InstanceFault> list = null;
        try {
            list = instanceAlertService.getListByInstId(instanceId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        if (list == null) {
            list = new ArrayList<InstanceFault>();
        }
        model.addAttribute("list", list);
        return new ModelAndView("instance/instanceFault");
    }

    @RequestMapping("/configSelect")
    public ModelAndView configSelect(HttpServletRequest request, HttpServletResponse response, Model model, Integer admin, Long instanceId, Long appId) {
        if (instanceId != null && instanceId > 0) {
            model.addAttribute("instanceId", instanceId);
            Map<String, String> redisConfigList = redisCenter.getRedisConfigList(instanceId.intValue());
            model.addAttribute("redisConfigList", redisConfigList);
        }
        if (appId != null && appId > 0) {
            model.addAttribute("appId", appId);
        }
        return new ModelAndView("instance/instanceConfigSelect");
    }

    @RequestMapping("/slowSelect")
    public ModelAndView slowSelect(HttpServletRequest request, HttpServletResponse response, Model model, Integer admin, Long instanceId) {
        if (instanceId != null && instanceId > 0) {
            model.addAttribute("instanceId", instanceId);
            List<RedisSlowLog> redisSlowLogs = redisCenter.getRedisSlowLogs(instanceId.intValue(), -1);
            model.addAttribute("redisSlowLogs", redisSlowLogs);
        }
        return new ModelAndView("instance/instanceSlowSelect");
    }

    @Autowired
    private WebClientComponent webClientComponent;
    @Autowired
    private InstanceDao instanceDao;
    @Autowired
    private AppService appService;

    @RequestMapping("/clientList")
    public ModelAndView clientList(HttpServletRequest request, HttpServletResponse response, Model model, Integer admin, Long instanceId, int condition) {
        if (instanceId != null && instanceId > 0) {
            model.addAttribute("instanceId", instanceId);
            model.addAttribute("condition", condition);
            long appId = instanceDao.getInstanceInfoById(instanceId).getAppId();
            List<String> redisClientList = appService.getAppOnlineInstanceInfo(appId).stream().map(instanceInfo -> instanceInfo.getIp()).collect(Collectors.toList());
            List<String> ccWebClientList = webClientComponent.getWebClientIps();

            List<String> clientList = redisCenter.getClientList(instanceId.intValue());
            model.addAttribute("clientList", clientList);

            List<Map<String, Object>> clientMapList = redisCenter.formatClientList(clientList);
            List<Map<String, Object>> clientMapList_final = new ArrayList<>();
            switch (condition) {
                case 0:
                    clientMapList_final = clientMapList.stream().filter(clientMap -> (!ccWebClientList.contains(clientMap.get("addr")) && !redisClientList.contains(clientMap.get("addr")))).collect(Collectors.toList());
                    break;
                case 1:
                    clientMapList_final = clientMapList.stream().filter(clientMap -> ccWebClientList.contains(clientMap.get("addr"))).collect(Collectors.toList());
                    break;
                case 2:
                    clientMapList_final = clientMapList.stream().filter(clientMap -> redisClientList.contains(clientMap.get("addr"))).collect(Collectors.toList());
                    break;
                case 3:
                    clientMapList_final = clientMapList;
                    break;
            }
            model.addAttribute("clientMapList", clientMapList_final);
        }
        return new ModelAndView("instance/instanceClientList");
    }

    @RequestMapping("/command")
    public ModelAndView command(HttpServletRequest request, HttpServletResponse response, Model model, Integer admin, Long instanceId, Long appId) {
        if (instanceId != null && instanceId > 0) {
            model.addAttribute("instanceId", instanceId);
        }
        return new ModelAndView("instance/instanceCommand");
    }

    @RequestMapping("/commandExecute")
    public ModelAndView commandExecute(HttpServletRequest request, HttpServletResponse response, Model model, Integer admin, Long instanceId, Long appId) {
        if (instanceId != null && instanceId > 0) {
            model.addAttribute("instanceId", instanceId);
            String command = request.getParameter("command");
            String result = instanceStatsCenter.executeCommand(instanceId, command);
            model.addAttribute("result", result);
        } else {
            model.addAttribute("result", "error");
        }
        return new ModelAndView("instance/commandExecute");
    }

}