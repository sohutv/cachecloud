package com.sohu.test.redis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.cache.constant.InstanceStatusEnum;
import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.util.TypeUtil;
import com.sohu.test.BaseTest;

import redis.clients.jedis.Jedis;

/**
 * redis重要数据处理（请注意BaseTest是local环境）
 * 
 * @author leifu
 * @Date 2015年3月4日
 * @Time 上午11:04:04
 */
public class RedisImportantDataDeal extends BaseTest {
    private final static Logger logger = LoggerFactory.getLogger(RedisImportantDataDeal.class);

    @Resource(name = "instanceDao")
    private InstanceDao instanceDao;
    
    @Resource(name = "redisCenter")
    private RedisCenter redisCenter;

    @Test
    public void clearAllAppData() {
        // /////////////一定要谨慎处理/////////////////
        // /////////////一定要谨慎处理/////////////////
        // /////////////一定要谨慎处理/////////////////
        long appId = 000L;
        // /////////////一定要谨慎处理///////////////
        // /////////////一定要谨慎处理/////////////////
        // /////////////一定要谨慎处理/////////////////

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        logger.warn("确认要清除appId:" + appId + "的所有内存数据(输入y代表确认):");
        String confirm = null;
        try {
            confirm = br.readLine();
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!"y".equals(confirm)) {
            return;
        }
        List<InstanceInfo> instanceList = instanceDao.getInstListByAppId(appId);
        if (CollectionUtils.isEmpty(instanceList)) {
            logger.error("appId: {}, 可能输入错误，不存在实例列表");
        }
        for (InstanceInfo instance : instanceList) {
            if (instance.getStatus() != InstanceStatusEnum.GOOD_STATUS.getStatus()) {
                continue;
            }
            String host = instance.getIp();
            int port = instance.getPort();
            // master + 非sentinel节点
            Boolean isMater = redisCenter.isMaster(appId, host, port);
            if (isMater != null && isMater.equals(true) && !TypeUtil.isRedisSentinel(instance.getType())) {
                Jedis jedis = new Jedis(host, port, 30000);
                try {
                    logger.info("{}:{} 开始清理内存", host, port);
                    long start = System.currentTimeMillis();
                    jedis.flushAll();
                    logger.info("{}:{} 清理完成, 耗时:{} ms", host, port, (System.currentTimeMillis() - start));
                } catch (Exception e) {
                    logger.error(e.getMessage());
                } finally {
                    jedis.close();
                }
            }
        }
    }
}
