package com.sohu.cache.ssh;

import static com.sohu.cache.constant.BaseConstant.WORD_SEPARATOR;
import static com.sohu.cache.constant.EmptyObjectConstant.EMPTY_STRING;
import static com.sohu.cache.constant.SymbolConstant.COMMA;

import com.sohu.cache.entity.MachineStats;
import com.sohu.cache.exception.IllegalParamException;
import com.sohu.cache.exception.SSHException;
import com.sohu.cache.protocol.MachineProtocol;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.IntegerUtil;
import com.sohu.cache.util.StringUtil;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yijunzhang on 14-6-20.
 */
public class SSHUtil {

    private static final Logger logger = LoggerFactory.getLogger(SSHUtil.class);
    
    private final static String COMMAND_TOP = "top -b -n 1 | head -5";
    private final static String COMMAND_DF_LH = "df -lh";
    private final static String LOAD_AVERAGE_STRING = "load average: ";
    private final static String MEM_USAGE_STRING = "Mem:";
    private final static String SWAP_USAGE_STRING = "Swap:";
    private final static String BUFFER_CACHE = "buff/cache";

    /**
     * Get HostPerformanceEntity[cpuUsage, memUsage, load] by ssh.<br>
     * 方法返回前已经释放了所有资源，调用方不需要关心
     *
     * @param ip
     * @param userName
     * @param password
     * @throws Exception
     * @since 1.0.0
     */
    public static MachineStats getMachineInfo(String ip, int port, String userName, String password) throws SSHException {

        if (StringUtil.isBlank(ip)) {
            try {
                throw new IllegalParamException("Param ip is empty!");
            } catch (IllegalParamException e) {
                throw new SSHException(e.getMessage(), e);
            }
        }
        port = IntegerUtil.defaultIfSmallerThan0(port, MachineProtocol.SSH_PORT_DEFAULT);
        Connection conn = null;
        try {
            conn = new Connection(ip, port);
            conn.connect(null, 2000, 2000);
            boolean isAuthenticated = conn.authenticateWithPassword(userName, password);
            if (isAuthenticated == false) {
                throw new Exception("SSH authentication failed with [ userName: " + userName + ", " +
                        "password: " + password + "] on ip: " + ip);
            }
            return getMachineInfo(conn);
        } catch (Exception e) {
            throw new SSHException("SSH error, ip: " + ip, e);
        } finally {
            if (null != conn)
                conn.close();
        }
    }

    /**
     * Get HostPerformanceEntity[cpuUsage, memUsage, load] by ssh.<br>
     * 方法返回前已经释放了所有资源，调用方不需要关心
     *
     * @param ip
     * @param userName
     * @param password
     * @throws Exception
     * @since 1.0.0
     */
    public static MachineStats getMachineInfo(String ip, int port, String userName, String keypath, String password) throws SSHException {

        if (StringUtil.isBlank(ip)) {
            try {
                throw new IllegalParamException("Param ip is empty!");
            } catch (IllegalParamException e) {
                throw new SSHException(e.getMessage(), e);
            }
        }
        port = IntegerUtil.defaultIfSmallerThan0(port, MachineProtocol.SSH_PORT_DEFAULT);
        Connection conn = null;
        try {
            conn = new Connection(ip, port);
            conn.connect(null, 2000, 2000);
            boolean isAuthenticated = false;
            File key = new File(keypath);
            if (key.exists())
            {
                isAuthenticated = conn.authenticateWithPublicKey(userName, key, password);
            }else{
                throw new Exception("File open failed with public key: " + keypath);
            }
            if (isAuthenticated == false) {
                    throw new Exception("SSH authentication failed with [ userName: " + userName + ",public key: " + keypath + "]");
            }
            return getMachineInfo(conn);
        } catch (Exception e) {
            throw new SSHException("SSH error, ip: " + ip, e);
        } finally {
            if (null != conn)
                conn.close();
        }
    }

