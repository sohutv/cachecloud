package com.sohu.tv.cc.client.spectator.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wenruiwu
 * @create 2019/12/15 16:20
 * @description
 */
public class ExceptionModel {

    private String node;
    private int count;
    private long cost;
    private int type;       //0-连接异常；1-命令超时
    private List<CommandFailedModel> commandFailedModels;      //超时命令TopN

    public ExceptionModel(String node){
        this.node = node;
    }

    public ExceptionModel(String node, int type){
        this.node = node;
        this.type = type;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public List<CommandFailedModel> getCommandFailedModels() {
        return commandFailedModels;
    }

    public void setCommandFailedModels(List<CommandFailedModel> commandFailedModels) {
        this.commandFailedModels = commandFailedModels;
    }

    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("node", this.node);
        map.put("count", this.count);
        map.put("cost", this.cost);
        map.put("type", this.type);
        if(this.commandFailedModels != null && this.commandFailedModels.size() > 0){
            List<Map<String, Object>> list = new ArrayList<>(this.commandFailedModels.size());
            for(CommandFailedModel model : commandFailedModels){
                list.add(model.toMap());
            }
            map.put("commandFailedModels", list);
        }
        return map;
    }
}
