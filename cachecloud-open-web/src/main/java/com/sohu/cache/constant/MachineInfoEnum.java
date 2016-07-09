package com.sohu.cache.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 机器信息枚举
 * @author leifu
 * @Date 2016年7月9日
 * @Time 下午4:38:15
 */
public class MachineInfoEnum {
    
    /**
     * 是否为可用
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

        public void setValue(int value) {
            this.value = value;
        }
    }
    
    /**
     * 机器类型
     * @author leifu
     * @Date 2016年7月9日
     * @Time 下午4:42:58
     */
    public static enum TypeEnum {
        REDIS_NODE(0, "redis实例"),
        REDIS_MIGRATE_TOOL(2, "redis迁移工具");

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
    
}
