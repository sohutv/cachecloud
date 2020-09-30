package com.sohu.cache.task.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * @author fulei
 */
public class InstanceInfoEnum {

    /**
     * 实例状态
     *
     * @author fulei
     * @date 2018年6月22日
     * @time 下午1:53:07
     */
    public enum InstanceStatusEnum {
        NEW_STATUS(-1, "元数据,没有真实实例"),
        ERROR_STATUS(0, "心跳停止"),
        GOOD_STATUS(1, "运行中"),
        OFFLINE_STATUS(2, "已下线"),
        PAUSE_STATUS(3, "暂停");

        private int status;

        private String info;

        private static Map<Integer, InstanceStatusEnum> MAP = new HashMap<Integer, InstanceStatusEnum>();

        static {
            for (InstanceStatusEnum instanceStatusEnum : InstanceStatusEnum.values()) {
                MAP.put(instanceStatusEnum.getStatus(), instanceStatusEnum);
            }
        }

        public static InstanceStatusEnum getByStatus(int status) {
            return MAP.get(status);
        }

        private InstanceStatusEnum(int status, String info) {
            this.status = status;
            this.info = info;
        }

        public int getStatus() {
            return status;
        }

        public String getInfo() {
            return info;
        }
    }

    /**
     * redis-port状态
     */
    public enum RedisPortStatusEnum {
        NEW_STATUS(0, "新节点"),
        GOOD_STATUS(1, "运行中"),
        SYNC_FINISH_STATUS(2, "同步完成"),
        PAUSE_STATUS(3, "暂停"),
        OFFLINE(4, "下线"),
        ERROR(5, "异常"),
        ;
        private int status;

        private String info;

        private static Map<Integer, RedisPortStatusEnum> MAP = new HashMap<Integer, RedisPortStatusEnum>();

        static {
            for (RedisPortStatusEnum redisPortStatusEnum : RedisPortStatusEnum.values()) {
                MAP.put(redisPortStatusEnum.getStatus(), redisPortStatusEnum);
            }
        }

        public static RedisPortStatusEnum getByStatus(int status) {
            return MAP.get(status);
        }

        RedisPortStatusEnum(int status, String info) {
            this.status = status;
            this.info = info;
        }

        public int getStatus() {
            return status;
        }

        public String getInfo() {
            return info;
        }
    }

    /**
     * redis-port状态
     *
     * @author fulei
     * @date 2018年6月22日
     * @time 下午1:53:07
     */
    public enum RedisMigrateToolStatusEnum {
        NEW_STATUS(0, "新节点"),
        GOOD_STATUS(1, "运行中"),
        SYNC_FINISH_STATUS(2, "同步完成"),
        PAUSE_STATUS(3, "暂停"),
        OFFLINE(4, "下线"),
        ERROR(5, "异常"),
        ;
        private int status;

        private String info;

        private static Map<Integer, RedisMigrateToolStatusEnum> MAP = new HashMap<Integer, RedisMigrateToolStatusEnum>();

        static {
            for (RedisMigrateToolStatusEnum redisMigrateToolStatusEnum : RedisMigrateToolStatusEnum.values()) {
                MAP.put(redisMigrateToolStatusEnum.getStatus(), redisMigrateToolStatusEnum);
            }
        }

        public static RedisMigrateToolStatusEnum getByStatus(int status) {
            return MAP.get(status);
        }

        RedisMigrateToolStatusEnum(int status, String info) {
            this.status = status;
            this.info = info;
        }

        public int getStatus() {
            return status;
        }

        public String getInfo() {
            return info;
        }
    }

    /**
     * 实例类型
     *
     */
    public enum InstanceTypeEnum {
        NUTCRACKER(1, "nutcracker", "nutcracker", "nutcracker"),
        REDIS_CLUSTER(2, "redis-cluster", "redis-cluster", "redis"),
        CODIS_PROXY(3, "codis-proxy", "codis-proxy", "codis"),
        PIKA(4, "pika", "pika", "pika"),
        REDIS_SENTINEL(5, "sentinel", "sentinel", "redis"),
        REDIS_SERVER(6, "redis", "redis-server", "redis"),
        MEMCACHE(7, "memcache", "memcache", "memcache"),
        REDIS_MIGRATE_TOOL(8, "redis-migrate-tool", "redis-migrate-tool", "redis-migrate-tool"),
        CODIS_SERVER(9, "codis-server", "codis-server", "codis"),
        CODIS_DASHBOARD(10, "codis-dashboard", "codis-dashboard", "codis"),
        REDIS_PORT(11, "redis-port", "redis-port", "redis-port"),
        ;

        private int type;

        /**
         * 目录用，不要改
         */
        private String name;

        private String info;

        private String localDir;

        private static Map<Integer, InstanceTypeEnum> MAP = new HashMap<Integer, InstanceTypeEnum>();

        static {
            for (InstanceTypeEnum instanceTypeEnum : InstanceTypeEnum.values()) {
                MAP.put(instanceTypeEnum.getType(), instanceTypeEnum);
            }
        }

        public static InstanceTypeEnum getByType(int type) {
            return MAP.get(type);
        }

        private InstanceTypeEnum(int type, String name, String info, String localDir) {
            this.type = type;
            this.name = name;
            this.info = info;
            this.localDir = localDir;
        }

        public int getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getInfo() {
            return info;
        }

        public String getLocalDir() {
            return localDir;
        }

    }

    /**
     * 是否报警
     */
    public static enum InstanceWarnEnum {
        YES(1),
        NO(0);

        private int value;

        private InstanceWarnEnum(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * 是否是临时实例
     */
    public static enum InstanceTempEumn {
        YES(1),
        NO(0);

        private int value;

        private InstanceTempEumn(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

}
