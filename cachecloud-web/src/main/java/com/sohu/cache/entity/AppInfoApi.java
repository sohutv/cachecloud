package com.sohu.cache.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 构建应用对象
 *
 * @author chenshi
 */
@Data
@ApiModel
public class AppInfoApi {

    /**
     * 应用名称
     */
    @ApiModelProperty(value = "应用名称", required = true)
    private String name;

    /**
     * 应用介绍
     */
    @ApiModelProperty(value = "应用详情", required = true)
    private String desc;

    /**
     * 申请人信息
     */
    @ApiModelProperty(value = "申请人信息", required = true)
    private AppUser user;

    /**
     * 机器列表信息
     */
    @ApiModelProperty(value = "应用部署机器信息", required = true)
    private List<String> iplist;

    /**
     * 应用内存总量
     */
    @ApiModelProperty(value = "应用申请总内存，单位G", required = true)
    private int memTotalSize;

    /**
     * 是否有从节点 0:无 1:有
     */
    @ApiModelProperty(value = "是否有从节点 0:无 1:有", required = true)
    private int hasSlave;

    /**
     * 应用申请类型: 2:cluster 5:sentinel 6:standalone
     */
    @ApiModelProperty(value = "应用申请类型: 2:cluster 5:sentinel 6:standalone", required = true)
    private int type;

    /**
     * 是否测试: 0:测试 1:正式
     */
    @ApiModelProperty(value = "是否测试: 0:正式 1:测试", required = true)
    private int isTest;

    /**
     * redis version版本
     */
    @ApiModelProperty(value = "redis version版本,可选 redis-3.0.7/redis-3.2.12/redis-4.0.11", required = true)
    private String redisVersion;

    /**
     * 机房名称
     */
    @ApiModelProperty(value = "机房名称,亦庄/兆维/北显/上海归侨/腾讯黑石", required = false)
    private String room;

    @ApiModelProperty(value = "redis实例数量", hidden = true)
    private int instanceNum;

    @ApiModelProperty(value = "sentinel实例数量,sentinel应用可传，默认值:3")
    private int sentinelNum;
}
