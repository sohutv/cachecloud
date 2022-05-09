package com.sohu.cache.web.enums;

import java.util.ArrayList;
import java.util.List;


/**
 * @Author: zengyizhao
 * @DateTime: 2021/9/22 13:34
 * @Description: 报警重要程度配置
 */
public enum ImportantLevelTypeEnum {
    NORMAL(0, "一般"),
    IMPORTANT(1, "重要"),
    URGENT(2, "紧急");


    private final static List<ImportantLevelTypeEnum> instanceAlertTypeEnumList = new ArrayList<ImportantLevelTypeEnum>();
    static {
        for (ImportantLevelTypeEnum instanceAlertTypeEnum : ImportantLevelTypeEnum.values()) {
            instanceAlertTypeEnumList.add(instanceAlertTypeEnum);
        }
    }

    private int type;

    private String info;

    private ImportantLevelTypeEnum(int type, String info) {
        this.type = type;
        this.info = info;
    }

    public int getType() {
        return type;
    }

    public String getInfo() {
        return info;
    }

    public static String getInfoByType(int type){
        for(ImportantLevelTypeEnum importantLevelTypeEnum: instanceAlertTypeEnumList){
            if(importantLevelTypeEnum.getType() == type){
                return importantLevelTypeEnum.getInfo();
            }
        }
        return ImportantLevelTypeEnum.NORMAL.getInfo();
    }
}