package com.sohu.cache.alert.strategy;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.sohu.cache.alert.bean.AlertConfigBaseData;
import com.sohu.cache.entity.InstanceAlertConfig;
import com.sohu.cache.entity.InstanceAlertValueResult;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.entity.StandardStats;
import com.sohu.cache.redis.enums.InstanceAlertCompareTypeEnum;
import com.sohu.cache.util.JsonUtil;

/**
 * @author leifu
 * @Date 2017年6月2日
 * @Time 下午3:24:53
 */
public abstract class AlertConfigStrategy {
    
    protected final static String MB_STRING = "MB";
    protected final static String EMPTY = "";

    /**
     * 检查配置
     * 
     * @param instanceAlertConfig
     * @param alertConfigBaseData
     */
    public abstract List<InstanceAlertValueResult> checkConfig(InstanceAlertConfig instanceAlertConfig,
            AlertConfigBaseData alertConfigBaseData);

    /**
     * 比较long类型
     * 
     * @param instanceAlertConfig 报警配置
     * @param currentValue 当前值
     * @return
     */
    protected boolean isCompareLongRight(InstanceAlertConfig instanceAlertConfig, long currentValue) {
        long alertValue = NumberUtils.toLong(instanceAlertConfig.getAlertValue());
        int compareType = instanceAlertConfig.getCompareType();
        if (compareType == InstanceAlertCompareTypeEnum.LESS_THAN.getValue() && currentValue < alertValue) {
            return false;
        } else if (compareType == InstanceAlertCompareTypeEnum.MORE_THAN.getValue() && currentValue > alertValue) {
            return false;
        } else if (compareType == InstanceAlertCompareTypeEnum.EQUAL.getValue() && currentValue == alertValue) {
            return false;
        } else if (compareType == InstanceAlertCompareTypeEnum.NOT_EQUAL.getValue() && currentValue != alertValue) {
            return false;
        }
        return true;
    }

    /**
     * 比较int类型
     * 
     * @param instanceAlertConfig 报警配置
     * @param currentValue 当前值
     * @return
     */
    protected boolean isCompareIntRight(InstanceAlertConfig instanceAlertConfig, int currentValue) {
        int alertValue = NumberUtils.toInt(instanceAlertConfig.getAlertValue());
        int compareType = instanceAlertConfig.getCompareType();
        if (compareType == InstanceAlertCompareTypeEnum.LESS_THAN.getValue() && currentValue < alertValue) {
            return false;
        } else if (compareType == InstanceAlertCompareTypeEnum.MORE_THAN.getValue() && currentValue > alertValue) {
            return false;
        } else if (compareType == InstanceAlertCompareTypeEnum.EQUAL.getValue() && currentValue == alertValue) {
            return false;
        } else if (compareType == InstanceAlertCompareTypeEnum.NOT_EQUAL.getValue() && currentValue != alertValue) {
            return false;
        }
        return true;
    }
    
    /**
     * 比较double类型
     * 
     * @param instanceAlertConfig 报警配置
     * @param currentValue 当前值
     * @return
     */
    protected boolean isCompareDoubleRight(InstanceAlertConfig instanceAlertConfig, double currentValue) {
        double alertValue = NumberUtils.toDouble(instanceAlertConfig.getAlertValue());
        int compareType = instanceAlertConfig.getCompareType();
        if (compareType == InstanceAlertCompareTypeEnum.LESS_THAN.getValue() && currentValue < alertValue) {
            return false;
        } else if (compareType == InstanceAlertCompareTypeEnum.MORE_THAN.getValue() && currentValue > alertValue) {
            return false;
        } else if (compareType == InstanceAlertCompareTypeEnum.EQUAL.getValue() && currentValue == alertValue) {
            return false;
        } else if (compareType == InstanceAlertCompareTypeEnum.NOT_EQUAL.getValue() && currentValue != alertValue) {
            return false;
        }
        return true;
    }

