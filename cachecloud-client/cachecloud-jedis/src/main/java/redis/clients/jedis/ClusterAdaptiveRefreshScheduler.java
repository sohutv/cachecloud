package redis.clients.jedis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.util.SafeEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class ClusterAdaptiveRefreshScheduler {

    private static Logger logger = LoggerFactory.getLogger(ClusterAdaptiveRefreshScheduler.class);

    private PipelineCluster pipelineCluster;
    private static final int MASTER_NODE_INDEX = 2;
    private final static int REFRESH_PERIOD_IN_MIN = 1;
    private final static int REFRESH_INITIAL_DELAY_IN_MIN = 1;

    private volatile Thread shutDownHook;

    public ClusterAdaptiveRefreshScheduler(PipelineCluster pipelineCluster) {
        this.pipelineCluster = pipelineCluster;
    }

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "ClusterAdaptiveRefreshThread");
            thread.setDaemon(true);
            return thread;
        }
    });

    public void start() {
        scheduler.scheduleAtFixedRate(new ClusterTopologyRefreshTask(pipelineCluster),
                REFRESH_INITIAL_DELAY_IN_MIN, REFRESH_PERIOD_IN_MIN, TimeUnit.MINUTES);
        registerShutDownHook();
    }

    public void registerShutDownHook() {
        if (shutDownHook == null) {
            shutDownHook = new Thread(new Runnable() {
                private volatile boolean hasShutdown = false;

                @Override
                public void run() {
                    synchronized (this) {
                        if (!this.hasShutdown) {
                            try {
                                scheduler.shutdown();
                            } catch (Throwable e) {
                                logger.error("Scheduler shutdown failed");
                            }
                        }
                    }
                }
            }, "SchedulerShutdownHook");
            Runtime.getRuntime().addShutdownHook(shutDownHook);
        }
    }

    private static class ClusterTopologyRefreshTask implements Runnable {

        private final PipelineCluster pipelineCluster;

        public ClusterTopologyRefreshTask(PipelineCluster pipelineCluster) {
            this.pipelineCluster = pipelineCluster;
        }

        @Override
        public void run() {
            Jedis jedis = null;
            try {
                JedisClusterConnectionHandler connectionHandler = pipelineCluster.getConnectionHandler();
                jedis = connectionHandler.getConnection();
                //获取最新cluster slots
                List<Object> slots = jedis.clusterSlots();
                Set<String> remoteNodes = discoverNodes(slots);
                //本地节点
                Map<String, JedisPool> jedisPoolMap = connectionHandler.getNodes();
                Set<String> localNodes = jedisPoolMap.keySet();
                if (localNodes.size() > 0 && remoteNodes.size() > 0) {
                    //判断节点下线
                    for (String localNode : localNodes) {
                        if (!remoteNodes.contains(localNode)) {     //本地存在但运程不存在
                            //再判断该节点是否存在于slots中
                            if (!connectionHandler.isInSlots(jedisPoolMap.get(localNode))) {
                                connectionHandler.removeNodeIfExist(localNode);
                                logger.warn("Remove local node={}, remote nodes={}, local nodes={}", localNode, remoteNodes, localNodes);
                            } else {
                                logger.warn("Local node={} does not exist in remote nodes={}, but still in slots!",
                                        localNode, remoteNodes);
                            }
                        }
                    }
                    //判断节点增加
                    for (String remoteNode : remoteNodes) {
                        if (!localNodes.contains(remoteNode)) {       //本地不存在但运程存在
                            connectionHandler.setupNodeIfNotExist(remoteNode);
                            logger.warn("Add remote node={}, remote nodes={}, local nodes={}", remoteNode, remoteNodes, localNodes);
                        }
                    }
                }
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
            } finally {
                if (jedis != null) {
                    try {
                        jedis.close();
                    } catch (Exception e) {}
                }
            }

        }

        /**
         * 根据最新slots解析成nodes
         */
        private Set<String> discoverNodes(List<Object> slots) {
            Set<String> remoteNodes = new HashSet<>();
            for (Object slotInfoObj : slots) {
                List<Object> slotInfo = (List<Object>) slotInfoObj;
                if (slotInfo.size() <= MASTER_NODE_INDEX) {
                    continue;
                }
                // hostInfos
                int size = slotInfo.size();
                for (int i = MASTER_NODE_INDEX; i < size; i++) {
                    List<Object> hostInfos = (List<Object>) slotInfo.get(i);
                    if (hostInfos.size() <= 0) {
                        continue;
                    }
                    String targetNode = generateHostAndPort(hostInfos);
                    remoteNodes.add(targetNode);
                }
            }
            return remoteNodes;
        }

        private String generateHostAndPort(List<Object> hostInfos) {
            String host = SafeEncoder.encode((byte[]) hostInfos.get(0));
            int port = ((Long) hostInfos.get(1)).intValue();
            return host + ":" + port;
        }
    }
}
