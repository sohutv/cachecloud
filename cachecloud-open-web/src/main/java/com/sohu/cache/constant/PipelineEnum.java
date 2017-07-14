package com.sohu.cache.constant;

/**
 * @author leifu
 * @Date 2017年7月13日
 * @Time 下午3:26:03
 */
public enum PipelineEnum {
    NO(0), YES(1);
    
    private int value;

    private PipelineEnum(int value) {
        this.value = value;
    }
    
    public static PipelineEnum getPipelineEnum(int pipelineInt) {
        for (PipelineEnum pipelineEnum : PipelineEnum.values()) {
            if (pipelineInt == pipelineEnum.value) {
                return pipelineEnum;
            }
        }
        return null;
    }

    public int getValue() {
        return value;
    }
}
