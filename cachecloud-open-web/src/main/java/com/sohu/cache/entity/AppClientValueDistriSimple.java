package com.sohu.cache.entity;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.sohu.tv.jedis.stat.enums.ValueSizeDistriEnum;

/**
 * 
 * @author leifu
 * @Date 2015-6-20
 * @Time 下午4:37:02
 */
public class AppClientValueDistriSimple {

    /**
     * 值分布类型
     */
    private int distributeType;

    /**
     * 调用次数
     */
    private long count;

    public int getDistributeType() {
        return distributeType;
    }

    public void setDistributeType(int distributeType) {
        this.distributeType = distributeType;
    }


    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getDistributeDesc() {
        ValueSizeDistriEnum valueSizeDistriEnum = ValueSizeDistriEnum.getByType(distributeType);
        return valueSizeDistriEnum == null ? "" : valueSizeDistriEnum.getInfo();
    }
    

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this).toString();
    }
    
}
