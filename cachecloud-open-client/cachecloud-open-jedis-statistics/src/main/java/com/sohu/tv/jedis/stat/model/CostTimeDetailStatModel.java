package com.sohu.tv.jedis.stat.model;

/**
 * 耗时详细统计(平均值、中位值、多维度最大值等等)
 * @author leifu
 * @Date 2015年1月23日
 * @Time 上午11:23:20
 */
public class CostTimeDetailStatModel {

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
     * 总次数
     */
    private long totalCount;

    public int getMedian() {
        return median;
    }

    public void setMedian(int median) {
        this.median = median;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public int getNinetyPercentMax() {
        return ninetyPercentMax;
    }

    public void setNinetyPercentMax(int ninetyPercentMax) {
        this.ninetyPercentMax = ninetyPercentMax;
    }

    public int getNinetyNinePercentMax() {
        return ninetyNinePercentMax;
    }

    public void setNinetyNinePercentMax(int ninetyNinePercentMax) {
        this.ninetyNinePercentMax = ninetyNinePercentMax;
    }

    public int getHundredMax() {
        return hundredMax;
    }

    public void setHundredMax(int hundredMax) {
        this.hundredMax = hundredMax;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    @Override
    public String toString() {
        return "CostTimeDetailStatModel [median=" + median + ", mean=" + mean + ", ninetyPercentMax="
                + ninetyPercentMax + ", ninetyNinePercentMax=" + ninetyNinePercentMax + ", hundredMax=" + hundredMax
                + ", totalCount=" + totalCount + "]";
    }



}
