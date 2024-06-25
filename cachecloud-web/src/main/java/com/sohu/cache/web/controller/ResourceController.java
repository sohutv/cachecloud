package com.sohu.cache.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.sohu.cache.entity.*;
import com.sohu.cache.ssh.SSHService;
import com.sohu.cache.task.TaskService;
import com.sohu.cache.task.constant.PushEnum;
import com.sohu.cache.task.constant.ResourceEnum;
import com.sohu.cache.web.enums.SuccessEnum;
import com.sohu.cache.web.service.ResourceService;
import com.sohu.cache.web.service.ServerDataService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by chenshi on 2020/7/3.
 */
@Controller
@RequestMapping("/manage/app/resource")
public class ResourceController extends BaseController {

    @Autowired
    ResourceService resourceService;
    @Autowired
    SSHService sshService;
    @Autowired
    TaskService taskService;
    @Autowired
    ServerDataService serverDataService;

    @RequestMapping("/index")
    public ModelAndView index(Model model, String tabTag, String searchName) {
        model.addAttribute("tabTag", tabTag);
        model.addAttribute("searchName", searchName);

        model.addAttribute("reourcesActive", SuccessEnum.SUCCESS.value());
        return new ModelAndView("manage/resource/list");
    }

    @RequestMapping("/redis/{tab}")
    public ModelAndView tab(@PathVariable("tab") String tab, String searchName, Model model) {
        List<SystemResource> resourceList = new ArrayList<SystemResource>();
        int resource_type = 0;
        List<ServerInfo> allServerInfo = new ArrayList<>();
        switch (tab) {
            case "respo":
                resource_type = ResourceEnum.ALL.getValue();
                break;
            case "script":
                resource_type = ResourceEnum.SCRIPT.getValue();
                break;
            case "redis":
                resource_type = ResourceEnum.REDIS.getValue();
                allServerInfo.addAll(serverDataService.getAllServerInfo());
                break;
            case "tool":
                resource_type = ResourceEnum.TOOL.getValue();
                break;
            case "sshkey":
                resource_type = ResourceEnum.SSHKEY.getValue();
                break;
            case "dir":
                resource_type = ResourceEnum.DIR.getValue();
                break;
            default:
                break;
        }

        if (StringUtils.isEmpty(searchName)) {
            resourceList = resourceService.getResourceList(resource_type);
        } else {
            resourceList = resourceService.getResourceList(resource_type, searchName);
            model.addAttribute("searchName", searchName);
        }

        //仓库资源
        List<SystemResource> reposlist = resourceService.getResourceList(ResourceEnum.Repository.getValue());
        if (!CollectionUtils.isEmpty(reposlist)) {
            model.addAttribute("repository", reposlist.get(0));
        }
        // 目录资源
        List<SystemResource> dirlist = resourceService.getResourceList(ResourceEnum.DIR.getValue());

        model.addAttribute("tabTag", tab);
        model.addAttribute("resourceList", resourceList);
        model.addAttribute("allServerInfo", allServerInfo);
        model.addAttribute("dirList", dirlist);
        model.addAttribute("appUseMap", resourceService.getAppUseRedis());
        model.addAttribute("reourcesActive", SuccessEnum.SUCCESS.value());
        return new ModelAndView("manage/resource/" + tab);
    }

    @RequestMapping("/add")
    public ModelAndView resourceAdd(HttpServletRequest request, HttpServletResponse response, Integer resourceId, Model model) {

        JSONObject result = new JSONObject();
        SystemResource resource = new SystemResource();
        //修改
        if (resourceId != null && resourceId > 0) {
            resource = resourceService.getResourceById(resourceId);
        }
        int versionCopyId = NumberUtils.toInt(request.getParameter("copyVersion"), -1);
        int resourceType = NumberUtils.toInt(request.getParameter("resourceType"));
        resource.setName(request.getParameter("resourceName"));
        resource.setIntro(request.getParameter("resourceDesc"));
        resource.setDir(request.getParameter("resourceDir"));
        resource.setStatus(NumberUtils.toInt(request.getParameter("resourceStatus")));
        resource.setType(resourceType);
        resource.setUrl(request.getParameter("resourceUrl"));
        resource.setOrderNum(NumberUtils.toInt(request.getParameter("orderNum")));
        resource.setLastmodify(new Date());
        resource.setUsername(getUserInfo(request).getName());


        SuccessEnum successEnum = null;
        if (resourceId != null && resourceId > 0) {
            resource.setId(resourceId);
            successEnum = resourceService.updateResource(resource);
        } else {
            // 验重
            SystemResource existResource = resourceService.getResourceByName(resource.getName());
            if (existResource != null && existResource.getId() > 0) {
                result.put("status", SuccessEnum.FAIL.value());
                result.put("message", "资源名已存在");
                sendMessage(response, result.toString());
                return null;
            }

            resource.setIspush(PushEnum.NO.getValue());
            successEnum = resourceService.saveResource(resource);
            if (versionCopyId > -1 && resourceType == ResourceEnum.REDIS.getValue()) {
                redisConfigTemplateService.copyRedisConfig(versionCopyId, resource);
            }
        }

        result.put("status", successEnum.value());
        sendMessage(response, result.toString());

        return null;
    }

