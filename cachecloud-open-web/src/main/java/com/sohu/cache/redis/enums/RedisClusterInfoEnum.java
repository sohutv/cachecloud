package com.sohu.cache.redis.enums;

/**
 * cluster info枚举
 * @author leifu
 * @Date 2017年6月21日
 * @Time 下午2:36:47
 */
public enum RedisClusterInfoEnum {
    
    cluster_state("cluster_state", "集群状态", false),
    cluster_slots_assigned("cluster_slots_assigned", "分配slot个数", false),
    cluster_slots_ok("cluster_slots_ok", "成功分配slot个数", false),
    cluster_slots_pfail("cluster_slots_pfail", "pfail个数", false),
    cluster_slots_fail("cluster_slots_fail", "fail个数", false),
    cluster_stats_messages_sent("cluster_stats_messages_sent", "发送消息字节数", false),
    cluster_stats_messages_received("cluster_stats_messages_received", "接收消息字节数", false),
    ;
    
    private String value;
    
    private String info;
    
    private boolean needCalDif;

    private RedisClusterInfoEnum(String value, String info, boolean needCalDif) {
        this.value = value;
        this.info = info;
        this.needCalDif = needCalDif;
    }

    public String getValue() {
        return value;
    }

    public String getInfo() {
        return info;
    }

    public boolean isNeedCalDif() {
        return needCalDif;
    }
    
}
