package com.sohu.cache.entity;

import lombok.Data;

/**
 * @author wenruiwu
 * @create 2019/11/13 11:09
 * @description
 */
@Data
public class AppTopMemFragRatio extends AppDesc{

    /**
     * 应用实例数
     */
    private int instanceCount;      //应用实例数
    /**
     * 平均碎片率
     */
    private double avgMemFragRatio; //平均碎片率
    /**
     * 内存使用率
     */
    private double memUsedRatio;        //内存使用率百分比

    /**
     * 异常数
     */
    private int exceptionCount;

}
