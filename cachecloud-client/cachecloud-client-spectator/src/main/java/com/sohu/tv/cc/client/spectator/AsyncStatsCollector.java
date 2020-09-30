package com.sohu.tv.cc.client.spectator;

import com.sohu.tv.cc.client.spectator.json.JSONUtils;
import com.sohu.tv.cc.client.spectator.model.CommandFailedModel;
import com.sohu.tv.cc.client.spectator.model.CommandStatsModel;
import com.sohu.tv.cc.client.spectator.model.ExceptionModel;
import com.sohu.tv.cc.client.spectator.model.report.ExceptionReport;
import com.sohu.tv.cc.client.spectator.model.report.StatsReport;
import com.sohu.tv.cc.client.spectator.stat.CommandStat;
import com.sohu.tv.cc.client.spectator.stat.ExpStat;
import com.sohu.tv.cc.client.spectator.util.Constants;
import com.sohu.tv.cc.client.spectator.util.HttpUtils;
import com.sohu.tv.cc.client.spectator.util.NetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author wenruiwu
 * @create 2019/12/18 18:14
 * @description
 */
public class AsyncStatsCollector implements StatsCollector {

    private static Logger logger = LoggerFactory.getLogger(AsyncStatsCollector.class);

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm00");
    private static final String SEND_SUCCESS = "success";
    private final AtomicInteger threadId = new AtomicInteger(0);
    private volatile boolean stopped = false;
    /**
     * 超时命令明文长度上限
     */
    private static final int COMMAND_ARGS_SIZE = 128;
    /**
     * redis应用id
     */
    private long appId;
    /**
     * jedis客户端配置
     */
    private ClientConfig clientConfig;
    /**
     * 指标发送队列上限
     */
    private final int queueSize;
    /**
     * 指标队列上限
     */
    private final int bufferSize;
    /**
     * 队列满之后丢弃的CommandStats数量
     */
    private AtomicLong discardCommandCount;
    /**
     * 队列满之后丢弃的ExpStats数量
     */
    private AtomicLong discardExpCount;
    /**
     * CommandStats消费线程
     */
    private Thread commandStatsWorker;
    /**
     * ExpStats消费线程
     */
    private Thread expStatsWorker;
    /**
     * 命令调用指标队列
     */
    private BlockingQueue<CommandStat> commandStatsQueue;
    /**
     * 异常指标队列
     */
    private BlockingQueue<ExpStat> expStatsQueue;
    /**
     * 指标发送线程池
     */
    private final ThreadPoolExecutor statsSendExecutor;
    /**
     * 指标发送队列
     */
    private ArrayBlockingQueue<Runnable> senderQueue;

