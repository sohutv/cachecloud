package com.sohu.cache.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 * 系统用户所属业务组
 * 
 * @author zengyizhao
 */
@Data
@ApiModel
@RequiredArgsConstructor(staticName = "of")
@NoArgsConstructor
@AllArgsConstructor
public class AppBiz implements Serializable {

    /**
     * 自增id
     */
    @ApiModelProperty(value = "业务组id",hidden = true)
    private Long id;

    /**
     * 业务组名称
     */
    @NonNull
    @ApiModelProperty(value = "业务组名称",required = true)
    private String name;

    /**
     * 备注
     */
    @NonNull
    @ApiModelProperty(value = "备注")
    private String bizDesc;



}
