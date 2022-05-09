package com.sohu.cache.alert.strategy;

import com.sohu.cache.alert.bean.AlertConfigBaseData;
import com.sohu.cache.entity.InstanceAlertConfig;
import com.sohu.cache.entity.InstanceAlertValueResult;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.redis.enums.RedisInfoEnum;
import org.apache.commons.lang.math.NumberUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @Author: rucao
 * @Date: 2021/6/9 上午11:03
 */
public class MinuteUsedCpuSysStrategy extends AlertConfigStrategy{

    @Override
    public List<InstanceAlertValueResult> checkConfig(InstanceAlertConfig instanceAlertConfig, AlertConfigBaseData alertConfigBaseData) {
        Object object = getValueFromDiffInfo(alertConfigBaseData.getStandardStats(), RedisInfoEnum.used_cpu_sys.getValue());
        if (object == null) {
            return null;
        }
        double min_used_cpu_sys_err= NumberUtils.toDouble(object.toString());
        boolean compareRight = isCompareDoubleRight(instanceAlertConfig, min_used_cpu_sys_err);
        if (compareRight) {
            return null;
        }
        InstanceInfo instanceInfo = alertConfigBaseData.getInstanceInfo();
        return Arrays.asList(new InstanceAlertValueResult(instanceAlertConfig, instanceInfo, String.valueOf(min_used_cpu_sys_err),
                instanceInfo.getAppId(), EMPTY));
    }
}
