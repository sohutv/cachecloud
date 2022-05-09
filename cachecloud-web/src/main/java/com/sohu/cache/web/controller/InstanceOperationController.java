package com.sohu.cache.web.controller;

import com.sohu.cache.constant.AppStatusEnum;
import com.sohu.cache.constant.ErrorMessageEnum;
import com.sohu.cache.dao.MachineDao;
import com.sohu.cache.entity.*;
import com.sohu.cache.task.constant.ResourceEnum;
import com.sohu.cache.util.StringUtil;
import com.sohu.cache.web.enums.CompareTypeEnum;
import com.sohu.cache.web.enums.SuccessEnum;
import com.sohu.cache.web.service.AppRedisCommandCheckService;
import com.sohu.cache.web.service.AppRedisConfigCheckService;
import com.sohu.cache.web.service.AppScrollRestartService;
import com.sohu.cache.web.vo.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: zengyizhao
 * @DateTime: 2021/9/27 17:34
 * @Description: 实例运维检测
 */
@Controller
@RequestMapping("manage/instance")
public class InstanceOperationController extends BaseController {

    @Autowired
    private AppRedisConfigCheckService appRedisConfigCheckService;

    @Autowired
    private AppRedisCommandCheckService appRedisCommandCheckService;

    @Autowired
    private AppScrollRestartService appScrollRestartService;

    @Autowired
    private MachineDao machineDao;

    /**
     * 实例运维页面
     * @param request
     * @param model
     * @return
     */
    @RequestMapping(value = "/opsList")
    public ModelAndView opsList(HttpServletRequest request, Model model) {
        AppUser currentUser = getUserInfo(request);
        //获取tab
        int tabId = NumberUtils.toInt(request.getParameter("tabId"), 1);
        model.addAttribute("tabId", tabId);
        model.addAttribute("insOperateActive", SuccessEnum.SUCCESS.value());
        if(tabId == 1){
            List<SystemResource> versionList = resourceService.getResourceList(ResourceEnum.REDIS.getValue());
            List<RedisConfigCheckResult> redisConfigCheckResult = appRedisConfigCheckService.getRedisConfigCheckResult();

            CompareTypeEnum[] values = CompareTypeEnum.values();
            List<CompareTypeEnum> compareTypeEnums = Arrays.asList(values);
            compareTypeEnums.sort((o1, o2) -> o1.getType());
            List<AppDesc> allAppDesc = appService.getAllAppDesc();
            allAppDesc = allAppDesc.stream().filter(appDesc -> appDesc.getStatus() == AppStatusEnum.STATUS_PUBLISHED.getStatus()).collect(Collectors.toList());
            model.addAttribute("appList", allAppDesc);
            model.addAttribute("redisVersionList", versionList);
            model.addAttribute("compareTypeList", compareTypeEnums);
            model.addAttribute("checkResultList", redisConfigCheckResult);
        }
        if(tabId == 2){
            List<RedisCommandCheckResult> redisCommandCheckResult = appRedisCommandCheckService.getRedisCommandCheckResult();
            model.addAttribute("commandCheckResult", redisCommandCheckResult);
        }
        if(tabId == 3){
            String appIdStr = request.getParameter("appId");
            String pageNoStr = request.getParameter("pageNo");
            Integer pageNo = 1;
            Long appId = null;
            if(!StringUtil.isBlank(appIdStr)) {
                appId = Long.valueOf(appIdStr);
            }
            ConfigRestartRecord configRestartRecord = new ConfigRestartRecord();
            configRestartRecord.setAppId(appId);
            if(!StringUtil.isBlank(pageNoStr)){
                pageNo = Integer.valueOf(pageNoStr);
            }
            int pageSize = 10;
            List<ConfigRestartRecord> configRestartRecordByCondition = appScrollRestartService.getConfigRestartRecordByCondition(model, configRestartRecord, pageNo, pageSize);
            List<InstanceInfo> instanceInfoList = new ArrayList<>();
            if(appId != null){
                instanceInfoList = appService.getAppBasicInstanceInfo(appId);
            }else{
                Set<Long> appIdSet = new HashSet<>();
                for (ConfigRestartRecord record : configRestartRecordByCondition) {
                    if(appIdSet.contains(record.getAppId())){
                        continue;
                    }
                    appIdSet.add(record.getAppId());
                    instanceInfoList.addAll(appService.getAppBasicInstanceInfo(record.getAppId()));
                }
            }
            Map<Integer, InstanceInfo> instanceInfoMap = instanceInfoList.stream().collect(Collectors.toMap(InstanceInfo::getId, Function.identity(), (v1, v2) -> v1));
            model.addAttribute("restartRecordList", configRestartRecordByCondition);
            model.addAttribute("appId", appId);
            model.addAttribute("instanceInfoMap", instanceInfoMap);
        }
        return new ModelAndView("manage/instanceOps/instanceOpsIndex");
    }

    /**
     * 配置检测
     * @param request
     * @param model
     * @return
     */
    @RequestMapping(value = "/configCheck")
    @ResponseBody
    public ModelAndView configCheck(HttpServletRequest request, Model model) {
        AppUser appUser = getUserInfo(request);
        AppRedisConfigCheckVo checkVo = getRedisConfigCheck(request);
        RedisConfigCheckResult configCheckResults = appRedisConfigCheckService.checkRedisConfig(appUser, checkVo);
        if(configCheckResults != null){
            model.addAttribute("status", SuccessEnum.SUCCESS.value());
        }else{
            model.addAttribute("message", ErrorMessageEnum.INNER_ERROR_MSG.getMessage());
        }
        return new ModelAndView("");
    }


