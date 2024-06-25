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
     * 该机器已分配的内存, 单位byte
     */
    private long applyMem;
    /**
     * 该机器的已使用的内存, 单位byte
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

    /**
     * 该机器的redis部署实际使用内存, 单位byte
     */
    private Long usedMemRss;

    /**
     * 操作系统发行版本，0:centos;1:ubuntu
     */
    private Integer disType;

    /**
     * 该机器的已使用的磁盘, 单位byte
     */
    private long usedDisk;

    /**
     * 机器磁盘总大小, 单位G
     */
    private long disk;

    /**
     * 类型：如Redis机器（0），RediSSD机器（6）
     * 详见 MachineInfoEnum.TypeEnum
     */
    private int type;

    public double getCpuUsage(){
        if(cpu != 0){
            return ((double)instanceNum)/cpu;
        }
        return 0;
    }

    public long getFreeMem(){
        if(usedMemRss == null){
            return mem * 1024;
        }
        return mem * 1024 - usedMemRss/1024/1024;
    }

    public long getFreeApplyMem(){
        return mem * 1024 - applyMem/1024/1024;
    }

    public long getFreeDisk(){
        return disk * 1024 - usedDisk/1024/1024;
    }

    public void addUsedMem(long mem){
        this.usedMem += mem;
        this.usedMemRss += mem;
    }

    public void addApplyMem(long mem){
        this.applyMem += mem;
    }

    public void addUsedDisk(long disk){
        this.usedDisk += disk;
    }

    public void addInstanceNum(int instanceNum){
        this.instanceNum += instanceNum;
    }

}
