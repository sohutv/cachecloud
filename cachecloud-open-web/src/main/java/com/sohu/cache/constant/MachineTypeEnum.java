package com.sohu.cache.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 机器类型
 * @author leifu
 * @Date 2016-6-9
 * @Time 下午2:30:48
 */
public enum MachineTypeEnum {
    REDIS_NODE(0, "redis实例"),
    REDIS_MIGRATE_TOOL(2, "redis迁移工具");

    private int type;

    private String info;

    private static Map<Integer, MachineTypeEnum> MAP = new HashMap<Integer, MachineTypeEnum>();
    static {
        for (MachineTypeEnum machineTypeEnum : MachineTypeEnum.values()) {
            MAP.put(machineTypeEnum.getType(), machineTypeEnum);
        }
    }

    public static MachineTypeEnum getByType(int type) {
        return MAP.get(type);
    }

    private MachineTypeEnum(int type, String info) {
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
