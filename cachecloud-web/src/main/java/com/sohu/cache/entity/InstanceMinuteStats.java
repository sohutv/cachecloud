package com.sohu.cache.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class InstanceMinuteStats extends InstanceCommandStats {

    /**
     * 内存碎片率
     */
    private double memFragmentationRatio;
}
