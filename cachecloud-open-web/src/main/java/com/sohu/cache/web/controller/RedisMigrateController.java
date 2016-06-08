package com.sohu.cache.web.controller;


import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.math.NumberUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.sohu.cache.constant.RedisMigrateEnum;
import com.sohu.cache.constant.RedisMigrateResult;
import com.sohu.cache.stats.app.RedisMigrateCenter;

/**
 * Redis数据迁移入口
 * 
 * @author leifu
 * @Date 2016-6-8
 * @Time 下午11:10:34
 */
@Controller
@RequestMapping("/migrate")
public class RedisMigrateController extends BaseController {

    @Resource(name = "redisMigrateCenter")
    private RedisMigrateCenter redisMigrateCenter;

    /**
     * 初始化界面
     * @return
     */
    @RequestMapping(value = "/init")
    public ModelAndView init(HttpServletRequest request, HttpServletResponse response, Model model) {
        return new ModelAndView("migrate/init");
    }

    /**
     * 检查配置
     * @return
     */
    @RequestMapping(value = "/check")
    public ModelAndView check(HttpServletRequest request, HttpServletResponse response, Model model) {
        //相关参数
        String migrateMachineIp = request.getParameter("migrateMachineIp");
        String sourceRedisMigrateIndex = request.getParameter("sourceRedisMigrateIndex");
        RedisMigrateEnum sourceRedisMigrateEnum = RedisMigrateEnum.getByIndex(NumberUtils.toInt(sourceRedisMigrateIndex, -1));
        String sourceServers = request.getParameter("sourceServers");
        String targetRedisMigrateIndex = request.getParameter("targetRedisMigrateIndex");
        RedisMigrateEnum targetRedisMigrateEnum = RedisMigrateEnum.getByIndex(NumberUtils.toInt(targetRedisMigrateIndex, -1));
        String targetServers = request.getParameter("targetServers");
        //检查返回结果
        RedisMigrateResult redisMigrateResult = redisMigrateCenter.check(migrateMachineIp, sourceRedisMigrateEnum, sourceServers, targetRedisMigrateEnum, targetServers);
        model.addAttribute("status", redisMigrateResult.getStatus());
        model.addAttribute("message", redisMigrateResult.getMessage());
        return new ModelAndView("");
    }

    /**
     * 开始迁移
     * @return
     */
    @RequestMapping(value = "/start")
    public ModelAndView start(HttpServletRequest request, HttpServletResponse response, Model model) {
        //相关参数
        String migrateMachineIp = request.getParameter("migrateMachineIp");
        String sourceRedisMigrateIndex = request.getParameter("sourceRedisMigrateIndex");
        RedisMigrateEnum sourceRedisMigrateEnum = RedisMigrateEnum.getByIndex(NumberUtils.toInt(sourceRedisMigrateIndex, -1));
        String sourceServers = request.getParameter("sourceServers");
        String targetRedisMigrateIndex = request.getParameter("targetRedisMigrateIndex");
        RedisMigrateEnum targetRedisMigrateEnum = RedisMigrateEnum.getByIndex(NumberUtils.toInt(targetRedisMigrateIndex, -1));
        String targetServers = request.getParameter("targetServers");

        // 不需要对格式进行检验,check已经做过了，开始迁移
        boolean isSuccess = redisMigrateCenter.migrate(migrateMachineIp, sourceRedisMigrateEnum, sourceServers,
                targetRedisMigrateEnum, targetServers);

        model.addAttribute("status", isSuccess ? 1 : 0);
        return new ModelAndView("");
    }
    
    /**
     * 停掉迁移任务
     * @return
     */
    @RequestMapping(value = "/stop")
    public ModelAndView stop(HttpServletRequest request, HttpServletResponse response, Model model) {
        //任务id：查到任务相关信息
        return new ModelAndView("migrate/stop");
    }
    
    /**
     * 查看迁移日志
     * @return
     */
    @RequestMapping(value = "/showLog")
    public ModelAndView showLog(HttpServletRequest request, HttpServletResponse response, Model model) {
        //任务id：查到任务相关信息
        return new ModelAndView("migrate/showLog");
    }
    
    /**
     * 查看迁移进度
     * @return
     */
    @RequestMapping(value = "/showProcess")
    public ModelAndView log(HttpServletRequest request, HttpServletResponse response, Model model) {
        //任务id：查到任务相关信息
        return new ModelAndView("migrate/showProcess");
    }

    /**
     * 查看迁移列表(包含历史)
     * @return
     */
    @RequestMapping(value = "/list")
    public ModelAndView list(HttpServletRequest request, HttpServletResponse response, Model model) {
        return new ModelAndView("migrate/list");
    }
    
    
}