    /**
     * 配置检测
     * @param request
     * @param model
     * @return
     */
    @RequestMapping(value = "/getMachineList")
    @ResponseBody
    public ModelAndView getMachineList(HttpServletRequest request, Model model,
                                       String ip, String realIp, Integer searchType) {
        List<MachineInfo> machineList = machineDao.getMachineListByCondition(ip, realIp);
        Set<String> ipSet = new HashSet<>();
        if(searchType == 1){
            ipSet = machineList.stream().map(machineInfo -> machineInfo.getRealIp()).collect(Collectors.toSet());
        }else if(searchType == 2){
            ipSet = machineList.stream().map(machineInfo -> machineInfo.getIp()).collect(Collectors.toSet());
        }
        model.addAttribute("ipSet", ipSet);
        model.addAttribute("status", SuccessEnum.SUCCESS.value());
        return new ModelAndView("");
    }

    /**
     * 获取某次配置检测结果
     * @param request
     * @param model
     * @param uuid
     * @return
     */
    @RequestMapping(value = "/getConfigCheck")
    public ModelAndView getConfigCheck(HttpServletRequest request, Model model, String uuid) {
        AppUser appUser = getUserInfo(request);
        List<AppRedisConfigCheckResult> redisConfigCheckResult = appRedisConfigCheckService.getRedisConfigCheckDetailResult(uuid);
        List<SystemResource> versionList = resourceService.getResourceList(ResourceEnum.REDIS.getValue());
        model.addAttribute("redisVersionList", versionList);
        CompareTypeEnum[] values = CompareTypeEnum.values();
        List<CompareTypeEnum> compareTypeEnums = Arrays.asList(values);
        compareTypeEnums.sort((o1, o2) -> o1.getType());
        model.addAttribute("compareTypeList", compareTypeEnums);
        model.addAttribute("checkResultList", redisConfigCheckResult);
        return new ModelAndView("manage/instanceOps/instanceConfigCheckList");
    }

    private AppRedisConfigCheckVo getRedisConfigCheck(HttpServletRequest request) {
        String appIdStr = request.getParameter("appId");
        Long appId = null;
        if(StringUtils.isNotEmpty(appIdStr)){
            appId = Long.parseLong(appIdStr);
        }
        String configName = request.getParameter("configName");
        String versionIdStr = request.getParameter("versionId");
        Integer versionId = null;
        if(StringUtils.isNotEmpty(versionIdStr)){
            versionId = Integer.parseInt(versionIdStr);
        }
        String expectValue = request.getParameter("expectValue");
        int compareType = NumberUtils.toInt(request.getParameter("compareType"));
        // 生成对象
        AppRedisConfigCheckVo appRedisConfigCheckVo = new AppRedisConfigCheckVo();
        appRedisConfigCheckVo.setAppId(appId);
        appRedisConfigCheckVo.setConfigName(configName);
        appRedisConfigCheckVo.setVersionId(versionId);
        appRedisConfigCheckVo.setCompareType(compareType);
        appRedisConfigCheckVo.setExpectValue(expectValue);
        return appRedisConfigCheckVo;
    }

    /**
     * 命令检测
     * @param request
     * @param model
     * @return
     */
    @RequestMapping(value = "/commandCheck")
    @ResponseBody
    public ModelAndView doStatList(HttpServletRequest request, Model model) {
        AppUser appUser = getUserInfo(request);
        AppRedisCommandCheckVo checkVo = getRedisCommandCheck(request);
        appRedisCommandCheckService.checkRedisCommand(appUser, checkVo);
        model.addAttribute("status", SuccessEnum.SUCCESS.value());
        return new ModelAndView("");
    }

    private AppRedisCommandCheckVo getRedisCommandCheck(HttpServletRequest request) {
        String machineIps = request.getParameter("machineIps");
        String podIp = request.getParameter("podIp");
        String command = request.getParameter("command");
        String checkTypeStr = request.getParameter("checkType");
        Integer checkType = null;
        if(StringUtils.isNotEmpty(checkTypeStr)){
            checkType = Integer.valueOf(checkTypeStr);
        }
        String infoIndicate = request.getParameter("infoIndicate");
        String indicateName = request.getParameter("indicateName");
        String maxTryStr = request.getParameter("maxTry");
        Integer maxTry = null;
        if(StringUtils.isNotEmpty(maxTryStr)){
            maxTry = Integer.valueOf(maxTryStr);
        }
        String expectValue = request.getParameter("expectValue");
        String minuteInternalStr = request.getParameter("minuteInternal");
        Integer minuteInternal = null;
        if(StringUtils.isNotEmpty(minuteInternalStr)){
            minuteInternal = Integer.valueOf(minuteInternalStr);
        }
        AppRedisCommandCheckVo checkVo = new AppRedisCommandCheckVo();
        checkVo.setMachineIps(machineIps);
        checkVo.setPodIp(podIp);
        checkVo.setCommand(command);
        checkVo.setCheckType(checkType);
        checkVo.setInfoIndicate(infoIndicate);
        checkVo.setIndicateName(indicateName);
        checkVo.setExpectValue(expectValue);
        checkVo.setMaxTry(maxTry);
        checkVo.setMinuteInternal(minuteInternal);
        return checkVo;
    }

    /**
     * 查询命令检测
     * @param request
     * @param model
     * @param uuid
     * @return
     */
    @RequestMapping(value = "/getCommandCheck")
    public ModelAndView getCommandCheck(HttpServletRequest request, Model model, String uuid) {
        AppUser appUser = getUserInfo(request);
        AppRedisCommandCheckResult appRedisCommandCheckResult = appRedisCommandCheckService.getRedisCommandCheckDetailResult(uuid);
        model.addAttribute("checkResult", appRedisCommandCheckResult);
        return new ModelAndView("manage/instanceOps/instanceCommandCheckList");
    }


}
