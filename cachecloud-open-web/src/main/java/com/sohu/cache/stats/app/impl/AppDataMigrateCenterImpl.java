package com.sohu.cache.stats.app.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

import com.sohu.cache.constant.AppDataMigrateEnum;
import com.sohu.cache.constant.AppDataMigrateResult;
import com.sohu.cache.constant.AppDataMigrateStatusEnum;
import com.sohu.cache.constant.RedisConstant;
import com.sohu.cache.constant.RedisMigrateToolConstant;
import com.sohu.cache.dao.AppDataMigrateStatusDao;
import com.sohu.cache.entity.AppDataMigrateStatus;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.entity.MachineInfo;
import com.sohu.cache.exception.SSHException;
import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.protocol.MachineProtocol;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.ssh.SSHUtil;
import com.sohu.cache.stats.app.AppDataMigrateCenter;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.TypeUtil;
import com.sohu.cache.web.service.AppService;

/**
 * 数据迁移(使用唯品会的开源工具redis-migrate-tool进行迁移)
 * 
 * @author leifu
 * @Date 2016-6-8
 * @Time 下午2:54:33
 */
public class AppDataMigrateCenterImpl implements AppDataMigrateCenter {

    private Logger logger = LoggerFactory.getLogger(AppDataMigrateCenterImpl.class);

    private AppService appService;

    private RedisCenter redisCenter;

    private MachineCenter machineCenter;
    
    private AppDataMigrateStatusDao appDataMigrateStatusDao;

    @Override
    public AppDataMigrateResult check(String migrateMachineIp, AppDataMigrateEnum sourceRedisMigrateEnum,
            String sourceServers, long targetAppId) {
        // 1.检查app是否存在
        AppDesc appDesc = appService.getByAppId(targetAppId);
        if (appDesc == null) {
            return AppDataMigrateResult.fail("目标appId=" + targetAppId + "不存在");
        }

        // 2.转换Redis类型
        AppDataMigrateEnum targetRedisMigrateEnum = TypeUtil.isRedisCluster(appDesc.getType()) ? AppDataMigrateEnum.REDIS_CLUSTER_NODE
                : AppDataMigrateEnum.REDIS_NODE;

        // 3.转换servers
        List<InstanceInfo> instanceInfoList = appService.getAppInstanceInfo(targetAppId);
        if (CollectionUtils.isEmpty(instanceInfoList)) {
            return AppDataMigrateResult.fail("目标appId=" + targetAppId + "不存在任何redis节点!");
        }
        StringBuffer targetServers = new StringBuffer();
        for (int i = 0; i < instanceInfoList.size(); i++) {
            InstanceInfo instanceInfo = instanceInfoList.get(i);
            targetServers.append(instanceInfo.getIp() + ":" + instanceInfo.getPort());
            if (i != instanceInfoList.size() - 1) {
                targetServers.append(ConstUtils.NEXT_LINE);
            }
        }

        return check(migrateMachineIp, sourceRedisMigrateEnum, sourceServers, targetRedisMigrateEnum,
                targetServers.toString());
    }

    @Override
    public AppDataMigrateResult check(String migrateMachineIp, AppDataMigrateEnum sourceRedisMigrateEnum,
            String sourceServers,
            AppDataMigrateEnum targetRedisMigrateEnum, String targetServers) {

        // 1. 检查migrateMachineIp是否安装
        AppDataMigrateResult migrateMachineResult = checkMigrateMachine(migrateMachineIp);
        if (!migrateMachineResult.isSuccess()) {
            return migrateMachineResult;
        }

        // 2. 检查源配置
        AppDataMigrateResult sourceResult = checkMigrateConfig(migrateMachineIp, sourceRedisMigrateEnum, sourceServers);
        if (!sourceResult.isSuccess()) {
            return sourceResult;
        }

        // 3. 检查目标
        AppDataMigrateResult targetResult = checkMigrateConfig(migrateMachineIp, targetRedisMigrateEnum, targetServers);
        if (!targetResult.isSuccess()) {
            return targetResult;
        }

        return AppDataMigrateResult.success();
    }

