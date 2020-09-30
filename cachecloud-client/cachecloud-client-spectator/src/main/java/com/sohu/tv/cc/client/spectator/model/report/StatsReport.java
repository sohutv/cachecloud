package com.sohu.tv.cc.client.spectator.model.report;

import com.sohu.tv.cc.client.spectator.model.CommandStatsModel;

import java.util.*;

/**
 * @author wenruiwu
 * @create 2019/12/16 10:54
 * @description
 */
public class StatsReport {
    /**
     * 当前分钟
     */
    private String currentMin;
    /**
     * appId
     */
    private long appId;
    /**
     * client ip
     */
    private String clientIp;
    /**
     * 客户端版本
     */
    private String clientVersion;
    /**
     * 命令调用记录
     */
    private List<CommandStatsModel> commandStatsModels;
    /**
     * 数据统计耗时
     */
    private long cost;

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public String getCurrentMin() {
        return currentMin;
    }

    public void setCurrentMin(String currentMin) {
        this.currentMin = currentMin;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public List<CommandStatsModel> getCommandStatsModels() {
        return commandStatsModels;
    }

    public void setCommandStatsModels(List<CommandStatsModel> commandStatsModels) {
        for (Iterator<CommandStatsModel> iter = commandStatsModels.iterator(); iter.hasNext(); ) {
            CommandStatsModel model = iter.next();
            model.setCost(model.getCost() / 1000000);
        }
        this.commandStatsModels = commandStatsModels;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("currentMin", this.currentMin);
        map.put("appId", this.appId);
        map.put("clientIp", this.clientIp);
        map.put("clientVersion", this.clientVersion);
        map.put("cost", this.cost);
        List<Map<String, Object>> list = new ArrayList<>(this.commandStatsModels.size());
        for (CommandStatsModel model : commandStatsModels) {
            list.add(model.toMap());
        }
        map.put("commandStatsModels", list);
        return map;
    }
}
