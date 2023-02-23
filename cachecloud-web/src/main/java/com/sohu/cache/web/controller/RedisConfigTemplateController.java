package com.sohu.cache.web.controller;

import com.sohu.cache.constant.ErrorMessageEnum;
import com.sohu.cache.constant.RedisConfigTemplateChangeEnum;
import com.sohu.cache.entity.AppUser;
import com.sohu.cache.entity.InstanceConfig;
import com.sohu.cache.entity.SystemResource;
import com.sohu.cache.redis.RedisConfigTemplateService;
import com.sohu.cache.task.constant.ResourceEnum;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.web.enums.SuccessEnum;
import com.sohu.cache.web.service.ResourceService;
import com.sohu.cache.web.util.AppEmailUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis配置模板管理
 *
 * @author leifu
 * @Date 2016-6-25
 * @Time 下午2:48:25
 */
@Controller
@RequestMapping("manage/redisConfig")
public class RedisConfigTemplateController extends BaseController {

    @Resource(name = "redisConfigTemplateService")
    private RedisConfigTemplateService redisConfigTemplateService;

    @Resource(name = "appEmailUtil")
    private AppEmailUtil appEmailUtil;

    @Autowired
    private ResourceService resourceService;

    /**
     * 初始化配置
     */
    @RequestMapping(value = "/init")
    public ModelAndView init(HttpServletRequest request, HttpServletResponse response, Model model) {
        // 默认是Redis普通节点配置
        int type = NumberUtils.toInt(request.getParameter("type"), ConstUtils.CACHE_REDIS_STANDALONE);
        int resourceId = NumberUtils.toInt(request.getParameter("versionid"),0);

        // 获取redis资源包
        List<SystemResource> resourceList = resourceService.getResourceList(ResourceEnum.REDIS.getValue());
        model.addAttribute("resourceList", resourceList);
        if(resourceId == 0){
            model.addAttribute("currentVersion", resourceList.get(0));
        }else{
            model.addAttribute("currentVersion", resourceService.getResourceById(resourceId));
        }
        model.addAttribute("redisConfigList", redisConfigTemplateService.getByVesionAndType(type, resourceId));
        model.addAttribute("success", request.getParameter("success"));
        model.addAttribute("redisConfigActive", SuccessEnum.SUCCESS.value());
        model.addAttribute("versionid", resourceId);
        model.addAttribute("type", type);
        return new ModelAndView("manage/redisConfig/init");
    }

    /**
     * 修改配置
     */
    @RequestMapping(value = "/update")
    public ModelAndView update(HttpServletRequest request, HttpServletResponse response, Model model) {
        AppUser appUser = getUserInfo(request);
        String versionName = StringUtils.isEmpty(request.getParameter("versionName")) ? "" : String.valueOf(request.getParameter("versionName"));
        String id = request.getParameter("id");
        String configKey = request.getParameter("configKey");
        String configValue = request.getParameter("configValue");
        String info = request.getParameter("info");
        int status = NumberUtils.toInt(request.getParameter("status"), -1);
        if (StringUtils.isBlank(id) || !NumberUtils.isDigits(id) || StringUtils.isBlank(configKey) || status > 1
                || status < 0) {
            model.addAttribute("status", SuccessEnum.FAIL.value());
            model.addAttribute("message", ErrorMessageEnum.PARAM_ERROR_MSG.getMessage() + "id=" + id + ",configKey="
                    + configKey + ",configValue=" + configValue + ",status=" + status);
            return new ModelAndView("");
        }
        //开始修改
        logger.warn("user {} want to change id={}'s configKey={}, configValue={}, info={}, status={}", appUser.getName(),
                id, configKey, configValue, info, status);
        SuccessEnum successEnum;
        InstanceConfig instanceConfig = redisConfigTemplateService.getById(NumberUtils.toLong(id));
        try {
            instanceConfig.setConfigValue(configValue);
            instanceConfig.setInfo(info);
            instanceConfig.setStatus(status);
            redisConfigTemplateService.saveOrUpdate(instanceConfig);
            successEnum = SuccessEnum.SUCCESS;
        } catch (Exception e) {
            successEnum = SuccessEnum.FAIL;
            model.addAttribute("message", ErrorMessageEnum.INNER_ERROR_MSG.getMessage());
            logger.error(e.getMessage(), e);
        }
        logger.warn("user {} want to change {} id={}'s configKey={}, configValue={}, info={}, status={}, result is {}", appUser.getName(), versionName,
                id, configKey, configValue, info, status, successEnum.value());
        //发送邮件通知
        appEmailUtil.sendRedisConfigTemplateChangeEmail(appUser, versionName, instanceConfig, successEnum, RedisConfigTemplateChangeEnum.UPDATE);
        model.addAttribute("status", successEnum.value());
        return new ModelAndView("");
    }

