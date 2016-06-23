package com.sohu.cache.redis.enums;

import com.sohu.cache.protocol.MachineProtocol;

/**
 * Created by yijunzhang on 14-7-27.
 */
public enum RedisConfigEnum {
    DAEMONIZE("daemonize", "no", "是否守护进程"),
    TCP_BACKLOG("tcp-backlog", "511", "TCP连接完成队列"),
    TIMEOUT("timeout", "0", "客户端闲置多少秒后关闭连接,默认为0,永不关闭"),
    TCP_KEEPALIVE("tcp-keepalive", "0", "检测客户端是否健康周期,默认关闭"),
    LOGLEVEL("loglevel", "notice", "默认普通的verbose"),
    DATABASES("databases", "16", "可用的数据库数，默认值为16个,默认数据库为0"),
    DIR("dir", MachineProtocol.DATA_DIR, "redis工作目录,默认:" + MachineProtocol.DATA_DIR),
    STOP_WRITES_ON_BGSAVE_ERROR("stop-writes-on-bgsave-error", "no", "bgsave出错了不停写"),
    REPL_TIMEOUT("repl-timeout", "60", "master批量数据传输时间或者ping回复时间间隔,默认:60秒"),
    REPL_PING_SLAVE_PERIOD("repl-ping-slave-period", "10", "指定slave定期ping master的周期,默认:10秒"),
    REPL_DISABLE_TCP_NODELAY("repl-disable-tcp-nodelay", "no", "是否禁用socket的NO_DELAY,默认关闭，影响主从延迟"),
    REPL_BACKLOG_SIZE("repl-backlog-size", "10M", "复制缓存区,默认:1mb,配置为:10Mb"),
    REPL_BACKLOG_TTL("repl-backlog-ttl", "7200", "master在没有Slave的情况下释放BACKLOG的时间多久:默认:3600,配置为:7200"),
    SLAVE_SERVE_STALE_DATA("slave-serve-stale-data", "yes", "当slave服务器和master服务器失去连接后，或者当数据正在复制传输的时候，如果此参数值设置“yes”，slave服务器可以继续接受客户端的请求"),
    SLAVE_READ_ONLY("slave-read-only", "yes", "slave服务器节点是否只读,cluster的slave节点默认读写都不可用,需要调用readonly开启可读模式"),
    SLAVE_PRIORITY("slave-priority", "100", "slave的优先级,影响sentinel/cluster晋升master操作,0永远不晋升"),
    LUA_TIME_LIMIT("lua-time-limit", "5000", "Lua脚本最长的执行时间，单位为毫秒"),
    SLOWLOG_LOG_SLOWER_THAN("slowlog-log-slower-than", "10000", "慢查询被记录的阀值,默认10毫秒"),
    SLOWLOG_MAX_LEN("slowlog-max-len", "128", "最多记录慢查询的条数"),
    HASH_MAX_ZIPLIST_ENTRIES("hash-max-ziplist-entries", "512", "hash数据结构优化参数"),
    HASH_MAX_ZIPLIST_VALUE("hash-max-ziplist-value", "64", "hash数据结构优化参数"),
    LIST_MAX_ZIPLIST_ENTRIES("list-max-ziplist-entries", "512", "list数据结构优化参数"),
    LIST_MAX_ZIPLIST_VALUE("list-max-ziplist-value", "64", "list数据结构优化参数"),
    SET_MAX_INTSET_ENTRIES("set-max-intset-entries", "512", "set数据结构优化参数"),
    ZSET_MAX_ZIPLIST_ENTRIES("zset-max-ziplist-entries", "128", "zset数据结构优化参数"),
    ZSET_MAX_ZIPLIST_VALUE("zset-max-ziplist-value", "64", "zset数据结构优化参数"),
    ACTIVEREHASHING("activerehashing", "yes", "是否激活重置哈希,默认:yes"),
    CLIENT_OUTPUT_BUFFER_LIMIT_NORMAL("client-output-buffer-limit normal", "0 0 0", ""),
    CLIENT_OUTPUT_BUFFER_LIMIT_SLAVE("client-output-buffer-limit slave", "512mb 128mb 60", ""),
    CLIENT_OUTPUT_BUFFER_LIMIT_PUBSUB("client-output-buffer-limit pubsub", "32mb 8mb 60", ""),
    HZ("hz", "10", "执行后台task数量,默认:10"),
    PORT("port", "%d", "端口"),
    MAXMEMORY("maxmemory", "%dmb", "当前实例最大可用内存"),
    MAXMEMORY_POLICY("maxmemory-policy", "volatile-lru", "内存不够时,淘汰策略,默认:volatile-lru"),
    APPENDONLY("appendonly", "yes", "开启append only持久化模式"),
    APPENDFSYNC("appendfsync", "everysec", "默认:aof每秒同步一次"),
    APPENDFILENAME("appendfilename", "appendonly-%d.aof", "aof文件名称,默认:appendonly-{port}.aof"),
    DBFILENAME("dbfilename", "dump-%d.rdb", "RDB文件默认名称,默认dump-{port}.rdb"),
    AOF_REWRITE_INCREMENTAL_FSYNC("aof-rewrite-incremental-fsync","yes","aof rewrite过程中,是否采取增量文件同步策略,默认:yes"),
    NO_APPENDFSYNC_ON_REWRITE("no-appendfsync-on-rewrite", "yes", "是否在后台aof文件rewrite期间调用fsync,默认调用,修改为yes,防止可能fsync阻塞,但可能丢失rewrite期间的数据"),
    AUTO_AOF_REWRITE_MIN_SIZE("auto-aof-rewrite-min-size", "64m", "触发rewrite的aof文件最小阀值,默认64m"),
    AUTO_AOF_REWRITE_PERCENTAGE("auto-aof-rewrite-percentage", "%d", "Redis重写aof文件的比例条件,默认从100开始,统一机器下不同实例按4%递减");

    private String key;

    private String value;

    private String desc;

    RedisConfigEnum(String key, String value, String desc) {
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

    public static RedisConfigEnum get(String key) {
        if (key == null) {
            return null;
        }
        for (RedisConfigEnum config : RedisConfigEnum.values()) {
            if (config.key.equals(key)) {
                return config;
            }
        }
        return null;
    }
}
