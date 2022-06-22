package com.sohu.cache.constant;

import com.sohu.cache.util.StringUtil;

/**
 * @Author: rucao
 * @Date: 2020/6/9 17:11
 */
public enum DiagnosticTypeEnum {
    SCAN_KEY(0, "scan", "扫描键"),
    BIG_KEY(1, "memoryUsed", "键内存诊断"),
    IDLE_KEY(2, "idlekey", "空闲键扫描"),
    HOT_KEY(3, "hotkey", "热点键诊断"),
    DEL_KEY(4, "deleteKey", "删除键"),
    SLOT_ANALYSIS(5, "slotAnalysis", "集群槽分析"),
    SCAN_CLEAN(6, "scanClean", "数据分析清理");

    int type;
    String desc;
    String more;

    DiagnosticTypeEnum(int type, String desc, String more) {
        this.type = type;
        this.desc = desc;
        this.more = more;
    }

    public int getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public String getMore() {
        return more;
    }

    public static int getDescKey(String desc) {
        for (DiagnosticTypeEnum diagnosticTypeEnum : DiagnosticTypeEnum.values()) {
            if (!StringUtil.isBlank(desc) && diagnosticTypeEnum.getDesc().equals(desc)) {
                return diagnosticTypeEnum.getType();
            }
        }
        return -1;
    }

    public static String getKeyDesc(int type) {
        for (DiagnosticTypeEnum diagnosticTypeEnum : DiagnosticTypeEnum.values()) {
            if (type == diagnosticTypeEnum.getType()) {
                return diagnosticTypeEnum.getDesc();
            }
        }
        return "";
    }
}
