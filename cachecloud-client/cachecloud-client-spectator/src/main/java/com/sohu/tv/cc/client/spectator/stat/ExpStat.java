package com.sohu.tv.cc.client.spectator.stat;

/**
 * @author wenruiwu
 * @create 2019/12/20 10:51
 * @description
 */
public class ExpStat {

    /**
     * Redis节点
     */
    private String node;
    /**
     * 失败耗时
     */
    private long cost;
    /**
     * 0：连接失败；1：命令超时
     */
    private int type;
    /**
     * 超时命令
     */
    private String command;
    /**
     * 超时命令参数
     */
    private String params;
    /**
     * 超时命令参数大小
     */
    private long paramsSize;
    /**
     * 调用时间 System.currentTimeMillis
     */
    private long invokeTime;

    public ExpStat(String node, long cost, int type, long invokeTime) {
        this.node = node;
        this.cost = cost;
        this.type = type;
        this.invokeTime = invokeTime;
    }

    public ExpStat(String node, String command, String params, long paramsSize, long cost, int type, long invokeTime) {
        this.node = node;
        this.command = command;
        this.params = params;
        this.paramsSize = paramsSize;
        this.cost = cost;
        this.type = type;
        this.invokeTime = invokeTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public long getParamsSize() {
        return paramsSize;
    }

    public void setParamsSize(long paramsSize) {
        this.paramsSize = paramsSize;
    }

    public long getInvokeTime() {
        return invokeTime;
    }

    public void setInvokeTime(long invokeTime) {
        this.invokeTime = invokeTime;
    }

    @Override
    public String toString() {
        return "ExpStat [command=" + command + ", node=" + node + ", invokeTime=" + invokeTime + ", cost=" + cost
                + "ns, type=" + type + ", params=" + params
                + ", paramsSize=" + paramsSize + "]";    }
}
