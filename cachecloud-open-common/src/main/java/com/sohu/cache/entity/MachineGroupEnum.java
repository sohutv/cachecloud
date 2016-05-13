package com.sohu.cache.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * 机器组别枚举
 * 
 * @author leifu
 * @Date 2015年5月5日
 * @Time 下午1:42:51
 */
public enum MachineGroupEnum {
    GROUP_ONE(1, "1组"),
    GROUP_TWO(2, "2组");

    private static final Map<Integer, MachineGroupEnum> MAP = new HashMap<Integer, MachineGroupEnum>();

    static {
        for (MachineGroupEnum machineGroupEnum : MachineGroupEnum.values()) {
            MAP.put(machineGroupEnum.getValue(), machineGroupEnum);
        }
    }

    public static String getMachineGroupInfo(Integer value) {
        MachineGroupEnum machineGroupEnum = MAP.get(value);
        return machineGroupEnum == null ? "未知" : machineGroupEnum.getInfo();
    }

    private int value;

    private String info;

    private MachineGroupEnum(int value, String info) {
        this.value = value;
        this.info = info;
    }

    public int getValue() {
        return value;
    }

    public String getInfo() {
        return info;
    }

}
