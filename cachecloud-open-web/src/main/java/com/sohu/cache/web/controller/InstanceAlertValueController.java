package com.sohu.cache.web.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.sohu.cache.constant.ErrorMessageEnum;
import com.sohu.cache.entity.AppUser;
import com.sohu.cache.entity.InstanceAlert;
import com.sohu.cache.stats.instance.InstanceAlertValueService;
import com.sohu.cache.web.enums.SuccessEnum;
import com.sohu.cache.web.util.AppEmailUtil;

/**
 * 实例报警阀值
 * @author leifu
 * @Date 2016年8月24日
 * @Time 下午1:24:25
 */
@Controller
@RequestMapping("manage/instanceAlert")
public class InstanceAlertValueController extends BaseController {

    @Resource(name = "instanceAlertValueService")
    private InstanceAlertValueService instanceAlertValueService;

    @Resource(name = "appEmailUtil")
    private AppEmailUtil appEmailUtil;

    /**
     * 初始化配置
     */
    @RequestMapping(value = "/init")
    public ModelAndView init(HttpServletRequest request, HttpServletResponse response, Model model) {
        model.addAttribute("instanceAlertList", instanceAlertValueService.getAllInstanceAlert());
        model.addAttribute("success", request.getParameter("success"));
        model.addAttribute("instanceAlertValueActive", SuccessEnum.SUCCESS.value());
        return new ModelAndView("manage/instanceAlert/init");
    }
    
    /**
     * 初始化配置
     */
    @RequestMapping(value = "/monitor")
    public ModelAndView monitor(HttpServletRequest request, HttpServletResponse response, Model model) {
        instanceAlertValueService.monitorLastMinuteAllInstanceInfo();
        return null;
    }

    /**
     * 修改配置
     */
    @RequestMapping(value = "/update")
    public ModelAndView update(HttpServletRequest request, HttpServletResponse response, Model model) {
        AppUser appUser = getUserInfo(request);
        String configKey = request.getParameter("configKey");
        String alertValue = request.getParameter("alertValue");
        int compareType = NumberUtils.toInt(request.getParameter("compareType"));
        int valueType = NumberUtils.toInt(request.getParameter("valueType"));
        String info = request.getParameter("info");
        int status = NumberUtils.toInt(request.getParameter("status"), -1);
        if (StringUtils.isBlank(configKey) || status > 1 || status < 0) {
            model.addAttribute("status", SuccessEnum.FAIL.value());
            model.addAttribute("message", ErrorMessageEnum.PARAM_ERROR_MSG.getMessage() + ",configKey="
                    + configKey + ",alertValue=" + alertValue + ",status=" + status);
            return new ModelAndView("");
        }
        //开始修改
        logger.warn("user {} want to change instance alert configKey={}, alertValue={}, info={}, status={}", appUser.getName(), configKey, alertValue, info, status);
        SuccessEnum successEnum;
        InstanceAlert instanceAlert = instanceAlertValueService.getByConfigKey(configKey);
        try {
            instanceAlert.setAlertValue(alertValue);
            instanceAlert.setValueType(valueType);
            instanceAlert.setInfo(info);
            instanceAlert.setStatus(status);
            instanceAlert.setCompareType(compareType);
            instanceAlertValueService.saveOrUpdate(instanceAlert);
            successEnum = SuccessEnum.SUCCESS;
        } catch (Exception e) {
            successEnum = SuccessEnum.FAIL;
            model.addAttribute("message", ErrorMessageEnum.INNER_ERROR_MSG.getMessage());
            logger.error(e.getMessage(), e);
        }
        logger.warn("user {} want to change instance alert configKey={}, alertValue={}, info={}, status={}, result is {}", appUser.getName(), configKey, alertValue, info, status, successEnum.value());
        model.addAttribute("status", successEnum.value());
        return new ModelAndView("");
    }

