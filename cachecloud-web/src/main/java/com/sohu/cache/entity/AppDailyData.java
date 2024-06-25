package com.sohu.cache.entity;

import com.sohu.cache.web.vo.AppDetailVO;
import lombok.Data;
import org.apache.commons.collections.MapUtils;

import java.util.Date;
import java.util.Map;

/**
 * 应用日报数据
 *
 * @author leifu
 */
@Data
public class AppDailyData {

    /**
     * 应用id
     */
    private long appId;

    /**
     * 开始日期
     */
    private Date startDate;

    /**
     * 结束日期
     */
    private Date endDate;

    /**
     * 日期
     */
    private Date date;

    /**
     * bigkey次数
     */
    private long bigKeyTimes;

    /**
     * bigkey信息
     */
    private String bigKeyInfo;

    /**
     * 慢查询次数
     */
    private long slowLogCount;

    /**
     * 延迟事件个数
     */
    private long latencyCount;

    /**
     * 客户端异常个数
     */
    private long clientExceptionCount;
    /**
     * 累计命令调用次数
     */
    private long clientCmdCount;
    /**
     * 平均命令调用耗时
     */
    private double clientAvgCmdCost;
    /**
     * 累计连接异常事件次数
     */
    private long clientConnExpCount;
    /**
     * 平均连接异常事件耗时
     */
    private double clientAvgConnExpCost;
    /**
     * 累计命令超时事件次数
     */
    private long clientCmdExpCount;
    /**
     * 平均命令超时事件耗时
     */
    private double clientAvgCmdExpCost;

    /**
     * 每分钟最大客户端连接数
     */
    private long maxMinuteClientCount;

    /**
     * 每分钟平均客户端连接数
     */
    private long avgMinuteClientCount;

    /**
     * 每分钟最大命令数
     */
    private long maxMinuteCommandCount;

    /**
     * 每分钟平均命令数
     */
    private long avgMinuteCommandCount;

    /**
     * 平均命中率
     */
    private double avgHitRatio;

    /**
     * 每分钟最小命中率
     */
    private double minMinuteHitRatio;

    /**
     * 每分钟最大命中率
     */
    private double maxMinuteHitRatio;

    /**
     * 平均内存使用量
     */
    private long avgUsedMemory;

    /**
     * 最大内存使用量
     */
    private long maxUsedMemory;

    /**
     * 过期键个数
     */
    private long expiredKeysCount;

    /**
     * 剔除键个数
     */
    private long evictedKeysCount;

    /**
     * 每分钟平均网络input量
     */
    private double avgMinuteNetInputByte;

    /**
     * 每分钟最大网络input量
     */
    private double maxMinuteNetInputByte;

    /**
     * 每分钟平均网络output量
     */
    private double avgMinuteNetOutputByte;

    /**
     * 每分钟最大网络output量
     */
    private double maxMinuteNetOutputByte;

    /**
     * 键个数平均值
     */
    private long avgObjectSize;

    /**
     * 键个数最大值
     */
    private long maxObjectSize;

    /**
     * 平均磁盘使用量
     */
    private long avgUsedDisk;

    /**
     * 最大磁盘使用量
     */
    private long maxUsedDisk;

    /**
     * 值分布
     */
    private Map<String, Long> valueSizeDistributeCountMap;

    /**
     * 应用详情
     */
    private AppDetailVO appDetailVO;

    public String getValueSizeDistributeCountDesc() {
        if (MapUtils.isEmpty(valueSizeDistributeCountMap)) {
            return "无";
        }
        StringBuffer desc = new StringBuffer();
        for (Map.Entry<String, Long> entry : valueSizeDistributeCountMap.entrySet()) {
            desc.append(entry.getKey()).append(":").append(entry.getValue()).append("次<br/>");
        }
        return desc.toString();
    }

    public String getValueSizeDistributeCountDescHtml() {
        return bigKeyInfo.replace("\n", "<br/>").replace(":", ":\t");
    }
}
