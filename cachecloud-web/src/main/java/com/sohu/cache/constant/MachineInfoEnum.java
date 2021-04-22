package com.sohu.cache.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 机器信息枚举
 *
 * @author leifu
 * @Date 2016年7月9日
 * @Time 下午4:38:15
 */
public class MachineInfoEnum {

    /**
     * 是否为可用
     *
     * @author leifu
     * @Date 2016年7月9日
     * @Time 下午4:43:49
     */
    public static enum AvailableEnum {
        YES(1),
        NO(0);

        private int value;

        private AvailableEnum(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

    }

    /**
     * 机器类型
     *
     * @author leifu
     * @Date 2016年7月9日
     * @Time 下午4:42:58
     */
    public static enum TypeEnum {
        REDIS_NODE(0, "Redis类型"),
        REDIS_MIGRATE_TOOL(2, "Redis迁移工具类型"),
        SENTINEL_NODE(3, "Sentinel类型"),
        TWEMPROXY_NODE(4, "Twemproxy类型"),
        PIKA_NODE(5, "Pika类型");

        private int type;

        private String info;

        private static Map<Integer, TypeEnum> MAP = new HashMap<Integer, TypeEnum>();

        static {
            for (TypeEnum typeEnum : TypeEnum.values()) {
                MAP.put(typeEnum.getType(), typeEnum);
            }
        }

        public static TypeEnum getByType(int type) {
            return MAP.get(type);
        }

        private TypeEnum(int type, String info) {
            this.type = type;
            this.info = info;
        }

        public int getType() {
            return type;
        }

        public String getInfo() {
            return info;
        }


    }

    public static enum MachineEnum {
        HOST("host"),
        CONTAINER("container");

        private String value;

        MachineEnum(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static enum MachineTypeEnum {
        HOST(1),
        CONTAINER(2),
        ALL(3);

        private int value;

        MachineTypeEnum(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

}
