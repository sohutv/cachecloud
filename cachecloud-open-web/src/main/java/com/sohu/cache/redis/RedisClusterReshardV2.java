package com.sohu.cache.redis;

import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.util.IdempotentConfirmer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisException;

import java.util.*;

/**
 * 水平扩容第二版
 * @author leifu
 * @Date 2016年12月4日
 * @Time 下午9:30:09
 */
public class RedisClusterReshardV2 {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private int migrateTimeout = 10000;

    private int defaultTimeout = Protocol.DEFAULT_TIMEOUT * 5;

    private final ReshardProcess reshardProcess = new ReshardProcess();

    private int migrateBatch = 100;
    

    /**
     * 将source中的startSlot到endSlot迁移到target
     *
     */
    public boolean migrateSlot(List<InstanceInfo> instanceInfoList, InstanceInfo sourceInstanceInfo,
			InstanceInfo targetInstanceInfo, int startSlot, int endSlot) {
		long startTime = System.currentTimeMillis();
		//上线类型
		reshardProcess.setType(0);
		//迁移的总slot个数
		reshardProcess.setTotalSlot(endSlot - startSlot + 1);
		//源和目标Jedis
		Jedis sourceJedis = getJedis(sourceInstanceInfo.getIp(), sourceInstanceInfo.getPort(), defaultTimeout);
		Jedis targetJedis = getJedis(targetInstanceInfo.getIp(), targetInstanceInfo.getPort(), defaultTimeout);
		//
        boolean hasError = false;
		for (int slot = startSlot; slot <= endSlot; slot++) {
			long slotStartTime = System.currentTimeMillis();
			try {
				//num是迁移key的总数
				int num = migrateSlotData(sourceJedis, targetJedis, slot);
				reshardProcess.addReshardSlot(slot, num);
				logger.warn("clusterReshard:{}->{}, slot={}, keys={}, costTime={} ms", sourceInstanceInfo.getHostPort(),
						targetInstanceInfo.getHostPort(), slot, num, (System.currentTimeMillis() - slotStartTime));
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				hasError = true;
				break;
			}
		}
		if (reshardProcess.getStatus() != 2) {
			reshardProcess.setStatus(1);
		}
		long endTime = System.currentTimeMillis();
		logger.warn("clusterReshard:{}->{}, slot:{}->{}, costTime={} ms", sourceInstanceInfo.getHostPort(),
				targetInstanceInfo.getHostPort(), startSlot, endSlot, (endTime - startTime));
		if (hasError) {
			reshardProcess.setStatus(2);
			return false;
		} else {
			reshardProcess.setStatus(1);
			return true;
		}
    }

    /**
     * 迁移slot数据，并稳定slot配置
     * @throws Exception
     */
    private int moveSlotData(final Jedis source, final Jedis target, final int slot) throws Exception {
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
                    //失败后，迁移时限加倍
                    private int migrateTimeOutFactor = 1;
                    @Override
                    public boolean execute() {
                        String response = source.migrate(target.getClient().getHost(), target.getClient().getPort(),
                                key, 0, migrateTimeout * (migrateTimeOutFactor++));
                        return response != null && (response.equalsIgnoreCase("OK") || /*TODO 确认*/ response.equalsIgnoreCase("NOKEY"));
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
        //改为对所有节点执行setslot
        
        
        boolean isDelSlots = new IdempotentConfirmer() {
            @Override
            public boolean execute() {
                String response = source.clusterDelSlots(slot);
                logger.info("clusterDelSlots-{}:{}={}", source.getClient().getHost(), source.getClient().getPort(), response);
                return response != null && response.equalsIgnoreCase("OK");
            }
        }.run();
        if (!isDelSlots) {
            throw new RuntimeException("clusterDelSlots:" + failedInfo(source, slot));
        }
        final String targetNodeId = getNodeId(target);
        boolean isClusterSetSlotNode;
        //设置 slot新归属节点
        isClusterSetSlotNode = new IdempotentConfirmer() {
            @Override
            public boolean execute() {
                String response = target.clusterSetSlotNode(slot, targetNodeId);
                boolean isOk = response != null && response.equalsIgnoreCase("OK");
                if (isOk) {
                    response = source.clusterSetSlotNode(slot, targetNodeId);
                    isOk = response != null && response.equalsIgnoreCase("OK");
                } else {
                    logger.error("clusterSetSlotNode-{}={}", getNodeId(target), response);
                }
                if (!isOk) {
                    logger.error("clusterSetSlotNode-{}={}", getNodeId(source), response);
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
    private int migrateSlotData(final Jedis source, final Jedis target, final int slot) {
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
            num = moveSlotData(source, target, slot);
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

    private final Map<String, String> nodeIdCachedMap = new HashMap<String, String>();

    public String getNodeId(final Jedis jedis) {
        String nodeKey = getNodeKey(jedis);
        if (nodeIdCachedMap.get(nodeKey) != null) {
            return nodeIdCachedMap.get(nodeKey);
        }
        try {
            final StringBuilder clusterNodes = new StringBuilder();
            boolean isGetNodes = new IdempotentConfirmer() {
                @Override
                public boolean execute() {
                    String nodes = jedis.clusterNodes();
                    if (nodes != null && nodes.length() > 0) {
                        clusterNodes.append(nodes);
                        return true;
                    }
                    return false;
                }
            }.run();
            if (!isGetNodes) {
                logger.error("clusterNodes" + failedInfo(jedis, -1));
                return null;
            }
            for (String infoLine : clusterNodes.toString().split("\n")) {
                if (infoLine.contains("myself")) {
                    String nodeId = infoLine.split(" ")[0];
                    nodeIdCachedMap.put(nodeKey, nodeId);
                    return nodeId;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
    
    private Jedis getJedis(String host, int port, int timeout) {
        return new Jedis(host, port, timeout);
    }
	
    private String getNodeKey(Jedis jedis) {
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
