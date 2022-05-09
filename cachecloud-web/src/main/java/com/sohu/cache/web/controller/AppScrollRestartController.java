package com.sohu.cache.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.sohu.cache.constant.InstanceStatusEnum;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.AppUser;
import com.sohu.cache.entity.ConfigRestartRecord;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.redis.AssistRedisService;
import com.sohu.cache.web.enums.AppTypeEnum;
import com.sohu.cache.web.enums.ConfigRestartOperateEnum;
import com.sohu.cache.web.enums.RestartStatusEnum;
import com.sohu.cache.web.service.AppScrollRestartService;
import com.sohu.cache.web.service.AppService;
import com.sohu.cache.web.vo.AppRedisConfigVo;
import com.sohu.cache.web.vo.ExecuteResult;
import com.sohu.cache.web.vo.GeneralResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: 应用滚动重启
 * </p>
 *
 * @author zengyizhao
 * @version 1.0
 * @date 2021/9/13
 */
@Controller
@RequestMapping("/manage/app/restart")
public class AppScrollRestartController extends BaseController {

    @Autowired
    private AppService appService;

    @Autowired
    private AppScrollRestartService appScrollRestartService;

    @Autowired
    private AssistRedisService assistRedisService;

    private final static String RESTART_CONFIG_KEY = "restart:config:";

