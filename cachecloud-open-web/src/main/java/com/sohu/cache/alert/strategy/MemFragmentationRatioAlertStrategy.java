package com.sohu.cache.alert.strategy;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;

import com.sohu.cache.alert.bean.AlertConfigBaseData;
import com.sohu.cache.entity.InstanceAlertConfig;
import com.sohu.cache.entity.InstanceAlertValueResult;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.redis.enums.RedisInfoEnum;

/**
 * 内存碎片率
 * 
 * @author leifu
 * @Date 2017年6月16日
 * @Time 下午2:34:10
 */
public class MemFragmentationRatioAlertStrategy extends AlertConfigStrategy {
    
    /**
     * 实例最小500MB才进行内存碎片率检查，否则价值不是很大
     */
    private final static long MIN_CHECK_MEMORY = 500 * 1024 * 1024;
    
    @Override
    public List<InstanceAlertValueResult> checkConfig(InstanceAlertConfig instanceAlertConfig, AlertConfigBaseData alertConfigBaseData) {
        // 检查内存
        Object usedMemoryObject = getValueFromRedisInfo(alertConfigBaseData.getStandardStats(), RedisInfoEnum.used_memory.getValue());
        long usedMemory = NumberUtils.toLong(usedMemoryObject.toString());
        if (usedMemory < MIN_CHECK_MEMORY) {
            return null;
        }
        
        // 内存碎片率
        Object memFragmentationRatioObject = getValueFromRedisInfo(alertConfigBaseData.getStandardStats(), RedisInfoEnum.mem_fragmentation_ratio.getValue());
        if (memFragmentationRatioObject == null) {
            return null;
        }
        
        // 关系比对
        double memFragmentationRatio = NumberUtils.toDouble(memFragmentationRatioObject.toString());
        boolean compareRight = isCompareDoubleRight(instanceAlertConfig, memFragmentationRatio);
        if (compareRight) {
            return null;
        }
        InstanceInfo instanceInfo = alertConfigBaseData.getInstanceInfo();
        InstanceAlertValueResult instanceAlertValueResult = new InstanceAlertValueResult(instanceAlertConfig, instanceInfo, String.valueOf(memFragmentationRatio),
                instanceInfo.getAppId(), EMPTY);
        instanceAlertValueResult.setOtherInfo(String.format("内存使用为%s MB", String.valueOf(changeByteToMB(usedMemory))));
        return Arrays.asList();
    }

}
