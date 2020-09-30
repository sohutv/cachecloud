package com.sohu.cache.entity;

import lombok.Data;

/**
 * Created by rucao
 */
@Data
public class MachineMemStatInfo {
    /**
     * 机器id
     */
    private long id;
    /**
     * ip地址
     */
    private String ip;
    /**
     * 机房
     */
    private String room;
    /**
     * 使用类型：Redis专用机器（0），Redis测试机器（1），混合部署机器（2），Redis迁移工具机器（3）
     */
    private int useType;
    /**
     * cpu数量
     */
    private int cpu;

    private int instanceNum;
    /**
     * 内存，单位G
     */
    private int mem;
    /**
     * 该机器已分配的内存
     */
    private long applyMem;
    /**
     * 该机器的已使用的内存
     */
    private long usedMem;
    /**
     * 宿主机ip
     */
    private String realIp;
    /**
     * 机架信息
     */
    private String rack;

}
