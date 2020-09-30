package com.sohu.cache.constant;

/**
 * 审批状态
 * 
 * @author leifu
 * @Time 2014年10月20日
 */
public enum AppCheckEnum {

    // 通过审批
    APP_PASS(1),
    // 驳回审批
    APP_REJECT(-1),
    // 等待审批
    APP_WATING_CHECK(0),
    //分配资源完毕
    APP_ALLOCATE_RESOURCE(2);

    private Integer value;

    public Integer value() {
        return value;
    }
    
    private AppCheckEnum(Integer value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
