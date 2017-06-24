package com.sohu.cache.redis.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis报警配置枚举
 * @author leifu
 * @Date 2017年6月13日
 * @Time 下午5:34:42
 */
public enum RedisAlertConfigEnum {
    aof_current_size("aof_current_size", "aof当前尺寸(单位：MB)"),
    minute_aof_delayed_fsync("aof_delayed_fsync", "分钟aof阻塞个数"),
    client_biggest_input_buf("client_biggest_input_buf", "输入缓冲区最大buffer大小(单位：MB)"),
    client_longest_output_list("client_longest_output_list", "输出缓冲区最大队列长度"),
    instantaneous_ops_per_sec("instantaneous_ops_per_sec", "实时ops"),
    latest_fork_usec("latest_fork_usec", "上次fork所用时间(单位：微秒)"),
    mem_fragmentation_ratio("mem_fragmentation_ratio", "内存碎片率(检测大于500MB)"),
    rdb_last_bgsave_status("rdb_last_bgsave_status", "上一次bgsave状态"),
    minute_rejected_connections("rejected_connections", "分钟拒绝连接数"),
    minute_sync_partial_err("sync_partial_err", "分钟部分复制失败次数"),
    minute_sync_partial_ok("sync_partial_ok", "分钟部分复制成功次数"),
    minute_sync_full("sync_full", "分钟全量复制执行次数"),
    minute_total_net_input_bytes("total_net_input_bytes", "分钟网络输入流量(单位：MB)"),
    minute_total_net_output_bytes("total_net_output_bytes", "分钟网络输出流量(单位：MB)"),
    master_slave_offset_diff("master_slave_offset_diff", "主从节点偏移量差(单位：字节)"),
    cluster_state("cluster_state", "集群状态"),
    cluster_slots_ok("cluster_slots_ok", "集群成功分配槽个数"),
    ;
    private final static List<RedisAlertConfigEnum> redisAlertConfigEnumList = new ArrayList<RedisAlertConfigEnum>();
    static {
        for (RedisAlertConfigEnum redisAlertConfigEnum : RedisAlertConfigEnum.values()) {
            redisAlertConfigEnumList.add(redisAlertConfigEnum);
        }
    }
    
    private final static Map<String, RedisAlertConfigEnum> redisAlertConfigEnumMap = new HashMap<String, RedisAlertConfigEnum>();
    static {
        for (RedisAlertConfigEnum redisAlertConfigEnum : RedisAlertConfigEnum.values()) {
            redisAlertConfigEnumMap.put(redisAlertConfigEnum.getValue(), redisAlertConfigEnum);
        }
    }
    
    private String value;
    
    private String info;
    

    public static List<RedisAlertConfigEnum> getRedisAlertConfigEnumList() {
        return redisAlertConfigEnumList;
    }

    public static Map<String, RedisAlertConfigEnum> getRedisAlertConfigEnumMap() {
        return redisAlertConfigEnumMap;
    }
    
    public static RedisAlertConfigEnum getRedisAlertConfig(String alertConfig) {
        return redisAlertConfigEnumMap.get(alertConfig);
    }

    private RedisAlertConfigEnum(String value, String info) {
        this.value = value;
        this.info = info;
    }

    public String getValue() {
        return value;
    }

    public String getInfo() {
        return info;
    }
    
}