    /**
     * 检查迁移的机器是否正常
     * 
     * @param migrateMachineIp
     * @return
     */
    private AppDataMigrateResult checkMigrateMachine(String migrateMachineIp) {
        if (StringUtils.isBlank(migrateMachineIp)) {
            return AppDataMigrateResult.fail("redis-migrate-tool所在机器的IP不能为空");
        }
        // 1. 检查机器是否存在在机器列表中
        try {
            MachineInfo machineInfo = machineCenter.getMachineInfoByIp(migrateMachineIp);
            if (machineInfo == null) {
                return AppDataMigrateResult.fail(migrateMachineIp + "没有在机器管理列表中");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return AppDataMigrateResult.fail("检测发生异常，请观察日志");
        }
        // 2. 检查是否安装redis-migrate-tool
        try {
            String cmd = ConstUtils.REDIS_MIGRATE_TOOL_CMD;
            String response = SSHUtil.execute(migrateMachineIp, cmd);
            if (StringUtils.isBlank(response) || !response.contains("source") || !response.contains("target")) {
                return AppDataMigrateResult.fail(migrateMachineIp + "下，" + cmd + "执行失败，请确保redis-migrate-tool安装正确!");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return AppDataMigrateResult.fail("检测发生异常，请观察日志");
        }

        // 3. 检查是否有运行的redis-migrate-tool
        // 3.1 从数据库里检测，每次迁移记录迁移的详情,状态不太好控制，暂时去掉
//        try {
//            int count = appDataMigrateStatusDao.getMigrateMachineStatCount(migrateMachineIp, AppDataMigrateStatusEnum.START.getStatus());
//            if (count > 0) {
//                return AppDataMigrateResult.fail(migrateMachineIp + "下有redis-migrate-tool进程，请确保只有一台机器只有一个迁移任务进行");
//            }
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//        }

        // 3.2 查看进程是否存在
        try {
            String cmd = "/bin/ps -ef | grep redis-migrate-tool | grep -v grep | grep -v tail";
            String response = SSHUtil.execute(migrateMachineIp, cmd);
            if (StringUtils.isNotBlank(response)) {
                return AppDataMigrateResult.fail(migrateMachineIp + "下有redis-migrate-tool进程，请确保只有一台机器只有一个迁移任务进行");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return AppDataMigrateResult.fail("检测发生异常，请观察日志");
        }

        return AppDataMigrateResult.success();
    }

    /**
     * 检测配置
     * 
     * @param migrateMachineIp
     * @param redisMigrateEnum
     * @param servers
     * @return
     */
    private AppDataMigrateResult checkMigrateConfig(String migrateMachineIp, AppDataMigrateEnum redisMigrateEnum,
            String servers) {
        if (StringUtils.isBlank(servers)) {
            return AppDataMigrateResult.fail("服务器信息不能为空!");
        }
        List<String> serverList = Arrays.asList(servers.split(ConstUtils.NEXT_LINE));
        if (CollectionUtils.isEmpty(serverList)) {
            return AppDataMigrateResult.fail("服务器信息格式有问题!");
        }
        for (String server : serverList) {
            if (AppDataMigrateEnum.RDB_FILE.equals(redisMigrateEnum)) {
                // 检查文件是否存在
                String filePath = server;
                String cmd = "head " + filePath;
                try {
                    String headResult = SSHUtil.execute(migrateMachineIp, cmd);
                    if (StringUtils.isBlank(headResult)) {
                        return AppDataMigrateResult.fail(migrateMachineIp + "上的rdb:" + filePath + "不存在或者为空!");
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    return AppDataMigrateResult.fail(migrateMachineIp + "上的rdb:" + filePath + "读取异常!");
                }
            } else {
                // 1. 检查是否为ip:port格式(简单检查一下，无需正则表达式)
                // 2. 检查Redis节点是否存在
                String[] instanceItems = server.split(":");
                if (instanceItems.length != 2) {
                    return AppDataMigrateResult.fail("实例信息" + server + "格式错误，必须为ip:port格式");
                }
                String ip = instanceItems[0];
                String portStr = instanceItems[1];
                boolean portIsDigit = NumberUtils.isDigits(portStr);
                if (!portIsDigit) {
                    return AppDataMigrateResult.fail(server + "中的port不是整数");
                }
                int port = NumberUtils.toInt(portStr);
                boolean isRun = redisCenter.isRun(ip, port);
                if (!isRun) {
                    return AppDataMigrateResult.fail(server + "不是存活的");
                }
            }
        }

        return AppDataMigrateResult.success();
    }

    @Override
    public boolean migrate(String migrateMachineIp, AppDataMigrateEnum sourceRedisMigrateEnum, String sourceServers,
            AppDataMigrateEnum targetRedisMigrateEnum, String targetServers, long sourceAppId, long targetAppId, long userId) {
        // 1. 生成配置
        int migrateMachinePort = ConstUtils.REDIS_MIGRATE_TOOL_PORT;
        String configContent = generateConfig(migrateMachinePort, sourceRedisMigrateEnum, sourceServers, targetRedisMigrateEnum,
                targetServers);
        // 2. 上传配置
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String confileFileName = "rmt-" + timestamp + ".conf";
        String logFileName = "rmt-" + timestamp + ".log";
        boolean uploadConfig = createRemoteFile(migrateMachineIp, confileFileName, configContent);
        if (!uploadConfig) {
            return false;
        }
        // 3. 开始执行: 指定的配置名、目录、日志名
        String cmd = ConstUtils.REDIS_MIGRATE_TOOL_CMD + " -c " + ConstUtils.REDIS_MIGRATE_TOOL_HOME + confileFileName
                + " -o " + ConstUtils.REDIS_MIGRATE_TOOL_HOME + logFileName + " -d";
        logger.warn(cmd);
        try {
            SSHUtil.execute(migrateMachineIp, cmd);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }

        // 4. 记录执行记录
        AppDataMigrateStatus appDataMigrateStatus = new AppDataMigrateStatus();
        appDataMigrateStatus.setMigrateMachineIp(migrateMachineIp);
        appDataMigrateStatus.setMigrateMachinePort(migrateMachinePort);
        appDataMigrateStatus.setStartTime(new Date());
        appDataMigrateStatus.setSourceMigrateType(sourceRedisMigrateEnum.getIndex());
        appDataMigrateStatus.setSourceServers(sourceServers);
        appDataMigrateStatus.setTargetMigrateType(targetRedisMigrateEnum.getIndex());
        appDataMigrateStatus.setTargetServers(targetServers);
        appDataMigrateStatus.setLogPath(ConstUtils.REDIS_MIGRATE_TOOL_HOME + logFileName);
        appDataMigrateStatus.setConfigPath(ConstUtils.REDIS_MIGRATE_TOOL_HOME + confileFileName);
        appDataMigrateStatus.setUserId(userId);
        appDataMigrateStatus.setSourceAppId(sourceAppId);
        appDataMigrateStatus.setTargetAppId(targetAppId);
        appDataMigrateStatus.setStatus(AppDataMigrateStatusEnum.START.getStatus());
        appDataMigrateStatusDao.save(appDataMigrateStatus);

        return true;
    }

    /**
     * 生成配置
     * 
     * @param sourceRedisMigrateEnum
     * @param sourceServers
     * @param targetRedisMigrateEnum
     * @param targetServers
     * @return
     */
    public String generateConfig(int listenPort, AppDataMigrateEnum sourceRedisMigrateEnum, String sourceServers,
            AppDataMigrateEnum targetRedisMigrateEnum, String targetServers) {
        // source
        StringBuffer config = new StringBuffer();
        config.append("[source]" + ConstUtils.NEXT_LINE);
        config.append("type: " + sourceRedisMigrateEnum.getType() + ConstUtils.NEXT_LINE);
        config.append("servers:" + ConstUtils.NEXT_LINE);
        List<String> sourceServerList = Arrays.asList(sourceServers.split(ConstUtils.NEXT_LINE));
        for (String server : sourceServerList) {
            config.append(" - " + server + ConstUtils.NEXT_LINE);
        }
        config.append(ConstUtils.NEXT_LINE);

        // target
        config.append("[target]" + ConstUtils.NEXT_LINE);
        config.append("type: " + targetRedisMigrateEnum.getType() + ConstUtils.NEXT_LINE);
        config.append("servers:" + ConstUtils.NEXT_LINE);
        List<String> targetServerList = Arrays.asList(targetServers.split(ConstUtils.NEXT_LINE));
        for (String server : targetServerList) {
            config.append(" - " + server + ConstUtils.NEXT_LINE);
        }
        config.append(ConstUtils.NEXT_LINE);

        // common:使用最简配置
        config.append("[common]" + ConstUtils.NEXT_LINE);
        config.append("listen: 0.0.0.0:" + listenPort);

        return config.toString();
    }

    /**
     * 创建远程文件
     * 
     * @param host
     * @param fileName
     * @param content
     */
    public boolean createRemoteFile(String host, String fileName, String content) {
        /**
         * 1. 创建本地文件
         */
        // 确认目录
        String localAbsolutePath = MachineProtocol.TMP_DIR + fileName;
        File tmpDir = new File(MachineProtocol.TMP_DIR);
        if (!tmpDir.exists()) {
            if (!tmpDir.mkdirs()) {
                logger.error("cannot create /tmp/cachecloud directory.");
            }
        }
        Path path = Paths.get(MachineProtocol.TMP_DIR + fileName);
        // 将配置文件的内容写到本地
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = Files.newBufferedWriter(path, Charset.forName(MachineProtocol.ENCODING_UTF8));
            bufferedWriter.write(content);
        } catch (IOException e) {
            logger.error("write rmt file error, ip: {}, filename: {}, content: {}", host, fileName, content, e);
            return false;
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 2. 将配置文件推送到目标机器上
         */
        try {
            SSHUtil.scpFileToRemote(host, localAbsolutePath, ConstUtils.REDIS_MIGRATE_TOOL_HOME);
        } catch (SSHException e) {
            logger.error("scp rmt file to remote server error: ip: {}, fileName: {}", host, fileName, e);
            return false;
        }

        /**
         * 3. 删除临时文件
         */
        File file = new File(localAbsolutePath);
        if (file.exists()) {
            file.delete();
        }

        return true;
    }
    
    @Override
    public List<AppDataMigrateStatus> search() {
        try {
            return appDataMigrateStatusDao.search(null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public String showDataMigrateLog(long id, int pageSize) {
        AppDataMigrateStatus appDataMigrateStatus = appDataMigrateStatusDao.get(id);
        if (appDataMigrateStatus == null) {
            return "";
        }
        String logPath = appDataMigrateStatus.getLogPath();
        String host = appDataMigrateStatus.getMigrateMachineIp();
        StringBuilder command = new StringBuilder();
        command.append("/usr/bin/tail -n").append(pageSize).append(" ").append(logPath);
        try {
            return SSHUtil.execute(host, command.toString());
        } catch (SSHException e) {
            logger.error(e.getMessage(), e);
            return "";
        }
    }
    
    @Override
    public String showDataMigrateConf(long id) {
        AppDataMigrateStatus appDataMigrateStatus = appDataMigrateStatusDao.get(id);
        if (appDataMigrateStatus == null) {
            return "";
        }
        String configPath = appDataMigrateStatus.getConfigPath();
        String host = appDataMigrateStatus.getMigrateMachineIp();
        String command = "cat " + configPath;
        try {
            return SSHUtil.execute(host, command);
        } catch (SSHException e) {
            logger.error(e.getMessage(), e);
            return "";
        }
    }
    
    @Override
    public Map<RedisMigrateToolConstant, Map<String, Object>> showMiragteToolProcess(long id) {
        AppDataMigrateStatus appDataMigrateStatus = appDataMigrateStatusDao.get(id);
        if (appDataMigrateStatus == null) {
            return Collections.emptyMap();
        }
        String info = "";
        String host = appDataMigrateStatus.getMigrateMachineIp();
        int port = appDataMigrateStatus.getMigrateMachinePort();
        Jedis jedis = null;
        try {
            jedis = new Jedis(host, port, 5000);
            info = jedis.info();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        if (StringUtils.isBlank(info)) {
            return Collections.emptyMap();
        }

        return processRedisMigrateToolStats(info);
        
    }
    
    /**
     * 处理迁移工具状态
     * @param statResult
     * @return
     */
    private Map<RedisMigrateToolConstant, Map<String, Object>> processRedisMigrateToolStats(String statResult) {
        Map<RedisMigrateToolConstant, Map<String, Object>> redisStatMap = new HashMap<RedisMigrateToolConstant, Map<String, Object>>();
        String[] data = statResult.split("\r\n");
        String key;
        int i = 0;
        int length = data.length;
        while (i < length) {
            if (data[i].contains("#")) {
                int index = data[i].indexOf('#');
                key = data[i].substring(index + 1);
                ++i;
                RedisMigrateToolConstant redisMigrateToolConstant = RedisMigrateToolConstant.value(key.trim());
                if (redisMigrateToolConstant == null) {
                    continue;
                }
                Map<String, Object> sectionMap = new LinkedHashMap<String, Object>();
                while (i < length && data[i].contains(":")) {
                    String[] pair = data[i].split(":");
                    sectionMap.put(pair[0], pair[1]);
                    i++;
                }
                redisStatMap.put(redisMigrateToolConstant, sectionMap);
            } else {
                i++;
            }
        }
        return redisStatMap;
    }
    

    public void setRedisCenter(RedisCenter redisCenter) {
        this.redisCenter = redisCenter;
    }

    public void setMachineCenter(MachineCenter machineCenter) {
        this.machineCenter = machineCenter;
    }

    public void setAppService(AppService appService) {
        this.appService = appService;
    }

    public void setAppDataMigrateStatusDao(AppDataMigrateStatusDao appDataMigrateStatusDao) {
        this.appDataMigrateStatusDao = appDataMigrateStatusDao;
    }

    

    

}
