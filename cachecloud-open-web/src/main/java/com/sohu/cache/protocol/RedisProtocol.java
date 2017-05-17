package com.sohu.cache.protocol;

import com.sohu.cache.web.util.DateUtil;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

/**
 * Created by yijunzhang on 14-11-26.
 */
public class RedisProtocol {

    private static final String RUN_SHELL = "redis-server %s > " + MachineProtocol.LOG_DIR + "redis-%d-%s.log 2>&1 &";

    private static final String SENTINEL_SHELL = "redis-server %s --sentinel > " + MachineProtocol.LOG_DIR + "redis-sentinel-%d-%s.log 2>&1 &";

    private static final String CLUSTER_CONFIG = "redis-cluster-%d.conf";

    private static final String COMMON_CONFIG = "redis-sentinel-%d.conf";

    public static String getRunShell(int port, boolean isCluster) {
        return String.format(RUN_SHELL, MachineProtocol.CONF_DIR + getConfig(port, isCluster), port, DateUtil.formatYYYYMMddHHMM(new Date()));
    }

    public static String getSentinelShell(int port) {
        return String.format(SENTINEL_SHELL, MachineProtocol.CONF_DIR + getConfig(port, false), port, DateUtil.formatYYYYMMddHHMM(new Date()));
    }

    public static String getExecuteCommandShell(String host, int port, String password, String command) {
    		StringBuffer shell = new StringBuffer();
    		shell.append(String.format("redis-cli -h %s -p %s", host, port));
    		if (StringUtils.isNotBlank(password)) {
    			shell.append(String.format(" -a %s", password));
    		}
    		shell.append(String.format(" --raw %s", command));
    		return shell.toString();
    }

    public static String getConfig(int port, boolean isCluster) {
        if (isCluster) {
            return String.format(CLUSTER_CONFIG, port);
        } else {
            return String.format(COMMON_CONFIG, port);
        }
    }

}
