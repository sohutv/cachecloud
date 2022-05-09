package com.sohu.cache.web.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 应用类型
 */
public enum MasterSlaveExistEnum {
    NONE(0, "no master or slave"),
    MASTER(1, "only master"),
    SLAVE(2, "only slave"),
    MASTRE_SLAVE(3, "both master and slave");

    private int type;

    private String info;

    private static Map<Integer, MasterSlaveExistEnum> MAP = new HashMap<Integer, MasterSlaveExistEnum>();

    static {
        for (MasterSlaveExistEnum appTypeEnum : MasterSlaveExistEnum.values()) {
            MAP.put(appTypeEnum.getType(), appTypeEnum);
        }
    }

    MasterSlaveExistEnum(int type, String info) {
        this.type = type;
        this.info = info;
    }

    public static MasterSlaveExistEnum getByType(int type) {
        return MAP.get(type);
    }

    public int getType() {
        return type;
    }

    public String getInfo() {
        return info;
    }

}