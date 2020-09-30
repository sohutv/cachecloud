package com.sohu.cache.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.sohu.cache.entity.AppUser;
import com.sohu.cache.entity.SystemConfig;
import com.sohu.cache.web.enums.SuccessEnum;
import com.sohu.cache.web.service.ConfigService;
import com.sohu.cache.web.util.AppEmailUtil;

/**
 * cachecloud配置管理
 * 
 * @author leifu
 * @Date 2016年5月23日
 * @Time 上午10:31:16
 */
@Controller
@RequestMapping("manage/config")
public class ConfigManageController extends BaseController {

    @Resource(name = "configService")
    private ConfigService configService;
    
    @Resource(name = "appEmailUtil")
    private AppEmailUtil appEmailUtil;

    /**
     * 初始化配置
     * 
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping(value = "/init")
    public ModelAndView init(HttpServletRequest request, HttpServletResponse response, Model model) {
        List<SystemConfig> configList = configService.getConfigList(1);
        model.addAttribute("configList", configList);
        model.addAttribute("success", request.getParameter("success"));
        model.addAttribute("configActive", SuccessEnum.SUCCESS.value());
        return new ModelAndView("manage/config/init");
    }

    /**
     * 修改配置
     * 
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping(value = "/update")
    public ModelAndView update(HttpServletRequest request, HttpServletResponse response, Model model) {
        AppUser appUser = getUserInfo(request);
        logger.warn("user {} want to change config!", appUser.getName());
        List<SystemConfig> oldConfigList = configService.getConfigList(1);
        SuccessEnum successEnum;
        Map<String, String> configMap = new HashMap<String, String>();
        try {
            Map<String, String[]> paramMap = request.getParameterMap();
            for (Entry<String, String[]> entry : paramMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue()[0];
                if (StringUtils.isNotBlank(key)) {
                    configMap.put(key, value);
                }
            }
            if (MapUtils.isEmpty(configMap)) {
                logger.error("params {} may be empty!!", paramMap);
            }
            successEnum = configService.updateConfig(configMap);
            if (successEnum.equals(SuccessEnum.SUCCESS)) {
                configService.reloadSystemConfig();
            }
        } catch (Exception e) {
            successEnum = SuccessEnum.FAIL;
            logger.error(e.getMessage(), e);
        }
        Map<String, String> systemDifConfigMap = getDifConfigMap(oldConfigList, configMap);
        appEmailUtil.sendSystemConfigDifEmail(appUser, systemDifConfigMap, successEnum);
        logger.warn("user {} change config result is {}!", appUser.getName(), successEnum.value());
        return new ModelAndView("redirect:/manage/config/init?success=" + successEnum.value());
    }

    private Map<String, String> getDifConfigMap(List<SystemConfig> oldConfigList, Map<String, String> configMap) {
        Map<String, String> systemDifConfigMap = new HashMap<String, String>();
        for (SystemConfig systemConfig : oldConfigList) {
            String key = systemConfig.getConfigKey();
            String oldValue = systemConfig.getConfigValue();
            String newValue = configMap.get(key);
            if (newValue != null && !oldValue.equals(newValue)) {
                systemDifConfigMap.put(systemConfig.getInfo(), String.format("old value: %s, new value: %s", oldValue, newValue));
            }
        }
        return systemDifConfigMap;
    }

}
