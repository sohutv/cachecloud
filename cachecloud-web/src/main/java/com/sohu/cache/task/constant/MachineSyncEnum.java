package com.sohu.cache.task.constant;

/**
 * Created by chenshi on 2020/5/18.
 */
public enum MachineSyncEnum {

    NO_CHANGE(0, "不执行同步任务"),
    SYNC_EXECUTING(4, "同步任务执行中..."),
    SYNC_SUCCESS(1, "同步任务成功"),
    SYNC_ABORT(2, "同步任务中断"),
    SYNC_ERROR(3, "同步任务异常");

    private int value;

    private String desc;

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    MachineSyncEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