    /**
     * 删除配置
     */
    @RequestMapping(value = "/remove")
    public ModelAndView remove(HttpServletRequest request, HttpServletResponse response, Model model) {
        AppUser appUser = getUserInfo(request);
        String idParam = request.getParameter("id");
        String versionName = StringUtils.isEmpty(request.getParameter("versionName")) ? "" : String.valueOf(request.getParameter("versionName"));
        long id = NumberUtils.toLong(idParam);
        if (id <= 0) {
            model.addAttribute("status", SuccessEnum.FAIL.value());
            model.addAttribute("message", ErrorMessageEnum.PARAM_ERROR_MSG.getMessage() + "id=" + idParam);
            return new ModelAndView("");
        }
        logger.warn("user {} want to delete id={}'s config", appUser.getName(), id);
        SuccessEnum successEnum;
        InstanceConfig instanceConfig = redisConfigTemplateService.getById(id);
        try {
            redisConfigTemplateService.remove(id);
            successEnum = SuccessEnum.SUCCESS;
        } catch (Exception e) {
            successEnum = SuccessEnum.FAIL;
            model.addAttribute("message", ErrorMessageEnum.INNER_ERROR_MSG.getMessage());
            logger.error(e.getMessage(), e);
        }
        logger.warn("user {} want to delete {} id={}'s config, result is {}", appUser.getName(), versionName, id, successEnum.value());
        //发送邮件通知
        appEmailUtil.sendRedisConfigTemplateChangeEmail(appUser, versionName, instanceConfig, successEnum, RedisConfigTemplateChangeEnum.DELETE);
        model.addAttribute("status", successEnum.value());
        return new ModelAndView("");

    }

    /**
     * 添加配置
     */
    @RequestMapping(value = "/add")
    public ModelAndView add(HttpServletRequest request, HttpServletResponse response, Model model) {
        AppUser appUser = getUserInfo(request);
        String versionName = StringUtils.isEmpty(request.getParameter("versionName")) ? "" : String.valueOf(request.getParameter("versionName"));
        InstanceConfig instanceConfig = getInstanceConfig(request);
        if (StringUtils.isBlank(instanceConfig.getConfigKey())) {
            model.addAttribute("status", SuccessEnum.FAIL.value());
            model.addAttribute("message", ErrorMessageEnum.PARAM_ERROR_MSG.getMessage() + "configKey=" + instanceConfig.getConfigKey());
            return new ModelAndView("");
        }
        logger.warn("user {} want to add config, configKey is {}, configValue is {}, type is {}", appUser.getName(),
                instanceConfig.getConfigKey(), instanceConfig.getConfigValue(), instanceConfig.getType());
        SuccessEnum successEnum;
        try {
            redisConfigTemplateService.saveOrUpdate(instanceConfig);
            successEnum = SuccessEnum.SUCCESS;
        } catch (Exception e) {
            successEnum = SuccessEnum.FAIL;
            model.addAttribute("message", ErrorMessageEnum.INNER_ERROR_MSG.getMessage());
            logger.error(e.getMessage(), e);
        }
        logger.warn("user {} want to add {} config, configKey is {}, configValue is {}, type is {}, result is {}",
                appUser.getName(), versionName,
                instanceConfig.getConfigKey(), instanceConfig.getConfigValue(), instanceConfig.getType(), successEnum.value());
        model.addAttribute("status", successEnum.value());
        //发送邮件通知
        appEmailUtil.sendRedisConfigTemplateChangeEmail(appUser, versionName, instanceConfig, successEnum, RedisConfigTemplateChangeEnum.ADD);
        return new ModelAndView("");

    }

