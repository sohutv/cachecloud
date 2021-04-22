package com.sohu.cache.web.enums;

/**
 * @Author: rucao
 * @Date: 2021/1/11 下午5:45
 */
public enum AppImportStatusEnum {
    PREPARE(0, "准备", "应用导入-未开始"),
    START(1, "进行中...", "应用导入-开始"),
    ERROR(2, "error", "应用导入-出错"),

    VERSION_BUILD_START(11, "进行中...", "新建redis版本-进行中"),
    VERSION_BUILD_ERROR(12, "error", "新建redis版本-出错"),
    VERSION_BUILD_END(20, "成功", "新建redis版本-完成"),

    APP_BUILD_INIT(21, "准备就绪", "新建redis应用-准备就绪"),
    APP_BUILD_START(22, "进行中...", "新建redis应用-进行中"),
    APP_BUILD_ERROR(23, "error", "新建redis应用-出错"),
    APP_BUILD_END(30, "成功", "新建redis应用-完成"),

    MIGRATE_INIT(31, "准备就绪", "数据迁移-准备就绪"),
    MIGRATE_START(32, "进行中...", "数据迁移-进行中"),
    MIGRATE_ERROR(33, "error", "数据迁移-出错"),
    MIGRATE_END(3, "成功", "应用导入-成功"),
    ;
    private int status;
    private String desc;
    private String info;

    AppImportStatusEnum(int status, String desc, String info) {
        this.status = status;
        this.desc = desc;
        this.info = info;
    }

    public int getStatus() {
        return status;
    }

    public String getInfo() {
        return info;
    }
}
