package com.sohu.cache.client.heartbeat;

import static com.google.common.base.Preconditions.checkArgument;

import com.sohu.cache.constant.ClientStatusEnum;
import com.sohu.cache.dao.AppDao;
import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.ObjectConvert;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * memcached心跳检测：server与client通过http请求交互
 *
 * User: lingguo
 * Date: 14-6-4
 * Time: 下午5:29
 */
@Controller
//@RequestMapping(value = "/cache/client")
public class MemcachedClientController {
    private Logger logger = LoggerFactory.getLogger(MemcachedClientController.class);

    @Resource
    AppDao appDao;
    @Resource
    InstanceDao instanceDao;

    /**
     *  新客户端：通过appId返回memcached应用的app信息，包括实例组和心跳检测频率
     *
     * @param appId
     * @param model
     */
    @RequestMapping(value = "/cache/client/memcached/app/{appId}.json")
    public void getAppById(HttpServletRequest request, @PathVariable long appId, Model model) {
        checkArgument(appId > 0);
        String clientVersion = request.getParameter("clientVersion");

        ResourceBundle rb = ResourceBundle.getBundle("client");
        List<String> goodVersions = Lists.newArrayList(rb.getString("good_versions").split(","));
        List<String> warnVersions = Lists.newArrayList(rb.getString("warn_versions").split(","));

        if (goodVersions.contains(clientVersion)) {
            model.addAttribute("status", ClientStatusEnum.GOOD.getStatus());
            model.addAttribute("message", "client is up to date, Cheers!");
        } else if (warnVersions.contains(clientVersion)) {
            model.addAttribute("status", ClientStatusEnum.WARN.getStatus());
            model.addAttribute("message", "WARN: client is NOT the newest, please update!");
        } else {
            model.addAttribute("status", ClientStatusEnum.ERROR.getStatus());
            model.addAttribute("message", "ERROR: client is TOO old or NOT recognized, please update NOW!");
            return;
        }

        AppDesc appDesc = appDao.getAppDescById(appId);
        List<InstanceInfo> instanceList = instanceDao.getInstListByAppId(appId);
        if (appDesc == null || instanceList == null || instanceList.isEmpty()) {
            model.addAttribute("status", ClientStatusEnum.ERROR.getStatus());
            model.addAttribute("message", "ERROR: cannot find app with appId: " + appId);
            return;
        }

        String shardsInfo = ObjectConvert.assembleInstance(instanceList);
        int shardNum = shardsInfo.split(" ").length;
        model.addAttribute("appId", appId);
        model.addAttribute("shardNum", shardNum);
        model.addAttribute("shardInfo", shardsInfo);
    }

    /**
     *  旧客户端：心跳检测，与memcloud旧客户端的兼容
     *
     * @param appId
     * @param model
     */
    @RequestMapping(value = "/memcloud/dns/{appId}.json", method = RequestMethod.GET)
    public void getAppByIdOld(@PathVariable String appId, Model model) {
        Assert.notNull(appId);
        AppDesc appDesc = appDao.getAppDescById(Long.valueOf(appId));
        List<InstanceInfo> instanceList = instanceDao.getInstListByAppId(Long.valueOf(appId));
        Assert.notNull(appDesc, "appDesc is null for appId: " + appId);
        Assert.notEmpty(instanceList, "instanceList is null for appId: " + appId);

        String shardsInfo = ObjectConvert.assembleInstance(instanceList);
        if (StringUtils.isEmpty(shardsInfo)) {
            logger.error("heartbeat: get shard info for appId: {} error", appId);
        }
        int shardNum = shardsInfo.split(" ").length;

        Map<String, Object> appMap = new HashMap<String, Object>();
        appMap.put("appId", appDesc.getAppId());
        appMap.put("version", appDesc.getVerId());
        appMap.put("groupText", shardsInfo);
        appMap.put("shardNum", shardNum);
        appMap.put("timestamp", System.currentTimeMillis());
        appMap.put("ttlSecond", ConstUtils.HEARTBEAT_INTERVAL_MEMCACHED);

        Map<String, Object> responseMap = new LinkedHashMap<String, Object>();
        responseMap.put("attachment", appMap);
        responseMap.put("debug", "");
        responseMap.put("message", "SUCC");
        responseMap.put("status", 200);

        model.addAllAttributes(responseMap);
    }
}
