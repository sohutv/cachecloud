package com.sohu.cache.entity;

import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by yijunzhang
 */
@Data
public class MachineStats{
    private long id;

    private long hostId;

    private String ip;

    private String cpuUsage;

    private String load;

    private String traffic;

    private String memoryUsageRatio;

    /**
     * 如果机器是物理机/虚机，收集内存占用空间正常;如果是容器，收集可能不是实际内存占用空间
     */
    private String memoryFree;

    private String memoryTotal;

    private int memoryAllocated;
    /**
     * 机器入库内存 MB
     */
    private int machineMemory;
    /**
     * 分配内存 MB
     */
    private int maxMemory;
    /**
     * 机器实例使用内存总和 MB
     */
    private int usedMemory;

    private MachineInfo info;

    private MachineMemInfo machineMemInfo;

    private String versionInfo;

    private Map<String/**挂载点*/, String/**使用百分比*/> diskUsageMap;

    /**
     * 实例个数
     */
    private int instanceCount;



    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date modifyTime;

    /**
     * Redis是否全部安装成功 0:未全部安装  1:全部安装成功
     */
    private int isInstall;

    private String diskTotal;

    private String diskAvailable;

    private String diskUsageRatio;

    /**
     * 时间格式化
     */
    public String getUpdateTimeFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(modifyTime);
    }

    //统计数据是否有效
    public boolean validate() {
        if (StringUtils.isBlank(load)
                && StringUtils.isBlank(cpuUsage)
                && StringUtils.isBlank(memoryFree)
                && StringUtils.isBlank(memoryTotal)) {
            return false;
        }
        return true;
    }

    public Date getCreateTime() {
        if(null == createTime){
            return null;
        }
        return (Date) createTime.clone();
    }

    public void setCreateTime(Date createTime) {
        if(null == createTime){
            this.createTime = null;
        }else{
            this.createTime = (Date) createTime.clone();
        }
    }

    public Date getModifyTime() {
        if(null == modifyTime){
            return null;
        }
        return (Date) modifyTime.clone();
    }

    public void setModifyTime(Date modifyTime) {
        if(null == modifyTime){
            this.modifyTime = null;
        }else{
            this.modifyTime = (Date) modifyTime.clone();
        }
    }

}