    /**
     * 比较字符串类型
     * 
     * @param instanceAlertConfig 报警配置
     * @param currentValue 当期值
     * @return
     */
    protected boolean isCompareStringRight(InstanceAlertConfig instanceAlertConfig, String currentValue) {
        String alertValue = instanceAlertConfig.getAlertValue();
        int compareType = instanceAlertConfig.getCompareType();
        if (compareType == InstanceAlertCompareTypeEnum.EQUAL.getValue() && currentValue.equals(alertValue)) {
            return false;
        } else if (compareType == InstanceAlertCompareTypeEnum.NOT_EQUAL.getValue()
                && !currentValue.equals(alertValue)) {
            return false;
        }
        return true;
    }

    /**
     * 生成instance级别报警文案
     * @param instanceAlertConfig
     * @param instanceInfo
     * @param currentValue
     * @param unit
     * @return
     */
    protected String genInstanceAlertText(InstanceAlertConfig instanceAlertConfig, InstanceInfo instanceInfo,
            String currentValue, String unit) {
        String configKey = instanceAlertConfig.getAlertConfig();
        String configValue = instanceAlertConfig.getAlertValue();
        String compareTypeInfo = InstanceAlertCompareTypeEnum
                .getInstanceAlertCompareTypeEnum(instanceAlertConfig.getCompareType()).getInfo();
        StringBuilder alertText = new StringBuilder();
        alertText.append(instanceInfo.getHostPort());
        alertText.append(",报警项:").append(configKey);
        alertText.append("=").append(currentValue).append(unit).append(",");
        alertText.append(compareTypeInfo);
        alertText.append("报警阈值:").append(configValue).append(unit);
        return alertText.toString();
    }

    /**
     * 获取全量统计项中的内容
     * @param redisInfo
     * @param attribute
     * @return
     */
    protected static Object getValueFromRedisInfo(StandardStats standardStats, String attribute) {
        if (standardStats == null) {
            return null;
        }
        // 转换成Map
        Map<String, Object> infoMap = JsonUtil.fromJson(standardStats.getInfoJson(), Map.class);
        if (MapUtils.isEmpty(infoMap)) {
            return null;
        }
        for (Entry<String, Object> entry : infoMap.entrySet()) {
            Object object = entry.getValue();
            // 转换成Map<String, Map<String,Object>>
            if (!(object instanceof Map)) {
                continue;
            }
            Map<String, Object> sectionInfoMap = (Map<String, Object>) object;
            if (sectionInfoMap != null && sectionInfoMap.containsKey(attribute)) {
                return MapUtils.getObject(sectionInfoMap, attribute);
            }
        }
        return null;
    }
    
    /**
     * 获取差值统计项中的内容
     * @param redisInfo
     * @param attribute
     * @return
     */
    protected static Object getValueFromDiffInfo(StandardStats standardStats, String attribute) {
        if (standardStats == null) {
            return null;
        }
        Map<String, Object> diffInfoMap = JsonUtil.fromJson(standardStats.getDiffJson(), Map.class);
        if (MapUtils.isEmpty(diffInfoMap)) {
            return null;
        }
        return MapUtils.getObject(diffInfoMap, attribute);
    }
    
    /**
     * 获取cluster info统计项中的内容
     * @param redisInfo
     * @param attribute
     * @return
     */
    protected static Object getValueFromClusterInfo(StandardStats standardStats, String attribute) {
        if (standardStats == null) {
            return null;
        }
        Map<String, Object> clusterInfoMap = JsonUtil.fromJson(standardStats.getClusterInfoJson(), Map.class);
        if (MapUtils.isEmpty(clusterInfoMap)) {
            return null;
        }
        return MapUtils.getObject(clusterInfoMap, attribute);
    }
    
    /**
     * 把字节变为兆
     * @param value
     * @return
     */
    protected long changeByteToMB(long value) {
        return value / 1024 / 1024;
    }

}
