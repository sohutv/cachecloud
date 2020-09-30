package com.sohu.cache.alert.strategy;

import com.sohu.cache.alert.bean.AlertConfigBaseData;
import com.sohu.cache.entity.InstanceAlertConfig;
import com.sohu.cache.entity.InstanceAlertValueResult;
import com.sohu.cache.entity.InstanceInfo;
import org.apache.commons.lang.math.NumberUtils;

import java.util.Arrays;
import java.util.List;

/**
 * cpu使用率监控
 * @author rucao
 */
public class MinuteUsedCpuAlertStrategy extends AlertConfigStrategy {

    @Override
    public List<InstanceAlertValueResult> checkConfig(InstanceAlertConfig instanceAlertConfig, AlertConfigBaseData alertConfigBaseData) {
        Object usedCpuSysObject = getValueFromDiffInfo(alertConfigBaseData.getStandardStats(), instanceAlertConfig.getAlertConfig());
        if (usedCpuSysObject == null) {
            return null;
        }

        // 关系比对
        double usedCpuSys = NumberUtils.toDouble(usedCpuSysObject.toString());
        boolean compareRight = isCompareDoubleRight(instanceAlertConfig, usedCpuSys);
        if (compareRight) {
            return null;
        }
        InstanceInfo instanceInfo = alertConfigBaseData.getInstanceInfo();
        InstanceAlertValueResult instanceAlertValueResult = new InstanceAlertValueResult(instanceAlertConfig, instanceInfo, String.valueOf(usedCpuSys), instanceInfo.getAppId(), "秒");
        return Arrays.asList(instanceAlertValueResult);
    }

}
