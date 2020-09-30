package com.sohu.cache.web.enums;

/**
 * 成功失败状态
 *
 * @author leifu
 * @Time 2014年10月16日
 */
public enum SuccessEnum {
    ERROR(-1),
    SUCCESS(1),
    FAIL(0),
    REPEAT(2),
    NO_REPEAT(4),
    INSTALLED(3);

    int value;

    private SuccessEnum(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public String info() {
        if (value == 3) {
            return "已安装";
        }else if (value == 2) {
            return "重复插入";
        }else if (value == 4) {
            return "不重复";
        } else if (value == 1) {
            return "成功";
        } else if (value == -1) {
            return "错误";
        } else {
            return "失败";
        }
    }
}