    /**
     * Get HostPerformanceEntity[cpuUsage, memUsage, load] by ssh.<br>
     * 方法返回前已经释放了所有资源，调用方不需要关心
     *
     * @param ip
     * @since 1.0.0
     */
    public static MachineStats getMachineInfo(String ip) throws SSHException {
        if (StringUtil.isBlank(ip)) {
            try {
                throw new IllegalParamException("Param ip is empty!");
            } catch (IllegalParamException e) {
                throw new SSHException(e.getMessage(), e);
            }
        }
        int sshPort = SSHUtil.getSshPort(ip);
        if(MachineProtocol.SSH_AUTH_PUBLICKEY.equals(ConstUtils.AUTHTYPE)){
            return getMachineInfo(ip, sshPort, ConstUtils.USERNAME, ConstUtils.KEY, ConstUtils.PASSWORD);
        }
        return getMachineInfo(ip, sshPort, ConstUtils.USERNAME, ConstUtils.PASSWORD);
    }

    /**
     * GetSystemPerformance
     *
     * @param conn a connection
     * @return double cpu usage
     * @throws Exception
     */
    private static MachineStats getMachineInfo(Connection conn) throws Exception {
        MachineStats systemPerformanceEntity = null;
        Session session = null;
        BufferedReader read = null;
        try {
            systemPerformanceEntity = new MachineStats();
            systemPerformanceEntity.setIp(conn.getHostname());
            session = conn.openSession();
            session.execCommand(COMMAND_TOP);
            //stderr
            printSessionStdErr(conn.getHostname(),COMMAND_TOP, session);

            read = new BufferedReader(new InputStreamReader(new StreamGobbler(session.getStdout())));
            String line = "";
            int lineNum = 0;

            String totalMem = EMPTY_STRING;
            String freeMem = EMPTY_STRING;
            String buffersMem = EMPTY_STRING;
            String cachedMem = EMPTY_STRING;
            while ((line = read.readLine()) != null) {

                if (StringUtil.isBlank(line))
                    continue;
                lineNum += 1;

                if (5 < lineNum)
                    return systemPerformanceEntity;

                if (1 == lineNum) {
                    // 第一行，通常是这样：
                    // top - 19:58:52 up 416 days, 30 min, 1 user, load average:
                    // 0.00, 0.00, 0.00
                    int loadAverageIndex = line.indexOf(LOAD_AVERAGE_STRING);
                    String loadAverages = line.substring(loadAverageIndex).replace(LOAD_AVERAGE_STRING, EMPTY_STRING);
                    String[] loadAverageArray = loadAverages.split(",");
                    if (3 != loadAverageArray.length)
                        continue;
                    systemPerformanceEntity.setLoad(StringUtil.trimToEmpty(loadAverageArray[0]));
                } else if (3 == lineNum) {
                    // 第三行通常是这样：
                    // , 0.0% sy, 0.0% ni, 100.0% id, 0.0% wa,
                    // 0.0% hi, 0.0% si
//                    redhat:%Cpu(s):  0.0 us
//                    centos7:Cpu(s): 0.0% us
                    double cpuUs = getUsCpu(line);
                    systemPerformanceEntity.setCpuUsage(String.valueOf(cpuUs));
                } else if (4 == lineNum) {
                    // 第四行通常是这样：
                    // Mem: 1572988k total, 1490452k used, 82536k free, 138300k
                    // buffers
                    String[] memArray = line.replace(MEM_USAGE_STRING, EMPTY_STRING).split(COMMA);
                    totalMem = matchMemLineNumber(memArray[0]).trim();
                    
                    if (line.contains(BUFFER_CACHE)) {
                        freeMem = matchMemLineNumber(memArray[1]).trim();
                    } else {
                        freeMem = matchMemLineNumber(memArray[2]).trim();
                    }
                    buffersMem = matchMemLineNumber(memArray[3]).trim();
                } else if (5 == lineNum) {
                    // 第四行通常是这样：
                    // Swap: 2096472k total, 252k used, 2096220k free, 788540k cached
                    String[] memArray = line.replace(SWAP_USAGE_STRING, EMPTY_STRING).split(COMMA);
                    if (memArray.length > 3) {
                        cachedMem = matchMemLineNumber(memArray[3]).trim();
                    } else {
                        cachedMem = "0";
                    }
                    if (StringUtil.isBlank(totalMem, freeMem, buffersMem))
                        throw new Exception("Error when get system performance of ip: " + conn.getHostname()
                                + ", can't get totalMem, freeMem, buffersMem or cachedMem");

                    Long totalMemLong = NumberUtils.toLong(totalMem);
                    Long freeMemLong = NumberUtils.toLong(freeMem);
                    Long buffersMemLong = NumberUtils.toLong(buffersMem);
                    Long cachedMemLong = NumberUtils.toLong(cachedMem);

                    Long usedMemFree = freeMemLong + buffersMemLong + cachedMemLong;
                    Double memoryUsage = 1 - (NumberUtils.toDouble(usedMemFree.toString()) / NumberUtils.toDouble(totalMemLong.toString()) / 1.0);
                    systemPerformanceEntity.setMemoryTotal(String.valueOf(totalMemLong));
                    systemPerformanceEntity.setMemoryFree(String.valueOf(usedMemFree));
                    DecimalFormat df = new DecimalFormat("0.00");
                    systemPerformanceEntity.setMemoryUsageRatio(df.format(memoryUsage * 100));
                } else {
                    continue;
                }
            }// parse the top output

            // 统计磁盘使用状况
            Map<String, String> diskUsageMap = new HashMap<String, String>();
            session = conn.openSession();
            session.execCommand(COMMAND_DF_LH);
            //stderr
            printSessionStdErr(conn.getHostname(),COMMAND_DF_LH, session);
            read = new BufferedReader(new InputStreamReader(new StreamGobbler(session.getStdout())));
            /**
             * 内容通常是这样： Filesystem 容量 已用 可用 已用% 挂载点 /dev/xvda2 5.8G 3.2G 2.4G
             * 57% / /dev/xvda1 99M 8.0M 86M 9% /boot none 769M 0 769M 0%
             * /dev/shm /dev/xvda7 68G 7.1G 57G 12% /home /dev/xvda6 2.0G 36M
             * 1.8G 2% /tmp /dev/xvda5 2.0G 199M 1.7G 11% /var
             * */
            boolean isFirstLine = true;
            while ((line = read.readLine()) != null) {

                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (StringUtil.isBlank(line))
                    continue;

                line = line.replaceAll(" {1,}", WORD_SEPARATOR);
                String[] lineArray = line.split(WORD_SEPARATOR);
                if (6 != lineArray.length) {
                    continue;
                }
                String diskUsage = lineArray[4];
                String mountedOn = lineArray[5];
                diskUsageMap.put(mountedOn, diskUsage);
            }
            systemPerformanceEntity.setDiskUsageMap(diskUsageMap);

            // 使用tsar统计当前网络流量 @TODO 
            Double traffic = 0.0;
            systemPerformanceEntity.setTraffic(traffic.toString());

        } catch (Exception e) {
            throw new Exception("Error when get system performance of ip: " + conn.getHostname(), e);
        } finally {
            try {
                if (null != read)
                    read.close();
                if (null != session)
                    session.close();
            } catch (Exception e) {
                // ingore
            }
        }

        return systemPerformanceEntity;
    }

