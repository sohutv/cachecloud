package com.sohu.cache.inspect;

/**
 * Created by yijunzhang on 15-1-20.
 */
public enum InspectParamEnum {
    /**
     * 分组字段:
     * HostInspectHandler 表示host
     * AppInspectHandler 表示appId
     */
    SPLIT_KEY("split_key"),
    INSTANCE_LIST("instance_list");

    private String value;

    InspectParamEnum(String value) {
        this.value = value;
    }

    public String value(){
        return value;
    }

}
