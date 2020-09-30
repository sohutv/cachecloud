package com.sohu.cache.entity;

import lombok.Data;

/**
 * <p>
 * Description: 机房信息
 * </p>
 *
 * @author chenshi
 */
@Data
public class MachineRoom {

    /**
     * 机房id
     */
    private int id;

    /**
     * 机房名称
     */
    private String name;

    /**
     * 是否有效 0:无效 1:有效
     */
    private int status;

    /**
     * 机房信息
     */
    private String desc;

    /**
     * 网段信息
     */
    private String ipNetwork;

    /**
     * 运营商
     */
    private String operator;

    public MachineRoom() {
    }

    public MachineRoom(String name, int status, String desc, String ipNetwork, String operator) {
        this.name = name;
        this.status = status;
        this.desc = desc;
        this.ipNetwork = ipNetwork;
        this.operator = operator;
    }

}
