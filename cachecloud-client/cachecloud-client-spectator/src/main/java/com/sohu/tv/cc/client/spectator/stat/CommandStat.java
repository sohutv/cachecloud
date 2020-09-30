package com.sohu.tv.cc.client.spectator.stat;

/**
 * @author wenruiwu
 * @create 2019/12/20 10:34
 * @description
 */
public class CommandStat {

    /**
     * Redis命令
     */
    private String command;
    /**
     * 命令调用耗时
     */
    private long cost;
    /**
     * 命令输入流量
     */
    private long bytesIn;
    /**
     * 命令输出流量
     */
    private long bytesOut;
    /**
     * redis 节点
     */
    private String node;

    public CommandStat(String command, String node, long cost, long bytesIn, long bytesOut) {
        this.command = command;
        this.node = node;
        this.cost = cost;
        this.bytesIn = bytesIn;
        this.bytesOut = bytesOut;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public long getBytesIn() {
        return bytesIn;
    }

    public void setBytesIn(long bytesIn) {
        this.bytesIn = bytesIn;
    }

    public long getBytesOut() {
        return bytesOut;
    }

    public void setBytesOut(long bytesOut) {
        this.bytesOut = bytesOut;
    }

    @Override
    public String toString() {
        return "CommandStat [command=" + command + ",node=" + node + ", cost=" + cost + "ns, bytesIn=" + bytesIn + ", bytesOut=" + bytesOut + "]";
    }
}
