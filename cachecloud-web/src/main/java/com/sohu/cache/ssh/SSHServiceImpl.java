package com.sohu.cache.ssh;

import com.sohu.cache.dao.MachineDao;
import com.sohu.cache.entity.MachineInfo;
import com.sohu.cache.exception.SSHException;
import com.sohu.cache.ssh.SSHTemplate.Result;
import com.sohu.cache.ssh.SSHTemplate.SSHCallback;
import com.sohu.cache.ssh.SSHTemplate.SSHSession;
import com.sohu.cache.util.ConstUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sohu.cache.constant.EmptyObjectConstant.EMPTY_STRING;
import static com.sohu.cache.constant.SymbolConstant.COMMA;

/**
 * @author fulei
 * @date 2018年6月25日
 */
@Service
public class SSHServiceImpl implements SSHService {

    private Logger logger = LoggerFactory.getLogger(SSHServiceImpl.class);

    @Autowired
    private MachineDao machineDao;

    private Map<String, MachineInfo> machineIpInfoMap = new ConcurrentHashMap<String, MachineInfo>();

    private Map<String, Long> appNameIdMap = new ConcurrentHashMap<String, Long>();

    @Autowired(required = false)
    private SSHTemplate sshTemplate;

    @Override
    public String execute(String ip, int port, String username, String password, final String command)
            throws SSHException {
        Result rst = executeWithResult(ip, port, username, password, command);
        if (rst.isSuccess()) {
            return rst.getResult();
        }
        return "";
    }

    @Override
    public Result executeWithResult(String ip, int port, String username, String password, final String command)
            throws SSHException {
        return sshTemplate.execute(ip, port, username, password, new SSHCallback() {
            public Result call(SSHSession session) {
                return session.executeCommand(command);
            }
        });
    }

    @Override
    public Result executeWithResult(String ip, int port, String username, String password, final String command, int timeoutMills)
            throws SSHException {
        return sshTemplate.execute(ip, port, username, password, new SSHCallback() {
            public Result call(SSHSession session) {
                return session.executeCommand(command, timeoutMills);
            }
        });
    }

    @Override
    public Result executeWithResult(String ip, String cmd) throws SSHException {
        return executeWithResult(ip, getSshPort(ip), ConstUtils.USERNAME, ConstUtils.PASSWORD, cmd);
    }

    public Result executeWithResult(String ip, String cmd, int millsSecond) throws SSHException {
        return executeWithResult(ip, getSshPort(ip), ConstUtils.USERNAME, ConstUtils.PASSWORD, cmd, millsSecond);
    }

    @Override
    public Result scpFileToRemote(String ip, int port, String username,
                                  String password, final String localPath, final String remoteDir) throws SSHException {
        return sshTemplate.execute(ip, port, username, password, new SSHCallback() {
            public Result call(SSHSession session) {
                return session.scpToDir(localPath, remoteDir, "0644");
            }
        });
    }

    @Override
    public Result scpFileToRemote(String ip, String localPath, String remoteDir) throws SSHException {
        return scpFileToRemote(ip, getSshPort(ip), ConstUtils.USERNAME, ConstUtils.PASSWORD, localPath, remoteDir);
    }

    @Override
    public String execute(String ip, String cmd) throws SSHException {
        return execute(ip, getSshPort(ip), ConstUtils.USERNAME, ConstUtils.PASSWORD, cmd);
    }

    @Override
    public boolean isPortUsed(String ip, int port) throws SSHException {
        /**
         * 执行ps命令，查看端口，以确认刚才执行的shell命令是否成功，返回一般是这样的：
         *  root     12510 12368  0 14:34 pts/0    00:00:00 redis-server *:6379
         */
        String psCmd = "/bin/ps -ef | grep %s | grep -v grep";
        psCmd = String.format(psCmd, port);
        String psResponse = execute(ip, psCmd);
        boolean isUsed = false;

        if (StringUtils.isNotBlank(psResponse)) {
            String[] resultArr = psResponse.split(System.lineSeparator());
            for (String resultLine : resultArr) {
                if (resultLine.contains(String.valueOf(port))) {
                    isUsed = true;
                    break;
                }
            }
        }
        return isUsed;
    }

    @Override
    public int getSshPort(String ip) {
        /**
         * 如果ssh默认端口不是22,请自行实现该逻辑
         */
        return ConstUtils.SSH_PORT_DEFAULT;
    }

    /**
     * 匹配字符串中的数字
     *
     * @param content
     * @return
     */
    private static String matchMemLineNumber(String content) {
        String result = EMPTY_STRING;
        if (content == null || EMPTY_STRING.equals(content.trim())) {
            return result;
        }
        Pattern pattern = Pattern.compile("(\\d+)");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            result = matcher.group(1);
        }
        return result;
    }

    /**
     * 从top的cpuLine解析出us
     *
     * @param cpuLine
     * @return
     */
    private static double getUsCpu(String cpuLine) {
        if (cpuLine == null || EMPTY_STRING.equals(cpuLine.trim())) {
            return 0;
        }
        String[] items = cpuLine.split(COMMA);
        if (items.length < 1) {
            return 0;
        }
        String usCpuStr = items[0];
        return NumberUtils.toDouble(matchCpuLine(usCpuStr));
    }

    private static String matchCpuLine(String content) {
        String result = EMPTY_STRING;
        if (content == null || EMPTY_STRING.equals(content.trim())) {
            return result;
        }
        Pattern pattern = Pattern.compile("(\\d+).(\\d+)");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            result = matcher.group();
        }
        return result;
    }

    public MachineInfo getMachineInfo(String ip) {
        MachineInfo machineInfo = machineIpInfoMap.get(ip);
        if (machineInfo == null) {
            machineInfo = machineDao.getMachineInfoByIp(ip);
            machineIpInfoMap.put(ip, machineInfo);
        }
        return machineInfo;
    }

    public Long getAppId(String name) {
        Long appId = appNameIdMap.get(name);
        return appId;
    }

}