    @RequestMapping("/get")
    public ModelAndView resourceGet(HttpServletResponse response, Integer resourceId) {

        JSONObject result = new JSONObject();
        SuccessEnum successEnum = SuccessEnum.FAIL;

        if (resourceId != null && resourceId > 0) {
            SystemResource resource = resourceService.getResourceById(resourceId);
            if (resource != null) {
                successEnum = SuccessEnum.SUCCESS;
                result.put("resource", resource);
            }
        }
        result.put("status", successEnum.value());
        sendMessage(response, result.toString());
        return null;
    }

    @RequestMapping("/ssh")
    public ModelAndView generateSshkey(HttpServletResponse response, String command, String containerIp, Integer resourceId) {

        JSONObject result = new JSONObject();
        if (StringUtils.isEmpty(containerIp)) {
            containerIp = machineCenter.getFirstMachineIp();
        }
        SuccessEnum successEnum = SuccessEnum.FAIL;

        /*if (resourceId != null && resourceId > 0) {
            Boolean flag = resourceService.generateSshkey(containerIp, command);
        }*/
        result.put("status", successEnum.value());
        sendMessage(response, result.toString());
        return null;
    }

    @RequestMapping("/push")
    public ModelAndView resourcePush(HttpServletRequest request, HttpServletResponse response, Integer repositoryId, Integer resourceId, String content) {

        // 1. push content
        SuccessEnum successEnum = null;
        JSONObject result = new JSONObject();
        AppUser userInfo = getUserInfo(request);
        if (repositoryId != null && resourceId != null) {
            SystemResource resource = resourceService.getResourceById(resourceId);
            if (resource.getType() == ResourceEnum.SCRIPT.getValue()) {
                // 1.1推送脚本资源
                successEnum = resourceService.pushScript(repositoryId, resourceId, content, userInfo);
                // 1.2.清理临时资源
                clearTempResource(String.valueOf(resourceId));
            } else if (resource.getType() == ResourceEnum.DIR.getValue()) {
                // 2.1.推送目录资源
                successEnum = resourceService.pushDir(repositoryId, resourceId, userInfo);
            }
            result.put("status", successEnum == null ? successEnum : successEnum.value());
        } else {
            result.put("status", SuccessEnum.ERROR.value());
            result.put("message", "资源id异常");
        }

        sendMessage(response, result.toString());

        return null;
    }

    @RequestMapping("/compile")
    public ModelAndView compile(HttpServletRequest request, HttpServletResponse response, String containerIp, String compileInfo, Integer repositoryId, Integer resourceId, String content) {

        // 1. push content
        JSONObject result = new JSONObject();

        if (StringUtils.isEmpty(containerIp)) {
            // 从机器列表获取一台可用机器
            containerIp = machineCenter.getFirstMachineIp();
        }
        AppUser userInfo = getUserInfo(request);
        if (repositoryId != null && resourceId != null) {
            // 推送资源
            long taskid = taskService.addResourceCompileTask(resourceId, repositoryId, containerIp, userInfo);
            SystemResource resource = resourceService.getResourceById(resourceId);
            resource.setTaskId(taskid);
            resource.setCompileInfo(compileInfo);
            resourceService.updateResource(resource);
            result.put("status", SuccessEnum.SUCCESS.value());
        } else {
            result.put("status", SuccessEnum.ERROR.value());
            result.put("message", "资源id异常");
        }

        sendMessage(response, result.toString());
        return null;
    }

    @RequestMapping("/temporarySave")
    public ModelAndView temporarySave(HttpServletResponse response, Integer resourceId, String content) {

        // 1. push content
        JSONObject result = new JSONObject();
        if (saveTempResource(String.valueOf(resourceId), content)) {
            result.put("status", SuccessEnum.SUCCESS.value());
            SystemResource resource = resourceService.getResourceById(resourceId);
            if (resource.getIspush() == PushEnum.NO.getValue()) {
                resource.setIspush(PushEnum.NO_WITH_MODIFY.getValue());
            } else if (resource.getIspush() == PushEnum.YES.getValue()) {
                resource.setIspush(PushEnum.YES_WITH_MODIFY.getValue());
            }
            resourceService.updateResource(resource);
        }
        sendMessage(response, result.toString());
        return null;
    }

    @RequestMapping("/script/load")
    public ModelAndView load(HttpServletResponse response, Integer resourceId, Integer respositoryId) {

        // 1. push content
        JSONObject result = new JSONObject();
        if (resourceId != null && respositoryId != null) {
            String localContent = getTempResource(String.valueOf(resourceId));

            if (StringUtils.isEmpty(localContent)) {
                String remoteContent = resourceService.getRemoteFileContent(resourceId, respositoryId);
                if (!StringUtils.isEmpty(remoteContent)) {
                    result.put("content", remoteContent);
                    // 来源:远程文件
                    result.put("source", "2");
                }
            } else {
                // 来源:临时文件
                result.put("content", localContent);
                result.put("source", "1");
            }
            result.put("script", localContent);
            result.put("status", SuccessEnum.SUCCESS.value());
        }
        sendMessage(response, result.toString());
        return null;
    }

    @RequestMapping("/config")
    public ModelAndView config(HttpServletRequest request, HttpServletResponse response, Integer repositoryId, Integer resourceId) {

        // 1. push content
        JSONObject result = new JSONObject();

        AppUser userInfo = getUserInfo(request);
        if (repositoryId != null && resourceId != null) {

        }

        return new ModelAndView("manage/resource/config");
    }

}
