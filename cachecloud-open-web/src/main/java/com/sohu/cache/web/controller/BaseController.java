package com.sohu.cache.web.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.sohu.cache.web.service.AppService;
import com.sohu.cache.web.service.UserService;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;

import com.sohu.cache.constant.AppUserTypeEnum;
import com.sohu.cache.constant.CacheCloudConstants;
import com.sohu.cache.entity.AppToUser;
import com.sohu.cache.entity.AppUser;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.entity.InstanceStats;
import com.sohu.cache.machine.MachineCenter;

/**
 * 基类controller
 *
 * @author leifu
 * @Time 2014年10月16日
 */
public class BaseController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected UserService userService;
    
    protected AppService appService;
    
    protected MachineCenter machineCenter;

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setAppService(AppService appService) {
        this.appService = appService;
    }

    public void setMachineCenter(MachineCenter machineCenter) {
        this.machineCenter = machineCenter;
    }

    public AppUser getUserInfo(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        Object object = session.getAttribute(CacheCloudConstants.LOGIN_USER_SESSION_NAME);
        AppUser user = object == null ? null : (AppUser) object;
        return user;
    }

    /**
     * 发送json消息
     *
     * @param response
     * @param message
     */
    public void sendMessage(HttpServletResponse response, String message) {
        response.reset();
        response.setContentType("application/X-JSON;charset=UTF-8");
        PrintWriter printWriter = null;
        try {
            printWriter = response.getWriter();
            printWriter.write(message);
        } catch (IOException e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            if (printWriter != null) {
                printWriter.flush();
                printWriter.close();
            }
        }

    }

    /**
     * @param response
     * @param result
     */
    protected void write(HttpServletResponse response, String result) {
        try {
            response.setContentType("text/javascript");
            response.getWriter().print(result);
            response.getWriter().flush();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    /**
     * 查看用户对于app操作的权限
     * @param request
     * @param appId
     * @return
     */
    protected boolean checkAppUserProvilege(HttpServletRequest request, long appId) {
        // 当前用户
        AppUser currentUser = getUserInfo(request);
        if (currentUser == null) {
            logger.error("currentUser is empty");
            return false;
        }
        
        if (AppUserTypeEnum.ADMIN_USER.value().equals(currentUser.getType())) {
            return true;
        }

        // 应用用户列表
        List<AppToUser> appToUsers = appService.getAppToUserList(appId);
        if (CollectionUtils.isEmpty(appToUsers)) {
            logger.error("appId {} userList is empty", appId);
            return false;
        }

        // 应用下用户id集合
        Set<Long> appUserIdSet = new HashSet<Long>();
        for (AppToUser appToUser : appToUsers) {
            appUserIdSet.add(appToUser.getUserId());
        }
        
        //最终判断
        if (!appUserIdSet.contains(currentUser.getId())) {
            logger.error("currentUser {} hasn't previlege in appId {}", currentUser.getId(), appId);
            return false;
        }
        return true;
    }
    
    /**
     * 实例统计信息
     * 
     * @param appId
     * @param model
     */
    protected void fillAppInstanceStats(Long appId, Model model) {
        List<InstanceInfo> instanceList = appService.getAppInstanceInfo(appId);
        List<InstanceStats> appInstanceStats = appService.getAppInstanceStats(appId);

        Map<String, InstanceStats> instanceStatsMap = new HashMap<String, InstanceStats>();

        for (InstanceStats instanceStats : appInstanceStats) {
            instanceStatsMap.put(instanceStats.getIp() + ":" + instanceStats.getPort(), instanceStats);
        }
        model.addAttribute("instanceList", instanceList);
        model.addAttribute("instanceStatsMap", instanceStatsMap);
    }

}
