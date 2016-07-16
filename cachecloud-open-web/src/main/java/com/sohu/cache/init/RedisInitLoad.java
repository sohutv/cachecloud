package com.sohu.cache.init;

import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.TypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 初始化加载所有的redis实例
 *
 * User: lingguo
 * Date: 14-6-11
 * Time: 下午11:13
 */
public class RedisInitLoad extends AsyncLoad{
    private final Logger logger = LoggerFactory.getLogger(RedisInitLoad.class);

    private InstanceDao instanceDao;

    private RedisCenter redisCenter;

    public void init() {
        if (ConstUtils.IS_DEBUG) {
            logger.warn("isDebug=true return");
            return;
        }
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    initAsync();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });
    }
    /**
     * spring启动时调用，加载所有的redis实例
     */
    public void initAsync() {
        initByType(ConstUtils.CACHE_TYPE_REDIS_CLUSTER);
        initByType(ConstUtils.CACHE_REDIS_STANDALONE);
    }

    private void initByType(int type) {
        List<InstanceInfo> instanceInfoList = instanceDao.getInstListByType(type);
        for (InstanceInfo instanceInfo : instanceInfoList) {
            if (TypeUtil.isRedisSentinel(instanceInfo.getType())) {
                continue;
            }
            String host = instanceInfo.getIp();
            int port = instanceInfo.getPort();
            Long appId = instanceInfo.getAppId();
            redisCenter.deployRedisCollection(appId, host, port);
            redisCenter.deployRedisSlowLogCollection(appId, host, port);
        }
        logger.info("init redis type={} deploy instance done.", type);
    }

    public void setInstanceDao(InstanceDao instanceDao) {
        this.instanceDao = instanceDao;
    }

    public void setRedisCenter(RedisCenter redisCenter) {
        this.redisCenter = redisCenter;
    }
    
}