    /**
     * Redis配置对比
     */
    @RequestMapping(value = "/contrast")
    public ModelAndView contrast(HttpServletRequest request, Model model, Integer upgradeVersionId, Integer currentVersionId) {

        //对比配置
        List<InstanceConfig> configList_current = redisConfigTemplateService.getByVesion(currentVersionId);
        List<InstanceConfig> configList_upgrade = redisConfigTemplateService.getByVesion(upgradeVersionId);
        // current Map
        Map<String, Object> currentConfigMap = new HashMap<String, Object>();
        for (InstanceConfig instanceConfig : configList_current) {
            if (instanceConfig.getStatus() == 1) {
                currentConfigMap.put(instanceConfig.getConfigKey(), instanceConfig.getConfigValue());
            }
        }
        // upgrade Map
        Map<String, Object> upgradeConfigMap = new HashMap<String, Object>();
        for (InstanceConfig instanceConfig : configList_upgrade) {
            if (instanceConfig.getStatus() == 1) {
                upgradeConfigMap.put(instanceConfig.getConfigKey(), instanceConfig.getConfigValue());
            }
        }
        // same Map
        Map<String, Object> sameConfigMap = new HashMap<String, Object>();

        logger.info("current config item size ={}", configList_current.size());
        logger.info("upgarde config item size ={}", configList_upgrade.size());
        // 临时变量
        List<InstanceConfig> localConfig = configList_upgrade;
        // 配置变更
        for (InstanceConfig instanceConfig : localConfig) {
            String key = instanceConfig.getConfigKey();
            String value = instanceConfig.getConfigValue();
            if (currentConfigMap.containsKey(key) && currentConfigMap.get(key).equals(value)) {
                currentConfigMap.remove(key);
                upgradeConfigMap.remove(key);
                sameConfigMap.put(key, value);
            }
        }
        logger.info("diff current : {},size ={}", currentConfigMap, currentConfigMap.size());
        logger.info("diff upgrade : {},size ={}", upgradeConfigMap, upgradeConfigMap.size());
        logger.info("same configuration: {},size ={}", sameConfigMap, sameConfigMap.size());

        // 获取当前redis的所有版本
        model.addAttribute("sameConfigMap", sameConfigMap);
        model.addAttribute("currentConfigMap", currentConfigMap);
        model.addAttribute("currentVersion", resourceService.getResourceById(currentVersionId));
        model.addAttribute("upgradeConfigMap", upgradeConfigMap);
        model.addAttribute("upgradeVersion", resourceService.getResourceById(upgradeVersionId));
        model.addAttribute("success", request.getParameter("success"));
        model.addAttribute("redisConfigActive", SuccessEnum.SUCCESS.value());

        return new ModelAndView("manage/redisConfig/contrast");
    }

    /**
     * 预览配置
     */
    @RequestMapping(value = "/preview")
    public ModelAndView preview(HttpServletRequest request, HttpServletResponse response, Model model) {
        //默认配置
        int type = NumberUtils.toInt(request.getParameter("type"), -1);
        String host = StringUtils.isBlank(request.getParameter("host")) ? "127.0.0.1" : request.getParameter("host");
        int port = NumberUtils.toInt(request.getParameter("port"), 6379);
        int maxMemory = NumberUtils.toInt(request.getParameter("maxMemory"), 2048);
        int sentinelPort = NumberUtils.toInt(request.getParameter("sentinelPort"), 26379);
        String masterName = StringUtils.isBlank(request.getParameter("masterName")) ? "myMaster" : request
                .getParameter("masterName");
        int versionId = NumberUtils.toInt(request.getParameter("versionId"), 1);

        // 根据类型生成配置模板
        List<String> configList = new ArrayList<String>();
        if (ConstUtils.CACHE_REDIS_STANDALONE == type) {
            configList = redisConfigTemplateService.handleCommonConfig(host, port, maxMemory, null, versionId);
        } else if (ConstUtils.CACHE_REDIS_SENTINEL == type) {
            configList = redisConfigTemplateService.handleSentinelConfig(masterName, host, port, host, sentinelPort, versionId);
        } else if (ConstUtils.CACHE_TYPE_REDIS_CLUSTER == type) {
            configList = redisConfigTemplateService.handleClusterConfig(port, versionId);
        }
        model.addAttribute("type", type);
        model.addAttribute("host", host);
        model.addAttribute("port", port);
        model.addAttribute("maxMemory", maxMemory);
        model.addAttribute("sentinelPort", sentinelPort);
        model.addAttribute("masterName", masterName);
        model.addAttribute("configList", configList);
        return new ModelAndView("manage/redisConfig/preview");
    }

    /**
     * 使用最简单的request生成InstanceConfig对象
     *
     * @return
     */
    private InstanceConfig getInstanceConfig(HttpServletRequest request) {
        String configKey = request.getParameter("configKey");
        String configValue = request.getParameter("configValue");
        String info = request.getParameter("info");
        String type = request.getParameter("type");
        String versionid = request.getParameter("versionid");
        InstanceConfig instanceConfig = new InstanceConfig();
        instanceConfig.setConfigKey(configKey);
        instanceConfig.setConfigValue(configValue);
        instanceConfig.setInfo(info);
        instanceConfig.setType(NumberUtils.toInt(type));
        instanceConfig.setStatus(1);
        instanceConfig.setVersionId(NumberUtils.toInt(versionid));

        return instanceConfig;
    }

}
