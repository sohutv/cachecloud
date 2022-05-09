package com.sohu.cache.web.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: zengyizhao
 * @DateTime: 2021/9/3 14:40
 * @Description: 邮件报警类型枚举
 */
public enum AlertTypeEnum {

    INSTANCE_RUNNING_STATE_CHANGE(1, "实例运行状态变更", 1, 1),
    INATANCE_EXCEPTION_STATE_MONITOR(2, "实例状态异常监控", 1, 1),
    MACHINE_MEMORY_OVER_PRESET(3, "机器内存报警", 1, 2),
    INSTANCE_MINUTE_MONITOR(4, "实例分钟报警", 1, 0),
    POD_RESTART_SYNC_TASK(5, "Pod重启机器同步任务报警", 1, 1),
    POD_RESTART_INSTANCE_RECOVER(6, "Pod重启探测Redis实例报警", 1, 1),
    MACHINE_MANAGE(7, "机器管理", 1, 1),
    APP_MINUTE_MONITOR(8, "实例分钟报警", 1, 0),
    APP_CLIENT_CONNECTION(9, "应用客户端连接数报警", 0, 2),
    APP_SHARD_CLENT_CONNECTION(10, "应用实例-分片客户端连接数报警", 0, 2),
    APP_HIT_RATIO(11, "应用平均命中率报警", 0, 2),
    APP_MEM_USED_RATIO(12, "应用内存使用率报警", 0, 2),
    APP_SHARD_MEM_USED_RATIO(13, "分片内存使用率报警", 0, 2);

    private int type;//邮件类型

    private String info;//邮件类型信息

    private int visibleType;//可见类型（0：均可见；1：仅管理员可见；）

    private int importantLevel;//重要类型（0：一般；1：重要；2：紧急）

    private static Map<Integer, AppTypeEnum> MAP = new HashMap<Integer, AppTypeEnum>();

    static {
        for (AppTypeEnum appTypeEnum : AppTypeEnum.values()) {
            MAP.put(appTypeEnum.getType(), appTypeEnum);
        }
    }

    AlertTypeEnum(int type, String info, int visibleType, int importantLevel) {
        this.type = type;
        this.info = info;
        this.visibleType = visibleType;
        this.importantLevel = importantLevel;
    }

    public static AppTypeEnum getByType(int type) {
        return MAP.get(type);
    }

    public int getType() {
        return type;
    }

    public String getInfo() {
        return info;
    }

    public int getVisibleType() {
        return visibleType;
    }

    public int getImportantLevel() {
        return importantLevel;
    }
}
