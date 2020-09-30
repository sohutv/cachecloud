package com.sohu.tv.cc.client.spectator.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wenruiwu
 * @create 2019/12/15 16:26
 * @description
 */
public class CommandStatsModel {

    private String command;
    private String node;
    private int count;
    private long cost;
    private long bytesIn;
    private long bytesOut;

    public CommandStatsModel(String command){
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
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

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("command", this.command);
        map.put("node", this.node);
        map.put("count", this.count);
        map.put("cost", this.cost);
        map.put("bytesIn", this.bytesIn);
        map.put("bytesOut", this.bytesOut);
        return map;
    }
}
