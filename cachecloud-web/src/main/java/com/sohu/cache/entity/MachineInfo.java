package com.sohu.cache.entity;


import com.sohu.cache.constant.MachineInfoEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 机器的属性信息
 * <p>
 */
@Data
@ApiModel
public class MachineInfo {
    /**
     * 机器id
     */
    @ApiModelProperty(hidden = true)
    private long id;

    /**
     * ssh用户名
     */
    @ApiModelProperty(hidden = true)
    private String sshUser;

    /**
     * ssh密码
     */
    @ApiModelProperty(hidden = true)
    private String sshPasswd;

    /**
     * ip地址
     */
    @ApiModelProperty(value = "机器ip", required = true)
    private String ip;

    /**
     * 机房
     */
    @ApiModelProperty(hidden = true)
    private String room;

    /**
     * 内存，单位G
     */
    @ApiModelProperty(value = "内存，单位G", required = true)
    private int mem;

    /**
     * cpu数量
     */
    @ApiModelProperty(value = "cpu核数", required = true)
    private int cpu;

    /**
     * 磁盘空间, 单位G
     */
    @ApiModelProperty(value = "磁盘空间",required = true)
    private int disk;

    /**
     * 是否虚机，0否，1是
     */
    @ApiModelProperty(value = "是否虚拟机", required = true)
    private int virtual;

    /**
     * 宿主机ip
     */
    @ApiModelProperty(value = "宿主机ip（虚机需要填写）", required = true)
    private String realIp;

    /**
     * 上线时间
     */
    @ApiModelProperty(hidden = true)
    private Date serviceTime;

    /**
     * 故障次数
     */
    @ApiModelProperty(hidden = true)
    private int faultCount;

    /**
     * 修改时间
     */
    @ApiModelProperty(hidden = true)
    private Date modifyTime;

    /**
     * 是否启用报警，0否，1是
     */
    @ApiModelProperty(hidden = true)
    private int warn;

    /**
     * 是否可用，MachineInfoEnum.AvailableEnum
     */
    @ApiModelProperty(hidden = true)
    private int available;

    /**
     * 机器类型：详见MachineInfoEnum.TypeEnum
     */
    @ApiModelProperty(hidden = true)
    private int type;

    /**
     * groupId
     */
    @ApiModelProperty(hidden = true)
    private int groupId;

    /**
     * 额外说明:(例如本机器有其他web或者其他服务)
     */
    @ApiModelProperty(value = "备注说明")
    private String extraDesc;

    @ApiModelProperty(value = "如是专用机器,请填写项目名称")
    private String projectName;

    /**
     * 是否收集服务器信息，0否，1是
     */
    @ApiModelProperty(hidden = true)
    private int collect;

    /**
     * redis版本安装情况 版本号#安装标识  -1:获取安装信息异常 0:未安装 1:安装成功
     */
    @ApiModelProperty(hidden = true)
    private String versionInstall;

    /**
     * 使用类型：Redis专用机器（0），Redis测试机器（1），混合部署机器（2）
     */
    @ApiModelProperty(value = "使用类型：Redis专用机器（0），Redis测试机器（1），混合部署机器（2），Redis sentienl部署（3）", required = true)
    private int useType;

    @ApiModelProperty(value = "机器是否分配，1是0否")
    private int isAllocating;

    /**
     * 是否k8s容器：0:不是 1:是
     */
    @ApiModelProperty(value = "是否k8s容器：0:不是 1:是", required = false)
    private int k8sType;

    @ApiModelProperty(value = "pod变更时间，单位 ms", required = false)
    private long podUpdateTime;

    @ApiModelProperty(value = "机架信息", required = false)
    private String rack;

    /**
     * 判断机器是否已经下线
     *
     * @return
     */
    public boolean isOffline() {
        return MachineInfoEnum.AvailableEnum.NO.getValue() == this.available;
    }

    /**
     * 是否是云主机
     *
     * @return
     */
    public boolean isYunMachine() {
        //return isYun == 1;
        return false;
    }

    public boolean isK8sMachine(int k8sType) {
        if (k8sType == 1) {
            return true;
        }
        return false;
    }

    /**
     * 时间格式化
     */
    public String getUpdateTimeFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(modifyTime);
    }

    public Date getModifyTime() {
        return (Date) modifyTime.clone();
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = (Date) modifyTime.clone();
    }

    public Date getServiceTime() {
        return (Date) serviceTime.clone();
    }

    public void setServiceTime(Date serviceTime) {
        this.serviceTime = (Date) serviceTime.clone();
    }
}