package com.sohu.cache.web.enums;

/**
 * Created by chenshi on 2019/4/19.
 */
public enum NodeEnum {

    REDIS_NODE(1),
    SENTINEL_NODE(2),
    TWEMPROXY_NODE(3),
    PIKA_NODE(4);

    private int value;

    private NodeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
