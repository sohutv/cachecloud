package com.sohu.cache.inspect.impl;

import com.sohu.cache.async.AsyncService;
import com.sohu.cache.async.KeyCallable;
import com.sohu.cache.async.NamedThreadFactory;
import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.inspect.InspectHandler;
import com.sohu.cache.inspect.InspectParamEnum;
import com.sohu.cache.inspect.Inspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by yijunzhang on 15-1-20.
 */
public abstract class AbstractInspectHandler implements InspectHandler {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected InstanceDao instanceDao;

    protected AsyncService asyncService;

    protected List<Inspector> inspectorList;

    protected abstract String getThreadPoolKey();

    protected abstract Map<String, List<InstanceInfo>> getSplitMap();

    public void init() {
        asyncService.assemblePool(getThreadPoolKey(), new ThreadPoolExecutor(5, 100,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024),
                new NamedThreadFactory(getThreadPoolKey(), true)));
    }

    public void handle() {
        if (inspectorList == null || inspectorList.isEmpty()) {
            logger.warn("inspectorList is null");
            return;
        }
        Map<String, List<InstanceInfo>> splitMap = getSplitMap();
        for (Map.Entry<String, List<InstanceInfo>> entry : splitMap.entrySet()) {
            String splitKey = entry.getKey();
            List<InstanceInfo> instances = entry.getValue();
            final Map<InspectParamEnum, Object> paramMap = new HashMap<InspectParamEnum, Object>();
            paramMap.put(InspectParamEnum.SPLIT_KEY, splitKey);
            paramMap.put(InspectParamEnum.INSTANCE_LIST, instances);
            String key = getThreadPoolKey() + "-" + splitKey;
            asyncService.submitFuture(getThreadPoolKey(), new KeyCallable<Boolean>(key) {
                @Override
                public Boolean execute() {
                    for (Inspector inspector : inspectorList) {
                        boolean isSuccess = false;
                        try {
                            isSuccess = inspector.inspect(paramMap);
                        } catch (Throwable e) {
                            logger.error(e.getMessage(), e);
                        }
                        if (!isSuccess) {
                            logger.error(getThreadPoolKey() + "-failed:" + inspector.getClass().getName());
                            return false;
                        }
                    }
                    return true;
                }
            });
        }
    }

    public void setInspectorList(List<Inspector> inspectorList) {
        this.inspectorList = inspectorList;
    }

    public List<InstanceInfo> getAllInstanceList() {
        return instanceDao.getAllInsts();
    }

    public void setInstanceDao(InstanceDao instanceDao) {
        this.instanceDao = instanceDao;
    }

    public void setAsyncService(AsyncService asyncService) {
        this.asyncService = asyncService;
    }
}
