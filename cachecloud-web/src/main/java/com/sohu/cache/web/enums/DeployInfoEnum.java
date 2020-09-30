package com.sohu.cache.web.enums;

/**
 * Created by rucao on 2018/10/12
 */
public enum DeployInfoEnum {
    SUCCESS("success"),
    EMPTY("empty:error"),
    EXCEPTION("exception:error"),
    PARAM_ERROR("param:error"),
    MACHINE_NUM_ERROR("machine_num:error");

    private String value;

    DeployInfoEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}