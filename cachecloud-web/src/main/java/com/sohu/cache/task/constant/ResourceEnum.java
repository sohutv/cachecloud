package com.sohu.cache.task.constant;

/**
 * Created by chenshi on 2020/7/6.
 */
public enum ResourceEnum {

    ALL(0, "所有资源"),
    Repository(1, "仓库管理"),
    SCRIPT(2, "脚本管路"),
    REDIS(3, "Redis资源管理"),
    SSHKEY(4, "sshkey管理"),
    DOCKERFILE(5, "镜像管理"),
    DIR(6, "目录管理"),
    TOOL(7, "迁移工具管理");

    private int value;

    private String desc;

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    ResourceEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

}
