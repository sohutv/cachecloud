package com.sohu.cache.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 首页
 * @author leifu
 * @Date 2014年10月28日
 * @Time 上午10:49:32
 */
@Controller
@RequestMapping("/")
public class IndexController extends BaseController {

    @RequestMapping(value = "")
    public ModelAndView index(HttpServletRequest request,
                        HttpServletResponse response, Model model){
        return new ModelAndView("redirect:/admin/app/list");
    }
}
