package com.sohu.cache.client.heartbeat;

import com.sohu.cache.client.command.AppClientParams;
import com.sohu.cache.client.service.AppClientService;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.web.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by zhangyijun on 2017/8/7.
 */
@RestController
@RequestMapping(value = "/cache/client")
public class RedisClientController {

    @Autowired
    private AppClientService appClientService;

    /**
     * 通过appId返回RedisCluster实例信息
     *
     * @param appId
     */
    @RequestMapping(value = "/redis/cluster/{appId}.json", method = RequestMethod.GET)
    public Map<String, Object> getClusterByAppIdAndKey(HttpServletRequest request, @PathVariable long appId) {
        AppClientParams clientParams = wrapClientParams(request, appId, ConstUtils.CACHE_TYPE_REDIS_CLUSTER);
        return appClientService.getAppClientInfo(clientParams);
    }

    /**
     * 通过appId返回RedisSentinel实例信息
     *
     * @param appId
     */
    @RequestMapping(value = "/redis/sentinel/{appId}.json")
    public Map<String, Object> getSentinelAppById(HttpServletRequest request, @PathVariable long appId, Model model) {
        AppClientParams clientParams = wrapClientParams(request, appId, ConstUtils.CACHE_REDIS_SENTINEL);
        return appClientService.getAppClientInfo(clientParams);
    }

    /**
     * 通过appId返回RedisStandalone实例信息
     *
     * @param appId
     */
    @RequestMapping(value = "/redis/standalone/{appId}.json")
    public Map<String, Object> getStandaloneAppById(HttpServletRequest request, @PathVariable long appId, Model model) {
        AppClientParams clientParams = wrapClientParams(request, appId, ConstUtils.CACHE_REDIS_STANDALONE);
        return appClientService.getAppClientInfo(clientParams);
    }

    private AppClientParams wrapClientParams(HttpServletRequest request, long appId, int type) {
        String appClientIp = IpUtil.getIpAddr(request);
        String clientVersion = request.getParameter("clientVersion");
        return new AppClientParams(appId, type, appClientIp, clientVersion);
    }

}
