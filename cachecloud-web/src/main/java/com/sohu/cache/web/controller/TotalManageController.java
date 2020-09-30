package com.sohu.cache.web.controller;

import com.sohu.cache.dao.QuartzDao;
import com.sohu.cache.dao.TaskQueueDao;
import com.sohu.cache.entity.*;
import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.stats.app.AppStatsCenter;
import com.sohu.cache.task.constant.ResourceEnum;
import com.sohu.cache.task.constant.TaskQueueEnum.TaskStatusEnum;
import com.sohu.cache.web.enums.StatEnum;
import com.sohu.cache.web.enums.SuccessEnum;
import com.sohu.cache.web.enums.TriggerStateEnum;
import com.sohu.cache.web.service.ResourceService;
import com.sohu.cache.web.util.Page;
import com.sohu.cache.web.vo.AppDetailVO;
import com.sohu.cache.web.vo.MachineStatsVo;
import org.apache.commons.collections.MapUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 全局统计
 *
 * @author leifu
 * @Time 2014年10月14日
 */
@Controller
@RequestMapping("manage/total")
public class TotalManageController extends BaseController {

    @Resource(name = "appStatsCenter")
    private AppStatsCenter appStatsCenter;

    @Resource
    private MachineCenter machineCenter;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private TaskQueueDao taskQueueDao;

    @Autowired
    private QuartzDao quartzDao;

    /**
     * 应用运维
     */
    @RequestMapping(value = "/list")
    public ModelAndView doTotalList(HttpServletRequest request,
                                    HttpServletResponse response, String appParam, Model model, AppSearch appSearch) {

        AppUser currentUser = getUserInfo(request);
        // 获取所有有效版本信息
        List<SystemResource> redisVersionList = resourceService.getResourceList(ResourceEnum.REDIS.getValue());
        //appParam 判断是查询appid还是应用名称
        if (!StringUtils.isEmpty(appParam)) {
            if (StringUtils.isNumeric(appParam)) {
                appSearch.setAppId(Long.parseLong(appParam));
            } else {
                appSearch.setAppName(appParam);
            }
        }
        //分页
        int totalCount = appService.getAppDescCount(currentUser, appSearch);
        int pageNo = NumberUtils.toInt(request.getParameter("pageNo"), 1);
        int pageSize = NumberUtils.toInt(request.getParameter("pageSize"), 20);
        Page page = new Page(pageNo, pageSize, totalCount);
        appSearch.setPage(page);

        List<AppDesc> apps = appService.getAppDescList(currentUser, appSearch);
        List<AppDetailVO> appDetailList = new ArrayList<AppDetailVO>();

        if (apps != null && apps.size() > 0) {
            for (AppDesc appDesc : apps) {
                AppDetailVO appDetail = appStatsCenter.getAppDetail(appDesc.getAppId());
                appDetailList.add(appDetail);
            }
        } else {
            //如果没有查询结果
            page.setTotalCount(0);
        }
        model.addAttribute("apps", apps);
        model.addAttribute("appDetailList", appDetailList);
        model.addAttribute("list", apps);
        model.addAttribute("appOperateActive", SuccessEnum.SUCCESS.value());
        model.addAttribute("appParam", appParam);
        model.addAttribute("page", page);
        model.addAttribute("redisVersionList", redisVersionList);

        return new ModelAndView("manage/total/list");
    }

    /**
     * 全局统计
     */
    @RequestMapping(value = "/statlist")
    public ModelAndView doStatList(HttpServletRequest request,
                                    HttpServletResponse response, Model model) {

        // 一.应用及内存统计
        Map<String, Object> appTotalStat = appStatsCenter.getAppTotalStat();
        model.addAttribute("totalRunningApps", MapUtils.getString(appTotalStat, StatEnum.TOTAL_EFFETIVE_APP.value(), "0"));
        model.addAttribute("totalMachineCount", MapUtils.getString(appTotalStat, StatEnum.TOTAL_MACHINE_NUM.value(), "0"));
        model.addAttribute("totalRunningInstance", MapUtils.getString(appTotalStat, StatEnum.TOTAL_INSTANCE_NUM.value(), "0"));
        model.addAttribute("redisTypeCount", MapUtils.getString(appTotalStat, StatEnum.REDIS_VERSION_COUNT.value()));
        model.addAttribute("redisDistributeList", MapUtils.getString(appTotalStat, StatEnum.REDIS_VERSION_DISTRIBUTE.value()));
        model.addAttribute("machineMemoryDistributeList", MapUtils.getString(appTotalStat, StatEnum.MACHINE_USEDMEMORY_DISTRIBUTE.value()));
        model.addAttribute("maxMemoryDistributeList", MapUtils.getString(appTotalStat, StatEnum.MACHINE_MAXMEMORY_DISTRIBUTE.value()));
        model.addAttribute("roomDistributeList", MapUtils.getString(appTotalStat, StatEnum.MACHIEN_ROOM_DISTRIBUTE.value()));
        List<MachineStatsVo> machineStatsVoList = machineCenter.getmachineStatsVoList();
        model.addAttribute("machineStatsVoList", machineStatsVoList);

        //二、quartz相关
        int triggerWaitingCount = quartzDao.getTriggerStateCount(TriggerStateEnum.WAITING.getState());
        int triggerErrorCount = quartzDao.getTriggerStateCount(TriggerStateEnum.ERROR.getState());
        int triggerPausedCount = quartzDao.getTriggerStateCount(TriggerStateEnum.PAUSED.getState());
        int triggerAcquiredCount = quartzDao.getTriggerStateCount(TriggerStateEnum.ACQUIRED.getState());
        int triggerBlockedCount = quartzDao.getTriggerStateCount(TriggerStateEnum.BLOCKED.getState());
        int misfireCount = quartzDao.getMisFireTriggerCount();
        int triggerTotalCount = triggerWaitingCount + triggerErrorCount + triggerPausedCount + triggerAcquiredCount + triggerBlockedCount;

        model.addAttribute("triggerWaitingCount", triggerWaitingCount);
        model.addAttribute("triggerErrorCount", triggerErrorCount);
        model.addAttribute("triggerPausedCount", triggerPausedCount);
        model.addAttribute("triggerAcquiredCount", triggerAcquiredCount);
        model.addAttribute("triggerBlockedCount", triggerBlockedCount);
        model.addAttribute("misfireCount", misfireCount);
        model.addAttribute("triggerTotalCount", triggerTotalCount);

        //三、任务相关
        int newTaskCount = taskQueueDao.getStatusCount(TaskStatusEnum.NEW.getStatus());
        int runningTaskCount = taskQueueDao.getStatusCount(TaskStatusEnum.RUNNING.getStatus());
        int abortTaskCount = taskQueueDao.getStatusCount(TaskStatusEnum.ABORT.getStatus());
        int successTaskCount = taskQueueDao.getStatusCount(TaskStatusEnum.SUCCESS.getStatus());
        int totalTaskCount = newTaskCount + runningTaskCount + abortTaskCount + successTaskCount;
        model.addAttribute("newTaskCount", newTaskCount);
        model.addAttribute("runningTaskCount", runningTaskCount);
        model.addAttribute("abortTaskCount", abortTaskCount);
        model.addAttribute("successTaskCount", successTaskCount);
        model.addAttribute("totalTaskCount", totalTaskCount);

        model.addAttribute("totalActive", SuccessEnum.SUCCESS.value());
        return new ModelAndView("manage/totalstat/list");
    }

}
