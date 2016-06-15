package com.sohu.cache.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录类型
 * 
 * @author leifu
 * @Date 2016年6月15日
 * @Time 下午12:03:05
 */
public enum UserLoginTypeEnum {

    SESSION(1, "session"),
    COOKIE(2, "cookie");

    private int type;

    private String desc;
    
    private final static Map<Integer, UserLoginTypeEnum> MAP = new HashMap<Integer, UserLoginTypeEnum>();
    static {
        for (UserLoginTypeEnum userLoginTypeEnum : UserLoginTypeEnum.values()) {
            MAP.put(userLoginTypeEnum.getType(), userLoginTypeEnum);
        }
    }

    public static UserLoginTypeEnum getLoginTypeEnum(int type) {
        return MAP.get(type);
    }
    
    private UserLoginTypeEnum(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public int getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

}
