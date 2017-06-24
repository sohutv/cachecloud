package com.sohu.cache.alert.strategy;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;

import com.sohu.cache.alert.bean.AlertConfigBaseData;
import com.sohu.cache.entity.InstanceAlertConfig;
import com.sohu.cache.entity.InstanceAlertValueResult;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.redis.enums.RedisInfoEnum;

/**
 * 主从偏移量监控
 * @author leifu
 * @Date 2017年6月16日
 * @Time 下午2:34:10
 */
public class MasterSlaveOffsetAlertStrategy extends AlertConfigStrategy {

    /**
     * 格式：
     *     connected_slaves:2
     *     slave0:ip=10.10.76.151,port=6380,state=online,offset=33119690469561,lag=1
     *     slave1:ip=10.10.76.160,port=6380,state=online,offset=33119690513578,lag=0
     *     master_repl_offset:33119653194425
     */
    @Override
    public List<InstanceAlertValueResult> checkConfig(InstanceAlertConfig instanceAlertConfig, AlertConfigBaseData alertConfigBaseData) {
        Object connectedSlavesObject = getValueFromRedisInfo(alertConfigBaseData.getStandardStats(), RedisInfoEnum.connected_slaves.getValue());
        if (connectedSlavesObject == null) {
            return null;
        }
        int connectedSlaves = NumberUtils.toInt(connectedSlavesObject.toString());
        if (connectedSlaves == 0) {
            return null;
        }
        Object masterReplOffsetObject = getValueFromRedisInfo(alertConfigBaseData.getStandardStats(), RedisInfoEnum.master_repl_offset.getValue());
        if (masterReplOffsetObject == null) {
            return null;
        }
        List<InstanceAlertValueResult> instanceAlertValueResultList = new ArrayList<InstanceAlertValueResult>();
        for (int i = 0; i < connectedSlaves; i++) {
            Object slaveInfo = getValueFromRedisInfo(alertConfigBaseData.getStandardStats(), "slave" + i);
            if (slaveInfo == null) {
                continue;
            }
            String[] arr = slaveInfo.toString().split(",");
            if (arr.length < 5) {
                continue;
            }
            String state = arr[2];
            if (!"state=online".equals(state)) {
                continue;
            }
            String slaveHostPort = arr[0] + "," + arr[1];
            String slaveOffsetStr = arr[3];
            String[] slaveOffsetArr = slaveOffsetStr.split("=");
            if (slaveOffsetArr.length != 2) {
                continue;
            }
            String slaveOffset = slaveOffsetArr[1];
            long diffOffset = Math.abs(NumberUtils.toLong(masterReplOffsetObject.toString()) - NumberUtils.toLong(slaveOffset));
            boolean compareRight = isCompareDoubleRight(instanceAlertConfig, diffOffset);
            if (compareRight) {
                return null;
            }
            InstanceInfo instanceInfo = alertConfigBaseData.getInstanceInfo();
            InstanceAlertValueResult instanceAlertValueResult = new InstanceAlertValueResult(instanceAlertConfig, instanceInfo, String.valueOf(diffOffset),
                    instanceInfo.getAppId(), EMPTY);
            String otherInfo = String.format("masterOffset is %s<br/>slaveOffset  is %s<br/>%s", masterReplOffsetObject.toString(), slaveOffset, slaveHostPort);
            instanceAlertValueResult.setOtherInfo(otherInfo);
            instanceAlertValueResultList.add(instanceAlertValueResult);
        }
        return instanceAlertValueResultList;
    }

}