    public AsyncStatsCollector(long appId, ClientConfig clientConfig) {
        this.appId = appId;
        this.clientConfig = clientConfig;
        this.queueSize = 2048;
        this.bufferSize = 1024;
        this.discardCommandCount = new AtomicLong(0L);
        this.discardExpCount = new AtomicLong(0L);
        this.commandStatsQueue = new ArrayBlockingQueue<>(bufferSize);
        this.expStatsQueue = new ArrayBlockingQueue<>(bufferSize);
        this.senderQueue = new ArrayBlockingQueue<>(queueSize);

        this.statsSendExecutor = new ThreadPoolExecutor(
                10,
                20,
                1000 * 60,
                TimeUnit.MILLISECONDS,
                this.senderQueue,
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable runnable) {
                        Thread thread = new Thread(runnable, "StatsSendThread");
                        return thread;
                    }
                });
    }

    @Override
    public void start() {
        this.commandStatsWorker = new Thread(new AsyncCommandStatsWorker(), "AsyncStatsCollector-CommandStatsConsumer-Thread-" + threadId.incrementAndGet());
        this.commandStatsWorker.setDaemon(true);
        this.commandStatsWorker.start();
        this.expStatsWorker = new Thread(new AsyncExpStatsWorker(), "AsyncStatsCollector-ExpStatsConsumer-Thread-" + threadId.incrementAndGet());
        this.expStatsWorker.setDaemon(true);
        this.expStatsWorker.start();
    }

    @Override
    public boolean appendCommandStat(CommandStat stat) {
        boolean result = false;
        String command = stat.getCommand();
        if (command != null) {
            long start = System.nanoTime();
            result = commandStatsQueue.offer(stat);
            if (!result && discardCommandCount.incrementAndGet() % 500 == 0) {
                logger.warn("CommandStatsQueue is full, discardCount={}, stat={}", discardCommandCount.get(), stat);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Append stat={} to CommandStatsQueue cost={} nanoseconds, queue size={}, result={}",
                        stat.toString(), (System.nanoTime() - start), commandStatsQueue.size(), result);
            }
        }
        return result;
    }

    @Override
    public boolean appendCommandExpStat(CommandTracker.CommandItem item) {
        ExpStat stat = buildCommandExpStat(item);
        long start = System.nanoTime();
        boolean result = expStatsQueue.offer(stat);
        if (!result) {
            logger.warn("ExpStatsQueue is full, discardCount={}, stat={}", discardExpCount.incrementAndGet(), stat);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Append stat={} to ExpStatsQueue cost={} nanoseconds, queue size={}, result={}",
                    item.toString(), (System.nanoTime() - start), expStatsQueue.size(), result);
        }
        return result;
    }

    @Override
    public boolean appendConnectExpStat(String node, long cost) {
        ExpStat stat = new ExpStat(node, cost, ExpStatEnum.CONNECT.getType(), System.currentTimeMillis());
        long start = System.nanoTime();
        boolean result = expStatsQueue.offer(stat);
        if (!result) {
            logger.warn("ExpStatsQueue is full, discardCount={}, stat={}", discardExpCount.incrementAndGet(), stat);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Append ExpStatsQueue cost={} nanoseconds, queue size={}, result={} with stat={}",
                    (System.nanoTime() - start), expStatsQueue.size(), result, stat.toString());
        }
        return result;
    }

    @Override
    public void shutdown() {
        this.stopped = true;
        this.statsSendExecutor.shutdown();
        long end = System.currentTimeMillis() + 500;
        while (commandStatsQueue.size() > 0 || expStatsQueue.size() > 0 || senderQueue.size() > 0 && System.currentTimeMillis() <= end) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                break;
            }
        }
        logger.info("send stats end,commandStatsQueue size={},expStatsQueue size={},senderQueue size={}",
                commandStatsQueue.size(), expStatsQueue.size(), senderQueue.size());
    }

    /**
     * 构造命令超时指标
     */
    private ExpStat buildCommandExpStat(CommandTracker.CommandItem item) {
        String node = item.getNode();
        String command = item.getCommand();
        long paramsSize = item.getInputBytes();
        long invokeTime = item.getInvokeTime();
        long cost = item.getCost();
        String params = "";
        try {
            StringBuilder sb = new StringBuilder();  //裁剪
            byte[][] args = item.getCommandArgs();
            byte[] bytes;
            for (int i = 0; i < args.length && sb.length() < COMMAND_ARGS_SIZE; i++) {
                bytes = args[i];
                sb.append(new String(bytes, "UTF-8")).append(",");
            }
            int len = sb.length();
            if (len > 0) {
                if (len > COMMAND_ARGS_SIZE) {
                    params = sb.substring(0, COMMAND_ARGS_SIZE);
                } else {
                    params = sb.substring(0, len - 1);
                }
            }
        } catch (Exception e) {
            logger.error("buildCommandExpStat getCommandArgs error", e.getMessage(), e);
        }
        ExpStat stat = new ExpStat(node, command, params, paramsSize, cost, ExpStatEnum.COMMAND.getType(), invokeTime);
        return stat;
    }

    /**
     * 客户端ip
     */
    private String getClientIp() {
        return NetUtils.getLocalHost();
    }

    /**
     * CommandStats消费线程
     */
    class AsyncCommandStatsWorker implements Runnable {
        private boolean stopped;
        private Map<String, CommandStatsModel> commandMap = new HashMap<>();
        private long lastTimestamp = System.currentTimeMillis();       //上次上报时间戳
        private long oneMinInMills = TimeUnit.SECONDS.toMillis(60);

        @Override
        public void run() {
            while (!stopped) {
                CommandStat stat = null;
                try {
                    //get stats data element from blocking Queue
                    stat = commandStatsQueue.poll(5, TimeUnit.MILLISECONDS);
                    process(stat);
                    if (AsyncStatsCollector.this.stopped) {
                        this.stopped = true;
                    }
                } catch (Throwable e) {
                    logger.error("AsyncCommandStatsWorker consume stat {} in timestamp {} error!", stat, System.currentTimeMillis(), e.getMessage(), e);
                }
            }
        }

        private void process(CommandStat stat) {
            if (!clientConfig.isClientStatIsOpen()) {     //未开启上报则直接丢弃指标
                return;
            }
            if (stat != null) {     //当前分钟累加
                accumulation(stat);
            }
            if (System.currentTimeMillis() >= lastTimestamp + oneMinInMills) {      //下一分钟上报
                try {
                    String currentMin = sdf.format(new Date());
                    if (commandMap.size() > 0) {
                        buildRequest(currentMin, new ArrayList<>(commandMap.values()));
                    }
                } finally {
                    lastTimestamp = System.currentTimeMillis();     //重置上次上报时间戳
                    commandMap.clear();
                }
            }
        }

        /**
         * 按分钟累加相同命令调用指标
         */
        private void accumulation(CommandStat stat) {
            String command = stat.getCommand();
            if (!commandMap.keySet().contains(command)) {
                commandMap.put(command, new CommandStatsModel(command));
            }
            CommandStatsModel model = commandMap.get(command);
            model.setCount(model.getCount() + 1);
            model.setCost(model.getCost() + stat.getCost());
            model.setBytesIn(model.getBytesIn() + stat.getBytesIn());
            model.setBytesOut(model.getBytesOut() + stat.getBytesOut());
        }

        private void buildRequest(String currentMin, List<CommandStatsModel> modelList) {
            StatsReport statsReport = new StatsReport();
            statsReport.setAppId(appId);
            statsReport.setClientVersion(Constants.CLIENT_VERSION);
            statsReport.setCommandStatsModels(modelList);
            statsReport.setClientIp(getClientIp());
            statsReport.setCurrentMin(currentMin);
            AsyncSendRunnable request = new AsyncSendRunnable(Constants.CLIENT_VERSION, JSONUtils.toJSONString(statsReport.toMap()), Constants.CACHECLOUD_COMMAND_REPORT_URL, currentMin);
            statsSendExecutor.submit(request);
        }
    }

    /**
     * ExpStats消费线程
     */
    class AsyncExpStatsWorker implements Runnable {
        private boolean stopped;
        private Map<String, ExceptionModel> connectExpMap = new HashMap<>();
        private Map<String, ExceptionModel> commandExpMap = new HashMap<>();
        private long lastTimestamp = System.currentTimeMillis();        //上次上报时间戳
        private long oneMinInMills = TimeUnit.SECONDS.toMillis(60);
        private int commandFailedListSize = 10;         //只记录前commandFailedListSize个超时命令

        @Override
        public void run() {
            while (!stopped) {
                ExpStat stat = null;
                try {
                    //get stats data element from blocking Queue
                    stat = expStatsQueue.poll(5, TimeUnit.MILLISECONDS);
                    process(stat);
                    if (AsyncStatsCollector.this.stopped) {
                        this.stopped = true;
                    }
                } catch (Throwable e) {
                    logger.error("AsyncExpStatsWorker consume stat {} in timestamp {} error!", stat, System.currentTimeMillis(), e.getMessage(), e);
                }
            }
        }

        private void process(ExpStat stat) {
            if (!clientConfig.isClientStatIsOpen()) {     //未开启上报则直接丢弃指标
                return;
            }
            if (stat != null) {     //当前分钟累加
                accumulation(stat);
            }
            if (System.currentTimeMillis() >= lastTimestamp + oneMinInMills) {      //下一分钟上报
                try {
                    String currentMin = sdf.format(new Date());
                    List<ExceptionModel> list = new ArrayList<>(connectExpMap.values());
                    list.addAll(commandExpMap.values());
                    if (list.size() > 0) {
                        buildRequest(currentMin, list);
                    }
                } finally {
                    lastTimestamp = System.currentTimeMillis();     //重置上次上报时间戳
                    connectExpMap.clear();
                    commandExpMap.clear();
                }
            }
        }

        /**
         * 按分钟累加相同redis节点调用指标
         */
        private void accumulation(ExpStat stat) {
            int type = stat.getType();
            String node = stat.getNode();
            if (type == ExpStatEnum.CONNECT.getType()) {      //连接异常
                if (!connectExpMap.keySet().contains(node)) {
                    connectExpMap.put(node, new ExceptionModel(node, type));
                }
                ExceptionModel model = connectExpMap.get(node);
                model.setCount(model.getCount() + 1);
                model.setCost(model.getCost() + stat.getCost());
            } else {        //命令超时
                if (!commandExpMap.keySet().contains(node)) {
                    commandExpMap.put(node, new ExceptionModel(node, type));
                }
                ExceptionModel model = commandExpMap.get(node);
                model.setCount(model.getCount() + 1);
                model.setCost(model.getCost() + stat.getCost());
                //命令超时记录明文
                CommandFailedModel commandFailedModel = new CommandFailedModel(stat.getCommand(), stat.getParamsSize(), stat.getParams(), stat.getInvokeTime());
                List<CommandFailedModel> list = model.getCommandFailedModels();
                if (list == null) {
                    list = new ArrayList<>();
                    list.add(commandFailedModel);
                    model.setCommandFailedModels(list);
                } else if (list.size() < commandFailedListSize) {
                    list.add(commandFailedModel);
                }
            }
        }

        private void buildRequest(String currentMin, List<ExceptionModel> modelList) {
            ExceptionReport expReport = new ExceptionReport();
            expReport.setAppId(appId);
            expReport.setClientVersion(Constants.CLIENT_VERSION);
            expReport.setExceptionModels(modelList);
            expReport.setClientIp(getClientIp());
            expReport.setCurrentMin(currentMin);
            expReport.setConfig(clientConfig.getConfigMap());
            AsyncSendRunnable request = new AsyncSendRunnable(Constants.CLIENT_VERSION, JSONUtils.toJSONString(expReport.toMap()), Constants.CACHECLOUD_EXP_REPORT_URL, currentMin);
            statsSendExecutor.submit(request);
        }
    }

    /**
     * 上报指标线程
     */
    static class AsyncSendRunnable implements Runnable {

        private String clientVersion;
        private String stats;
        private String url;
        private String currentMin;
        private long startTime;

        public AsyncSendRunnable(String clientVersion, String stats, String url, String currentMin) {
            this.clientVersion = clientVersion;
            this.stats = stats;
            this.url = url;
            this.currentMin = currentMin;
            this.startTime = System.currentTimeMillis();
        }

        @Override
        public void run() {
            send(clientVersion, stats, url);
        }

        public void send(String clientVersion, String stats, String url) {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("stats", stats);
            parameters.put("clientVersion", clientVersion);
            String response = "";
            try {
                response = HttpUtils.doPost(url, parameters);
                if (!SEND_SUCCESS.equals(response)) {
                    logger.warn("AsyncSendRunnable send current min={} stats={} failed. Response={}", currentMin, stats, response);
                }
            } catch (Throwable e) {
                logger.error("AsyncSendRunnable send current min={} stats={} error.", currentMin, stats, e.getMessage(), e);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("AsyncSendRunnable send current min={} stats={} response={} cost={} ms", currentMin, stats, response, (System.currentTimeMillis() - startTime));
            }
        }
    }

}

enum ExpStatEnum {
    /**
     * 连接失败
     */
    CONNECT(0),
    /**
     * 调用超时
     */
    COMMAND(1);

    private int type;

    ExpStatEnum(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