    /**
     * 删除配置
     */
    @RequestMapping(value = "/remove")
    public ModelAndView remove(HttpServletRequest request, HttpServletResponse response, Model model) {
        AppUser appUser = getUserInfo(request);
        String configKey = request.getParameter("configKey");
        if (StringUtils.isBlank(configKey)) {
            model.addAttribute("status", SuccessEnum.FAIL.value());
            model.addAttribute("message", ErrorMessageEnum.PARAM_ERROR_MSG.getMessage() + "configKey=" + configKey);
            return new ModelAndView("");
        }
        logger.warn("user {} want to delete configKey {}", appUser.getName(), configKey);
        SuccessEnum successEnum;
        try {
            instanceAlertValueService.remove(configKey);
            successEnum = SuccessEnum.SUCCESS;
        } catch (Exception e) {
            successEnum = SuccessEnum.FAIL;
            model.addAttribute("message", ErrorMessageEnum.INNER_ERROR_MSG.getMessage());
            logger.error(e.getMessage(), e);
        }
        logger.warn("user {} want to delete configKey {} , result is {}", appUser.getName(), configKey, successEnum.info());
        model.addAttribute("status", successEnum.value());
        return new ModelAndView("");

    }

    /**
     * 添加配置
     */
    @RequestMapping(value = "/add")
    public ModelAndView add(HttpServletRequest request, HttpServletResponse response, Model model) {
        AppUser appUser = getUserInfo(request);
        InstanceAlert instanceAlert = getInstanceAlert(request);
        if (StringUtils.isBlank(instanceAlert.getConfigKey())) {
            model.addAttribute("status", SuccessEnum.FAIL.value());
            model.addAttribute("message", ErrorMessageEnum.PARAM_ERROR_MSG.getMessage() + "configKey=" + instanceAlert.getConfigKey());
            return new ModelAndView("");
        }
        logger.warn("user {} want to add instance alert, configKey is {}, alertValue is {}, info is {}, orderId is {}", appUser.getName(),
                instanceAlert.getConfigKey(), instanceAlert.getAlertValue(), instanceAlert.getInfo(), instanceAlert.getOrderId());
        SuccessEnum successEnum;
        try {
            instanceAlertValueService.saveOrUpdate(instanceAlert);
            successEnum = SuccessEnum.SUCCESS;
        } catch (Exception e) {
            successEnum = SuccessEnum.FAIL;
            model.addAttribute("message", ErrorMessageEnum.INNER_ERROR_MSG.getMessage());
            logger.error(e.getMessage(), e);
        }
        logger.warn("user {} want to add instance alert, configKey is {}, alertValue is {}, info is {}, orderId is {}, result is {}", appUser.getName(),
                instanceAlert.getConfigKey(), instanceAlert.getAlertValue(), instanceAlert.getInfo(), instanceAlert.getOrderId(), successEnum.info());
        model.addAttribute("status", successEnum.value());
        return new ModelAndView("");

    }

    /**
     * 使用最简单的request生成InstanceAlert对象
     * 
     * @return
     */
    private InstanceAlert getInstanceAlert(HttpServletRequest request) {
        String configKey = request.getParameter("configKey");
        String alertValue = request.getParameter("alertValue");
        String info = request.getParameter("info");
        int compareType = NumberUtils.toInt(request.getParameter("compareType"));
        int valueType = NumberUtils.toInt(request.getParameter("valueType"));
        int orderId = NumberUtils.toInt(request.getParameter("orderId"));
        InstanceAlert instanceAlert = new InstanceAlert();
        instanceAlert.setConfigKey(configKey);
        instanceAlert.setAlertValue(alertValue);
        instanceAlert.setValueType(valueType);
        instanceAlert.setCompareType(compareType);
        instanceAlert.setInfo(info);
        instanceAlert.setOrderId(orderId);
        instanceAlert.setStatus(1);
        return instanceAlert;
    }

}
