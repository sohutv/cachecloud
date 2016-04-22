package com.sohu.tv.jedis.stat.model;

/**
 * 从jedis收集有价值的数据简单模型
 * 
 * @author leifu
 * @Date 2015年1月13日
 * @Time 下午3:42:01
 */
public class UsefulDataModel {
    /**
     * sendCommand开始时间
     */
    private long startTime;

    /**
     * process结束时间
     */
    private long endTime;

    /**
     * 命令名
     */
    private String command;

    /**
     * 值大小
     */
    private int valueBytesLength;
    
    /**
     * host:ip
     */
    private String hostPort;

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

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int getValueBytesLength() {
        return valueBytesLength;
    }

    public void setValueBytesLength(int valueBytesLength) {
        this.valueBytesLength = valueBytesLength;
    }

    private UsefulDataModel() {
        super();
    }

    public long getCost() {
        return this.endTime - this.startTime;
    }

    public String getHostPort() {
        return hostPort;
    }

    public void setHostPort(String hostPort) {
        this.hostPort = hostPort;
    }

    /**
     * 从ThreadLocal里面获取JedisUsefulData
     * 
     * @param threadLocal
     * @return
     */
    public static UsefulDataModel getCostModel(ThreadLocal<UsefulDataModel> threadLocal) {
        UsefulDataModel costModel = threadLocal.get();
        if (costModel == null) {
            costModel = new UsefulDataModel();
            threadLocal.set(costModel);
        }
        return costModel;
    }

    @Override
    public String toString() {
        return "UsefulDataModel [startTime=" + startTime + ", endTime=" + endTime + ", command=" + command
                + ", valueBytesLength=" + valueBytesLength + ", costTime=" + (endTime - startTime) + "ms]";
    }

}
