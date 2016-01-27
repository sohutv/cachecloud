package com.sohu.cache.web.controller;

import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.memcached.MemcachedCenter;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.util.ConstUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

/**
 * User: lingguo
 * Date: 14-5-19
 * Time: 下午12:45
 */
@Controller
@RequestMapping(value = "/cache/jobs")
public class JobController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private MemcachedCenter memcachedCenter;
    
    @Resource
    private RedisCenter redisCenter;
    
    @Resource
    private MachineCenter machineCenter;

    @RequestMapping(value = "/add/{appId}/{type}/{host}/{port}")
    public void addJob(@PathVariable long appId, @PathVariable int type, @PathVariable String host, @PathVariable int port) {
        Assert.isTrue(appId > 0);
        Assert.isTrue(type > 0);
        Assert.hasText(host);
        Assert.isTrue(port > 0);

        if (type == ConstUtils.CACHE_TYPE_MEMCACHED) {
            memcachedCenter.deployMemcachedCollection(appId, host, port);
        } else if (type == ConstUtils.CACHE_TYPE_REDIS_CLUSTER) {
            redisCenter.deployRedisCollection(appId, host, port);
        }

        logger.info("deploy instance: {}:{} done.", host, port);
    }
}
