package com.sohu.cache.schedule.jobs;

import com.alibaba.druid.pool.DruidDataSource;
import com.sohu.cache.async.AsyncThreadPoolFactory;
import com.sohu.cache.dao.DbPoolStatDao;
import com.sohu.cache.dao.ThreadPoolStatDao;
import com.sohu.cache.entity.DbPoolStat;
import com.sohu.cache.entity.ThreadPoolStat;
import com.sohu.cache.web.util.IpUtil;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.metrics.PoolStats;
import com.zaxxer.hikari.pool.HikariPool;
import org.apache.catalina.connector.Connector;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.coyote.ProtocolHandler;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.springframework.beans.BeansException;

import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author fulei
 * @date 2018年6月26日
 * @time 下午1:36:30
 */
@Service
public class MonitorThreadPoolJob extends CacheBaseJob {

    private static final long serialVersionUID = -8271862555794690462L;

    IpUtil ipUtil;

    @Override
    public void action(JobExecutionContext context) {

        long startTime = System.currentTimeMillis();
        try {
            SchedulerContext schedulerContext = context.getScheduler().getContext();
            ApplicationContext applicationContext = (ApplicationContext) schedulerContext.get(APPLICATION_CONTEXT_KEY);
            ThreadPoolStatDao threadPoolStatDao = (ThreadPoolStatDao) applicationContext.getBean("threadPoolStatDao");
            DbPoolStatDao dbPoolStatDao = (DbPoolStatDao) applicationContext.getBean("dbPoolStatDao");
            ipUtil = (IpUtil) applicationContext.getBean("ipUtil");

            //threadpool
            Map<String, ThreadPoolExecutor> threadPoolMap = new LinkedHashMap<String, ThreadPoolExecutor>();
            try {
                Object quartzThreadPoolObject = applicationContext.getBean("quartzThreadPool");
                if (quartzThreadPoolObject != null) {
                    threadPoolMap.put("quartzThreadPool",
                            ((ThreadPoolTaskExecutor) quartzThreadPoolObject).getThreadPoolExecutor());
                }
                Object dataJvmQuartzThreadPool = applicationContext.getBean("jvmQuartzThreadPool");
                if (dataJvmQuartzThreadPool != null) {
                    threadPoolMap.put("jvmQuartzThreadPool",
                            ((ThreadPoolTaskExecutor) dataJvmQuartzThreadPool).getThreadPoolExecutor());
                }
            } catch (BeansException e) {
                logger.error(e.getMessage(), e);
            } catch (IllegalStateException e) {
                logger.error(e.getMessage(), e);
            }

            threadPoolMap
                    .put(AsyncThreadPoolFactory.DEFAULT_ASYNC_POOL, AsyncThreadPoolFactory.DEFAULT_ASYNC_THREAD_POOL);
            threadPoolMap
                    .put(AsyncThreadPoolFactory.TASK_EXECUTE_POOL, AsyncThreadPoolFactory.TASK_EXECUTE_THREAD_POOL);
            threadPoolMap
                    .put(AsyncThreadPoolFactory.CLIENT_REPORT_POOL, AsyncThreadPoolFactory.CLIENT_REPORT_THREAD_POOL);
            threadPoolMap
                    .put(AsyncThreadPoolFactory.REDIS_SLOWLOG_POOL, AsyncThreadPoolFactory.REDIS_SLOWLOG_THREAD_POOL);
            threadPoolMap.put(AsyncThreadPoolFactory.MACHINE_POOL, AsyncThreadPoolFactory.MACHINE_THREAD_POOL);
            threadPoolMap.put(AsyncThreadPoolFactory.APP_POOL, AsyncThreadPoolFactory.APP_THREAD_POOL);
            threadPoolMap.put(AsyncThreadPoolFactory.BREVITY_SCHEDULER_POOL,
                    AsyncThreadPoolFactory.BREVITY_SCHEDULER_ASYNC_THREAD_POOL);
            threadPoolMap.put(AsyncThreadPoolFactory.RESHARD_PROCESS_POOL,
                    AsyncThreadPoolFactory.RESHARD_PROCESS_THREAD_POOL);

            //tomcat

            if (applicationContext instanceof EmbeddedWebApplicationContext) {
                EmbeddedServletContainer embeddedServletContainer = ((EmbeddedWebApplicationContext) applicationContext)
                        .getEmbeddedServletContainer();
                if (embeddedServletContainer instanceof TomcatEmbeddedServletContainer) {
                    Connector connector = ((TomcatEmbeddedServletContainer) embeddedServletContainer).getTomcat()
                            .getConnector();
                    ProtocolHandler handler = connector.getProtocolHandler();
                    org.apache.tomcat.util.threads.ThreadPoolExecutor executor = (org.apache.tomcat.util.threads.ThreadPoolExecutor) handler
                            .getExecutor();
                    threadPoolMap.put("tomcat-thread-pool", executor);
                }
            }
            //print
            printAndSaveThreadPoolMap(threadPoolStatDao, threadPoolMap);

            //datasource
//            Map<String, DruidDataSource> dsMap = new LinkedHashMap<String, DruidDataSource>();
//            dsMap.put("cacheCloudDB", (DruidDataSource) applicationContext.getBean("cacheCloudDB"));
            Map<String, HikariDataSource> dsMap = new LinkedHashMap<>();
            dsMap.put("cacheCloudDB", (HikariDataSource) applicationContext.getBean("cacheCloudDB"));
            //print
//            printAndSaveDataSourcePoolMap(dbPoolStatDao, dsMap);
            printAndSaveDataSourcePoolMap2(dbPoolStatDao, dsMap);

            //jedisPool
            /*Map<String, JedisPool> jedisPoolMap = new LinkedHashMap<String, JedisPool>();
            jedisPoolMap.put("jedisPoolMain", (JedisPool) applicationContext.getBean("jedisPoolMain"));
            jedisPoolMap.put("jedisPoolBack", (JedisPool) applicationContext.getBean("jedisPoolBack"));
            //print
            printJedisPoolMap(jedisPoolMap);*/
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        }
        logger.warn("============MonitorThreadPoolJob cost time {} ms==========",
                (System.currentTimeMillis() - startTime));
    }

