package com.sohu.cache.util;

import com.sohu.cache.entity.InstanceInfo;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * 将实例列表转化为ip1:port1 ip2:port2
     *
     * @param instanceList
     * @return
     */
    public static String assembleInstance(List<InstanceInfo> instanceList) {
        if (instanceList.isEmpty()) {
            return null;
        }
        StringBuilder instanceBuilder = new StringBuilder();
        for (int i = 0; i < instanceList.size(); i++) {
            InstanceInfo instanceInfo = instanceList.get(i);
            if (instanceInfo.isOffline()) {
                continue;
            }
            if (i > 0) {
                instanceBuilder.append(" ");
            }
            instanceBuilder.append(instanceInfo.getIp()).append(":").append(instanceInfo.getPort());
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
