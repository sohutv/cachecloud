package com.sohu.tv.jedis.stat.model;

import java.util.List;
import java.util.Map;

/**
 * 上报实体
 * 
 * @author leifu
 * @Date 2015年1月16日
 * @Time 下午3:01:06
 */
public class ClientReportBean {
	
    /**
     * 客户端ip
     */
    private String clientIp;

    /**
     * 收集时间
     */
    private long collectTime;
    
    /**
     * 上报时间
     */
    private long reportTimeStamp;

    /**
     * 上报数据
     */
    private List<Map<String, Object>> datas;
    
    /**
     * 其他信息
     */
    private Map<String,Object> otherInfo;

    public ClientReportBean() {
    }

	public ClientReportBean(String clientIp, long collectTime, long reportTimeStamp, List<Map<String, Object>> datas,
            Map<String, Object> otherInfo) {
        this.clientIp = clientIp;
        this.collectTime = collectTime;
        this.reportTimeStamp = reportTimeStamp;
        this.datas = datas;
        this.otherInfo = otherInfo;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public long getCollectTime() {
        return collectTime;
    }

    public void setCollectTime(long collectTime) {
        this.collectTime = collectTime;
    }

    public List<Map<String, Object>> getDatas() {
        return datas;
    }

    public void setDatas(List<Map<String, Object>> datas) {
        this.datas = datas;
    }


    public long getReportTimeStamp() {
        return reportTimeStamp;
    }

    public void setReportTimeStamp(long reportTimeStamp) {
        this.reportTimeStamp = reportTimeStamp;
    }

    public Map<String, Object> getOtherInfo() {
        return otherInfo;
    }

    public void setOtherInfo(Map<String, Object> otherInfo) {
        this.otherInfo = otherInfo;
    }

    @Override
    public String toString() {
        return "ClientReportBean [clientIp=" + clientIp + ", collectTime=" + collectTime + ", reportTimeStamp="
                + reportTimeStamp + ", datas=" + datas + ", otherInfo=" + otherInfo + "]";
    }


}
