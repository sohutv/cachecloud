package com.sohu.cache.web.controller;

import com.sohu.cache.constant.MachineInfoEnum;
import com.sohu.cache.dao.AppClientStatisticGatherDao;
import com.sohu.cache.dao.AppDao;
import com.sohu.cache.entity.AppClientStatisticGather;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.entity.TimeBetween;
import com.sohu.cache.task.tasks.daily.TopologyExamTask;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.web.enums.SuccessEnum;
import com.sohu.cache.web.service.AppService;
import com.sohu.cache.web.util.DateUtil;
import com.sohu.cache.web.vo.AppDetailVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 应用常用统计
 *
 * @author leifu
 * @Time 2014年10月14日
 */
@Controller
@RequestMapping("manage/app/stat")
public class AppStatController extends BaseController {

    @Resource(name = "appService")
    private AppService appService;
    @Resource
    private AppDao appDao;
    @Autowired
    private AppClientStatisticGatherDao appClientStatisticGatherDao;
    @Autowired
    TopologyExamTask topologyExamTask;

    @RequestMapping(value = "/list")
    public ModelAndView doAppStatsList(HttpServletRequest request,
                                       HttpServletResponse response, Model model) {
        //获取tab
        int tabId = NumberUtils.toInt(request.getParameter("tabId"), 0);
        model.addAttribute("tabId", tabId);

        //获取appId，判断有无
        long appId = NumberUtils.toLong(request.getParameter("appId"), -1l);
        model.addAttribute("appId", appId == -1l ? "" : appId);
        if (appId != -1l) {
            AppDesc appDesc = appDao.getAppDescById(appId);
            if (appDesc == null) {
                return new ModelAndView("manage/appStat/list");
            }
        }
        //获取searchDate，判断有无
        String searchDate = request.getParameter("searchDate");
        TimeBetween timeBetween = new TimeBetween();
        try {
            timeBetween = DateUtil.fillWithDateFormat(searchDate);
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
        }
        searchDate = timeBetween.getFormatStartDate();
        model.addAttribute("searchDate", searchDate);

        //appDescList
        List<AppDesc> appDescList = appDao.getOnlineApps();
        model.addAttribute("list", appDescList);


        switch (tabId) {
            case 0: {
                //appClientGatherStatMap
                Map<Long, Map<String, Object>> appClientGatherStatMap = appService.getAppClientStatGather(appId, searchDate);
                model.addAttribute("appClientGatherStatMap", appClientGatherStatMap);
                break;
            }
        }

        model.addAttribute("appStatActive", SuccessEnum.SUCCESS.value());
        model.addAttribute("collectAlert", "(请等待" + ConstUtils.MACHINE_STATS_CRON_MINUTE + "分钟)");
        return new ModelAndView("manage/appStat/list");
    }


    @RequestMapping(value = "/list/server")
    public ModelAndView doAppStatsListForServer(HttpServletRequest request,
                                                HttpServletResponse response, Model model) {
        //获取tab
        int tabId = NumberUtils.toInt(request.getParameter("tabId"), 1);
        model.addAttribute("tabId", tabId);

        //获取appId，判断有无
        long appId = NumberUtils.toLong(request.getParameter("appId"), -1l);
        model.addAttribute("appId", appId == -1l ? "" : appId);
        if (appId != -1l) {
            AppDesc appDesc = appDao.getAppDescById(appId);
            if (appDesc == null) {
                return new ModelAndView("manage/appStat/listServer");
            }
        }
        //获取searchDate，判断有无
        String searchDate = request.getParameter("searchDate");
        TimeBetween timeBetween = new TimeBetween();
        try {
            timeBetween = DateUtil.fillWithDateFormat(searchDate);
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
        }
        searchDate = timeBetween.getFormatStartDate();
        model.addAttribute("searchDate", searchDate);

        if(tabId == 1 || tabId == 2 || tabId == 3){
            //appDescList
            List<AppDesc> appDescList = appDao.getOnlineApps();
            appDescList.forEach(appDesc -> {
                String versionName = Optional.ofNullable(resourceService.getResourceById(appDesc.getVersionId())).map(ver -> ver.getName()).orElse("");
                appDesc.setVersionName(versionName);
            });
            model.addAttribute("appDescList", appDescList);

            //appDetailVOMap
            Map<Long, AppDetailVO> appDetailVOMap = appStatsCenter.getOnlineAppDetails();
            model.addAttribute("appDetailVOMap", appDetailVOMap);

            //appClientGatherStatMap
            Map<Long, Map<String, Object>> appClientGatherStatMap = appService.getAppClientStatGather(appId, searchDate);
            model.addAttribute("appClientGatherStatMap", appClientGatherStatMap);
        }
        //机器环境检查
        if(tabId == 4 ) {
            SimpleDateFormat searchFormat = new SimpleDateFormat("yyyy-MM-dd");
            Map<String, Object> machineEnvMap = null;
            try {
                machineEnvMap = machineCenter.getAllMachineEnv(searchFormat.parse(searchDate), MachineInfoEnum.MachineTypeEnum.CONTAINER.getValue());
            } catch (ParseException e) {
                logger.error("machineCenter get container date:{} error :{}",searchDate,e.getMessage());
            }
            model.addAttribute("machineEnvMap", machineEnvMap);
        }
        // 宿主环境检查
        if(tabId == 5) {
            SimpleDateFormat searchFormat = new SimpleDateFormat("yyyy-MM-dd");
            Map<String, Object> machineEnvMap = null;
            try {
                machineEnvMap = machineCenter.getAllMachineEnv(searchFormat.parse(searchDate), MachineInfoEnum.MachineTypeEnum.HOST.getValue());
            } catch (ParseException e) {
                logger.error("machineCenter get host date:{} error :{}",searchDate,e.getMessage());
            }
            model.addAttribute("machineEnvMap", machineEnvMap);
        }

        model.addAttribute("appStatServerActive", SuccessEnum.SUCCESS.value());
        return new ModelAndView("manage/appStat/listServer");
    }

    @RequestMapping("/clientList")
    public ModelAndView clientList(HttpServletRequest request, HttpServletResponse response, Model model, Long appId) {

        List<String> clientList = new ArrayList<>();

        List<InstanceInfo> instanceList = appService.getAppOnlineInstanceInfo(appId);
        for (InstanceInfo instance : instanceList) {
            if ("master".equals(instance.getRoleDesc())) {
                clientList = redisCenter.getClientList(instance.getId());
                break;
            }
        }
        Set<String> clientSet = clientList.stream()
                .filter(clientInfo -> StringUtils.isNotBlank(clientInfo))
                .filter(clientInfo -> clientInfo.contains("flags=N"))
//                .filter(clientInfo -> !clientInfo.contains("cmd=client"))
                .map(clientInfo -> clientInfo.split(" |:")[1])
                .collect(Collectors.toSet());

        model.addAttribute("clientSet", clientSet.stream().collect(Collectors.joining("\n")));
        return new ModelAndView("");
    }

    @RequestMapping("/topologyUpdate")
    public ModelAndView topologyUpdate(HttpServletRequest request, HttpServletResponse response, Model model) {
        try {
            List<AppClientStatisticGather> topologyExamList = topologyExamTask.checkAppsTopology(new Date());
            if (CollectionUtils.isNotEmpty(topologyExamList)) {
                appClientStatisticGatherDao.batchSaveTopologyExam(topologyExamList);
            }
            model.addAttribute("status", 1);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            model.addAttribute("status", 0);
        }
        return new ModelAndView("");
    }

}
