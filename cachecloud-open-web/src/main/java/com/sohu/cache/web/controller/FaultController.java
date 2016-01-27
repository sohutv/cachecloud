package com.sohu.cache.web.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.sohu.cache.entity.InstanceFault;
import com.sohu.cache.web.service.MemFaultService;

/**
 * 故障展示
 * @author leifu
 * @Time 2014年6月12日
 */
@Controller
@RequestMapping("manage/fault")
public class FaultController extends BaseController {

    @Resource(name = "memFaultService")
    private MemFaultService memFaultService;

    
    @RequestMapping(value = "/list")
    public ModelAndView doUserList(HttpServletRequest request,
            HttpServletResponse response, Model model) {
        List<InstanceFault> faults = memFaultService.getFaultList();
        model.addAttribute("faults", faults);
        return new ModelAndView("manage/fault/list");
    }

}
