package com.sohu.cache.redis;

import com.sohu.cache.util.IdempotentConfirmer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.ClusterNodeInformation;
import redis.clients.util.ClusterNodeInformationParser;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 *
 * Created by yijunzhang on 14-9-4.
 */
public class RedisClusterReshard {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<String, HostAndPort> nodeMap = new LinkedHashMap<String, HostAndPort>();

    private int migrateTimeout = 10000;

    private int defaultTimeout = Protocol.DEFAULT_TIMEOUT * 5;

    private final ReshardProcess reshardProcess;

    private int migrateBatch = 100;

    private final Set<HostAndPort> hosts;

    private static final int allSlots = 16384;

    static {
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
    }

    public RedisClusterReshard(Set<HostAndPort> hosts) {
        for (HostAndPort host : hosts) {
            String key = JedisClusterInfoCache.getNodeKey(host);
            nodeMap.put(key, host);
        }
        this.hosts = hosts;
        reshardProcess = new ReshardProcess();
    }

    private boolean isInCluster(Jedis jedis, List<ClusterNodeInformation> masterNodes) {
        for (ClusterNodeInformation info : masterNodes) {
            String nodeKey = getNodeKey(info.getNode());
            String jedisKey = getNodeKey(jedis);
            if (nodeKey.equals(jedisKey)) {
                return true;
            }
        }
        return false;
    }

    protected String getNodeKey(HostAndPort hnp) {
        return hnp.getHost() + ":" + hnp.getPort();
    }

    protected String getNodeKey(Jedis jedis) {
        return jedis.getClient().getHost() + ":" + jedis.getClient().getPort();
    }

