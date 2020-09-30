package com.sohu.tv.cachecloud.client.redis.crossroom.enums;

/**
 * @author leifu
 * @Date 2016年4月25日
 * @Time 下午3:33:24
 */
public enum DataStatusEnum {

    SUCCESS(1, "OK"),
    FAIL(2, "FAIL");

    private int status;

    private String info;

    private DataStatusEnum(int status, String info) {
        this.status = status;
        this.info = info;
    }

    public int getStatus() {
        return status;
    }

    public String getInfo() {
        return info;
    }

}
