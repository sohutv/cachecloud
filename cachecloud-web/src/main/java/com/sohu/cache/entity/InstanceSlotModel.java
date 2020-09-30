package com.sohu.cache.entity;

import lombok.Data;

import java.util.List;

/**
 * 实例slot
 *
 * @author leifu
 */
@Data
public class InstanceSlotModel {
    /**
     * slot分布，例如： 0-4096 或者0-8 9-4096
     */
    private List<String> slotDistributeList;

    /**
     * slot列表
     */
    private List<Integer> slotList;

    /**
     * ip
     */
    private String host;

    /**
     * 端口
     */
    private int port;

}
