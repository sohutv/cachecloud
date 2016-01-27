package com.sohu.cache.stats.instance.impl;

import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.exception.SSHException;
import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.memcached.MemcachedCenter;
import com.sohu.cache.protocol.RedisProtocol;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.ssh.SSHUtil;
import com.sohu.cache.stats.instance.InstanceDeployCenter;
import com.sohu.cache.util.TypeUtil;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * Created by yijunzhang on 14-11-26.
 */
public class InstanceDeployCenterImpl implements InstanceDeployCenter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private InstanceDao instanceDao;

    private RedisCenter redisCenter;

    private MemcachedCenter memcachedCenter;

    private MachineCenter machineCenter;

    @Override
    public boolean startExistInstance(int instanceId) {
        Assert.isTrue(instanceId > 0L);
        InstanceInfo instanceInfo = instanceDao.getInstanceInfoById(instanceId);
        Assert.isTrue(instanceInfo != null);
        int type = instanceInfo.getType();
        String host = instanceInfo.getIp();
        int port = instanceInfo.getPort();
        boolean isRun;
        if (TypeUtil.isRedisType(type)) {
            isRun = redisCenter.isRun(host, port);
            if (isRun) {
                logger.warn("{}:{} instance is Running", host, port);
            } else {
                String runShell;
                if (TypeUtil.isRedisCluster(type)) {
                    runShell = RedisProtocol.getRunShell(port, true);
                } else if (TypeUtil.isRedisSentinel(type)) {
                    runShell = RedisProtocol.getSentinelShell(port);
                } else {
                    runShell = RedisProtocol.getRunShell(port, false);
                }
                boolean isRunShell = machineCenter.startProcessAtPort(host, port, runShell);
                if (!isRunShell) {
                    logger.error("startProcessAtPort-> {}:{} shell= {} failed", host, port, runShell);
                    return false;
                } else {
                    logger.warn("{}:{} instance has Run", host, port);
                }
                isRun = redisCenter.isRun(host, port);
            }
        } else if (TypeUtil.isMemcacheType(type)) {
            isRun = memcachedCenter.isRun(host, port);
            if (isRun) {
                logger.warn("{}:{} instance is Running", host, port);
            } else {
                String cmd = instanceInfo.getCmd();
                if (StringUtils.isBlank(cmd)) {
                    logger.warn("{}:{} cmd is null");
                    return false;
                }
                boolean isRunShell = machineCenter.startProcessAtPort(host, port, cmd);
                if (!isRunShell) {
                    logger.error("startProcessAtPort-> {}:{} shell= {} failed", host, port, cmd);
                    return false;
                } else {
                    logger.warn("{}:{} instance has Run", host, port);
                }
                isRun = memcachedCenter.isRun(host, port);
            }
        } else {
            logger.error("type={} not match!", type);
            isRun = false;
        }
        if (isRun) {
            instanceInfo.setStatus(1);
            instanceDao.update(instanceInfo);
            if (TypeUtil.isRedisType(type)) {
                redisCenter.deployRedisCollection(instanceInfo.getAppId(), instanceInfo.getIp(), instanceInfo.getPort());
            } else {
                memcachedCenter.deployMemcachedCollection(instanceInfo.getAppId(), instanceInfo.getIp(), instanceInfo.getPort());
            }
        }

        return isRun;
    }

    @Override
    public boolean shutdownExistInstance(int instanceId) {
        Assert.isTrue(instanceId > 0L);
        InstanceInfo instanceInfo = instanceDao.getInstanceInfoById(instanceId);
        Assert.isTrue(instanceInfo != null);
        int type = instanceInfo.getType();
        String host = instanceInfo.getIp();
        int port = instanceInfo.getPort();
        boolean isShutdown;
        if (TypeUtil.isRedisType(type)) {
            isShutdown = redisCenter.shutdown(host, port);
            if (isShutdown) {
                logger.warn("{}:{} redis is shutdown", host, port);
            } else {
                logger.error("{}:{} redis shutdown error", host, port);
            }
        } else if (TypeUtil.isMemcacheType(type)) {
            isShutdown = memcachedCenter.shutdown(host, port);
            if (isShutdown) {
                logger.warn("{}:{} memcache is shutdown", host, port);
            } else {
                logger.error("{}:{} memcache shutdown error", host, port);
            }
        } else {
            logger.error("type={} not match!", type);
            isShutdown = false;
        }

        if (isShutdown) {
            instanceInfo.setStatus(2);
            instanceDao.update(instanceInfo);
            if (TypeUtil.isRedisType(type)) {
                redisCenter.unDeployRedisCollection(instanceInfo.getAppId(), instanceInfo.getIp(), instanceInfo.getPort());
            } else {
                memcachedCenter.unDeployMemcachedCollection(instanceInfo.getAppId(), instanceInfo.getIp(), instanceInfo.getPort());
            }
        }
        return isShutdown;
    }
    
    @Override
    public String showInstanceRecentLog(int instanceId, int maxLineNum){
        Assert.isTrue(instanceId > 0L);
        InstanceInfo instanceInfo = instanceDao.getInstanceInfoById(instanceId);
        Assert.isTrue(instanceInfo != null);
        String host = instanceInfo.getIp();
        int port = instanceInfo.getPort();
        try {
            String remoteFilePath = "/opt/cachecloud/logs/redis-"+port+"-*.log";
            try {
                String loglog = SSHUtil.execute(host, "tail -n100 " + remoteFilePath);
            } catch (SSHException e) {
                logger.error(e.getMessage(), e);
            }
            return machineCenter.showInstanceRecentLog(host, port, maxLineNum);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
    

    public void setInstanceDao(InstanceDao instanceDao) {
        this.instanceDao = instanceDao;
    }

    public void setRedisCenter(RedisCenter redisCenter) {
        this.redisCenter = redisCenter;
    }

    public void setMemcachedCenter(MemcachedCenter memcachedCenter) {
        this.memcachedCenter = memcachedCenter;
    }

    public void setMachineCenter(MachineCenter machineCenter) {
        this.machineCenter = machineCenter;
    }

    

}
