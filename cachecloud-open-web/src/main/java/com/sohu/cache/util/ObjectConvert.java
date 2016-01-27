package com.sohu.cache.util;

import com.sohu.cache.entity.InstanceInfo;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 对象转换工具类
 *
 * User: lingguo
 * Date: 14-5-29
 * Time: 下午6:17
 */
public class ObjectConvert {
    private static Logger logger = LoggerFactory.getLogger(ObjectConvert.class);

    /**
     * 将ip和port连接起来
     *
     * @param ip
     * @param port
     * @return
     */
    public static String linkIpAndPort(String ip, int port) {
        return ip + ":" + port;
    }

    /**
     * 将实例列表根据主从关系配对，主从之间以逗号分隔，分片之间以空格分隔，如：
     *   "xx.xx.xx.xx:1008,xx.xx.xx.xx:1009 xx.xx.xx.xx:1201,xx.xx.xx.xx:1301"
     *
     * @param instanceList
     * @return
     */
    public static String assembleInstance(List<InstanceInfo> instanceList) {
        if (instanceList.isEmpty()) {
            return null;
        }
        // 将主从实例分离
        List<InstanceInfo> masterInstList = new ArrayList<InstanceInfo>();
        List<InstanceInfo> slaveInstList = new ArrayList<InstanceInfo>();
        for (InstanceInfo instanceInfo: instanceList) {
            if (instanceInfo.getParentId() == 0) {
                masterInstList.add(instanceInfo);
            } else {
                slaveInstList.add(instanceInfo);
            }
        }

        // 主从实例配对，并构成"主,从 主,从"的形式返回(存在一主多从的情况)
        StringBuilder instanceBuilder = new StringBuilder();
        for (InstanceInfo master : masterInstList) {
            instanceBuilder.append(master.getIp()).append(":").append(master.getPort());
            for (InstanceInfo slave : slaveInstList) {
                if (master.getId() == slave.getParentId()) {
                    instanceBuilder.append(",");
                    instanceBuilder.append(slave.getIp()).append(":").append(slave.getPort());
                }
            }
            instanceBuilder.append(" ");
        }
        return StringUtils.trim(instanceBuilder.toString());
    }

    /**
     * 将百分比的比值转换为对应浮点数
     *
     * @param value         百分比表示
     * @param defaultVal    默认值
     * @return              转换后的浮点表示
     */
    public static double percentToDouble(String value, double defaultVal) {
        double result = defaultVal;
        if (value == null || value.isEmpty()) {
            return result;
        }
        try {
            result = Double.valueOf(value.substring(0, value.length() - 1));
        } catch (NumberFormatException e) {
            logger.error(e.getMessage(), e);
        }

        return result;
    }

}
