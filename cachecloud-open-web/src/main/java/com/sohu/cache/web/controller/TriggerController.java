package com.sohu.cache.web.controller;

import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.ObjectConvert;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * trigger操作
 * @author leifu
 * @Date 2014年05月19日
 * @Time 下午5:15:36
 */
@Controller
@RequestMapping(value = "/cache/triggers")
public class TriggerController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired(required = false)
    @Qualifier("clusterScheduler")
    private Scheduler scheduler;

    @RequestMapping(value = "/pause/{appId}/{type}/{host}/{port}")
    public void pauseTrigger(@PathVariable long appId, @PathVariable int type, @PathVariable String host, @PathVariable int port) {
        Assert.isTrue(appId > 0);
        Assert.isTrue(type > 0);
        Assert.hasText(host);
        Assert.isTrue(port > 0);

        String triggerName = ObjectConvert.linkIpAndPort(host, port);
        String triggerGroup = "";
        if (type == ConstUtils.CACHE_TYPE_REDIS_CLUSTER) {
            triggerGroup = ConstUtils.REDIS_TRIGGER_GROUP + appId;
        }

        TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroup);
        try {
            scheduler.pauseTrigger(triggerKey);
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("trigger with name: {}, group: {} is paused", port, host);
    }

    @RequestMapping(value = "/resume/{appId}/{type}/{host}/{port}")
    public void resumeTrigger(@PathVariable long appId, @PathVariable int type, @PathVariable String host, @PathVariable int port) {
        Assert.isTrue(appId > 0);
        Assert.isTrue(type > 0);
        Assert.hasText(host);
        Assert.isTrue(port > 0);

        String triggerName = ObjectConvert.linkIpAndPort(host, port);
        String triggerGroup = "";
        if (type == ConstUtils.CACHE_TYPE_REDIS_CLUSTER) {
            triggerGroup = ConstUtils.REDIS_TRIGGER_GROUP + appId;
        }

        TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroup);
        try {
            scheduler.resumeTrigger(triggerKey);
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("trigger with name: {}, group: {} is resumed", port, host);
    }
}
