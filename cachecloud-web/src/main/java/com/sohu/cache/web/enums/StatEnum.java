package com.sohu.cache.web.enums;

/**
 * <p>
 * Description:后台管理总览相关统计参数
 * </p>
 * @author chenshi
 * @version 1.0
 * @date 2017/8/14
 * @param
 * @return
 */
public enum StatEnum {

    /**
     * 基础统计:
     * 1.在线应用数
     * 2.在线实例数
     * 3.在线机器数
     * 4.Redis版本数量
     */
    TOTAL_EFFETIVE_APP("effetiveAppCount"),
    TOTAL_INSTANCE_NUM("totalInstanceCount"),
    TOTAL_MACHINE_NUM("totalMachineCount"),
    TOTAL_PHYSICAL_MACHINE_NUM("totalPhysicalMachineCount"),
    REDIS_VERSION_COUNT("redisTypeCount"),
    /**
     * 分布统计：
     * 1.Redis版本分布与统计
     * 2.机器内存统计
     * 3.实例内存统计
     * 4.按机房内存统计
     */
    REDIS_VERSION_DISTRIBUTE("redisDistribute"),
    MACHINE_MAXMEMORY_DISTRIBUTE("maxMemoryDistrubute"),
    MACHINE_USEDMEMORY_DISTRIBUTE("usedMemoryDistribute"),
    MACHIEN_ROOMMEMORY_DISTRIBUTE("roomMemoryDistribute"),
    MACHIEN_ROOM_DISTRIBUTE("roomDistribute");

    String value;

    private StatEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
