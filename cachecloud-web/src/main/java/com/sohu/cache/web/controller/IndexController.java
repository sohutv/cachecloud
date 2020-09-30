package com.sohu.cache.web.controller;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 首页
 *
 * @author leifu
 * @Date 2014年10月28日
 * @Time 上午10:49:32
 */
@Controller
@RequestMapping("/")
public class IndexController extends BaseController {

    @RequestMapping(value = "")
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) {
        String userName = userLoginStatusService.getUserNameFromTicket(request);
        String redirectUrl = request.getParameter("redirectUrl");
        if(StringUtils.isBlank(redirectUrl)){
            redirectUrl = "/admin/app/list";
        }
        if (StringUtils.isNotBlank(userName)) {
            userLoginStatusService.addLoginStatus(request, response, userName);
        }
        return new ModelAndView("redirect:" + redirectUrl);
    }
}
