package com.sohu.cache.redis;

import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.redis.ReshardProcess.ReshardStatusEnum;
import com.sohu.cache.util.IdempotentConfirmer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisException;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 水平扩容重构
 * @author leifu
 * @Date 2016年12月7日
 * @Time 上午10:13:00
 */
public class RedisClusterReshard {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * migrate超时时间
     */
    private int migrateTimeout = 10000;

    /**
     * 普通jedis操作超时时间
     */
    private int defaultTimeout = Protocol.DEFAULT_TIMEOUT * 5;

    /**
     * 每次迁移key个数
     */
    private int migrateBatch = 10;

    /**
     * 所有有效节点
     */
    private Set<HostAndPort> hosts;
    
    /**
     * redis操作封装
     */
    private RedisCenter redisCenter;
    
    /**
     * 迁移状态
     */
    private final ReshardProcess reshardProcess = new ReshardProcess();
    
    public RedisClusterReshard(Set<HostAndPort> hosts, RedisCenter redisCenter) {
        this.hosts = hosts;
        this.redisCenter = redisCenter;
    }

    /**
     * 加入主从分片
     */
    public boolean joinCluster(String masterHost, int masterPort, final String slaveHost, final int slavePort) {
        //1. 确认主从节点是否正常
        final Jedis masterJedis = getJedis(masterHost, masterPort, defaultTimeout);
        boolean isRun = redisCenter.isRun(masterHost, masterPort);
        if (!isRun) {
            logger.error(String.format("joinCluster: master host=%s,port=%s is not run", masterHost, masterPort));
            return false;
        }
        boolean hasSlave = StringUtils.isNotBlank(slaveHost) && slavePort > 0;
        final Jedis slaveJedis = hasSlave ? getJedis(slaveHost, slavePort, defaultTimeout) : null;
        if (hasSlave) {
            isRun = redisCenter.isRun(slaveHost, slavePort);
            if (!isRun) {
                logger.error(String.format("joinCluster: slave host=%s,port=%s is not run", slaveHost, slavePort));
                return false;
            }
        }

        //2. 对主从节点进行meet操作
        //获取所有主节点 
        List<HostAndPort> masterHostAndPostList = getMasterNodeList();
        //meet master
        boolean isClusterMeet = clusterMeet(masterHostAndPostList, masterHost, masterPort);
        if (!isClusterMeet) {
            logger.error("master isClusterMeet failed {}:{}", masterHost, masterPort);
            return false;
        }
        if (hasSlave) {
            isClusterMeet = clusterMeet(masterHostAndPostList, slaveHost, slavePort);
            if (!isClusterMeet) {
                logger.error("slave isClusterMeet failed {}:{}", slaveHost, slavePort);
                return false;
            }
        }
        
        //3.复制
        if (hasSlave) {
            final String masterNodeId = getNodeId(masterJedis);
            if (masterNodeId == null) {
                logger.error(String.format("joinCluster:host=%s,port=%s nodeId is null", masterHost, masterPort));
                return false;
            }
            return new IdempotentConfirmer() {
                @Override
                public boolean execute() {
                    try {
                        //等待广播节点
                        TimeUnit.SECONDS.sleep(2);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    String response = slaveJedis.clusterReplicate(masterNodeId);
                    logger.info("clusterReplicate-{}:{}={}", slaveHost, slavePort, response);
                    return response != null && response.equalsIgnoreCase("OK");
                }
            }.run();
        } else {
            return true;
        }
    }

    /**
     * 节点meet
     * @param masterHostAndPostList
     * @param host
     * @param port
     * @return
     */
    private boolean clusterMeet(List<HostAndPort> masterHostAndPostList, final String host, final int port) {
        boolean isSingleNode = redisCenter.isSingleClusterNode(host, port);
        if (!isSingleNode) {
            logger.error("{}:{} isNotSingleNode", host, port);
            return false;
        } else {
            logger.warn("{}:{} isSingleNode", host, port);
        }
        for (HostAndPort hostAndPort : masterHostAndPostList) {
            String clusterHost = hostAndPort.getHost();
            int clusterPort = hostAndPort.getPort();
            final Jedis jedis = new Jedis(clusterHost, clusterPort, defaultTimeout);
            try {
                boolean isClusterMeet = new IdempotentConfirmer() {
                    @Override
                    public boolean execute() {
                        //将新节点添加到集群当中,成为集群中已知新节点
                        String meet = jedis.clusterMeet(host, port);
                        return meet != null && meet.equalsIgnoreCase("OK");
                    }
                }.run();
                if (isClusterMeet) {
                    return true;
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                if (jedis != null)
                    jedis.close();
            }
        }
        return false;
    }
    
    /**
     * 将source中的startSlot到endSlot迁移到target
     *
     */
    public boolean migrateSlot(InstanceInfo sourceInstanceInfo, InstanceInfo targetInstanceInfo, int startSlot, int endSlot, boolean isPipelineMigrate) {
        long startTime = System.currentTimeMillis();
        //上线类型
        reshardProcess.setType(0);
        //迁移的总slot个数
        reshardProcess.setTotalSlot(endSlot - startSlot + 1);
        //源和目标Jedis
        Jedis sourceJedis = getJedis(sourceInstanceInfo.getIp(), sourceInstanceInfo.getPort(), defaultTimeout);
        Jedis targetJedis = getJedis(targetInstanceInfo.getIp(), targetInstanceInfo.getPort(), defaultTimeout);
        //逐个slot迁移
        boolean hasError = false;
        for (int slot = startSlot; slot <= endSlot; slot++) {
            long slotStartTime = System.currentTimeMillis();
            try {
                //num是迁移key的总数
                int num = migrateSlotData(sourceJedis, targetJedis, slot, isPipelineMigrate);
                reshardProcess.addReshardSlot(slot, num);
                logger.warn("clusterReshard:{}->{}, slot={}, keys={}, costTime={} ms", sourceInstanceInfo.getHostPort(),
                        targetInstanceInfo.getHostPort(), slot, num, (System.currentTimeMillis() - slotStartTime));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                hasError = true;
                break;
            }
        }
        if (reshardProcess.getStatus() != ReshardStatusEnum.ERROR.getValue()) {
            reshardProcess.setStatus(ReshardStatusEnum.FINISH.getValue());
        }
        long endTime = System.currentTimeMillis();
        logger.warn("clusterReshard:{}->{}, slot:{}->{}, costTime={} ms", sourceInstanceInfo.getHostPort(),
                targetInstanceInfo.getHostPort(), startSlot, endSlot, (endTime - startTime));
        if (hasError) {
            reshardProcess.setStatus(ReshardStatusEnum.ERROR.getValue());
            return false;
        } else {
            reshardProcess.setStatus(ReshardStatusEnum.FINISH.getValue());
            return true;
        }
    }


    /**
     * 迁移slot数据，并稳定slot配置
     * @throws Exception
     */
    private int moveSlotData(final Jedis source, final Jedis target, final int slot, boolean isPipelineMigrate) throws Exception {
        int num = 0;
        while (true) {
            final Set<String> keys = new HashSet<String>();
            boolean isGetKeysInSlot = new IdempotentConfirmer() {
                @Override
                public boolean execute() {
                    List<String> perKeys = source.clusterGetKeysInSlot(slot, migrateBatch);
                    if (perKeys != null && perKeys.size() > 0) {
                        keys.addAll(perKeys);
                    }
                    return true;
                }
            }.run();
            if (!isGetKeysInSlot) {
                throw new RuntimeException(String.format("get keys failed slot=%d num=%d", slot, num));
            }
            if (keys.isEmpty()) {
                break;
            }
            for (final String key : keys) {
                boolean isKeyMigrate = new IdempotentConfirmer() {
                    // 失败后，迁移时限加倍
                    private int migrateTimeOutFactor = 1;

                    @Override
                    public boolean execute() {
                        String response = source.migrate(target.getClient().getHost(), target.getClient().getPort(),
                                key, 0, migrateTimeout * (migrateTimeOutFactor++));
                        return response != null && (response.equalsIgnoreCase("OK") || response.equalsIgnoreCase("NOKEY"));
                    }
                }.run();
                if (!isKeyMigrate) {
                    throw new RuntimeException("migrate key=" + key + failedInfo(source, slot));
                } else {
                    num++;
                    logger.info("migrate key={};response=OK", key);
                }
            }
        }
        final String targetNodeId = getNodeId(target);
        boolean isClusterSetSlotNode;
        //设置 slot新归属节点
        isClusterSetSlotNode = new IdempotentConfirmer() {
            @Override
            public boolean execute() {
                boolean isOk = false;
                List<HostAndPort> masterNodesList = getMasterNodeList();
                for (HostAndPort hostAndPort : masterNodesList) {
                    Jedis jedis = null;
                    try {
                        jedis = new Jedis(hostAndPort.getHost(), hostAndPort.getPort());
                        String response = jedis.clusterSetSlotNode(slot, targetNodeId);
                        isOk = response != null && response.equalsIgnoreCase("OK");
                        if (isOk) {
                            response = source.clusterSetSlotNode(slot, targetNodeId);
                            isOk = response != null && response.equalsIgnoreCase("OK");
                        } else {
                            logger.error("clusterSetSlotNode-{}={}", getNodeId(target), response);
                            break;
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    } finally {
                       if (jedis != null)
                           jedis.close();
                    }
                }
                return isOk;
            }
        }.run();
        if (!isClusterSetSlotNode) {
            throw new RuntimeException("clusterSetSlotNode:" + failedInfo(target, slot));
        }
        return num;
    }

    /**
     * 指派迁移节点数据
     * CLUSTER SETSLOT <slot> IMPORTING <node_id> 从 node_id 指定的节点中导入槽 slot 到本节点。
     * CLUSTER SETSLOT <slot> MIGRATING <node_id> 将本节点的槽 slot 迁移到 node_id 指定的节点中。
     * CLUSTER GETKEYSINSLOT <slot> <count> 返回 count 个 slot 槽中的键。
     * MIGRATE host port key destination-db timeout [COPY] [REPLACE]
     * CLUSTER SETSLOT <slot> NODE <node_id> 将槽 slot 指派给 node_id 指定的节点，如果槽已经指派给另一个节点，那么先让另一个节点删除该槽>，然后再进行指派。
     */
    private int migrateSlotData(final Jedis source, final Jedis target, final int slot, boolean isPipelineMigrate) {
        int num = 0;
        final String sourceNodeId = getNodeId(source);
        final String targetNodeId = getNodeId(target);
        boolean isError = false;
        if (sourceNodeId == null || targetNodeId == null) {
            throw new JedisException(String.format("sourceNodeId = %s || targetNodeId = %s", sourceNodeId, targetNodeId));
        }
        boolean isImport = new IdempotentConfirmer() {
            @Override
            public boolean execute() {
                String importing = target.clusterSetSlotImporting(slot, sourceNodeId);
                logger.info("slot={},clusterSetSlotImporting={}", slot, importing);
                return importing != null && importing.equalsIgnoreCase("OK");
            }
        }.run();
        if (!isImport) {
            isError = true;
            logger.error("clusterSetSlotImporting" + failedInfo(target, slot));
        }
        boolean isMigrate = new IdempotentConfirmer() {
            @Override
            public boolean execute() {
                String migrating = source.clusterSetSlotMigrating(slot, targetNodeId);
                logger.info("slot={},clusterSetSlotMigrating={}", slot, migrating);
                return migrating != null && migrating.equalsIgnoreCase("OK");
            }
        }.run();

        if (!isMigrate) {
            isError = true;
            logger.error("clusterSetSlotMigrating" + failedInfo(source, slot));
        }

        try {
            num = moveSlotData(source, target, slot, isPipelineMigrate);
        } catch (Exception e) {
            isError = true;
            logger.error(e.getMessage(), e);
        }
        if (!isError) {
            return num;
        } else {
            String errorMessage = "source=%s target=%s slot=%d num=%d reShard failed";
            throw new RuntimeException(String.format(errorMessage, getNodeKey(source), getNodeKey(target), slot, num));
        }
    }

    private String failedInfo(Jedis jedis, int slot) {
        return String.format(" failed %s:%d slot=%d", jedis.getClient().getHost(), jedis.getClient().getPort(), slot);
    }
    
    /**
     * 获取所有主节点
     * @return
     */
    private List<HostAndPort> getMasterNodeList() {
        List<HostAndPort> masterNodeList = new ArrayList<HostAndPort>();
        //获取RedisCluster所有节点
        JedisCluster jedisCluster = new JedisCluster(hosts, defaultTimeout);
        Collection<JedisPool> allNodes = jedisCluster.getConnectionHandler().getNodes().values();
        try {
            for (JedisPool jedisPool : allNodes) {
                String host = jedisPool.getHost();
                int port = jedisPool.getPort();
                if (!redisCenter.isMaster(host, port)) {
                    continue;
                }
                masterNodeList.add(new HostAndPort(host, port));
            }
        } finally {
            jedisCluster.close();
        }
        return masterNodeList;
    }


    private Jedis getJedis(String host, int port, int timeout) {
        return new Jedis(host, port, timeout);
    }

    private final Map<String, String> nodeIdCachedMap = new HashMap<String, String>();

    public String getNodeId(final Jedis jedis) {
        String nodeKey = getNodeKey(jedis);
        if (nodeIdCachedMap.get(nodeKey) != null) {
            return nodeIdCachedMap.get(nodeKey);
        } else {
            String nodeId = redisCenter.getNodeId(jedis.getClient().getHost(), jedis.getClient().getPort());
            nodeIdCachedMap.put(nodeKey, nodeId);
            return nodeId;
        }
    }
    
    protected String getNodeKey(Jedis jedis) {
        return jedis.getClient().getHost() + ":" + jedis.getClient().getPort();
    }

    public void setMigrateTimeout(int migrateTimeout) {
        this.migrateTimeout = migrateTimeout;
    }

    public void setDefaultTimeout(int defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    public ReshardProcess getReshardProcess() {
        return reshardProcess;
    }

}
