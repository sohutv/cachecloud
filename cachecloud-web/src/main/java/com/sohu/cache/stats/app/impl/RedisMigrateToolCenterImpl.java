package com.sohu.cache.stats.app.impl;

import com.sohu.cache.constant.*;
import com.sohu.cache.dao.AppDataMigrateStatusDao;
import com.sohu.cache.entity.*;
import com.sohu.cache.exception.SSHException;
import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.protocol.MachineProtocol;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.ssh.SSHUtil;
import com.sohu.cache.stats.app.AppDataMigrateCenter;
import com.sohu.cache.stats.app.RedisMigrateToolCenter;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.TypeUtil;
import com.sohu.cache.web.enums.BooleanEnum;
import com.sohu.cache.web.service.AppService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 数据迁移(使用唯品会的开源工具redis-migrate-tool进行迁移)
 *
 * @author leifu
 * @Date 2016-6-8
 * @Time 下午2:54:33
 */
@Service("redisMigrateToolCenter")
public class RedisMigrateToolCenterImpl implements RedisMigrateToolCenter {
    private Logger logger = LoggerFactory.getLogger(RedisMigrateToolCenterImpl.class);
    @Autowired
    private AppService appService;
    @Autowired
    @Lazy
    private RedisCenter redisCenter;
    @Autowired
    private MachineCenter machineCenter;
    @Autowired
    private AppDataMigrateCenter appDataMigrateCenter;
    @Autowired
    private AppDataMigrateStatusDao appDataMigrateStatusDao;

