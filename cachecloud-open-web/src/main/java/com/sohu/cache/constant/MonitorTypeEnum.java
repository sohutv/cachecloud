package com.sohu.cache.constant;

/**
 * Created by hym on 14-10-14.
 */
public enum MonitorTypeEnum {

    HitsPercentLT("命中率"),
    MemUsedPercentGT("命中率大于"),
    MemUsedPercentLT("命中率小于"),
    IsLRU("lru"),
    ConnectionNumGT("当前连接数大于"),
    SlowSelectGT("慢查询次数大于"),
    ConnectionRefuse("连接拒绝"),
    ConnectionTimeOut("连接超时");

    private String value;

    private MonitorTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
