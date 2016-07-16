package com.sohu.cache.redis.enums;

/**
 * Created by yijunzhang on 14-8-25.
 */
public enum RedisClusterConfigEnum {
    CLUSTER_ENABLED("cluster-enabled", "yes", "是否开启集群模式"),
    CLUSTER_NODE_TIMEOUT("cluster-node-timeout", "15000", "集群节点超时时间,默认15秒"),
    CLUSTER_SLAVE_VALIDITY_FACTOR("cluster-slave-validity-factor", "10", "集群从节点,延迟有效性判断因子,默认10秒:(node-timeout * slave-validity-factor) + repl-ping-slave-period"),
    CLUSTER_MIGRATION_BARRIER("cluster-migration-barrier", "1", "cluster主从迁移至少需要的从节点数,默认1个"),
    CLUSTER_CONFIG_FILE("cluster-config-file", "nodes-%d.conf", "集群配置文件名称,格式:nodes-{port}.conf"),
    CLUSTER_REQUIRE_FULL_COVERAGE("cluster-require-full-coverage", "no", "节点部分失败期间,其他节点是否继续工作");

    private String key;

    private String value;

    private String desc;

    RedisClusterConfigEnum(String key, String value, String desc) {
        this.key = key;
        this.value = value;
        this.desc = desc;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public String getKey() {
        return key;
    }

    public static RedisClusterConfigEnum get(String key) {
        if (key == null) {
            return null;
        }
        for (RedisClusterConfigEnum config : RedisClusterConfigEnum.values()) {
            if (config.key.equals(key)) {
                return config;
            }
        }
        return null;
    }

}
