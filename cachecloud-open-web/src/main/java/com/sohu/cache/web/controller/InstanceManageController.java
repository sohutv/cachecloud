package com.sohu.cache.web.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.sohu.cache.entity.AppUser;
import com.sohu.cache.stats.instance.InstanceDeployCenter;
import com.sohu.cache.web.enums.SuccessEnum;

/**
 * 应用后台管理
 * 
 * @author leifu
 * @Time 2014年7月3日
 */
@Controller
@RequestMapping("manage/instance")
public class InstanceManageController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(InstanceManageController.class);

    @Resource(name = "instanceDeployCenter")
    private InstanceDeployCenter instanceDeployCenter;

    /**
     * 上线(和下线分开)
     * 
     * @param instanceId
     */
    @RequestMapping(value = "/startInstance")
    public ModelAndView doStartInstance(HttpServletRequest request, HttpServletResponse response, Model model, int instanceId) {
        AppUser appUser = getUserInfo(request);
        logger.warn("user {} startInstance {} ", appUser.getName(), instanceId);
        boolean result = false;
        if (instanceId > 0) {
            try {
                result = instanceDeployCenter.startExistInstance(instanceId);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                model.addAttribute("message", e.getMessage());
            }
        } else {
            logger.error("doStartInstance instanceId:{}", instanceId);
            model.addAttribute("message", "wrong param");
        }
        logger.warn("user {} startInstance {} result is {}", appUser.getName(), instanceId, result);
        if (result) {
            model.addAttribute("success", SuccessEnum.SUCCESS.value());
        } else {
            model.addAttribute("success", SuccessEnum.FAIL.value());
        }
        return new ModelAndView();
    }

    /**
     * 下线实例
     * 
     * @param instanceId
     * @param ip
     */
    @RequestMapping(value = "/shutdownInstance")
    public ModelAndView doShutdownInstance(HttpServletRequest request, HttpServletResponse response, Model model, int instanceId) {
        AppUser appUser = getUserInfo(request);
        logger.warn("user {} shutdownInstance {} ", appUser.getName(), instanceId);
        boolean result = false;
        if (instanceId > 0) {
            try {
                result = instanceDeployCenter.shutdownExistInstance(instanceId);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                model.addAttribute("message", e.getMessage());
            }
        } else {
            logger.error("doShutdownInstance instanceId:{}", instanceId);
            model.addAttribute("message", "wrong param");
        }
        logger.warn("user {} shutdownInstance {}, result is {}", appUser.getName(), instanceId, result);
        if (result) {
            model.addAttribute("success", SuccessEnum.SUCCESS.value());
        } else {
            model.addAttribute("success", SuccessEnum.FAIL.value());
        }
        return new ModelAndView();
    }
}
