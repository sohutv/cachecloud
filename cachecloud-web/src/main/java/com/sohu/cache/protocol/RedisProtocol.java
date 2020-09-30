package com.sohu.cache.protocol;

import com.sohu.cache.web.util.DateUtil;
import org.apache.commons.lang.StringUtils;

import java.util.Date;

/**
 * Created by yijunzhang on 14-11-26.
 */
public class RedisProtocol {

    private static final String RUN_SHELL_VERSION = "%s/src/redis-server %s > " + MachineProtocol.LOG_DIR + "redis-%d-%s.log 2>&1 &";

    private static final String SENTINEL_SHELL_VERSION = "%s/src/redis-sentinel %s --sentinel > " + MachineProtocol.LOG_DIR + "redis-sentinel-%d-%s.log 2>&1 &";

    private static final String K8S_RUN_SHELL_VERSION = "%s/src/redis-server %s > %sredis-%d-%s.log 2>&1 &";

    private static final String K8S_SENTINEL_SHELL_VERSION = "%s/src/redis-sentinel %s --sentinel > %sredis-sentinel-%d-%s.log 2>&1 &";

    private static final String CLUSTER_CONFIG = "redis-cluster-%d.conf";

    private static final String COMMON_CONFIG = "redis-sentinel-%d.conf";

    /**
     * 2018-08-28 根据不同版本路径启动redis-cluster
     *
     * @param port
     * @param isCluster
     * @param dir       redis启动路径
     * @return 启动redis命令
     */
    public static String getRunShellByVersion(int port, boolean isCluster, String dir) {
        return String.format(RUN_SHELL_VERSION, dir, MachineProtocol.CONF_DIR + getConfig(port, isCluster), port, DateUtil.formatYYYYMMddHHMM(new Date()));
    }

    /**
     * 2018-08-28 根据不同版本路径启动redis-sentinel
     *
     * @param port
     * @param dir  redis启动路径
     * @return 启动redis命令
     */
    public static String getSentinelShellByVersion(int port, String dir) {
        return String.format(SENTINEL_SHELL_VERSION, dir, MachineProtocol.CONF_DIR + getConfig(port, false), port, DateUtil.formatYYYYMMddHHMM(new Date()));
    }

    /**
     * 2019-05-14 k8s容器启动路径  /opt/cachecloud/conf/${host}/redis-${port}.conf
     *
     * @param host
     * @param port
     * @param isCluster
     * @param dir
     * @return 启动redis命令
     */
    public static String getK8sRunShellByVersion(String host, int port, boolean isCluster, String dir) {
        return String.format(K8S_RUN_SHELL_VERSION, dir, MachineProtocol.getK8sConfDir(host) + getConfig(port, isCluster), MachineProtocol.getK8sLogDir(host), port, DateUtil.formatYYYYMMddHHMM(new Date()));
    }

    /**
     * 2019-05-14  k8s容器启动路径  /opt/cachecloud/conf/${host}/redis-${port}.conf
     *
     * @param host
     * @param port
     * @param dir
     * @return 启动redis sentinel命令
     */
    public static String getK8sSentinelShellByVersion(String host, int port, String dir) {
        return String.format(K8S_SENTINEL_SHELL_VERSION, dir, MachineProtocol.getK8sConfDir(host) + getConfig(port, false), MachineProtocol.getK8sLogDir(host), port, DateUtil.formatYYYYMMddHHMM(new Date()));
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


    public static String getExecuteAdminCommandShell(String host, int port, String password, String command) {
        StringBuffer shell = new StringBuffer();
        shell.append(String.format("redis-cli -h %s -p %s", host, port));
        if (StringUtils.isNotBlank(password)) {
            shell.append(String.format(" -a %s", password));
        }
        shell.append(String.format(" %s", command));
        return shell.toString();
    }

    public static String getConfig(int port, boolean isCluster) {
        if (isCluster) {
            return String.format(CLUSTER_CONFIG, port);
        } else {
            return String.format(COMMON_CONFIG, port);
        }
    }

    public static String getRedisPortPidFilePath() {
        return "logs/redis-port.pid";
    }

    private static final String NUT_CRACKER_SHELL = "bin/nutcracker -c %s/%s -p %s/%s -o %s/%s -s %d  -v %d";
    public static String getNutCrackerConfName() {
    	return "nutcracker.conf";
    }
    public static String getNutCrackerPidName() {
    	return "nutcracker.pid";
    }
    public static String getNutCrackerLogName() {
    	return "nutcracker.log";
    }
    public static String getNutCrackerShell(String confFilePath, String pidPath, String logPath, int statPort, int logLevel) {
		return String.format(NUT_CRACKER_SHELL, confFilePath, getNutCrackerConfName(), pidPath,
				getNutCrackerPidName(), logPath, getNutCrackerLogName(), statPort, logLevel);
    }
    public static String getNutCrackerStartCmd(String confFilePath, String pidPath, String logPath, int statPort, int logLevel) {
    	return String.format("bin/mon -a 60 -d \"%s\"", getNutCrackerShell(confFilePath, pidPath, logPath, statPort, logLevel));
    }
    public static String getNutCrackerRunCmd(String confFilePath, String pidPath, String logPath, int statPort, int logLevel) {
    	return getNutCrackerShell(confFilePath, pidPath, logPath, statPort, logLevel);
    }

}
