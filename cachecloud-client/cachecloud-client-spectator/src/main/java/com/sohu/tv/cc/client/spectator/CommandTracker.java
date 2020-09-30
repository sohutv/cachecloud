package com.sohu.tv.cc.client.spectator;

import com.sohu.tv.cc.client.spectator.stat.CommandStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author wenruiwu
 * @create 2019/12/9 12:54
 * @description
 */
public class CommandTracker {

    private static Logger logger = LoggerFactory.getLogger(CommandTracker.class);

    private static ThreadLocal<CommandTracker> threadLocal = new ThreadLocal<>();

    /**
     * 命令调用队列
     */
    private final Queue<CommandItem> queue = new LinkedList<>();
    /**
     * 忽略指标统计的命令
     */
    private static List<String> ignoreCommands = new ArrayList<>();

    static {
        ignoreCommands.add("blpop");
        ignoreCommands.add("brpop");
        ignoreCommands.add("brpoplpush");
        ignoreCommands.add("bzpopmax");
        ignoreCommands.add("bzpopmin");
        ignoreCommands.add("subsribe");
        ignoreCommands.add("psubsribe");
    }

    /**
     * 命令开始
     */
    public void commandStart(StatsCollector statsCollector, String cmd, String hostPort, long invokeTime, long startTime, boolean isFailed, byte[]... args) {
        if (ignoreCommands.contains(cmd)) {
            return;
        }
        long total = 0;
        for (int i = 0; i < args.length; i++) {
            total = total + args[i].length;
        }
        if (isFailed) {       //sendCommand failed
            try {
                long endTime = System.nanoTime();
                long cost = endTime - startTime;
                CommandItem commandItem = new CommandItem(cmd, hostPort, total, args, invokeTime, startTime);
                commandItem.setCost(cost);
                statsCollector.appendCommandExpStat(commandItem);
            } catch (Exception e) {
                logger.error("Append sendCommand failed stat error.", e.getMessage(), e);
            } finally {
                this.clear();
            }
        } else {    //sendCommand succeed
            CommandItem commandItem = new CommandItem(cmd, hostPort, total, args, invokeTime, startTime);
            queue.add(commandItem);
        }
    }

    /**
     * flushFailed
     */
    public void flushFailed(StatsCollector statsCollector) {
        try {
            long endTime = System.nanoTime();
            long cost;
            if (queue.size() > 0) {
                for (Iterator<CommandItem> iter = queue.iterator(); iter.hasNext(); ) {
                    CommandItem item = iter.next();
                    if (item != null) {
                        cost = endTime - item.getStartTime();
                        item.setCost(cost);
                        statsCollector.appendCommandExpStat(item);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Append flushFailed stat error.", e.getMessage(), e);
        } finally {
            this.clear();
        }
    }

    /**
     * 命令调用完成
     */
    public void commandCompleted(Object response, boolean isFailed, StatsCollector statsCollector) {
        try {
            if (queue.size() > 0) {
                CommandItem item = queue.poll();
                if (item != null) {     //
                    long startTime = item.getStartTime();
                    long cost = System.nanoTime() - startTime;
                    if (isFailed) {       //readProtocolFailed
                        item.setCost(cost);
                        statsCollector.appendCommandExpStat(item);
                    } else {        //command succeed
                        long bytesOut = calBytesOut(response);
                        CommandStat commandStat = new CommandStat(item.getCommand(), item.getNode(), cost, item.getInputBytes(), bytesOut);
                        statsCollector.appendCommandStat(commandStat);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Append CommandCompleted stat error.", e.getMessage(), e);
        } finally {
            if (queue.size() == 0) {        //队列为空，清除ThreadLocal对象
                this.clear();
            }
        }
    }

    public static CommandTracker getCommandTracker() {
        CommandTracker commandTracker = threadLocal.get();
        if (commandTracker == null) {
            commandTracker = new CommandTracker();
            threadLocal.set(commandTracker);
        }
        return commandTracker;
    }

    /**
     * clear threadLocal
     */
    private void clear() {
        threadLocal.remove();
    }

    /**
     * 递归计算RESP Response大小
     *
     * @param response
     * @return response字节数
     */
    private long calBytesOut(Object response) {
        if (response == null) {
            return 0L;
        }
        long bytesOut = 0L;
        if (response instanceof byte[]) {
            byte[] bytes = (byte[]) response;
            bytesOut = bytes.length;
        } else if (response instanceof List) {
            for (int i = 0; i < ((List) response).size(); i++) {
                Object obj = ((List) response).get(i);
                bytesOut = bytesOut + calBytesOut(obj);
            }
        } else {        //忽略写命令返回Long类型的情况
            bytesOut = 0L;
        }
        return bytesOut;
    }

    /**
     * 封装命令调用记录
     */
    static class CommandItem {
        private String command;
        private long inputBytes;
        private byte[][] commandArgs;
        private long invokeTime;
        private long startTime;
        private long endTime;
        private long cost;
        private String node;

        public CommandItem(String command, String node, long inputBytes, byte[][] commandArgs, long invokeTime, long startTime) {
            this.command = command;
            this.node = node;
            this.inputBytes = inputBytes;
            this.commandArgs = commandArgs;
            this.invokeTime = invokeTime;
            this.startTime = startTime;
        }

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public long getInputBytes() {
            return inputBytes;
        }

        public void setInputBytes(long inputBytes) {
            this.inputBytes = inputBytes;
        }

        public byte[][] getCommandArgs() {
            return commandArgs;
        }

        public void setCommandArgs(byte[][] commandArgs) {
            this.commandArgs = commandArgs;
        }

        public long getInvokeTime() {
            return invokeTime;
        }

        public void setInvokeTime(long invokeTime) {
            this.invokeTime = invokeTime;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }

        public long getCost() {
            return cost;
        }

        public void setCost(long cost) {
            this.cost = cost;
        }

        public String getNode() {
            return node;
        }

        public void setNode(String node) {
            this.node = node;
        }

        @Override
        public String toString() {
            return "CommandItem [command=" + command + ", node=" + node + ", invokeTime=" + invokeTime + ", cost=" + getCost()
                    + "ns" + ", inputBytes=" + inputBytes + "]";
        }
    }
}
