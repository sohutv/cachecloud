package com.sohu.cache.web.controller;

import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.redis.RedisCenter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

/**
 * job操作
 * @author leifu
 * @Date 2014年05月19日
 * @Time 下午12:45:36
 */
@Controller
@RequestMapping(value = "/cache/jobs")
public class JobController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

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

        redisCenter.deployRedisCollection(appId, host, port);
        logger.info("deploy instance: {}:{} done.", host, port);
    }
}
