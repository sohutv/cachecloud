package com.sohu.cache.web.controller;

import com.sohu.cache.util.ConstUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 前端页面相关基础资源
 *
 * @author zengyizhao
 * @Time 2023年7月3日
 */
@Controller
@RequestMapping("/web/resource")
public class WebResourceController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(WebResourceController.class);


    /**
     * 初始化贡献者页面
     *
     * @return
     */
    @RequestMapping("/{path}/{filename}")
    public ModelAndView doInitBecomeContributor(@PathVariable String path, @PathVariable String filename,
                                                HttpServletRequest request,
                                                HttpServletResponse response, Model model) {
        if(("inc".equals(path) && "daily".equals(filename)) || ("inc".equals(path) && "contact".equals(filename))){
            model.addAttribute("contact", ConstUtils.CONTACT);
        }
        return new ModelAndView(path + "/" + filename);
    }

    /**
     * 初始化贡献者页面
     *
     * @return
     */
    @RequestMapping("/noPower")
    public ModelAndView redirectNoPower(@RequestParam("appId") String appId,
                                                Model model,
                                                HttpServletRequest request,
                                                HttpServletResponse response) {
        model.addAttribute("appId", appId);
        return new ModelAndView("noPower");
    }

}