    /**
     * 查询滚动重启记录
     * @param model
     * @param appId
     * @return
     */
    @RequestMapping(value = "getRestartRecord", method = RequestMethod.GET)
    public ModelAndView getRestartRecord(Model model, Long appId, Integer pageNo) {
        ConfigRestartRecord configRestartRecord = new ConfigRestartRecord();
        configRestartRecord.setAppId(appId);
        if(pageNo == null){
            pageNo = 1;
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
        return new ModelAndView("manage/appOps/appRestartList");
    }

    /**
     * 滚动重启
     * @param appRedisConfigVo
     * @return
     */
    @RequestMapping(value = "scrollRestart", method = RequestMethod.POST)
    public void scrollRestart(HttpServletRequest request, HttpServletResponse response, @RequestBody AppRedisConfigVo appRedisConfigVo) {
        AppUser appUser = getUserInfo(request);
        if(assistRedisService.get(RESTART_CONFIG_KEY + appRedisConfigVo.getAppId()) != null){
            String json = JSONObject.toJSONString(GeneralResponse.error(500, "滚动重启/修改配置正在执行中，不允许重复操作。"));
            sendMessage(response, json);
            return;
        }
        // 1.获取应用信息
        AppDesc appDesc = appService.getByAppId(appRedisConfigVo.getAppId());
        //无有效应用时,或应用redis类型不符时，返回
        if(appDesc == null || appDesc.getType() != AppTypeEnum.REDIS_SENTINEL.getType() && appDesc.getType() != AppTypeEnum.REDIS_CLUSTER.getType()){
            String json = JSONObject.toJSONString(GeneralResponse.error(500, "参数有误，请确认。"));
            sendMessage(response, json);
            return;
        }
        // 2.获取当前应用下redis实例信息
        List<InstanceInfo> instanceList = appService.getAppBasicInstanceInfo(appRedisConfigVo.getAppId());
        //过滤出运行中的实例
        instanceList = instanceList.stream().filter(instanceInfo -> instanceInfo.getStatus() == InstanceStatusEnum.GOOD_STATUS.getStatus()).collect(Collectors.toList());
        if(assistRedisService.get(RESTART_CONFIG_KEY + appRedisConfigVo.getAppId()) != null){
            String json = JSONObject.toJSONString(GeneralResponse.error(500, "滚动重启/修改配置正在执行中，不允许重复操作。"));
            sendMessage(response, json);
            return;
        }
        //处理实例信息，封装主从信息
        boolean executeFlag = appScrollRestartService.handleAppInstanceInfo(instanceList, appDesc);
        if(!executeFlag){
            String json = JSONObject.toJSONString(GeneralResponse.error(500, "未正确获取到实例主从信息，请重试。"));
            sendMessage(response, json);
            return;
        }
        if(assistRedisService.get(RESTART_CONFIG_KEY + appRedisConfigVo.getAppId()) != null){
            String json = JSONObject.toJSONString(GeneralResponse.error(500, "滚动重启/修改配置正在执行中，不允许重复操作。"));
            sendMessage(response, json);
            return;
        }
        //校验有效实例，并校验传入的instancId全部有效，无效直接返回
        boolean check = this.checkPointedInstance(instanceList, appRedisConfigVo.getInstanceList());
        if(!check){
            String json = JSONObject.toJSONString(GeneralResponse.error(500, "实例不满足此操作。"));
            sendMessage(response, json);
            return;
        }
        if(assistRedisService.get(RESTART_CONFIG_KEY + appRedisConfigVo.getAppId()) != null){
            String json = JSONObject.toJSONString(GeneralResponse.error(500, "滚动重启/修改配置正在执行中，不允许重复操作。"));
            sendMessage(response, json);
            return;
        }
        ExecuteResult executeResult = appScrollRestartService.handleRestart(appUser, appDesc, instanceList, appRedisConfigVo);
        String responseJson = null;
        if(executeResult.getMessage() != null){
            responseJson = JSONObject.toJSONString(GeneralResponse.ok(executeResult.getMessage()));
        }
        sendMessage(response, responseJson);
        return;
    }

    /**
     * 停止滚动重启
     * @param response
     * @param appId
     * @return
     */
    @RequestMapping(value = "stopRestart", method = RequestMethod.GET)
    public void getRestartRecord(Model model, HttpServletResponse response, Long appId) {
        boolean existsStopRestartFlag = appScrollRestartService.existsStopRestartFlag(appId);
        if(existsStopRestartFlag){
            String json = JSONObject.toJSONString(GeneralResponse.error(500, "停止请求不允许重复发送，请通过日志查看重启进度。"));
            sendMessage(response, json);
            return;
        }

        ConfigRestartRecord configRestartRecord = new ConfigRestartRecord();
        configRestartRecord.setAppId(appId);
        int pageNo = 1;
        int pageSize = 10;
        List<ConfigRestartRecord> configRestartRecordByCondition = appScrollRestartService.getConfigRestartRecordByCondition(model, configRestartRecord, pageNo, pageSize);
        boolean existRestart = false;
        for (ConfigRestartRecord record : configRestartRecordByCondition) {
            if((record.getStatus() == RestartStatusEnum.RUNNING.getValue() && record.getOperateType() == ConfigRestartOperateEnum.RESTART.getValue())
                || (record.getStatus() == RestartStatusEnum.RESTART_AFTER_CONFIG.getValue() && record.getOperateType() == ConfigRestartOperateEnum.CONFIG_RESTART.getValue())){
                existRestart = true;
                break;
            }
        }
        if(existRestart){
            boolean result = appScrollRestartService.addStopRestartFlag(appId);
            if(result){
                String json = JSONObject.toJSONString(GeneralResponse.ok("停止请求已发送，但不确保停止，请通过日志查看重启进度。"));
                sendMessage(response, json);
                return;
            }
        }else{
            String json = JSONObject.toJSONString(GeneralResponse.ok("重启任务不存在或已结束，请确认。"));
            sendMessage(response, json);
            return;
        }
    }


    /**
     * 修改配置
     * @param appRedisConfigVo
     * @return
     */
    @RequestMapping(value = "updateConfig", method = RequestMethod.POST)
    public void updateConfig(HttpServletRequest request, HttpServletResponse response, @RequestBody AppRedisConfigVo appRedisConfigVo) {
        AppUser appUser = getUserInfo(request);
        if (!assistRedisService.setNx(RESTART_CONFIG_KEY + appRedisConfigVo.getAppId(), "1")) {
            String json = JSONObject.toJSONString(GeneralResponse.error(500, "滚动重启/修改配置正在执行中，不允许重复操作。"));
            sendMessage(response, json);
            return;
        }
        try {
            // 1.获取应用信息
            AppDesc appDesc = appService.getByAppId(appRedisConfigVo.getAppId());
            //无有效应用时,或应用redis类型不符时，返回
            if (appDesc == null || appDesc.getType() != AppTypeEnum.REDIS_SENTINEL.getType() && appDesc.getType() != AppTypeEnum.REDIS_CLUSTER.getType()
            ) {
                String json = JSONObject.toJSONString(GeneralResponse.error(500, "参数有误，请确认。"));
                sendMessage(response, json);
                return;
            }
            //判断是否有配置信息
            if (CollectionUtils.isEmpty(appRedisConfigVo.getConfigList())) {
                String json = JSONObject.toJSONString(GeneralResponse.error(500, "参数有误，请确认。"));
                sendMessage(response, json);
                return;
            }
            // 2.获取当前应用下redis实例信息
            List<InstanceInfo> instanceList = appService.getAppBasicInstanceInfo(appRedisConfigVo.getAppId());
            //过滤出运行中的实例
            instanceList = instanceList.stream().filter(instanceInfo -> instanceInfo.getStatus() == InstanceStatusEnum.GOOD_STATUS.getStatus()).collect(Collectors.toList());
            //处理实例信息，封装主从信息
            boolean executeFlag = appScrollRestartService.handleAppInstanceInfo(instanceList, appDesc);
            if(!executeFlag){
                String json = JSONObject.toJSONString(GeneralResponse.error(500, "未正确获取到实例主从信息，请重试。"));
                sendMessage(response, json);
                return;
            }
            //校验有效实例，并校验传入的instancId全部有效，无效直接返回
            boolean check = this.checkPointedInstance(instanceList, appRedisConfigVo.getInstanceList());
            if (!check) {
                String json = JSONObject.toJSONString(GeneralResponse.error(500, "实例不满足此操作。"));
                sendMessage(response, json);
                return;
            }

            Map<String, Object> map = appScrollRestartService.handleConfig(appUser, appDesc, instanceList, appRedisConfigVo);
            String handleResult = (String) map.get("errorInfo");
            String responseJson = null;
            if (StringUtils.isNotEmpty(handleResult)) {
                responseJson = JSONObject.toJSONString(GeneralResponse.error(500, handleResult));
            } else {
                responseJson = JSONObject.toJSONString(GeneralResponse.ok(map));
            }
            sendMessage(response, responseJson);
            return;
        }catch (Exception e){
            logger.error("updateConfig error: ", e);
        }finally {
            assistRedisService.remove(RESTART_CONFIG_KEY + appRedisConfigVo.getAppId());
        }
    }

    /**
     * 校验有效实例是否为空，并校验传入的instancId全部有效，无效直接返回
     * @param instanceInfoList
     * @param instanceIdList
     * @return
     */
    private boolean checkPointedInstance(List<InstanceInfo> instanceInfoList, List<Integer> instanceIdList){
        if(CollectionUtils.isEmpty(instanceInfoList)){
            return false;
        }
        if(CollectionUtils.isEmpty(instanceIdList)){
            return true;
        }
        boolean existFlag = false;
        for(Integer instanceId : instanceIdList){
            existFlag = false;
            for(InstanceInfo instanceInfo : instanceInfoList){
                if(instanceId == instanceInfo.getId()){
                    existFlag = true;
                    break;
                }
            }
            if(!existFlag){
                return false;
            }
        }
        return true;
    }

    /**
     * 发送json消息
     *
     * @param response
     * @param responseMessage
     */
    public void sendMessage(HttpServletResponse response, GeneralResponse responseMessage) {
        response.reset();
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter printWriter = null;
        try {
            printWriter = response.getWriter();
            printWriter.write(JSONObject.toJSONString(responseMessage));
        } catch (IOException e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            if (printWriter != null) {
                printWriter.flush();
                printWriter.close();
            }
        }
    }

}
