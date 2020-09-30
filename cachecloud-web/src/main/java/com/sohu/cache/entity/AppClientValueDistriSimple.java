package com.sohu.cache.entity;

import com.sohu.cache.constant.ValueSizeDistriEnum;
import lombok.Data;

/**
 * @author leifu
 */
@Data
public class AppClientValueDistriSimple {

    /**
     * 值分布类型
     */
    private int distributeType;

    /**
     * 调用次数
     */
    private long count;

    public String getDistributeDesc() {
        ValueSizeDistriEnum valueSizeDistriEnum = ValueSizeDistriEnum.getByType(distributeType);
        return valueSizeDistriEnum == null ? "" : valueSizeDistriEnum.getInfo();
    }

}
