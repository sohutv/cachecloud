package com.sohu.tv.cc.client.spectator.model.report;

import com.sohu.tv.cc.client.spectator.model.ExceptionModel;

import java.util.*;

/**
 * @author wenruiwu
 * @create 2019/12/16 10:27
 * @description 异常上报Bean
 */
public class ExceptionReport {
    /**
     * appId
     */
    private long appId;
    /**

     * client ip
     */
    private String clientIp;
    /**
     * 当前分钟
     */
    private String currentMin;
    /**
     * 当前分钟
     */
    private Map<String, Object> config;
    /**
     * 异常记录
     */
    private List<ExceptionModel> exceptionModels;
    /**
     * 数据统计耗时
     */
    private long cost;
    /**
     * 客户端版本
     */
    private String clientVersion;


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

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }

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

    public List<ExceptionModel> getExceptionModels() {
        return exceptionModels;
    }

    public void setExceptionModels(List<ExceptionModel> exceptionModels) {
        for (Iterator<ExceptionModel> iter = exceptionModels.iterator(); iter.hasNext();){
            ExceptionModel model = iter.next();
            model.setCost(model.getCost() / 1000000);
        }
        this.exceptionModels = exceptionModels;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("currentMin", this.currentMin);
        map.put("appId", this.appId);
        map.put("clientIp", this.clientIp);
        map.put("clientVersion", this.clientVersion);
        map.put("cost", this.cost);
        List<Map<String, Object>> list = new ArrayList<>(this.exceptionModels.size());
        for (ExceptionModel model : exceptionModels) {
            list.add(model.toMap());
        }
        map.put("exceptionModels", list);
        map.put("config", this.config);
        return map;
    }
}
