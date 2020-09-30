package com.sohu.cache.async;

import com.google.common.util.concurrent.AtomicLongMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author fulei
 * @date 2018年8月18日
 * @time 上午10:29:59
 */
public class CounterRejectedExecutionHandler implements RejectedExecutionHandler {
	
    private Logger logger = LoggerFactory.getLogger(CounterRejectedExecutionHandler.class);
	
    public static final AtomicLongMap<String> THREAD_POOL_REJECT_MAP = AtomicLongMap.create();
    
	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		NamedThreadFactory namedThreadFactory = (NamedThreadFactory) executor.getThreadFactory();
		String threadPoolName = namedThreadFactory.getThreadPoolName();
		if (StringUtils.isBlank(threadPoolName)) {
			logger.warn("threadPoolName is null");
			return;
		}
		THREAD_POOL_REJECT_MAP.getAndIncrement(threadPoolName);
		throw new RejectedExecutionException("Task " + r.toString() +
                " rejected from " +
                executor.toString());
	}

}