    private List<ClusterNodeInformation> getMasterNodes() {
        Map<String, ClusterNodeInformation> masterNodeMap = new LinkedHashMap<String, ClusterNodeInformation>();
        JedisCluster jedisCluster = new JedisCluster(hosts, defaultTimeout);
        //所有节点
        Collection<JedisPool> allNodes = jedisCluster.getConnectionHandler().getNodes().values();
        try {
            for (JedisPool jedisPool : allNodes) {
                final String host = jedisPool.getHost();
                final int port = jedisPool.getPort();
                final Jedis jedis = getJedis(host, port, defaultTimeout);
                if(!isMaster(jedis)){
                    continue;
                }
                try {
                    final StringBuilder clusterNodes = new StringBuilder();
                    boolean isGetNodes = new IdempotentConfirmer() {
                        @Override
                        public boolean execute() {
                            String nodes = jedis.clusterNodes();
                            if (nodes != null && nodes.length() > 0) {
                                String[] array = nodes.split("\n");
                                for (String node : array) {
                                    if (node.contains(host + ":" + port)) {
                                        clusterNodes.append(node);
                                    }
                                }
                                return true;
                            }
                            return false;
                        }
                    }.run();
                    if (!isGetNodes) {
                        logger.error("clusterNodes" + failedInfo(jedis, -1));
                        continue;
                    }
                    String nodeInfo = clusterNodes.toString();
                    if (StringUtils.isNotBlank(nodeInfo)) {
                        ClusterNodeInformationParser nodeInfoParser = new ClusterNodeInformationParser();
                        ClusterNodeInformation clusterNodeInfo = nodeInfoParser.parse(
                                nodeInfo, new HostAndPort(jedis.getClient().getHost(),
                                jedis.getClient().getPort()));
                        masterNodeMap.put(getNodeKey(clusterNodeInfo.getNode()), clusterNodeInfo);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    jedis.close();
                }
            }
        } finally {
            jedisCluster.close();
        }
        List<ClusterNodeInformation> resultList = new ArrayList<ClusterNodeInformation>(masterNodeMap.values());
        //按slot大小排序
        Collections.sort(resultList, new Comparator<ClusterNodeInformation>() {
            @Override
            public int compare(ClusterNodeInformation node1, ClusterNodeInformation node2) {
                if (node1 == node2) {
                    return 0;
                }
                int slotNum1 = 0;
                int slotNum2 = 0;
                List<Integer> slots1 = node1.getAvailableSlots();
                for (Integer slot : slots1) {
                    slotNum1 += slot;
                }
                List<Integer> slots2 = node2.getAvailableSlots();
                for (Integer slot : slots2) {
                    slotNum2 += slot;
                }
                if (slots2.isEmpty()) {
                    if (slots1.isEmpty()) {
                        return 0;
                    } else {
                        return 1;
                    }
                }
                if (slots1.isEmpty()) {
                    if (slots2.isEmpty()) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
                slotNum1 = slotNum1 / slots1.size();
                slotNum2 = slotNum2 / slots2.size();
                if (slotNum1 == slotNum2) {
                    return 0;
                } else if (slotNum1 > slotNum2) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        return resultList;
    }

    private boolean isMaster(final Jedis jedis) {
        return new IdempotentConfirmer() {
            @Override
            public boolean execute() {
                String replications = jedis.info("Replication");
                if (StringUtils.isNotBlank(replications)) {
                    String[] data = replications.split("\r\n");
                    for (String line : data) {
                        String[] arr = line.split(":");
                        if (arr.length > 1) {
                            String value = arr[1];
                            if (value.equalsIgnoreCase("master")) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
        }.run();
    }

    public boolean offLineMaster(String host, int port) {
        long begin = System.currentTimeMillis();
        reshardProcess.setType(1);
        final Jedis offJedis = getJedis(host, port, defaultTimeout);
        boolean isRun = isRun(offJedis);
        if (!isRun) {
            throw new JedisException(String.format("clusterReshard:host=%s,port=%s is not run", host, port));
        }
        List<ClusterNodeInformation> masterNodes = getMasterNodes();
        if (!isInCluster(offJedis, masterNodes)) {
            throw new JedisException(String.format("clusterReshard:host=%s,port=%s not inCluster", host, port));
        }
        boolean isCheckAndMoveSlot = checkAndMovingSlot(masterNodes);
        if (!isCheckAndMoveSlot) {
            logger.error("checkAndMovingSlot error!");
            return false;
        }

        ClusterNodeInformation offNode = null;
        for (ClusterNodeInformation nodeInfo : masterNodes) {
            if (nodeInfo.getNode().getHost().equals(host)
                    && nodeInfo.getNode().getPort() == port) {
                offNode = nodeInfo;
                break;
            }
        }
        if (offNode == null) {
            throw new JedisException(String.format("clusterReshard:host=%s,port=%s not find in masters", host, port));
        }
        List<Integer> slots = new ArrayList<Integer>(offNode.getAvailableSlots());
        if (slots.isEmpty()) {
            logger.warn(String.format("clusterReshard:host=%s,port=%s slots is null", host, port));
            reshardProcess.setStatus(1);
            return true;
        }
        //设置slots数量
        reshardProcess.setTotalSlot(slots.size());
        List<ClusterNodeInformation> allocatNodes = new ArrayList<ClusterNodeInformation>(masterNodes);
        for (Iterator<ClusterNodeInformation> i = allocatNodes.iterator(); i.hasNext(); ) {
            ClusterNodeInformation nodeInfo = i.next();
            if (nodeInfo.getNode().getHost().equals(host)
                    && nodeInfo.getNode().getPort() == port) {
                //移除自身
                i.remove();
            }
        }

        Map<String, Integer> balanceSlotMap = getBalanceSlotMap(allocatNodes, false);
        boolean hasError = false;
        for (ClusterNodeInformation nodeInfo : allocatNodes) {
            if (hasError) {
                reshardProcess.setStatus(2);
                break;
            }
            String nodeKey = getNodeKey(nodeInfo.getNode());
            Integer thresholdSize = balanceSlotMap.get(nodeKey);
            if (thresholdSize == null || thresholdSize == 0) {
                continue;
            }
            Jedis targetJedis = getJedis(nodeInfo.getNode().getHost(), nodeInfo.getNode().getPort(), defaultTimeout);
            try {
                int moveSize = 0;
                for (Iterator<Integer> i = slots.iterator(); i.hasNext(); ) {
                    final Integer slot = i.next();
                    logger.info("startMigrateSlot={}", slot);
                    if (moveSize++ >= thresholdSize) {
                        break;
                    }
                    try {
                        int num = migrateSlotData(offJedis, targetJedis, slot);
                        reshardProcess.addReshardSlot(slot, num);
                        logger.warn("clusterReshard:{} -> {} ; slot={};num={}", getNodeKey(offJedis), getNodeKey(targetJedis), slot, num);
                        i.remove();
                    } catch (Throwable e) {
                        logger.error(e.getMessage(), e);
                        hasError = true;
                        break;
                    }
                }
            } finally {
                targetJedis.close();
            }
        }

        if (reshardProcess.getStatus() != 2) {
            reshardProcess.setStatus(1);
        }
        offJedis.close();
        long end = System.currentTimeMillis();
        logger.warn("{}:{} joinNewMaster cost:{} ms", host, port, (end - begin));

        return reshardProcess.getStatus() != 2;
    }

    private Jedis getJedis(String host, int port, int timeout) {
        return new Jedis(host, port, timeout);
    }

    /**
     * 返回平衡的迁移slot数量
     *
     */
    private Map<String, Integer> getBalanceSlotMap(List<ClusterNodeInformation> allocatNodes, boolean isAdd) {
        return getBalanceSlotMap(allocatNodes, isAdd, 1);
    }

    private Map<String, Integer> getBalanceSlotMap(List<ClusterNodeInformation> allocatNodes, boolean isAdd, int addNodeCount) {
        int nodeSize = allocatNodes.size();
        int perSize = (int) Math.ceil((double) allSlots / nodeSize);
        Map<String, Integer> resultMap = new HashMap<String, Integer>();
        for (ClusterNodeInformation node : allocatNodes) {
            String key = getNodeKey(node.getNode());
            int balanceSize;
            if (isAdd) {
                balanceSize = node.getAvailableSlots().size() - perSize;
            } else {
                balanceSize = perSize - node.getAvailableSlots().size();
            }
            if (balanceSize > 0) {
                resultMap.put(key, balanceSize / addNodeCount);
            }
        }
        return resultMap;
    }

    /**
     * 加入主从分片
     */
    public boolean joinCluster(String masterHost, int masterPort, final String slaveHost, final int slavePort) {
        final Jedis masterJedis = getJedis(masterHost, masterPort, defaultTimeout);
        boolean isRun = isRun(masterJedis);
        if (!isRun) {
            logger.error(String.format("joinCluster:host=%s,port=%s is not run", masterHost, masterPort));
            return false;
        }
        boolean hasSlave = StringUtils.isNotBlank(slaveHost) && slavePort > 0;
        final Jedis slaveJedis = hasSlave ? getJedis(slaveHost, slavePort, defaultTimeout) : null;
        if (hasSlave) {
            isRun = isRun(slaveJedis);
            if (!isRun) {
                logger.error(String.format("joinCluster:host=%s,port=%s is not run", slaveHost, slavePort));
                return false;
            }
        }

        List<ClusterNodeInformation> masterNodes = getMasterNodes();
        if (!isInCluster(masterJedis, masterNodes)) {
            boolean isClusterMeet = clusterMeet(masterNodes, masterHost, masterPort);
            if (!isClusterMeet) {
                logger.error("isClusterMeet failed {}:{}", masterHost, masterPort);
                return false;
            }
        }
        if (hasSlave) {
            if (!isInCluster(slaveJedis, masterNodes)) {
                boolean isClusterMeet = clusterMeet(masterNodes, slaveHost, slavePort);
                if (!isClusterMeet) {
                    logger.error("isClusterMeet failed {}:{}", slaveHost, slavePort);
                    return false;
                }
            }
        }
        if (hasSlave) {
            final String masterNodeId = getNodeId(masterJedis);
            if (masterNodeId == null) {
                logger.error(String.format("joinCluster :host=%s,port=%s nodeId is null", masterHost, masterPort));
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

    private boolean clusterMeet(List<ClusterNodeInformation> masterNodes, final String host, final int port) {
        for (ClusterNodeInformation info : masterNodes) {
            String clusterHost = info.getNode().getHost();
            int clusterPort = info.getNode().getPort();
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
            }
        }
        return false;
    }

    private boolean isRun(final Jedis jedis) {
        return new IdempotentConfirmer() {
            @Override
            public boolean execute() {
                String pong = jedis.ping();
                return pong != null && pong.equalsIgnoreCase("PONG");
            }
        }.run();
    }


    /**
     * 对新加入的节点 尽心solt重分配
     * @param machines 泛型string的格式为ip:port
     */
    public boolean joinNewMasters(Set<String> machines) {
        Map<String, Integer> balanceSlotMap = null;
        for (String ipPort : machines) {
            String[] ipPorts = ipPort.split(":");
            String host = ipPorts[0];
            int port = Integer.parseInt(ipPorts[1]);
            long begin = System.currentTimeMillis();
            reshardProcess.setType(0);
            HostAndPort newHost = new HostAndPort(host, port);
            Jedis newJedis = getJedis(host, port, defaultTimeout);
            boolean isRun = isRun(newJedis);
            if (!isRun) {
                throw new JedisException(String.format("clusterReshard:host=%s,port=%s no ping response", host, port));
            }
            List<ClusterNodeInformation> masterNodes = getMasterNodes();
            ClusterNodeInformation newNode = null;
            //判定是否在集群中
            if (!isInCluster(newJedis, masterNodes)) {
                boolean isClusterMeet = clusterMeet(masterNodes, host, port);
                if (!isClusterMeet) {
                    logger.error("clusterMeet error {}:{}", host, port);
                    return false;
                }
            } else {
                //判断是否有存在slot
                for (ClusterNodeInformation nodeInfo : masterNodes) {
                    if (getNodeKey(nodeInfo.getNode()).equals(getNodeKey(newHost))) {
                        newNode = nodeInfo;
                    }
                }
            }

            if (newNode == null) {
                newNode = new ClusterNodeInformation(newHost);
                //加入到masterNodes中,计算平均slot
                masterNodes.add(newNode);
            } else {
                //检查未导完slot并续传.
                boolean isCheckAndMoveSlot = checkAndMovingSlot(masterNodes);
                if (!isCheckAndMoveSlot) {
                    logger.error("checkAndMovingSlot error!");
                    return false;
                }
            }
            if (balanceSlotMap == null) {
                balanceSlotMap = getBalanceSlotMap(masterNodes, true, machines.size());
            }

            //设置总slots数量
            int totalSlot = 0;
            for (Integer num : balanceSlotMap.values()) {
                if (num != null && num > 0) {
                    totalSlot += num;
                }
            }
            reshardProcess.setTotalSlot(totalSlot);

            boolean hasError = false;
            Jedis targetJedis = getJedis(newNode.getNode().getHost(), newNode.getNode().getPort(), defaultTimeout);
            for (ClusterNodeInformation nodeInfo : masterNodes) {
                if (hasError) {
                    reshardProcess.setStatus(2);
                    break;
                }

                if (machines.contains(getNodeKey(nodeInfo.getNode()))) {
//                    if (machines.contains(getNodeKey(newHost))) {
                    //忽略自身
                    continue;
                }
                Integer moveSlot = balanceSlotMap.get(getNodeKey(nodeInfo.getNode()));
                if (moveSlot == null || moveSlot <= 0) {
                    continue;
                }
                List<Integer> slots = new ArrayList<Integer>(nodeInfo.getAvailableSlots());
                Collections.sort(slots);
                Collections.reverse(slots);

                Jedis sourceJedis = getJedis(nodeInfo.getNode().getHost(), nodeInfo.getNode().getPort(), defaultTimeout);
                int index = 0;
                try {
                    for (Iterator<Integer> i = slots.iterator(); i.hasNext(); ) {
                        if (index++ == moveSlot) {
                            break;
                        }
                        Integer slot = i.next();
                        try {
                            int num = migrateSlotData(sourceJedis, targetJedis, slot);
                            reshardProcess.addReshardSlot(slot, num);
                            logger.warn("clusterReshard:{} -> {} ; slot={};num={}", getNodeKey(sourceJedis), getNodeKey(targetJedis), slot, num);
                            i.remove();
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                            hasError = true;
                            break;
                        }
                    }
                } finally {
                    sourceJedis.close();
                }
            }
            targetJedis.close();
            if (reshardProcess.getStatus() != 2) {
                reshardProcess.setStatus(1);
            }
            long end = System.currentTimeMillis();
            logger.warn("{}:{} joinNewMaster cost:{} ms", host, port, (end - begin));
            if (reshardProcess.getStatus() != 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * 集群中加入一个新节点,并reshard集群已有数据
     *
     */
    public boolean joinNewMaster(final String host, final int port) {
        return joinNewMasters(new HashSet<String>(Arrays.asList(host + ":" + port)));
    }

    //检查&导入 moveslot
    private boolean checkAndMovingSlot(List<ClusterNodeInformation> masterNodes) {
        Map<String, List<Integer>> importedMap = new LinkedHashMap<String, List<Integer>>();
        Map<String, List<Integer>> migratedMap = new LinkedHashMap<String, List<Integer>>();
        for (ClusterNodeInformation node : masterNodes) {
            if (node.getSlotsBeingImported().size() > 0) {
                importedMap.put(getNodeKey(node.getNode()), node.getSlotsBeingImported());
            }
            if (node.getSlotsBeingMigrated().size() > 0) {
                migratedMap.put(getNodeKey(node.getNode()), node.getSlotsBeingMigrated());
            }
        }
        if (importedMap.isEmpty() && migratedMap.isEmpty()) {
            return true;
        }
        for (String hostPort : importedMap.keySet()) {
            List<Integer> importedSlots = importedMap.get(hostPort);
            for (Integer slot : importedSlots) {
                for (String subHostPort : migratedMap.keySet()) {
                    List<Integer> migratedSlots = migratedMap.get(subHostPort);
                    if (migratedSlots.contains(slot)) {
                        Jedis source = new Jedis(subHostPort.split(":")[0], Integer.parseInt(subHostPort.split(":")[1]), defaultTimeout);
                        Jedis target = new Jedis(hostPort.split(":")[0], Integer.parseInt(hostPort.split(":")[1]), defaultTimeout);
                        try {
                            int num = moveSlotData(source, target, slot);
                            for(ClusterNodeInformation node : masterNodes){
                                //完成迁移后删除对应slot
                                node.getAvailableSlots().remove(slot);
                            }
                            logger.warn("beingMoveSlotData:{} -> {} slot={} num={}", subHostPort, hostPort, slot, num);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                            return false;
                        }
                        break;
                    }
                }
            }

        }
        return true;
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
