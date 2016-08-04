package com.sohu.cache.entity;

/**
 * 应用数据迁移搜索
 * 
 * @author leifu
 * @Date 2016年8月4日
 * @Time 下午2:49:43
 */
public class AppDataMigrateSearch {
    /**
     * 源应用id
     */
    private Long sourceAppId;
    
    /**
     * 目标应用id
     */
    private Long targetAppId;

    /**
     * 源实例
     */
    private String sourceInstanceIp;
    
    /**
     * 目标实例
     */
    private String targetInstanceIp;

    /**
     * 开始时间
     */
    private String startDate;

    /**
     * 结束时间
     */
    private String endDate;
    
    /**
     * 状态
     */
    private int status = -2;

    public AppDataMigrateSearch() {
        super();
    }

    public AppDataMigrateSearch(Long sourceAppId, Long targetAppId, String sourceInstanceIp, String targetInstanceIp,
            String startDate, String endDate, int status) {
        super();
        this.sourceAppId = sourceAppId;
        this.targetAppId = targetAppId;
        this.sourceInstanceIp = sourceInstanceIp;
        this.targetInstanceIp = targetInstanceIp;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public Long getSourceAppId() {
        return sourceAppId;
    }


    public void setSourceAppId(Long sourceAppId) {
        this.sourceAppId = sourceAppId;
    }


    public Long getTargetAppId() {
        return targetAppId;
    }


    public void setTargetAppId(Long targetAppId) {
        this.targetAppId = targetAppId;
    }


    public String getSourceInstanceIp() {
        return sourceInstanceIp;
    }


    public void setSourceInstanceIp(String sourceInstanceIp) {
        this.sourceInstanceIp = sourceInstanceIp;
    }


    public String getTargetInstanceIp() {
        return targetInstanceIp;
    }


    public void setTargetInstanceIp(String targetInstanceIp) {
        this.targetInstanceIp = targetInstanceIp;
    }


    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "AppDataMigrateSearch [sourceAppId=" + sourceAppId + ", targetAppId=" + targetAppId
                + ", sourceInstanceIp=" + sourceInstanceIp + ", targetInstanceIp=" + targetInstanceIp + ", startDate="
                + startDate + ", endDate=" + endDate + ", status=" + status + "]";
    }



}
