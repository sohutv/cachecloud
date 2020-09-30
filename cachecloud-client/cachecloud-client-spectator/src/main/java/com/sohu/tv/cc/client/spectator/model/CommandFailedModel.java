package com.sohu.tv.cc.client.spectator.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wenruiwu
 * @create 2019/12/15 16:24
 * @description
 */
public class CommandFailedModel {

    private String command; //超时命令
    private long size;      //超时命令长度
    private String args;    //超时命令明文
    private long invokeTime;    //命令调用时间

    public CommandFailedModel(String command, long size, String args, long invokeTime) {
        this.command = command;
        this.size = size;
        this.args = args;
        this.invokeTime = invokeTime;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getInvokeTime() {
        return invokeTime;
    }

    public void setInvokeTime(long invokeTime) {
        this.invokeTime = invokeTime;
    }

    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("command", this.command);
        map.put("size", this.size);
        map.put("args", this.args);
        map.put("invokeTime", this.invokeTime);
        return map;
    }
}