    private void printAndSaveDataSourcePoolMap(DbPoolStatDao dbPoolStatDao, Map<String, DruidDataSource> dsMap) {
        Date collectDate = new Date();

        try {
            String format = "%30s%30s%10s%10s%10s%10s%10s\n";
            StringBuilder logBuilder = new StringBuilder("\n");
            // todo 可量化driud统计指标: 活跃连接峰值；
            /**
             * max 连接池最大连接数
             * min 连接池最小连接数
             * busy 当前活跃连接数
             * idle 当前空闲连接数
             * total 连接池总连接
             */
            logBuilder.append(String.format(format, "timestamp", "ds", "max", "min", "busy", "idle", "total"));
            for (Entry<String, DruidDataSource> entry : dsMap.entrySet()) {
                String dataSourceName = entry.getKey() + "-stat";
                DruidDataSource ds = entry.getValue();
                logBuilder.append(String
                        .format(format, dataSourceName, getDateFormat(collectDate), ds.getMaxActive(), ds.getMinIdle(),
                                ds.getActiveCount(), ds.getPoolingCount(),
                                ds.getActiveCount() + ds.getPoolingCount()));

                DbPoolStat dbPoolStat = new DbPoolStat();
                String ipport = ipUtil.getCurrentIpPort();
                String ip = ipport.split(":")[0];
                int port = NumberUtils.toInt(ipport.split(":")[1]);
                dbPoolStat.setIp(ip);
                dbPoolStat.setPort(port);
                dbPoolStat.setDbPoolName(dataSourceName);
                dbPoolStat.setCollectTime(getCollectTime(collectDate));
                dbPoolStat.setCollectDate(collectDate);
                dbPoolStat.setMaxSize(ds.getMaxActive());
                dbPoolStat.setMinSize(ds.getMinIdle());
                dbPoolStat.setBusySize(ds.getActiveCount());
                dbPoolStat.setIdleSize(ds.getPoolingCount());
                dbPoolStat.setTotalSize(ds.getActiveCount() + ds.getPoolingCount());
                Date now = new Date();
                dbPoolStat.setCreateTime(now);
                dbPoolStat.setUpdateTime(now);
                dbPoolStatDao.save(dbPoolStat);
            }
            logger.warn(logBuilder.toString());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void printAndSaveDataSourcePoolMap2(DbPoolStatDao dbPoolStatDao, Map<String, HikariDataSource> dsMap) {
        Date collectDate = new Date();

        try {
            String format = "%30s%30s%10s%10s%10s%10s%10s\n";
            StringBuilder logBuilder = new StringBuilder("\n");

            logBuilder.append(String.format(format, "timestamp", "ds", "max", "min", "busy", "idle", "total"));
            for (Entry<String, HikariDataSource> entry : dsMap.entrySet()) {
                String dataSourceName = entry.getKey() + "-stat";
                HikariDataSource ds = entry.getValue();

                //get HikariPool pool
                Field hikariPool = ds.getClass().getDeclaredField("pool");
                hikariPool.setAccessible(true);
                HikariPool pool = (HikariPool) hikariPool.get(ds);

                //get PoolStats poolStats
                Method getPoolStats = pool.getClass().getDeclaredMethod("getPoolStats");
                getPoolStats.setAccessible(true);
                PoolStats poolStats = (PoolStats) getPoolStats.invoke(pool);

                logBuilder.append(String.format(format, getDateFormat(collectDate), dataSourceName,
                        poolStats.getMaxConnections(), poolStats.getMinConnections(),
                        poolStats.getActiveConnections(), poolStats.getIdleConnections(),
                        poolStats.getTotalConnections()));

                DbPoolStat dbPoolStat = new DbPoolStat();
                String ipport = ipUtil.getCurrentIpPort();
                String ip = ipport.split(":")[0];
                int port = NumberUtils.toInt(ipport.split(":")[1]);
                dbPoolStat.setIp(ip);
                dbPoolStat.setPort(port);
                dbPoolStat.setDbPoolName(dataSourceName);
                dbPoolStat.setCollectTime(getCollectTime(collectDate));
                dbPoolStat.setCollectDate(collectDate);
                dbPoolStat.setMaxSize(poolStats.getMaxConnections());
                dbPoolStat.setMinSize(poolStats.getMinConnections());
                dbPoolStat.setBusySize(poolStats.getActiveConnections());
                dbPoolStat.setIdleSize(poolStats.getIdleConnections());
                dbPoolStat.setTotalSize(poolStats.getTotalConnections());
                Date now = new Date();
                dbPoolStat.setCreateTime(now);
                dbPoolStat.setUpdateTime(now);
                dbPoolStatDao.save(dbPoolStat);
            }
            logger.warn(logBuilder.toString());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void printAndSaveThreadPoolMap(ThreadPoolStatDao threadPoolStatDao,
                                           Map<String, ThreadPoolExecutor> threadPoolMap) {
        Date collectDate = new Date();

        String format = "%30s%30s%10s%10s%10s%15s%20s%10s\n";
        StringBuilder logBuilder = new StringBuilder("\n");
        /**
         * max: 线程池最大工作线程数量
         * core: 线程池初始化工作线程数量
         * active: 正在执行任务的工作线程数量
         * taskCount: 执行任务总数量
         * completedTaskCount: 已执行完成任务总数量
         * queueSize: queue队列情况
         */
        logBuilder.append(String
                .format(format, "ThreadPool", "timestamp", "max", "core", "active", "taskCount", "completedTaskCount",
                        "queueSize"));

        for (Entry<String, ThreadPoolExecutor> entry : threadPoolMap.entrySet()) {
            try {
                //1.打印日志
                String threadPoolName = entry.getKey() + "-stat";
                ThreadPoolExecutor threadPool = entry.getValue();
                logBuilder.append(String
                        .format(format, threadPoolName, getDateFormat(collectDate), threadPool.getMaximumPoolSize(),
                                threadPool.getCorePoolSize(),
                                threadPool.getActiveCount(), threadPool.getTaskCount(),
                                threadPool.getCompletedTaskCount(), threadPool.getQueue().size()));

                //2.保存线程池统计
                ThreadPoolStat threadPoolStat = new ThreadPoolStat();
                String ipport = ipUtil.getCurrentIpPort();
                String ip = ipport.split(":")[0];
                int port = NumberUtils.toInt(ipport.split(":")[1]);
                threadPoolStat.setIp(ip);
                threadPoolStat.setPort(port);
                threadPoolStat.setThreadPoolName(threadPoolName);
                threadPoolStat.setCollectTime(getCollectTime(collectDate));
                threadPoolStat.setCollectDate(collectDate);
                threadPoolStat.setMaximumPoolSize(threadPool.getMaximumPoolSize());
                threadPoolStat.setCorePoolSize(threadPool.getCorePoolSize());
                threadPoolStat.setActiveCount(threadPool.getActiveCount());
                threadPoolStat.setTaskCount(threadPool.getTaskCount());
                threadPoolStat.setCompletedTaskCount(threadPool.getCompletedTaskCount());
                threadPoolStat.setQueueSize(threadPool.getQueue().size());
                Date now = new Date();
                threadPoolStat.setCreateTime(now);
                threadPoolStat.setUpdateTime(now);
                //差值
                Long lastTaskCount = threadPoolStatDao.getLastTaskCount(ip, port, threadPoolName);
                if (lastTaskCount == null) {
                    lastTaskCount = 0L;
                }
                long diffTaskCount = threadPool.getTaskCount() - lastTaskCount;
                if (diffTaskCount < 0) {
                    diffTaskCount = threadPool.getTaskCount();
                }
                threadPoolStat.setDiffTaskCount(diffTaskCount);

                threadPoolStatDao.save(threadPoolStat);
            } catch (NumberFormatException e) {
                logger.error(e.getMessage(), e);
            }
        }
        logger.warn(logBuilder.toString());
    }

    private long getCollectTime(Date collectDate) {
        return NumberUtils.toLong(new SimpleDateFormat("yyyyMMddHHmmss").format(collectDate));
    }

    public String getDateFormat(Date collectDate) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(collectDate);
    }

}