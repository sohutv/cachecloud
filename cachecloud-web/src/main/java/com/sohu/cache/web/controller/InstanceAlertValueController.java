package com.sohu.cache.web.controller;

import com.sohu.cache.constant.ErrorMessageEnum;
import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.entity.AppUser;
import com.sohu.cache.entity.InstanceAlertConfig;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.redis.enums.*;
import com.sohu.cache.stats.instance.InstanceAlertConfigService;
import com.sohu.cache.util.TypeUtil;
import com.sohu.cache.web.enums.SuccessEnum;
import com.sohu.cache.web.vo.AlertConfig;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 实例报警阀值
 *
 * @author leifu
 * @Date 2016年8月24日
 * @Time 下午1:24:25
 */
@Controller
@RequestMapping("manage/instanceAlert")
public class InstanceAlertValueController extends BaseController {

    @Resource(name = "instanceAlertConfigService")
    private InstanceAlertConfigService instanceAlertConfigService;

    @Resource(name = "instanceDao")
    private InstanceDao instanceDao;

    /**
     * 初始化配置
     */
    @RequestMapping(value = "/init")
    public ModelAndView init(HttpServletRequest request, HttpServletResponse response, Model model) {
        model.addAttribute("instanceAlertCheckCycleEnumList", InstanceAlertCheckCycleEnum.getInstanceAlertCheckCycleEnumList());
        model.addAttribute("instanceAlertCompareTypeEnumList", InstanceAlertCompareTypeEnum.getInstanceAlertCompareTypeEnumList());
        model.addAttribute("redisAlertConfigEnumList", RedisAlertConfigEnum.getRedisAlertConfigEnumList());
        List<InstanceAlertConfig> globalAlertConfigList = instanceAlertConfigService.getByType(InstanceAlertTypeEnum.ALL_ALERT.getValue());
        model.addAttribute("instanceAlertAllList", globalAlertConfigList);
        model.addAttribute("redisUsedGlobalAlertConfigList", distinctUsedGlobalAlert(globalAlertConfigList));
        model.addAttribute("instanceAlertList", instanceAlertConfigService.getAll());
        model.addAttribute("success", request.getParameter("success"));
        model.addAttribute("instanceAlertValueActive", SuccessEnum.SUCCESS.value());
        List<InstanceAlertConfig> instanceAlertSpecialList = instanceAlertConfigService.getByType(InstanceAlertTypeEnum.INSTANCE_ALERT.getValue());
        List<InstanceAlertConfig> appAlertSpecialList = instanceAlertConfigService.getByType(InstanceAlertTypeEnum.APP_ALERT.getValue());
        fillinstanceHostPort(instanceAlertSpecialList);
        List<InstanceAlertConfig> appAndInstanceAlertConfigList = addAppAlertConfigToInstanceSpecialList(appAlertSpecialList, instanceAlertSpecialList);
        model.addAttribute("instanceAlertSpecialList", appAndInstanceAlertConfigList);
        return new ModelAndView("manage/instanceAlert/init");
    }

