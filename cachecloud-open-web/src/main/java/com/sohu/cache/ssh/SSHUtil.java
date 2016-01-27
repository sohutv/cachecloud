package com.sohu.cache.ssh;

import static com.sohu.cache.constant.BaseConstant.WORD_SEPARATOR;
import static com.sohu.cache.constant.EmptyObjectConstant.EMPTY_STRING;
import static com.sohu.cache.constant.SymbolConstant.COMMA;

import com.sohu.cache.constant.CacheCloudConstants;
import com.sohu.cache.entity.MachineStats;
import com.sohu.cache.exception.IllegalParamException;
import com.sohu.cache.exception.SSHException;
import com.sohu.cache.protocol.MachineProtocol;
import com.sohu.cache.util.IntegerUtil;
import com.sohu.cache.util.StringUtil;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.NumberUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yijunzhang on 14-6-20.
 */
public class SSHUtil {

    private static final Logger logger = LoggerFactory.getLogger(SSHUtil.class);
    
    private final static String USERNAME = "";
    private final static String PASSWORD = "";

    private final static String COMMAND_TOP = "top -b -n 1 | head -5";
    private final static String COMMAND_DF_LH = "df -lh";
    private final static String LOAD_AVERAGE_STRING = "load average: ";
    private final static String CPU_USAGE_STRING = "Cpu(s):";
    private final static String MEM_USAGE_STRING = "Mem:";
    private final static String SWAP_USAGE_STRING = "Swap:";
    private final static String COMMAND_TSAR = "tsar --traffic -D | tail -n 5";

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
        userName = StringUtil.defaultIfBlank(userName, USERNAME);
        password = StringUtil.defaultIfBlank(password, PASSWORD);
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
                    // Cpu(s): 0.0% us, 0.0% sy, 0.0% ni, 100.0% id, 0.0% wa,
                    // 0.0% hi, 0.0% si
                    String cpuUsage = line.split(",")[0].replace(CPU_USAGE_STRING, EMPTY_STRING).replace("us", EMPTY_STRING);
                    systemPerformanceEntity.setCpuUsage(StringUtil.trimToEmpty(cpuUsage));
                } else if (4 == lineNum) {
                    // 第四行通常是这样：
                    // Mem: 1572988k total, 1490452k used, 82536k free, 138300k
                    // buffers
                    String[] memArray = line.replace(MEM_USAGE_STRING, EMPTY_STRING).split(COMMA);
                    totalMem = StringUtil.trimToEmpty(memArray[0].replace("total", EMPTY_STRING)).replace("k", EMPTY_STRING);
                    freeMem = StringUtil.trimToEmpty(memArray[2].replace("free", EMPTY_STRING)).replace("k", EMPTY_STRING);
                    buffersMem = StringUtil.trimToEmpty(memArray[3].replace("buffers", EMPTY_STRING)).replace("k", EMPTY_STRING);
                } else if (5 == lineNum) {
                    // 第四行通常是这样：
                    // Swap: 2096472k total, 252k used, 2096220k free, 788540k
                    // cached
                    String[] memArray = line.replace(SWAP_USAGE_STRING, EMPTY_STRING).split(COMMA);
                    cachedMem = StringUtil.trimToEmpty(memArray[3].replace("cached", EMPTY_STRING)).replace("k", EMPTY_STRING);

                    if (StringUtil.isBlank(totalMem, freeMem, buffersMem, cachedMem))
                        throw new Exception("Error when get system performance of ip: " + conn.getHostname()
                                + ", can't get totalMem, freeMem, buffersMem or cachedMem");

                    Long totalMemLong = Long.parseLong(totalMem);
                    Long freeMemLong = Long.parseLong(freeMem);
                    Long buffersMemLong = Long.parseLong(buffersMem);
                    Long cachedMemLong = Long.parseLong(cachedMem);

                    Long usedMemFree = freeMemLong + buffersMemLong + cachedMemLong;
                    Double memoryUsage = 1 - (NumberUtils.parseNumber(usedMemFree.toString(), Double.class) / NumberUtils.parseNumber(totalMemLong.toString(), Double.class));
                    systemPerformanceEntity.setMemoryTotal(String.valueOf(totalMemLong));
                    systemPerformanceEntity.setMemoryFree(String.valueOf(usedMemFree));
                    DecimalFormat df = new DecimalFormat(".00");
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

            // 使用tsar统计当前网络流量
            Double traffic = 0.0;
            session = conn.openSession();
            session.execCommand(COMMAND_TSAR);
            //stderr
            printSessionStdErr(conn.getHostname(),COMMAND_TSAR, session);
            read = new BufferedReader(new InputStreamReader(new StreamGobbler(session.getStdout())));
            /**
             * 内容一般是这样的，这里仅取了倒数5行，使用倒数第5行的当前网络流量值：
             * Time           -------------traffic------------
             Time            bytin  bytout   pktin  pktout
             15/09/14-16:25 894.00    0.00    7.00    0.00
             15/09/14-16:30 955.00    0.00    9.00    0.00
             15/09/14-16:35 793.00   38.00    7.00    0.00
             15/09/14-16:40 776.00   45.00    6.00    0.00
             15/09/14-16:45 943.00  104.00    8.00    0.00
             15/09/14-16:50 939.00    8.00    7.00    0.00

             MAX            1053.00  104.00   10.00    0.00
             MEAN           900.42   16.25    7.75    0.00
             MIN            776.00    0.00    6.00    0.00
             */
            while ((line = read.readLine()) != null) {
                // 第一行即为当前的网络流量，将in和out加起来
                if (StringUtils.isBlank(line)) {
                    continue;
                }
                String[] lineArray = line.split("\\s+");    // 以任意空白符分隔
                if (lineArray.length < 3) {
                    continue;
                }
                Double bytin = Double.valueOf(lineArray[1]);
                Double bytout = Double.valueOf(lineArray[2]);
                traffic = bytin + bytout;
                if(traffic > 0){
                    break;
                }
            }
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
            username = StringUtil.defaultIfBlank(username, USERNAME);
            password = StringUtil.defaultIfBlank(password, PASSWORD);
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
     * 将
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
        return scpFileToRemote(ip, sshPort, CacheCloudConstants.USERNAME, CacheCloudConstants.PASSWORD, localPath, remoteDir);
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
        return execute(ip, sshPort, CacheCloudConstants.USERNAME, CacheCloudConstants.PASSWORD, cmd);
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
}
