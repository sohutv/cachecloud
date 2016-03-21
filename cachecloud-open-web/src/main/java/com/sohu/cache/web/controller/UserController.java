package com.sohu.cache.web.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.sohu.cache.constant.AppAuditType;
import com.sohu.cache.entity.AppAudit;
import com.sohu.cache.entity.AppUser;
import com.sohu.cache.web.enums.SuccessEnum;
import com.sohu.cache.web.util.AppEmailUtil;

/**
 * 注册用户管理(页面没有权限限制)
 * 
 * @author leifu
 * @Date 2014年10月28日
 * @Time 上午10:49:32
 */
@Controller
@RequestMapping("/user")
public class UserController extends BaseController{
    
    @Resource(name = "appEmailUtil")
    private AppEmailUtil appEmailUtil;
    
    /**
     * 注册用户页面
     */
    @RequestMapping(value = "/register")
    public ModelAndView userRegister(HttpServletRequest request,
            HttpServletResponse response, Model model, Integer success) {
        model.addAttribute("success", success);
        return new ModelAndView("user/userRegister");
    }
    
    /**
     * 注册用户申请
     */
    @RequestMapping(value = "/apply")
    public ModelAndView doAddUser(HttpServletRequest request,
            HttpServletResponse response, Model model, String name, String chName, String email, String mobile,
            Integer type, Long userId) {
        SuccessEnum success = SuccessEnum.SUCCESS;
        try {
            //保存用户(type=-1为无效用户,需要审批)
            AppUser appUser = AppUser.buildFrom(userId, name, chName, email, mobile, type);
            userService.save(appUser);
            //提交审批
            AppAudit appAudit = appService.saveRegisterUserApply(appUser,AppAuditType.REGISTER_USER_APPLY);
            appEmailUtil.noticeUserResult(appUser, appAudit);
        } catch (Exception e) {
            success = SuccessEnum.FAIL;
            logger.error(e.getMessage(), e);
        }
        return new ModelAndView("redirect:/user/register?success=" + success.value());
    }
    
    
    @RequestMapping(value = "/checkUserNameExist")
    public ModelAndView doCheckUserNameExist(HttpServletRequest request,
            HttpServletResponse response, Model model, String userName) {
        AppUser appUser = userService.getByName(userName);
        if (appUser != null) {
            write(response, String.valueOf(SuccessEnum.SUCCESS.value()));
        } else {
            write(response, String.valueOf(SuccessEnum.FAIL.value()));
        }
        return null;
    }
}
