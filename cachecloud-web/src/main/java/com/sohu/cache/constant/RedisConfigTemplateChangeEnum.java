package com.sohu.cache.constant;

/**
 * 配置模板改变行为枚举
 * 
 * @author leifu
 * @Date 2016年7月27日
 * @Time 下午3:51:13
 */
public enum RedisConfigTemplateChangeEnum {

    UPDATE(1, "更新"),
    ADD(2, "添加"),
    DELETE(3, "删除");

    private int index;

    private String info;

    private RedisConfigTemplateChangeEnum(int index, String info) {
        this.index = index;
        this.info = info;
    }

    public int getIndex() {
        return index;
    }

    public String getInfo() {
        return info;
    }

}