    /**
     * 打印ssh错误信息
     * @param session
     */
    private static void printSessionStdErr(String ip, String command, Session session) {
        if (session == null) {
            logger.error("session is null");
            return;
        }
        StringBuffer sshErrorMsg = new StringBuffer();
        BufferedReader read = null;
        try {
            read = new BufferedReader(new InputStreamReader(new StreamGobbler(session.getStderr())));
            String line = null;
            while ((line = read.readLine()) != null) {
                sshErrorMsg.append(line);
            }
            if (StringUtils.isNotBlank(sshErrorMsg.toString())) {
                logger.error("ip {} execute command:({}), sshErrorMsg:{}", ip, command, sshErrorMsg.toString());
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (read != null) {
                try {
                    read.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * SSH 方式登录远程主机，执行命令,方法内部会关闭所有资源，调用方无须关心。
     *
     * @param ip       主机ip
     * @param username 用户名
     * @param password 密码
     * @param command  要执行的命令
     */
    public static String execute(String ip, int port, String username, String password, String command) throws SSHException {

        if (StringUtil.isBlank(command))
            return EMPTY_STRING;
        port = IntegerUtil.defaultIfSmallerThan0(port, MachineProtocol.SSH_PORT_DEFAULT);
        Connection conn = null;
        Session session = null;
        BufferedReader read = null;
        StringBuffer sb = new StringBuffer();
        try {
            if (StringUtil.isBlank(ip)) {
                throw new IllegalParamException("Param ip is empty!");
            }
            conn = new Connection(ip, port);
            conn.connect(null, 6000, 6000);
            boolean isAuthenticated = conn.authenticateWithPassword(username, password);
            if (isAuthenticated == false) {
                throw new Exception("SSH authentication failed with [ userName: " + username + ", password: " + password + "]");
            }
            session = conn.openSession();
            session.execCommand(command);
            //stderr
            printSessionStdErr(ip, command, session);
            //stdout
            read = new BufferedReader(new InputStreamReader(new StreamGobbler(session.getStdout())));
            String line = "";
            int lineNumber = 1;
            while ((line = read.readLine()) != null) {
//                sb.append(line).append(BR);
                if (lineNumber++ > 1) {
                    sb.append(System.lineSeparator());
                }
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            throw new SSHException("SSH远程执行command: " + command + " 出现错误: " + e.getMessage(), e);
        } finally {
            if (null != read) {
                try {
                    read.close();
                } catch (IOException e) {
                }
            }
            if (null != session)
                session.close();
            if (null != conn)
                conn.close();
        }
    }

    /**
     * SSH 方式登录远程主机，执行命令,方法内部会关闭所有资源，调用方无须关心。
     *
     * @param ip       主机ip
     * @param username 用户名
     * @param keypath      public key file
     * @param password key密码
     * @param command  要执行的命令
     */
    public static String execute(String ip, int port, String username, String keypath, String password, String command) throws SSHException {

        if (StringUtil.isBlank(command))
            return EMPTY_STRING;
        port = IntegerUtil.defaultIfSmallerThan0(port, MachineProtocol.SSH_PORT_DEFAULT);
        Connection conn = null;
        Session session = null;
        BufferedReader read = null;
        StringBuffer sb = new StringBuffer();
        try {
            if (StringUtil.isBlank(ip)) {
                throw new IllegalParamException("Param ip is empty!");
            }
            conn = new Connection(ip, port);
            conn.connect(null, 6000, 6000);
            boolean isAuthenticated = false;
            File key = new File(keypath);
            if (key.exists())
            {
                isAuthenticated = conn.authenticateWithPublicKey(username, key, password);
            }
            if (isAuthenticated == false) {
                    throw new Exception("SSH authentication failed with [ userName: " + username + ",public key: " + keypath + "]");
            }
            session = conn.openSession();
            session.execCommand(command);
            //stderr
            printSessionStdErr(ip, command, session);
            //stdout
            read = new BufferedReader(new InputStreamReader(new StreamGobbler(session.getStdout())));
            String line = "";
            int lineNumber = 1;
            while ((line = read.readLine()) != null) {
//                sb.append(line).append(BR);
                if (lineNumber++ > 1) {
                    sb.append(System.lineSeparator());
                }
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            throw new SSHException("SSH远程执行command: " + command + " 出现错误: " + e.getMessage(), e);
        } finally {
            if (null != read) {
                try {
                    read.close();
                } catch (IOException e) {
                }
            }
            if (null != session)
                session.close();
            if (null != conn)
                conn.close();
        }
    }
    /**
     * @param ip
     * @param port
     * @param username
     * @param password
     * @param localPath
     * @param remoteDir
     * @return
     * @throws SSHException
     */
    public static boolean scpFileToRemote(String ip, int port, String username, String password, String localPath, String remoteDir) throws SSHException{
        boolean isSuccess = true;
        Connection connection = new Connection(ip, port);
        try {
            connection.connect();
            boolean isAuthed = connection.authenticateWithPassword(username, password);
            if (!isAuthed) {
                throw new SSHException("auth error.");
            }
            SCPClient scpClient = connection.createSCPClient();
            scpClient.put(localPath, remoteDir, "0644");
        } catch (IOException e) {
            isSuccess = false;
            throw new SSHException("scp file to remote error.", e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        return isSuccess;
    }
    /**
     * @param ip
     * @param port
     * @param username
     * @param password
     * @param localPath
     * @param remoteDir
     * @return
     * @throws SSHException
     */
    public static boolean scpFileToRemote(String ip, int port, String username, String keypath,  String password, String localPath, String remoteDir) throws SSHException{
        boolean isSuccess = true;
        Connection connection = new Connection(ip, port);
        try {
            connection.connect();
            boolean isAuthed = false;
            File key = new File(keypath);
            if (key.exists())
            {
                isAuthed = connection.authenticateWithPublicKey(username, key, password);
            }
            if (!isAuthed) {
                throw new SSHException("auth error.");
            }
            SCPClient scpClient = connection.createSCPClient();
            scpClient.put(localPath, remoteDir, "0644");
        } catch (IOException e) {
            isSuccess = false;
            throw new SSHException("scp file to remote error.", e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        return isSuccess;
    }
    /**
     * 重载，使用默认端口、用户名和密码
     *
     * @param ip
     * @param localPath
     * @param remoteDir
     * @return
     * @throws SSHException
     */
    public static boolean scpFileToRemote(String ip, String localPath, String remoteDir) throws SSHException {
        int sshPort = SSHUtil.getSshPort(ip);
        if(MachineProtocol.SSH_AUTH_PUBLICKEY.equals(ConstUtils.AUTHTYPE)){
            return scpFileToRemote(ip, sshPort, ConstUtils.USERNAME, ConstUtils.KEY, ConstUtils.PASSWORD, localPath, remoteDir);
        }
        return scpFileToRemote(ip, sshPort, ConstUtils.USERNAME, ConstUtils.PASSWORD, localPath, remoteDir);
    }

    /**
     * 重载，使用默认端口、用户名和密码
     *
     * @param ip
     * @param cmd
     * @return
     * @throws SSHException
     */
    public static String execute(String ip, String cmd) throws SSHException {
        int sshPort = SSHUtil.getSshPort(ip);
        if(MachineProtocol.SSH_AUTH_PUBLICKEY.equals(ConstUtils.AUTHTYPE)){
            return execute(ip, sshPort, ConstUtils.USERNAME, ConstUtils.KEY, ConstUtils.PASSWORD, cmd);
        }
        return execute(ip, sshPort, ConstUtils.USERNAME, ConstUtils.PASSWORD, cmd);
    }

    /**
     * 查看机器ip上的端口port是否已被占用；
     *
     * @param ip    机器ip
     * @param port  要检查的端口
     * @return  如果被占用返回true，否则返回false；
     * @throws SSHException
     */
    public static boolean isPortUsed(String ip, int port) throws SSHException {
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
            for (String resultLine: resultArr) {
                if (resultLine.contains(String.valueOf(port))) {
                    isUsed = true;
                    break;
                }
            }
        }
        return isUsed;
    }

    /**
     * 通过ip来判断ssh端口
     *
     * @param ip
     * @return
     */
    public static int getSshPort(String ip) {
        /**
         * 如果ssh默认端口不是22,请自行实现该逻辑
         */
        return MachineProtocol.SSH_PORT_DEFAULT;
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
     * @param cpuLine
     * @return
     */
    public static double getUsCpu(String cpuLine) {
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
    
    
}