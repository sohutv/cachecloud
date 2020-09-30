package com.sohu.cache.entity;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.Data;
import org.apache.commons.lang.math.NumberUtils;

/**
 * 基于应用全局耗时统计(uniquekey: app_id, command, collect_time)
 * @author leifu
 */
@Data
public class AppClientCostTimeTotalStat {
    
    private long id;

    /**
     * 应用id
     */
    private long appId;

    /**
     * 格式yyyyMMddHHmm00
     */
    private long collectTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 命令
     */
    private String command;

    /**
     * 调用总次数
     */
    private long totalCount;
    
    /**
     * 调用总耗时
     */
    private double totalCost;

    /**
     * 中位值
     */
    private int median;

    /**
     * 平均值
     */
    private double mean;

    /**
     * 90%最大值
     */
    private int ninetyPercentMax;

    /**
     * 99%最大值
     */
    private int ninetyNinePercentMax;

    /**
     * 100%最大值
     */
    private int hundredMax;
    
    /**
     * 实例ip
     */
    private String maxInstanceHost;

    /**
     * 实例port
     */
    private int maxInstancePort;
    
    /**
     * 实例id
     */
    private long maxInstanceId;
    
    /**
     * 客户端
     */
    private String maxClientIp;

    public Date getCreateTime() {
        return (Date) createTime.clone();
    }

    public void setCreateTime(Date createTime) {
        this.createTime = (Date) createTime.clone();
    }

    public AppClientCostTimeTotalStat(long id, long appId, long collectTime, Date createTime, String command,
            long totalCount, double totalCost, int median, double mean, int ninetyPercentMax, int ninetyNinePercentMax,
            int hundredMax, String maxInstanceHost, int maxInstancePort, long maxInstanceId, String maxClientIp) {
        this.id = id;
        this.appId = appId;
        this.collectTime = collectTime;
        this.createTime = createTime;
        this.command = command;
        this.totalCount = totalCount;
        this.totalCost = totalCost;
        this.median = median;
        this.mean = mean;
        this.ninetyPercentMax = ninetyPercentMax;
        this.ninetyNinePercentMax = ninetyNinePercentMax;
        this.hundredMax = hundredMax;
        this.maxInstanceHost = maxInstanceHost;
        this.maxInstancePort = maxInstancePort;
        this.maxInstanceId = maxInstanceId;
        this.maxClientIp = maxClientIp;
    }

    public AppClientCostTimeTotalStat() {
    }

    public Long getTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            Date date = sdf.parse(String.valueOf(this.collectTime));
            return date.getTime();
        } catch (ParseException e) {
            return 0L;
        }
        
    }

    public static AppClientCostTimeTotalStat getFromAppClientCostTimeStat(AppClientCostTimeStat stat) {
        AppClientCostTimeTotalStat appClientCostTimeTotalStat = new AppClientCostTimeTotalStat();
        appClientCostTimeTotalStat.setAppId(stat.getAppId());
        appClientCostTimeTotalStat.setCollectTime(stat.getCollectTime());
        appClientCostTimeTotalStat.setCommand(stat.getCommand());
        appClientCostTimeTotalStat.setCreateTime(stat.getCreateTime());
        appClientCostTimeTotalStat.setMean(stat.getMean());
        appClientCostTimeTotalStat.setMedian(stat.getMedian());
        appClientCostTimeTotalStat.setHundredMax(stat.getHundredMax());
        appClientCostTimeTotalStat.setNinetyPercentMax(stat.getNinetyPercentMax());
        appClientCostTimeTotalStat.setNinetyNinePercentMax(stat.getNinetyNinePercentMax());
        appClientCostTimeTotalStat.setMaxClientIp(stat.getClientIp());
        appClientCostTimeTotalStat.setMaxInstanceHost(stat.getInstanceHost());
        appClientCostTimeTotalStat.setMaxInstancePort(stat.getInstancePort());
        appClientCostTimeTotalStat.setMaxInstanceId(stat.getInstanceId());
        appClientCostTimeTotalStat.setMaxClientIp(stat.getClientIp());
        //保留两位小数
        DecimalFormat df = new DecimalFormat("#.00");
        appClientCostTimeTotalStat.setTotalCost(NumberUtils.toDouble(df.format(stat.getMean() * stat.getCount())));
        appClientCostTimeTotalStat.setTotalCount(stat.getCount());
        return appClientCostTimeTotalStat;
    }
    
}