    @Override
    public AppDataMigrateResult check(String migrateMachineIp, AppDataMigrateEnum sourceRedisMigrateEnum,
                                      String sourceServers,
                                      AppDataMigrateEnum targetRedisMigrateEnum, String targetServers, String redisSourcePass, String redisTargetPass,SystemResource resource) {
        // 1. 检查migrateMachineIp是否安装
        AppDataMigrateResult migrateMachineResult = checkMigrateMachine(migrateMachineIp,resource);
        if (!migrateMachineResult.isSuccess()) {
            return migrateMachineResult;
        }
        // 2. 检查源配置
        AppDataMigrateResult sourceResult = checkMigrateConfig(migrateMachineIp, sourceRedisMigrateEnum, sourceServers, redisSourcePass, true);
        if (!sourceResult.isSuccess()) {
            return sourceResult;
        }
        // 3. 检查目标
        AppDataMigrateResult targetResult = checkMigrateConfig(migrateMachineIp, targetRedisMigrateEnum, targetServers, redisTargetPass, false);
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
    private AppDataMigrateResult checkMigrateMachine(String migrateMachineIp,SystemResource resource) {
        if (StringUtils.isBlank(migrateMachineIp)) {
            return AppDataMigrateResult.fail("redis-migrate-tool所在机器的IP不能为空");
        }
        // 1. 检查机器是否存在在机器列表中
        try {
            MachineInfo machineInfo = machineCenter.getMachineInfoByIp(migrateMachineIp);
            if (machineInfo == null) {
                return AppDataMigrateResult.fail(migrateMachineIp + "没有在机器管理列表中");
            } else if (machineInfo.isOffline()) {
                return AppDataMigrateResult.fail(migrateMachineIp + ",该机器已经被删除");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return AppDataMigrateResult.fail("检测发生异常，请观察日志");
        }
        // 2. 检查是否安装redis-migrate-tool
        try {
            String cmd = ConstUtils.getRedisMigrateToolCmd(resource.getName());
            String response = SSHUtil.execute(migrateMachineIp, cmd);
            if (StringUtils.isBlank(response) || !response.contains("source") || !response.contains("target")) {
                return AppDataMigrateResult.fail(migrateMachineIp + "下，" + cmd + "执行失败，请确保redis-migrate-tool安装正确!");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return AppDataMigrateResult.fail("检测发生异常，请观察日志");
        }
        // 3. 检查是否有运行的redis-migrate-tool
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
                                                    String servers, String redisPassword, boolean isSource) {
        //target如果是rdb是没有路径的，不需要检测
        if (isSource || !AppDataMigrateEnum.isFileType(redisMigrateEnum)) {
            if (StringUtils.isBlank(servers)) {
                return AppDataMigrateResult.fail("服务器信息不能为空!");
            }
        }
        List<String> serverList = Arrays.asList(servers.split(ConstUtils.NEXT_LINE));
        if (CollectionUtils.isEmpty(serverList)) {
            return AppDataMigrateResult.fail("服务器信息格式有问题!");
        }
        for (String server : serverList) {
            if (AppDataMigrateEnum.isFileType(redisMigrateEnum)) {
                if (!isSource) {
                    continue;
                }
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
                boolean isRun = redisCenter.isRun(ip, port, redisPassword);
                if (!isRun) {
                    return AppDataMigrateResult.fail(server + "不是存活的或者密码错误!");
                }
            }
        }
        return AppDataMigrateResult.success();
    }

    @Override
    public boolean migrate(String migrateMachineIp, AppDataMigrateEnum sourceRedisMigrateEnum, String sourceServers,
                           AppDataMigrateEnum targetRedisMigrateEnum, String targetServers, long sourceAppId, long targetAppId,
                           String redisSourcePass, String redisTargetPass, long userId, SystemResource resource) {
        // 1. 生成配置
        int migrateMachinePort = ConstUtils.REDIS_MIGRATE_TOOL_PORT;
        String configContent = generateConfig(migrateMachinePort, sourceRedisMigrateEnum, sourceServers, targetRedisMigrateEnum,
                targetServers, redisSourcePass, redisTargetPass);
        // 2. 上传配置
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String confileFileName = "rmt-" + timestamp + ".conf";
        String logFileName = "rmt-" + timestamp + ".log";
        boolean uploadConfig = createRemoteFile(migrateMachineIp, confileFileName, configContent);
        if (!uploadConfig) {
            return false;
        }
        // 3. 开始执行: 指定的配置名、目录、日志名
        String cmd = ConstUtils.getRedisMigrateToolCmd(resource.getName()) + " -c " + ConstUtils.getRedisMigrateToolDir() + confileFileName
                + " -o " + ConstUtils.getRedisMigrateToolDir() + logFileName + " -d";
        logger.warn(cmd);
        try {
            SSHUtil.execute(migrateMachineIp, cmd);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        // 4. 记录执行记录
        AppDataMigrateStatus appDataMigrateStatus = new AppDataMigrateStatus();
        appDataMigrateStatus.setMigrateId(timestamp);
        appDataMigrateStatus.setMigrateTool(1);
        appDataMigrateStatus.setMigrateMachineIp(migrateMachineIp);
        appDataMigrateStatus.setMigrateMachinePort(migrateMachinePort);
        appDataMigrateStatus.setStartTime(new Date());
        appDataMigrateStatus.setSourceMigrateType(sourceRedisMigrateEnum.getIndex());
        appDataMigrateStatus.setSourceServers(sourceServers);
        appDataMigrateStatus.setTargetMigrateType(targetRedisMigrateEnum.getIndex());
        appDataMigrateStatus.setTargetServers(targetServers);
        appDataMigrateStatus.setLogPath(ConstUtils.getRedisMigrateToolDir() + logFileName);
        appDataMigrateStatus.setConfigPath(ConstUtils.getRedisMigrateToolDir() + confileFileName);
        appDataMigrateStatus.setUserId(userId);
        appDataMigrateStatus.setSourceAppId(sourceAppId);
        appDataMigrateStatus.setTargetAppId(targetAppId);
        appDataMigrateStatus.setStatus(AppDataMigrateStatusEnum.PREPARE.getStatus());

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
                                 AppDataMigrateEnum targetRedisMigrateEnum, String targetServers, String redisSourcePass, String redisTargetPass) {
        // source
        StringBuffer config = new StringBuffer();
        config.append("[source]" + ConstUtils.NEXT_LINE);
        config.append("type: " + sourceRedisMigrateEnum.getType() + ConstUtils.NEXT_LINE);
        config.append("servers:" + ConstUtils.NEXT_LINE);
        List<String> sourceServerList = Arrays.asList(sourceServers.split(ConstUtils.NEXT_LINE));
        for (String server : sourceServerList) {
            config.append(" - " + server + ConstUtils.NEXT_LINE);
        }
        if (StringUtils.isNotBlank(redisSourcePass)) {
            config.append("redis_auth: " + redisSourcePass + ConstUtils.NEXT_LINE);
        }
        config.append(ConstUtils.NEXT_LINE);
        // target
        config.append("[target]" + ConstUtils.NEXT_LINE);
        config.append("type: " + targetRedisMigrateEnum.getType() + ConstUtils.NEXT_LINE);
        if (!AppDataMigrateEnum.isFileType(targetRedisMigrateEnum)) {
            config.append("servers:" + ConstUtils.NEXT_LINE);
            List<String> targetServerList = Arrays.asList(targetServers.split(ConstUtils.NEXT_LINE));
            for (String server : targetServerList) {
                config.append(" - " + server + ConstUtils.NEXT_LINE);
            }
            if (StringUtils.isNotBlank(redisTargetPass)) {
                config.append("redis_auth: " + redisTargetPass + ConstUtils.NEXT_LINE);
            }
            config.append(ConstUtils.NEXT_LINE);
        }
        // common:使用最简配置
        config.append("[common]" + ConstUtils.NEXT_LINE);
        config.append("listen: 0.0.0.0:" + listenPort + ConstUtils.NEXT_LINE);
        config.append("dir: " + ConstUtils.getRedisMigrateToolDir());
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
                    logger.error(e.getMessage(), e);
                }
            }
        }
        /**
         * 2. 将配置文件推送到目标机器上
         */
        try {
            SSHUtil.scpFileToRemote(host, localAbsolutePath, ConstUtils.getRedisMigrateToolDir());
        } catch (SSHException e) {
            logger.error("scp rmt file to remote server error: ip: {}, fileName: {}", host, fileName, e);
            return false;
        }
        /**
         * 3. 删除临时文件
         */
        File file = new File(localAbsolutePath);
        if (file.exists()) {
            boolean del = file.delete();
            if (!del) {
                logger.warn("file.delete:{}", del);
            }
        }
        return true;
    }


    @Override
    public CommandResult sampleCheckData(long id, int nums) {
        /*AppDataMigrateStatus appDataMigrateStatus = appDataMigrateStatusDao.get(id);
        if (appDataMigrateStatus == null) {
            return null;
        }
        String ip = appDataMigrateStatus.getMigrateMachineIp();
        String configPath = appDataMigrateStatus.getConfigPath();
        String sampleCheckDataCmd = ConstUtils.getRedisMigrateToolCmd() + " -c " + configPath + " -C" + " 'redis_check " + nums + "'";
        logger.warn("sampleCheckDataCmd: {}", sampleCheckDataCmd);
        try {
            return new CommandResult(sampleCheckDataCmd, SSHUtil.execute(ip, sampleCheckDataCmd));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new CommandResult(sampleCheckDataCmd, ErrorMessageEnum.INNER_ERROR_MSG.getMessage());
        }*/
        return null;
    }

    @Override
    public AppDataMigrateResult stopMigrate(long id) {
        // 获取基本信息
        AppDataMigrateStatus appDataMigrateStatus = appDataMigrateStatusDao.get(id);
        if (appDataMigrateStatus == null) {
            return AppDataMigrateResult.fail("id=" + id + "迁移记录不存在!");
        }
        // 获取进程号
        String migrateMachineIp = appDataMigrateStatus.getMigrateMachineIp();
        String migrateMachineHostPort = migrateMachineIp + ":" + appDataMigrateStatus.getMigrateMachinePort();
        Map<RedisMigrateToolConstant, Map<String, Object>> redisMigrateToolStatMap = appDataMigrateCenter.showMiragteToolProcess(id);
        if (MapUtils.isEmpty(redisMigrateToolStatMap)) {
            return AppDataMigrateResult.fail("获取" + migrateMachineHostPort + "相关信息失败，可能是进程不存在或者客户端超时，请查找原因或重试!");
        }
        Map<String, Object> serverMap = redisMigrateToolStatMap.get(RedisMigrateToolConstant.Server);
        int pid = MapUtils.getInteger(serverMap, "process_id", -1);
        if (pid <= 0) {
            return AppDataMigrateResult.fail("获取" + migrateMachineHostPort + "的进程号" + pid + "异常");
        }
        // 确认进程号是redis-migrate-tool进程
        BooleanEnum exist = checkPidWhetherIsRmt(migrateMachineIp, pid);
        if (exist == BooleanEnum.OTHER) {
            return AppDataMigrateResult.fail("执行过程中发生异常,请查看系统日志!");
        } else if (exist == BooleanEnum.FALSE) {
            return AppDataMigrateResult.fail(migrateMachineIp + "进程号" + pid + "不存在,请确认!");
        }
        // kill掉进程
        try {
            String cmd = "kill " + pid;
            SSHUtil.execute(migrateMachineIp, cmd);
            exist = checkPidWhetherIsRmt(migrateMachineIp, pid);
            if (exist == BooleanEnum.OTHER) {
                return AppDataMigrateResult.fail(ErrorMessageEnum.INNER_ERROR_MSG.getMessage());
            } else if (exist == BooleanEnum.FALSE) {
                // 更新记录完成更新
                appDataMigrateStatusDao.updateStatus(id, AppDataMigrateStatusEnum.END.getStatus());
                return AppDataMigrateResult.success("已经成功停止了id=" + id + "的迁移任务");
            } else {
                return AppDataMigrateResult.fail(migrateMachineIp + "进程号" + pid + "仍然存在,没有kill掉,请确认!");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return AppDataMigrateResult.fail(ErrorMessageEnum.INNER_ERROR_MSG.getMessage());
        }
    }

    @Override
    public String getAppInstanceListForRedisMigrateTool(long appId) {
        AppDesc appDesc = appService.getByAppId(appId);
        StringBuffer instances = new StringBuffer();
        List<InstanceInfo> instanceList = appService.getAppInstanceInfo(appId);
        if (CollectionUtils.isNotEmpty(instanceList)) {
            for (int i = 0; i < instanceList.size(); i++) {
                InstanceInfo instanceInfo = instanceList.get(i);
                if (instanceInfo == null) {
                    continue;
                }
                if (instanceInfo.isOffline()) {
                    continue;
                }
                // 如果是sentinel类型的应用只出master
                if (TypeUtil.isRedisSentinel(appDesc.getType())) {
                    if (TypeUtil.isRedisSentinel(instanceInfo.getType())) {
                        continue;
                    }
                    if (redisCenter.isMaster(appId, instanceInfo.getIp(), instanceInfo.getPort()) != BooleanEnum.TRUE) {
                        continue;
                    }
                }
                instances.append(instanceInfo.getIp() + ":" + instanceInfo.getPort());
                if (i != instanceList.size() - 1) {
                    instances.append(ConstUtils.NEXT_LINE);
                }
            }
        }
        return instances.toString();
    }

    /**
     * 检查pid是否是redis-migrate-tool进程
     *
     * @param migrateMachineIp
     * @param pid
     * @return
     * @throws SSHException
     */
    private BooleanEnum checkPidWhetherIsRmt(String migrateMachineIp, int pid) {
        try {
            String cmd = "/bin/ps -ef | grep redis-migrate-tool | grep -v grep | grep " + pid;
            String response = SSHUtil.execute(migrateMachineIp, cmd);
            if (StringUtils.isNotBlank(response)) {
                return BooleanEnum.TRUE;
            } else {
                return BooleanEnum.FALSE;
            }
        } catch (SSHException e) {
            logger.error(e.getMessage(), e);
            return BooleanEnum.OTHER;
        }
    }
}