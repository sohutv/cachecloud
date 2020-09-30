package com.sohu.cache.web.enums;

/**
 * Created by chenshi on 2019/5/28.
 */
public enum MachineTaskEnum {

    //未同步
    UNSYNC(0),
    //已同步
    SYNCED(1),
    //同步中
    SYNCING(-1),
    //同步失败
    SYNC_FAILED(-2);

    private int value;

    MachineTaskEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
