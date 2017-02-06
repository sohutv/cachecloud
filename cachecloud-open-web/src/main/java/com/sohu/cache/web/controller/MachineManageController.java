package com.sohu.cache.web.controller;

import com.sohu.cache.constant.MachineInfoEnum;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.entity.InstanceStats;
import com.sohu.cache.entity.MachineInfo;
import com.sohu.cache.entity.MachineStats;
import com.sohu.cache.machine.MachineDeployCenter;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.TypeUtil;
import com.sohu.cache.web.enums.SuccessEnum;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 机器管理
 *
 * @author leifu
 * @Time 2014年10月14日
 */
@Controller
@RequestMapping("manage/machine")
public class MachineManageController extends BaseController{

    @Resource
    private MachineDeployCenter machineDeployCenter;

    @RequestMapping(value = "/list")
    public ModelAndView doMachineList(HttpServletRequest request,
                                HttpServletResponse response, Model model, String ipLike) {
        List<MachineStats> machineList = machineCenter.getMachineStats(ipLike);
        Map<String, Integer> machineInstanceCountMap = machineCenter.getMachineInstanceCountMap();
        model.addAttribute("list", machineList);
        model.addAttribute("ipLike", ipLike);
        model.addAttribute("machineActive", SuccessEnum.SUCCESS.value());
        model.addAttribute("collectAlert", "(请等待" + ConstUtils.MACHINE_STATS_CRON_MINUTE + "分钟)");
        model.addAttribute("machineInstanceCountMap", machineInstanceCountMap);
        return new ModelAndView("manage/machine/list");
    }
    
    /**
     * 机器实例展示
     * @param ip
     * @return
     */
    @RequestMapping(value = "/machineInstances")
    public ModelAndView doMachineInstances(HttpServletRequest request,
                                HttpServletResponse response, Model model, String ip) {
        //机器以及机器下面的实例信息
        MachineInfo machineInfo = machineCenter.getMachineInfoByIp(ip);
        List<InstanceInfo> instanceList = machineCenter.getMachineInstanceInfo(ip);
        List<InstanceStats> instanceStatList = machineCenter.getMachineInstanceStatsByIp(ip);       
        //统计信息
        fillInstanceModel(instanceList, instanceStatList, model);
        
        model.addAttribute("machineInfo", machineInfo);
        model.addAttribute("machineActive", SuccessEnum.SUCCESS.value());
        return new ModelAndView("manage/machine/machineInstances");
    }
    
    /**
     * 检查机器下是否有存活的实例
     * @param ip
     * @return
     */
    @RequestMapping(value = "/checkMachineInstances")
    public ModelAndView doCheckMachineInstances(HttpServletRequest request,
                                HttpServletResponse response, Model model, String ip) {
        List<InstanceInfo> instanceList = machineCenter.getMachineInstanceInfo(ip);
        model.addAttribute("machineHasInstance", CollectionUtils.isNotEmpty(instanceList));
        return new ModelAndView("");
    }
    

    @RequestMapping(value = "/add", method = {RequestMethod.POST})
    public ModelAndView doAdd(HttpServletRequest request,
                              HttpServletResponse response, Model model) {
        MachineInfo machineInfo = new MachineInfo();
        machineInfo.setIp(request.getParameter("ip"));
        machineInfo.setRoom(request.getParameter("room"));
        machineInfo.setMem(NumberUtils.toInt(request.getParameter("mem"), 0));
        machineInfo.setCpu(NumberUtils.toInt(request.getParameter("cpu"), 0));
        machineInfo.setVirtual(NumberUtils.toInt(request.getParameter("virtual"), 0));
        machineInfo.setRealIp(request.getParameter("realIp"));
        machineInfo.setType(NumberUtils.toInt(request.getParameter("machineType"), 0));
        machineInfo.setExtraDesc(request.getParameter("extraDesc"));
        machineInfo.setCollect(NumberUtils.toInt(request.getParameter("collect"), 1));
        
        Date date = new Date();
        machineInfo.setSshUser(ConstUtils.USERNAME);
        machineInfo.setSshPasswd(ConstUtils.PASSWORD);
        machineInfo.setServiceTime(date);
        machineInfo.setModifyTime(date);
        machineInfo.setAvailable(MachineInfoEnum.AvailableEnum.YES.getValue());
        boolean isSuccess = machineDeployCenter.addMachine(machineInfo);
        model.addAttribute("result", isSuccess);
        return new ModelAndView("");
    }
    
    @RequestMapping(value = "/delete")
    public ModelAndView doDelete(HttpServletRequest request, HttpServletResponse response, Model model) {
        String machineIp = request.getParameter("machineIp");
        if (StringUtils.isNotBlank(machineIp)) {
            MachineInfo machineInfo = machineCenter.getMachineInfoByIp(machineIp);
            boolean success = machineDeployCenter.removeMachine(machineInfo);
            logger.warn("delete machine {}, result is {}", machineIp, success);
        } else {
            logger.warn("machineIp is empty!");
        }
        return new ModelAndView("redirect:/manage/machine/list");
    }
    
    
    /**
     * 实例统计信息
     * @param appAudit
     * @param model
     */
    protected void fillInstanceModel(List<InstanceInfo> instanceList, List<InstanceStats> appInstanceStats, Model model) {
        Map<String, MachineStats> machineStatsMap = new HashMap<String, MachineStats>();
        Map<String, Long> machineCanUseMem = new HashMap<String, Long>();
        Map<String, InstanceStats> instanceStatsMap = new HashMap<String, InstanceStats>();
        Map<Long, AppDesc> appInfoMap = new HashMap<Long, AppDesc>();

        for (InstanceStats instanceStats : appInstanceStats) {
            instanceStatsMap.put(instanceStats.getIp() + ":" + instanceStats.getPort(), instanceStats);
            appInfoMap.put(instanceStats.getAppId(), appService.getByAppId(instanceStats.getAppId()));
        }

        for (InstanceInfo instanceInfo : instanceList) {
            if (TypeUtil.isRedisSentinel(instanceInfo.getType())) {
                continue;
            }
            String ip = instanceInfo.getIp();
            if (machineStatsMap.containsKey(ip)) {
                continue;
            }
            List<MachineStats> machineStatsList = machineCenter.getMachineStats(ip);
            MachineStats machineStats = null;
            for (MachineStats stats : machineStatsList) {
                if (stats.getIp().equals(ip)) {
                    machineStats = stats;
                    machineStatsMap.put(ip, machineStats);
                    break;
                }
            }
            MachineStats ms = machineCenter.getMachineMemoryDetail(ip);
            machineCanUseMem.put(ip, ms.getMachineMemInfo().getLockedMem());
        }
        model.addAttribute("appInfoMap", appInfoMap);
        
        model.addAttribute("machineCanUseMem", machineCanUseMem);
        model.addAttribute("machineStatsMap", machineStatsMap);

        model.addAttribute("instanceList", instanceList);
        model.addAttribute("instanceStatsMap", instanceStatsMap);
    }
    
    
}