    /**
     * 筛选出使用的全局配置，唯一的
     * @param globalAlertConfigList
     * @return
     */
    private List<AlertConfig> distinctUsedGlobalAlert(List<InstanceAlertConfig> globalAlertConfigList) {
        if(CollectionUtils.isEmpty(globalAlertConfigList)){
            return Collections.emptyList();
        }
        List<AlertConfig> alertConfigList = new ArrayList<>();
        globalAlertConfigList.stream().forEach(instanceAlertConfig -> {
            AlertConfig alertConfig = new AlertConfig();
            alertConfig.setValue(instanceAlertConfig.getAlertConfig());
            alertConfig.setInfo(instanceAlertConfig.getConfigInfo());
            alertConfigList.add(alertConfig);
        });
        return alertConfigList.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 填充hostport
     *
     * @param instanceAlertSpecialList
     */
    private void fillinstanceHostPort(List<InstanceAlertConfig> instanceAlertSpecialList) {
        if (CollectionUtils.isEmpty(instanceAlertSpecialList)) {
            return;
        }
        for (InstanceAlertConfig instanceAlertConfig : instanceAlertSpecialList) {
            long instanceId = instanceAlertConfig.getInstanceId();
            InstanceInfo instanceInfo = instanceDao.getInstanceInfoById(instanceId);
            if (instanceInfo == null) {
                continue;
            }
            instanceAlertConfig.setInstanceInfo(instanceInfo);
        }
    }

    /**
     * 将应用报警封装为特殊实例报警进行返回
     * @param appAlertSpecialList
     * @param instanceAlertSpecialList
     */
    private List<InstanceAlertConfig> addAppAlertConfigToInstanceSpecialList(List<InstanceAlertConfig> appAlertSpecialList, List<InstanceAlertConfig> instanceAlertSpecialList) {
        if(CollectionUtils.isNotEmpty(appAlertSpecialList)){
            if(CollectionUtils.isEmpty(instanceAlertSpecialList)){
                return appAlertSpecialList;
            }else{
                instanceAlertSpecialList.addAll(appAlertSpecialList);
                return instanceAlertSpecialList.stream().sorted(Comparator.comparing(instanceAlertConfig -> instanceAlertConfig.getId())).collect(Collectors.toList());
            }
        }
        return instanceAlertSpecialList;
    }

    /**
     * 添加配置
     */
    @RequestMapping(value = "/add")
    public ModelAndView add(HttpServletRequest request, HttpServletResponse response, Model model) {
        AppUser appUser = getUserInfo(request);
        InstanceAlertConfig instanceAlertConfig = getInstanceAlertConfig(request);
        SuccessEnum successEnum;
        try {
            //如果未传重要程度，则查询已存在的全局配置获取
            if(instanceAlertConfig.getImportantLevel() == null){
                int globalImportantLevel = instanceAlertConfigService.getImportantLevelByAlertConfigAndCompareType(instanceAlertConfig.getAlertConfig(), instanceAlertConfig.getCompareType());
                instanceAlertConfig.setImportantLevel(globalImportantLevel);
            }
            logger.warn("user {} want to add instanceAlertConfig {}", appUser.getName(), instanceAlertConfig);
            instanceAlertConfigService.save(instanceAlertConfig);
            successEnum = SuccessEnum.SUCCESS;
        } catch (Exception e) {
            successEnum = SuccessEnum.FAIL;
            model.addAttribute("message", ErrorMessageEnum.INNER_ERROR_MSG.getMessage());
            logger.error(e.getMessage(), e);
        }
        logger.warn("user {} add instanceAlertConfig {}, result is {}", appUser.getName(), instanceAlertConfig, successEnum.value());
        model.addAttribute("status", successEnum.value());
        return new ModelAndView("");
    }

    /**
     * 添加应用配置,仅添加一条信息
     */
    @RequestMapping(value = "/addApp")
    public ModelAndView addApp(HttpServletRequest request, HttpServletResponse response, Model model) {
        AppUser appUser = getUserInfo(request);
        int appid = NumberUtils.toInt(request.getParameter("appid"));
        //判断此应用下是否有实例
        List<InstanceInfo> instancelist = instanceDao.getEffectiveInstListByAppId(appid);
        if(instancelist != null && instancelist.size()>0){
            InstanceAlertConfig appAlertConfig = getAppAlertConfig(request);
            SuccessEnum successEnum;
            try {
                logger.warn("user {} want to add app instanceAlertConfig, size is {}", appUser.getName(), instancelist.size());
                //如果未传重要程度，则查询已存在的全局配置获取
                if(appAlertConfig.getImportantLevel() == null){
                    int globalImportantLevel = instanceAlertConfigService.getImportantLevelByAlertConfigAndCompareType(appAlertConfig.getAlertConfig(), appAlertConfig.getCompareType());
                    appAlertConfig.setImportantLevel(globalImportantLevel);
                }
                instanceAlertConfigService.save(appAlertConfig);
                successEnum = SuccessEnum.SUCCESS;
            } catch (Exception e) {
                successEnum = SuccessEnum.FAIL;
                model.addAttribute("message", ErrorMessageEnum.INNER_ERROR_MSG.getMessage());
                logger.error(e.getMessage(), e);
            }
            logger.warn("user {} add app , result is {}", appUser.getName(), successEnum);
            model.addAttribute("status", successEnum.value());
        }
        return new ModelAndView("");
    }

    /**
     * 检查hostPort是否存在
     */
    @RequestMapping(value = "/checkInstanceHostPort")
    public ModelAndView checkInstanceHostPort(HttpServletRequest request, HttpServletResponse response, Model model) {
        String hostPort = request.getParameter("instanceHostPort");
        if (StringUtils.isBlank(hostPort)) {
            model.addAttribute("status", SuccessEnum.FAIL.value());
            model.addAttribute("message", "参数为空");
            return new ModelAndView("");
        }
        String[] hostPortArr = hostPort.split(":");
        if (hostPortArr.length != 2) {
            model.addAttribute("status", SuccessEnum.FAIL.value());
            model.addAttribute("message", "hostPort:" + hostPort + "格式错误");
            return new ModelAndView("");
        }
        String host = hostPortArr[0];
        int port = NumberUtils.toInt(hostPortArr[1]);
        InstanceInfo instanceInfo = instanceDao.getAllInstByIpAndPort(host, port);
        if (instanceInfo == null) {
            model.addAttribute("status", SuccessEnum.FAIL.value());
            model.addAttribute("message", "hostPort:" + hostPort + "不存在");
        } else {
            model.addAttribute("status", SuccessEnum.SUCCESS.value());
        }
        return new ModelAndView("");
    }

    /**
     * 初始化配置
     */
    @RequestMapping(value = "/monitor")
    public ModelAndView monitor(HttpServletRequest request, HttpServletResponse response, Model model) {
        instanceAlertConfigService.monitorLastMinuteAllInstanceInfo();
        return null;
    }

    /**
     * 修改配置
     */
    @RequestMapping(value = "/update")
    public ModelAndView update(HttpServletRequest request, HttpServletResponse response, Model model) {
        AppUser appUser = getUserInfo(request);
        int id = NumberUtils.toInt(request.getParameter("id"));
        String alertValue = request.getParameter("alertValue");
        int checkCycle = NumberUtils.toInt(request.getParameter("checkCycle"));
        int compareType = NumberUtils.toInt(request.getParameter("compareType"));
        int importantLevel = NumberUtils.toInt(request.getParameter("importantLevel"));
        logger.warn("user {} want to change instance alert id={}, alertValue={}, checkCycle={}, compareType={}", appUser.getName(), id, alertValue, checkCycle, compareType);
        SuccessEnum successEnum;
        try {
            InstanceAlertConfig orgInstAlertConfig = instanceAlertConfigService.get(id);
            instanceAlertConfigService.update(id, alertValue, checkCycle, compareType, importantLevel);
            //判断是否为更新紧急程度，如是，判断是否为全局报警，如是，则更新所有报警级别为此级别
            if(orgInstAlertConfig != null && importantLevel != orgInstAlertConfig.getImportantLevel()){
                if(InstanceAlertTypeEnum.ALL_ALERT.getValue() == orgInstAlertConfig.getType()){
                    instanceAlertConfigService.updateImportantLevel(orgInstAlertConfig.getAlertConfig(), compareType, importantLevel);
                }
            }
            successEnum = SuccessEnum.SUCCESS;
        } catch (DuplicateKeyException e) {
            successEnum = SuccessEnum.FAIL;
            model.addAttribute("message", ErrorMessageEnum.ALERT_CONFIG_CONSTRAINT_ERROR_MSG.getMessage());
            logger.error(e.getMessage(), e);
        }catch (Exception e) {
            successEnum = SuccessEnum.FAIL;
            model.addAttribute("message", ErrorMessageEnum.INNER_ERROR_MSG.getMessage());
            logger.error(e.getMessage(), e);
        }
        logger.warn("user {} change instance alert id={}, alertValue={}, checkCycle={}, compareType={}, result is {}", appUser.getName(), id, alertValue, checkCycle, compareType, successEnum.info());
        model.addAttribute("status", successEnum.value());
        return new ModelAndView("");
    }

    /**
     * 删除配置
     */
    @RequestMapping(value = "/remove")
    public ModelAndView remove(HttpServletRequest request, HttpServletResponse response, Model model) {
        AppUser appUser = getUserInfo(request);
        int id = NumberUtils.toInt(request.getParameter("id"));
        InstanceAlertConfig instanceAlertConfig = instanceAlertConfigService.get(id);
        logger.warn("user {} want to delete config id {}, instanceAlertConfig {}", appUser.getName(), id, instanceAlertConfig);
        SuccessEnum successEnum;
        try {
            instanceAlertConfigService.remove(id);
            successEnum = SuccessEnum.SUCCESS;
        } catch (Exception e) {
            successEnum = SuccessEnum.FAIL;
            model.addAttribute("message", ErrorMessageEnum.INNER_ERROR_MSG.getMessage());
            logger.error(e.getMessage(), e);
        }
        logger.warn("user {} want to delete config id {}, instanceAlertConfig {}, result is {}", appUser.getName(), id, instanceAlertConfig, successEnum.info());
        model.addAttribute("status", successEnum.value());
        return new ModelAndView("");

    }


    private InstanceInfo getInstanceInfo(String hostPort) {
        String[] hostPortArr = hostPort.split(":");
        String host = hostPortArr[0];
        int port = NumberUtils.toInt(hostPortArr[1]);
        return instanceDao.getAllInstByIpAndPort(host, port);
    }


    private InstanceAlertConfig getInstanceAlertConfig(HttpServletRequest request) {
        // 相关参数
        Date now = new Date();
        String alertConfig = request.getParameter("alertConfig");
        String alertValue = request.getParameter("alertValue");
        String configInfo = request.getParameter("configInfo");
        int compareType = NumberUtils.toInt(request.getParameter("compareType"));
        int checkCycle = NumberUtils.toInt(request.getParameter("checkCycle"));
        String importantLevelStr = request.getParameter("importantLevel");
        Integer importantLevel = null;
        if(StringUtils.isNotEmpty(importantLevelStr)){
            importantLevel = NumberUtils.toInt(importantLevelStr);
        }
        int instanceId = 0;
        int type = NumberUtils.toInt(request.getParameter("type"));
        if (InstanceAlertTypeEnum.INSTANCE_ALERT.getValue() == type) {
            String hostPort = request.getParameter("instanceHostPort");
            InstanceInfo instanceInfo = getInstanceInfo(hostPort);
            instanceId = instanceInfo.getId();
        }
        // 生成对象
        InstanceAlertConfig instanceAlertConfig = new InstanceAlertConfig();
        instanceAlertConfig.setAlertConfig(alertConfig);
        instanceAlertConfig.setAlertValue(alertValue);
        instanceAlertConfig.setConfigInfo(configInfo);
        instanceAlertConfig.setCompareType(compareType);
        instanceAlertConfig.setInstanceId(instanceId);
        instanceAlertConfig.setCheckCycle(checkCycle);
        instanceAlertConfig.setLastCheckTime(now);
        instanceAlertConfig.setType(type);
        instanceAlertConfig.setUpdateTime(now);
        instanceAlertConfig.setImportantLevel(importantLevel);
        instanceAlertConfig.setStatus(InstanceAlertStatusEnum.YES.getValue());
        return instanceAlertConfig;
    }

    private List<InstanceAlertConfig> getInstanceAlertConfig(HttpServletRequest request, List<InstanceInfo> instanceList) {

        List<InstanceAlertConfig> instanceAlertConfigList = new ArrayList<InstanceAlertConfig>();
        if (instanceList != null && instanceList.size() > 0) {
            for (InstanceInfo instance : instanceList) {

                // sentinel节点忽略
                if(TypeUtil.isRedisSentinel(instance.getType())){
                    logger.info("sentinel node ignore");
                    continue;
                }
                logger.info("ip:{},port:{} alert born ",instance.getHostId(),instance.getHostPort());
                // 相关参数
                Date now = new Date();
                String alertConfig = request.getParameter("alertConfig");
                String alertValue = request.getParameter("alertValue");
                RedisAlertConfigEnum redisAlertConfigEnum = RedisAlertConfigEnum.getRedisAlertConfig(alertConfig);
                String configInfo = redisAlertConfigEnum == null ? "" : redisAlertConfigEnum.getInfo();
                int compareType = NumberUtils.toInt(request.getParameter("compareType"));
                int checkCycle = NumberUtils.toInt(request.getParameter("checkCycle"));
                int instanceId =  instance.getId();
                // 生成对象
                InstanceAlertConfig instanceAlertConfig = new InstanceAlertConfig();
                instanceAlertConfig.setAlertConfig(alertConfig);
                instanceAlertConfig.setAlertValue(alertValue);
                instanceAlertConfig.setConfigInfo(configInfo);
                instanceAlertConfig.setCompareType(compareType);
                instanceAlertConfig.setInstanceId(instanceId);
                instanceAlertConfig.setCheckCycle(checkCycle);
                instanceAlertConfig.setLastCheckTime(now);
                instanceAlertConfig.setType(2);
                instanceAlertConfig.setUpdateTime(now);
                instanceAlertConfig.setStatus(InstanceAlertStatusEnum.YES.getValue());
                //添加至监控列表
                instanceAlertConfigList.add(instanceAlertConfig);
            }
        }

        return instanceAlertConfigList;
    }

    private InstanceAlertConfig getAppAlertConfig(HttpServletRequest request) {
        // 相关参数
        Date now = new Date();
        long appid = NumberUtils.toLong(request.getParameter("appid"));
        String alertConfig = request.getParameter("alertConfig");
        String alertValue = request.getParameter("alertValue");
        String configInfo = request.getParameter("configInfo");
        int compareType = NumberUtils.toInt(request.getParameter("compareType"));
        int checkCycle = NumberUtils.toInt(request.getParameter("checkCycle"));
        String importantLevelStr = request.getParameter("importantLevel");
        Integer importantLevel = null;
        if(StringUtils.isNotEmpty(importantLevelStr)){
            importantLevel = NumberUtils.toInt(importantLevelStr);
        }
        // 生成对象
        InstanceAlertConfig instanceAlertConfig = new InstanceAlertConfig();
        instanceAlertConfig.setAlertConfig(alertConfig);
        instanceAlertConfig.setAlertValue(alertValue);
        instanceAlertConfig.setConfigInfo(configInfo);
        instanceAlertConfig.setCompareType(compareType);
        instanceAlertConfig.setInstanceId(appid);
        instanceAlertConfig.setCheckCycle(checkCycle);
        instanceAlertConfig.setLastCheckTime(now);
        instanceAlertConfig.setType(3);
        instanceAlertConfig.setUpdateTime(now);
        instanceAlertConfig.setStatus(InstanceAlertStatusEnum.YES.getValue());
        instanceAlertConfig.setImportantLevel(importantLevel);
        return instanceAlertConfig;
    }

}
