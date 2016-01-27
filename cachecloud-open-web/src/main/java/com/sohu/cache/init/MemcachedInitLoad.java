package com.sohu.cache.init;


import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.memcached.MemcachedCenter;
import com.sohu.cache.util.ConfigUtil;
import com.sohu.cache.util.ConstUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * memcached 初始化加载：启动时加载所有的实例（创建trigger，并部署）
 *
 * User: lingguo
 * Date: 14-6-11
 * Time: 下午11:13
 */
public class MemcachedInitLoad extends AsyncLoad {
    private final Logger logger = LoggerFactory.getLogger(MemcachedInitLoad.class);

    private InstanceDao instanceDao;
    private MemcachedCenter memcachedCenter;
    /**
     * spring启动时即加载该方法，初始化所有的memcached实例
     */
    public void init() {
        if (ConfigUtil.I.isDebug()) {
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
     * 异步加载
     */
    public void initAsync() {
        List<InstanceInfo> instanceInfoList = instanceDao.getInstListByType(ConstUtils.CACHE_TYPE_MEMCACHED);
        for (InstanceInfo instanceInfo: instanceInfoList) {
            String host = instanceInfo.getIp();
            int port = instanceInfo.getPort();
            long appId = instanceInfo.getAppId();
            memcachedCenter.deployMemcachedCollection(appId, host, port);
        }
        logger.info("init deploy all memcached instance done.");
    }

    public void setInstanceDao(InstanceDao instanceDao) {
        this.instanceDao = instanceDao;
    }

    public void setMemcachedCenter(MemcachedCenter memcachedCenter) {
        this.memcachedCenter = memcachedCenter;
    }
}
