package com.sohu.cache.alert.strategy;

import com.sohu.cache.alert.bean.AlertConfigBaseData;
import com.sohu.cache.entity.InstanceAlertConfig;
import com.sohu.cache.entity.InstanceAlertValueResult;
import com.sohu.cache.entity.InstanceInfo;
import org.apache.commons.lang.math.NumberUtils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * aof当前尺寸检测
 * @author leifu
 * @Date 2017年6月16日
 * @Time 下午2:34:10
 */
public class DefaultCommonAlertStrategy extends AlertConfigStrategy {

    @Override
    public List<InstanceAlertValueResult> checkConfig(InstanceAlertConfig instanceAlertConfig, AlertConfigBaseData alertConfigBaseData) {
        Object object = getValueFromRedisInfo(alertConfigBaseData.getStandardStats(), instanceAlertConfig.getAlertConfig());
        if (object == null) {
            return null;
        }
        if(judgeNumber(object)){
            if(judegNumberIsDouble(object)){
                double currentValue= NumberUtils.toDouble(object.toString());
                boolean compareRight = isCompareDoubleRight(instanceAlertConfig, currentValue);
                if (compareRight) {
                    return null;
                }
            }else{
                long currentValue = NumberUtils.toLong(object.toString());
                boolean compareRight = isCompareLongRight(instanceAlertConfig, currentValue);
                if (compareRight) {
                    return null;
                }
            }
        }else{
            String currentValue = object.toString();
            boolean compareRight = isCompareStringRight(instanceAlertConfig, currentValue);
            if (compareRight) {
                return null;
            }
        }
        InstanceInfo instanceInfo = alertConfigBaseData.getInstanceInfo();
        return Arrays.asList(new InstanceAlertValueResult(instanceAlertConfig, instanceInfo, object.toString(),
                instanceInfo.getAppId(), EMPTY));
    }

    private boolean judgeNumber(Object object){
        Pattern numberPattern = Pattern.compile("^-?(([0-9]|([1-9][0-9]*))(\\.[0-9]+)?)$");
        Matcher matcher = numberPattern.matcher(object.toString());
        return matcher.matches();
    }

    private boolean judegNumberIsDouble(Object object){
        return object.toString().contains(".");
    }

}
