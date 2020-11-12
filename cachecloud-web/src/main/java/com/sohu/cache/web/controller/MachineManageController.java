package com.sohu.cache.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.sohu.cache.constant.MachineInfoEnum;
import com.sohu.cache.entity.*;
import com.sohu.cache.machine.MachineDeployCenter;
import com.sohu.cache.task.TaskService;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.TypeUtil;
import com.sohu.cache.web.enums.MachineTaskEnum;
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
import java.util.*;

/**
 * 机器管理
 *
 * @author leifu
 * @Time 2014年10月14日
 */
@Controller
@RequestMapping("manage/machine")
public class MachineManageController extends BaseController {

    private final static String COMMA = ",";
    @Resource
    private TaskService taskService;
    @Resource
    private MachineDeployCenter machineDeployCenter;

    @RequestMapping("/index")
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response, Model model,
                              String tabTag,
                              String ipLike, Integer versionId, Integer isInstall, Integer useType, Integer type, Integer k8sType, String realip) {
        model.addAttribute("tabTag", tabTag);
        model.addAttribute("isInstall", isInstall);
        model.addAttribute("versionId",versionId);
        model.addAttribute("useType", useType);
        model.addAttribute("ipLike", ipLike);
        model.addAttribute("k8sType", k8sType);
        model.addAttribute("type", type);
        model.addAttribute("realip", realip);
        model.addAttribute("machineActive", SuccessEnum.SUCCESS.value());
        return new ModelAndView("manage/machine/list");
    }

    @RequestMapping(value = "/pod/changelist")
    public ModelAndView doPodList(Model model, String ip) {

        List<MachineRelation> machineRelationList = machineDeployCenter.getMachineRelationList(ip);

        MachineInfo machineinfo = machineCenter.getMachineInfoByIp(ip);
        String realIp = machineinfo != null ? machineinfo.getRealIp() : "";
        logger.info("ip:{} ,realIp:{} ,pod change size:{}", ip, realIp, machineRelationList.size());
        model.addAttribute("ip", ip);
        model.addAttribute("realIp", realIp);
        model.addAttribute("relationList", machineRelationList);

        return new ModelAndView("manage/pod/list");
    }

    @RequestMapping(value = "/pod/add/syncTask", method = RequestMethod.POST)
    public ModelAndView doAddMachineSyncTask(Model model, HttpServletResponse response, String containerIp, String sourceIp, String targetIp, Integer relationId) {

        Map<String, Object> taskMap = new HashMap<String, Object>();
        try {
            if (!StringUtils.isEmpty(containerIp) && !StringUtils.isEmpty(sourceIp) && !StringUtils.isEmpty(targetIp)) {
                // 检查是否已经有同步任务
                SuccessEnum successEnum = machineDeployCenter.checkMachineSyncStatus(containerIp, sourceIp, MachineTaskEnum.SYNCING.getValue());
                if (successEnum.value() == SuccessEnum.NO_REPEAT.value()) {
                    long taskId = taskService.addMachineSyncTask(sourceIp, targetIp, containerIp, String.format("sourceMachine:%s tagetMachine:%s ", sourceIp, targetIp), 0);
                    if (taskId > 0) {
                        logger.info("add machine sync task:{} ", taskId);
                        taskMap.put("status", SuccessEnum.SUCCESS.value());
                        taskMap.put("taskId", taskId);
                        machineDeployCenter.updateMachineRelation(relationId, taskId, MachineTaskEnum.SYNCING.getValue());
                    }
                } else {
                    taskMap.put("status", SuccessEnum.FAIL.value());
                    taskMap.put("message", "containerIp:" + containerIp + ",sourceIp:" + sourceIp + " ,任务:" + successEnum.info());
                    machineDeployCenter.updateMachineRelation(relationId, null, MachineTaskEnum.SYNC_FAILED.getValue());
                }
            } else {
                taskMap.put("status", SuccessEnum.FAIL.value());
                taskMap.put("message", "参数验证失败 containerIp:" + containerIp + ",sourceIp:" + sourceIp + ",targetIp:" + targetIp);
            }
        } catch (Exception e) {
            taskMap.put("status", SuccessEnum.FAIL.value());
            taskMap.put("message", "add task exception:" + e.getMessage());
            logger.error(e.getMessage(), e);
        }
        sendMessage(response, JSONObject.toJSONString(taskMap));
        return null;
    }

    @RequestMapping(value = "/list")
    public ModelAndView doMachineList(HttpServletRequest request,
                                      HttpServletResponse response, Model model,
                                      String tabTag,
                                      String ipLike, Integer versionId, Integer isInstall, Integer useType, Integer type, Integer k8sType, String realip) {

        if (tabTag.equals("machine")) {
            List<MachineStats> machineList = machineCenter.getMachineStats(ipLike, useType, type, versionId, isInstall, k8sType, realip);
            Map<String, Integer> machineInstanceCountMap = machineCenter.getMachineInstanceCountMap();
            List<MachineRoom> roomList = machineCenter.getEffectiveRoom();
            model.addAttribute("roomList", roomList);
            model.addAttribute("list", machineList);
            model.addAttribute("isInstall", isInstall);
            model.addAttribute("versionId",versionId);
            model.addAttribute("useType", useType);
            model.addAttribute("ipLike", ipLike);
            model.addAttribute("k8sType", k8sType);
            model.addAttribute("type", type);
            model.addAttribute("realip", realip);
            model.addAttribute("machineActive", SuccessEnum.SUCCESS.value());
            model.addAttribute("collectAlert", "(请等待" + ConstUtils.MACHINE_STATS_CRON_MINUTE + "分钟)");
            model.addAttribute("machineInstanceCountMap", machineInstanceCountMap);

            return new ModelAndView("manage/machine/machineList");
        } else if (tabTag.equals("room")) {
            List<MachineRoom> roomList = machineCenter.getAllRoom();
            model.addAttribute("roomList", roomList);
            return new ModelAndView("manage/machine/roomList");
        }
        return new ModelAndView("");
    }

    /**
     * 机器实例展示
     *
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
     *
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
        machineInfo.setDisk(NumberUtils.toInt(request.getParameter("disk"), 0));
        machineInfo.setVirtual(NumberUtils.toInt(request.getParameter("virtual"), 0));
        machineInfo.setRealIp(request.getParameter("realIp"));
        machineInfo.setType(NumberUtils.toInt(request.getParameter("machineType"), 0));
        machineInfo.setExtraDesc(request.getParameter("extraDesc"));
        machineInfo.setCollect(NumberUtils.toInt(request.getParameter("collect"), 1));
        machineInfo.setVersionInstall(request.getParameter("versionInfo"));

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

    @RequestMapping(value = "/addMultiple", method = {RequestMethod.POST})
    public ModelAndView doAddMultiple(HttpServletRequest request,
                                      HttpServletResponse response, Model model) {
        boolean isSuccess = true;
        List<String> ipList = Arrays.asList(request.getParameter("ip").split(COMMA));
        List<String> realIpList = Arrays.asList(request.getParameter("realIp").split(COMMA));//ip与realIp一一对应
        for (int i = 0; i < ipList.size(); i++) {
            MachineInfo machineInfo = new MachineInfo();
            machineInfo.setIp(ipList.get(i));
            machineInfo.setRoom(request.getParameter("room"));
            machineInfo.setMem(NumberUtils.toInt(request.getParameter("mem"), 0));
            machineInfo.setCpu(NumberUtils.toInt(request.getParameter("cpu"), 0));
            machineInfo.setDisk(NumberUtils.toInt(request.getParameter("disk"), 0));
            machineInfo.setVirtual(NumberUtils.toInt(request.getParameter("virtual"), 0));
            machineInfo.setRealIp(i < realIpList.size() ? realIpList.get(i) : "");
            machineInfo.setType(NumberUtils.toInt(request.getParameter("machineType"), 0));
            machineInfo.setUseType(NumberUtils.toInt(request.getParameter("useType"), 0));
            machineInfo.setK8sType(NumberUtils.toInt(request.getParameter("k8sType"), 0));
            machineInfo.setExtraDesc(request.getParameter("extraDesc"));
            machineInfo.setCollect(NumberUtils.toInt(request.getParameter("collect"), 1));
            machineInfo.setVersionInstall(request.getParameter("versionInfo"));
            machineInfo.setRack(request.getParameter("rack"));

            Date date = new Date();
            machineInfo.setSshUser(ConstUtils.USERNAME);
            machineInfo.setSshPasswd(ConstUtils.PASSWORD);
            machineInfo.setServiceTime(date);
            machineInfo.setModifyTime(date);
            machineInfo.setAvailable(MachineInfoEnum.AvailableEnum.YES.getValue());
            if (!machineDeployCenter.addMachine(machineInfo)) {
                isSuccess = false;
                break;
            }
        }
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
        return new ModelAndView("redirect:/manage/machine/index?tabTag=machine");
    }


    /**
     * 实例统计信息
     *
     * @param
     * @param model
     */
    protected void fillInstanceModel(List<InstanceInfo> instanceList, List<InstanceStats> appInstanceStats, Model model) {
        Map<String, MachineStats> machineStatsMap = new HashMap<String, MachineStats>();
        Map<String, Long> machineCanUseMem = new HashMap<String, Long>();
        Map<String, InstanceStats> instanceStatsMap = new HashMap<String, InstanceStats>();
        Map<Long, AppDesc> appInfoMap = new HashMap<Long, AppDesc>();

        for (InstanceStats instanceStats : appInstanceStats) {
            instanceStatsMap.put(instanceStats.getIp() + ":" + instanceStats.getPort(), instanceStats);
            AppDesc appDesc = appService.getByAppId(instanceStats.getAppId());
            appDesc.setOfficer(userService.getOfficerName(appDesc.getOfficer()));
            appInfoMap.put(instanceStats.getAppId(), appDesc);
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


    @RequestMapping(value = "room/add", method = {RequestMethod.POST})
    public ModelAndView doRoomAdd(HttpServletRequest request,
                                  HttpServletResponse response, Model model) {
        MachineRoom room = new MachineRoom();
        room.setId(NumberUtils.toInt(request.getParameter("id")));
        room.setName(request.getParameter("name"));
        room.setStatus(NumberUtils.toInt(request.getParameter("status"), 1));
        room.setDesc(request.getParameter("desc"));
        room.setIpNetwork(request.getParameter("ipNetwork"));
        room.setOperator(request.getParameter("operator"));

        boolean isSuccess = machineDeployCenter.addMachineRoom(room);
        model.addAttribute("result", isSuccess);
        return new ModelAndView("");
    }

    @RequestMapping(value = "room/delete")
    public ModelAndView doRoomDelete(HttpServletRequest request, HttpServletResponse response, Model model) {
        int roomId = NumberUtils.toInt(request.getParameter("id"), -1);
        if (roomId > -1) {
            boolean success = machineDeployCenter.removeMachineRoom(roomId);
            logger.warn("delete machine {}, result is {}", roomId, success);
        } else {
            logger.warn("machineIp is empty!");
        }
        return new ModelAndView("redirect:/manage/machine/index?tabTag=room");
    }
}
