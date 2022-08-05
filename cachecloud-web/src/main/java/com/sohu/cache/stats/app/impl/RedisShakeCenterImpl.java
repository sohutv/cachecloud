package com.sohu.cache.stats.app.impl;

import com.google.common.base.Joiner;
import com.sohu.cache.constant.*;
import com.sohu.cache.dao.AppDataMigrateStatusDao;
import com.sohu.cache.entity.AppDataMigrateStatus;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.entity.SystemResource;
import com.sohu.cache.exception.SSHException;
import com.sohu.cache.protocol.MachineProtocol;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.ssh.SSHService;
import com.sohu.cache.ssh.SSHTemplate;
import com.sohu.cache.ssh.SSHUtil;
import com.sohu.cache.stats.app.RedisShakeCenter;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.TypeUtil;
import com.sohu.cache.web.service.AppService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * Created by rucao on 2019/10/23
 */
@Slf4j
@Service
public class RedisShakeCenterImpl implements RedisShakeCenter {
    @Autowired
    private AppService appService;
    @Autowired
    private RedisCenter redisCenter;
    @Autowired
    private SSHService sshService;
    @Autowired
    private AppDataMigrateStatusDao appDataMigrateStatusDao;

    @Override
    public AppDataMigrateResult check(String migrateMachineIp, AppDataMigrateEnum sourceRedisMigrateEnum, String sourceServers, AppDataMigrateEnum targetRedisMigrateEnum, String targetServers, String redisSourcePass, String redisTargetPass, SystemResource resource) {
        // 1. 检查migrateMachineIp是否安装
        AppDataMigrateResult migrateMachineResult = checkRedisShakeMachine(migrateMachineIp, resource);
        if (!migrateMachineResult.isSuccess()) {
            return migrateMachineResult;
        }

        // 2. 检查源配置
        AppDataMigrateResult sourceResult = checkMigrateConfigOfRedisShake(sourceRedisMigrateEnum, sourceServers, redisSourcePass);
        if (!sourceResult.isSuccess()) {
            return sourceResult;
        }

        // 3. 检查目标
        AppDataMigrateResult targetResult = checkMigrateConfigOfRedisShake(targetRedisMigrateEnum, targetServers, redisTargetPass);
        if (!targetResult.isSuccess()) {
            return targetResult;
        }

        return AppDataMigrateResult.success();
    }

    @Override
    public AppDataMigrateStatus migrate(String migrateMachineIp, int source_rdb_parallel, int parallel,
                                        AppDataMigrateEnum sourceRedisMigrateEnum, String sourceServers,
                                        AppDataMigrateEnum targetRedisMigrateEnum, String targetServers,
                                        long sourceAppId, long targetAppId,
                                        String redisSourcePass, String redisTargetPass,
                                        String redisSourceVersion, String redisTargetVersion,
                                        long userId, SystemResource resource) {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        // 1. 生成配置
        String configContent = generateRedisShakeConfig(timestamp, source_rdb_parallel, parallel,
                sourceRedisMigrateEnum, sourceServers, targetRedisMigrateEnum,
                targetServers, redisSourcePass, redisTargetPass, resource);
        // 2. 上传配置
        String fileName = "redis-shake-";
        String confileFileName = fileName + timestamp + ".conf";
        String logFileName = fileName + timestamp + ".log";
        boolean uploadConfig = createRemoteFile(migrateMachineIp, confileFileName, configContent, resource);
        if (!uploadConfig) {
            return null;
        }
        // 3. 开始执行: 指定的配置名、目录、日志名
        String cmd = "nohup " + ConstUtils.getRedisShakeLinuxCmd(resource.getName()) + " -conf=" + ConstUtils.getRedisShakeConfDir(resource.getName()) + confileFileName + " -type=sync > "+ConstUtils.REDIS_INSTALL_BASE_DIR+"/shake.out 2>&1 &";

        log.warn(cmd);
        try {
            SSHTemplate.Result cmdResult = sshService.executeWithResult(migrateMachineIp, cmd);

            // 4. 记录执行记录
            if (cmdResult.isSuccess()) {
                AppDataMigrateStatus appDataMigrateStatus = new AppDataMigrateStatus();
                appDataMigrateStatus.setMigrateId(timestamp);
                appDataMigrateStatus.setMigrateTool(0);
                appDataMigrateStatus.setMigrateMachineIp(migrateMachineIp);
                appDataMigrateStatus.setStartTime(new Date());
                appDataMigrateStatus.setSourceMigrateType(sourceRedisMigrateEnum.getIndex());
                appDataMigrateStatus.setSourceServers(sourceServers);
                appDataMigrateStatus.setTargetMigrateType(targetRedisMigrateEnum.getIndex());
                appDataMigrateStatus.setTargetServers(targetServers);
                appDataMigrateStatus.setLogPath(ConstUtils.getRedisShakeLogsDir(resource.getName()) + logFileName);
                appDataMigrateStatus.setConfigPath(ConstUtils.getRedisShakeConfDir(resource.getName()) + confileFileName);
                appDataMigrateStatus.setUserId(userId);
                appDataMigrateStatus.setSourceAppId(sourceAppId);
                appDataMigrateStatus.setTargetAppId(targetAppId);
                appDataMigrateStatus.setRedisSourceVersion(redisSourceVersion);
                appDataMigrateStatus.setRedisTargetVersion(redisTargetVersion);
                appDataMigrateStatus.setStatus(AppDataMigrateStatusEnum.PREPARE.getStatus());
                appDataMigrateStatusDao.save(appDataMigrateStatus);
                return appDataMigrateStatus;
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public AppDataMigrateResult stopMigrate(long id) {
        // 获取基本信息
        AppDataMigrateStatus appDataMigrateStatus = appDataMigrateStatusDao.get(id);
        if (appDataMigrateStatus == null) {
            return AppDataMigrateResult.fail("id=" + id + "迁移记录不存在!");
        }
        String migrateMachineIp = appDataMigrateStatus.getMigrateMachineIp();
        // redis-shake stop.sh
        try {
            String configPath = appDataMigrateStatus.getConfigPath();
            ConstUtils.REDIS_SHAKE_HOME = configPath.substring(0, configPath.indexOf("conf"));

            String migrateId = appDataMigrateStatus.getMigrateId();
            String stopCmd = ConstUtils.getRedisShakeStopCmd() + " " + String.format("%s/pid/redis-shake-%s.pid", ConstUtils.REDIS_SHAKE_HOME, migrateId);
            String stopResult = SSHUtil.execute(migrateMachineIp, stopCmd);
            if (StringUtils.isNotEmpty(stopResult) && stopResult.contains("Fail")) {
                if (stopResult.contains("No process number")) {
                    String migrateLog = showProcess(id);
                    if (StringUtils.isNotEmpty(migrateId) && migrateLog.contains(RedisShakeEnum.LOG_ERROR.getKeyword())) {
                        appDataMigrateStatusDao.updateStatus(id, AppDataMigrateStatusEnum.ERROR.getStatus());
                        return AppDataMigrateResult.fail("迁移任务migrate id:" + migrateId + "异常，任务终止");
                    } else {
                        appDataMigrateStatusDao.updateStatus(id, AppDataMigrateStatusEnum.END.getStatus());
                        return AppDataMigrateResult.fail("迁移任务migrate id:" + migrateId + "已经停止");
                    }
                }
                return AppDataMigrateResult.fail("迁移任务id:" + id + stopResult);
            }
            appDataMigrateStatusDao.updateStatus(id, AppDataMigrateStatusEnum.END.getStatus());
            return AppDataMigrateResult.success("已经成功停止了id=" + id + "的迁移任务");
        } catch (Exception e) {
            log.error(e.getMessage());
            return AppDataMigrateResult.fail(ErrorMessageEnum.INNER_ERROR_MSG.getMessage());
        }
    }

    @Override
    public CommandResult checkData(long id, int batchcount, int comparemode) {
        AppDataMigrateStatus appDataMigrateStatus = appDataMigrateStatusDao.get(id);
        if (appDataMigrateStatus == null) {
            return null;
        }
        String ip = appDataMigrateStatus.getMigrateMachineIp();
        String sampleCheckDataCmd = generateRedisFullCheckConfig(appDataMigrateStatus, batchcount, comparemode);
        log.warn("checkDataCmd: {}", sampleCheckDataCmd);
        try {
            String checkResult = SSHUtil.execute(ip, sampleCheckDataCmd);
            if (StringUtils.isNotEmpty(checkResult)) {
                return new CommandResult(sampleCheckDataCmd, ErrorMessageEnum.INNER_ERROR_MSG.getMessage());
            }
            return new CommandResult(sampleCheckDataCmd, checkResult);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new CommandResult(sampleCheckDataCmd, ErrorMessageEnum.INNER_ERROR_MSG.getMessage());
        }
    }

    @Override
    public String getAppInstanceListForRedisShake(long appId) {
        AppDesc appDesc = appService.getByAppId(appId);
        StringBuffer instances = new StringBuffer();
        List<InstanceInfo> instanceList = appService.getAppOnlineInstanceInfo(appId);
        int sentinelFlag = 0;
        if (CollectionUtils.isNotEmpty(instanceList)) {
            for (int i = 0; i < instanceList.size(); i++) {
                InstanceInfo instanceInfo = instanceList.get(i);
                if (instanceInfo == null) {
                    continue;
                }
                if (instanceInfo.isOffline()) {
                    continue;
                }
                // 如果是sentinel类型的应用
                // master_name:master/slave
                // sentinelIp1:sentinelPort1
                if (TypeUtil.isRedisSentinel(appDesc.getType())) {
                    if ("slave".equals(instanceInfo.getRoleDesc())) {
                        continue;
                    } else if ("sentinel".equals(instanceInfo.getRoleDesc())) {
                        if (sentinelFlag == 0) {
                            String sentinel_master_name = instanceInfo.getCmd() + ":master" + ConstUtils.NEXT_LINE;
                            instances.insert(0, sentinel_master_name);
                            sentinelFlag = 1;
                        }
                    } else if ("master".equals(instanceInfo.getRoleDesc())) {
                        continue;
                    }
                }
                if (TypeUtil.isRedisCluster(appDesc.getType())) {
                    if ("slave".equals(instanceInfo.getRoleDesc())) {
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

    @Override
    public String showProcess(long id) {
        AppDataMigrateStatus appDataMigrateStatus = appDataMigrateStatusDao.get(id);
        if (appDataMigrateStatus == null) {
            return "";
        }
        String logPath = appDataMigrateStatus.getLogPath();
        String host = appDataMigrateStatus.getMigrateMachineIp();
        StringBuilder command = new StringBuilder();
        String keyword;
        String result;
        try {
            keyword = RedisShakeEnum.LOG_ERROR.getKeyword();
            command.append("grep ").append(keyword).append(" ").append(logPath).append(" | tail -n 10").toString();
            result = SSHUtil.execute(host, command.toString());
            if (StringUtils.isNotEmpty(result)) {
                return RedisShakeEnum.LOG_ERROR.getDescription() + ConstUtils.NEXT_LINE + result;
            } else {
                command.delete(0, command.length());
                keyword = RedisShakeEnum.LOG_FORWARD_COMMANDS.getKeyword();
                command.append("grep ").append(keyword).append(" ").append(logPath).append(" | tail -n 10").toString();
                result = SSHUtil.execute(host, command.toString());
                if (StringUtils.isNotEmpty(result)) {
                    return RedisShakeEnum.LOG_FORWARD_COMMANDS.getDescription() + ConstUtils.NEXT_LINE + result;
                } else {
                    command.delete(0, command.length());
                    keyword = RedisShakeEnum.LOG_SYNCING.getKeyword();
                    command.append("grep ").append(keyword).append(" ").append(logPath).append(" | tail -n 10").toString();
                    result = SSHUtil.execute(host, command.toString());
                    if (StringUtils.isNotEmpty(result)) {
                        return RedisShakeEnum.LOG_SYNCING.getDescription() + ConstUtils.NEXT_LINE + result;
                    } else {
                        command.delete(0, command.length());
                        keyword = RedisShakeEnum.LOG_WAITING_SOURCE_RDB.getKeyword();
                        command.append("grep ").append(keyword).append(" ").append(logPath).append(" | tail -n 10").toString();
                        result = SSHUtil.execute(host, command.toString());
                        if (StringUtils.isNotEmpty(result)) {
                            return RedisShakeEnum.LOG_WAITING_SOURCE_RDB.getDescription() + ConstUtils.NEXT_LINE + result;
                        }
                    }
                }
            }
            return result;
        } catch (SSHException e) {
            log.error(e.getMessage(), e);
            return "";
        }
    }


    public int updateProcess(long id) {
        AppDataMigrateStatus appDataMigrateStatus = appDataMigrateStatusDao.get(id);
        if (appDataMigrateStatus == null) {
            return 0;
        }
        int migrateStatus = appDataMigrateStatus.getStatus();
        String logPath = appDataMigrateStatus.getLogPath();
        String host = appDataMigrateStatus.getMigrateMachineIp();
        StringBuilder command = new StringBuilder();
        String keyword;
        String result;
        try {
            int logStatus = migrateStatus;
            keyword = RedisShakeEnum.LOG_ERROR.getKeyword();
            command.append("grep ").append(keyword).append(" ").append(logPath).append(" | tail -n 10").toString();
            result = SSHUtil.execute(host, command.toString());
            if (StringUtils.isNotEmpty(result)) {
                logStatus = AppDataMigrateStatusEnum.ERROR.getStatus();
            } else {
                command.delete(0, command.length());
                keyword = RedisShakeEnum.LOG_FORWARD_COMMANDS.getKeyword();
                command.append("grep ").append(keyword).append(" ").append(logPath).append(" | tail -n 10").toString();
                result = SSHUtil.execute(host, command.toString());
                if (StringUtils.isNotEmpty(result)) {
                    logStatus = AppDataMigrateStatusEnum.FULL_END.getStatus();
                } else {
                    command.delete(0, command.length());
                    keyword = RedisShakeEnum.LOG_SYNCING.getKeyword();
                    command.append("grep ").append(keyword).append(" ").append(logPath).append(" | tail -n 10").toString();
                    result = SSHUtil.execute(host, command.toString());
                    if (StringUtils.isNotEmpty(result)) {
                        logStatus = AppDataMigrateStatusEnum.START.getStatus();
                    } else {
                        command.delete(0, command.length());
                        keyword = RedisShakeEnum.LOG_WAITING_SOURCE_RDB.getKeyword();
                        command.append("grep ").append(keyword).append(" ").append(logPath).append(" | tail -n 10").toString();
                        result = SSHUtil.execute(host, command.toString());
                        if (StringUtils.isNotEmpty(result)) {
                            logStatus = AppDataMigrateStatusEnum.PREPARE.getStatus();
                        }
                    }
                }
            }
            if (migrateStatus != logStatus) {
                return appDataMigrateStatusDao.updateStatus(id, logStatus);
            }

        } catch (SSHException e) {
            log.error(e.getMessage(), e);
        }
        return 0;
    }

    /**
     * 检查用redis-shake迁移的机器是否正常
     *
     * @param migrateMachineIp
     * @return
     */
    private AppDataMigrateResult checkRedisShakeMachine(String migrateMachineIp, SystemResource resource) {
        if (StringUtils.isBlank(migrateMachineIp)) {
            return AppDataMigrateResult.fail("redis-migrate-tool所在机器的IP不能为空");
        }
        // 1. 检查机器是否存在在机器列表中
        /*try {
            MachineInfo machineInfo = machineCenter.getMachineInfoByIp(migrateMachineIp);
            if (machineInfo == null) {
                return AppDataMigrateResult.fail(migrateMachineIp + "没有在机器管理列表中");
            } else if (machineInfo.isOffline()) {
                return AppDataMigrateResult.fail(migrateMachineIp + ",该机器已经被删除");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return AppDataMigrateResult.fail("检测发生异常，请观察日志");
        }*/
        //2. 检查是否安装redis-shake
        /*try {
            String cmd = ConstUtils.getRedisShakeLinuxCmd(resource.getName());
            String response = SSHUtil.execute(migrateMachineIp, cmd);
            if (StringUtils.isBlank(response) || !response.contains("start.sh") || !response.contains("conf")) {
                return AppDataMigrateResult.fail(migrateMachineIp + "下，" + cmd + "执行失败，请确保redis-shake安装正确!");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return AppDataMigrateResult.fail("检测发生异常，请观察日志");
        }*/
        //3. 查看进程是数量
        try {
            String cmd = "ps -ef | grep redis-shake | grep -v grep | grep -v tail";
            String response = SSHUtil.execute(migrateMachineIp, cmd);
            if (StringUtils.isNotEmpty(response)) {
                String[] redis_shake_count = response.split(ConstUtils.NEXT_LINE);
                if (redis_shake_count.length >= 3) {
                    return AppDataMigrateResult.fail(migrateMachineIp + "下有" + redis_shake_count.length + "个redis-shake进程，" +
                            "请切换迁移机器或等待其他迁移任务执行完毕后重试");
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return AppDataMigrateResult.fail("检测发生异常，请观察日志");
        }
        return AppDataMigrateResult.success();
    }

    /**
     * redis-shake 检测配置
     *
     * @param redisMigrateEnum
     * @param servers
     * @param redisPassword
     * @return
     */
    private AppDataMigrateResult checkMigrateConfigOfRedisShake(AppDataMigrateEnum redisMigrateEnum,
                                                                String servers, String redisPassword) {
        List<String> serverList = Arrays.asList(servers.split(ConstUtils.NEXT_LINE));
        if (CollectionUtils.isEmpty(serverList)) {
            return AppDataMigrateResult.fail("服务器信息格式有问题!");
        }
        for (int i = 0; i < serverList.size(); i++) {
            // 1. 检查是否为ip:port格式(简单检查一下，无需正则表达式)
            // 2. 检查Redis节点是否存在
            String server = serverList.get(i);
            String[] instanceItems = server.split(":");
            if (instanceItems.length != 2) {
                return AppDataMigrateResult.fail("实例信息" + server + "格式错误，必须为ip:port格式");
            }
            if (redisMigrateEnum == AppDataMigrateEnum.sentinel && i == 0) {
                String sentinel_master_name = instanceItems[0];
                String master_or_slave = instanceItems[1];
                if (!(master_or_slave.equalsIgnoreCase("master") || master_or_slave.equalsIgnoreCase("slave"))) {
                    return AppDataMigrateResult.fail(server + "不是sentinel_master_name:master_or_slave格式");
                }
            } else {
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

    /**
     * 生成redis-shake配置
     *
     * @param migrateId
     * @param sourceRedisMigrateEnum
     * @param sourceServers
     * @param targetRedisMigrateEnum
     * @param targetServers
     * @param redisSourcePass
     * @param redisTargetPass
     * @return
     */
    private String generateRedisShakeConfig(String migrateId, int source_rdb_parallel, int parallel,
                                            AppDataMigrateEnum sourceRedisMigrateEnum, String sourceServers,
                                            AppDataMigrateEnum targetRedisMigrateEnum, String targetServers, String redisSourcePass, String redisTargetPass,
                                            SystemResource resource) {
        StringBuffer config = new StringBuffer();
        config.append("id = redis-shake-" + migrateId + ConstUtils.NEXT_LINE);
        config.append("log.file = " + ConstUtils.getRedisShakeLogsDir(resource.getName()) + "redis-shake-" + migrateId + ".log" + ConstUtils.NEXT_LINE);
        config.append("pid_path = " + ConstUtils.getRedisShakePidDir(resource.getName()) + ConstUtils.NEXT_LINE);
        config.append("rewrite = true" + ConstUtils.NEXT_LINE);
        config.append("metric = false" + ConstUtils.NEXT_LINE);
        //config.append("http_profile = " + ConstUtils.getRedisShakeHttpPort() + ConstUtils.NEXT_LINE);
        config.append("http_profile = -1" + ConstUtils.NEXT_LINE);
        config.append("metric.print_log = false" + ConstUtils.NEXT_LINE);
        config.append("parallel = " + parallel + ConstUtils.NEXT_LINE);
        config.append("source.rdb.parallel = " + source_rdb_parallel + ConstUtils.NEXT_LINE);

        //source
        config.append("source.type = " + sourceRedisMigrateEnum.getType() + ConstUtils.NEXT_LINE);
        config.append("source.address = " + formatAddress(sourceServers, sourceRedisMigrateEnum.getIndex()) + ConstUtils.NEXT_LINE);
        config.append("source.auth_type = auth" + ConstUtils.NEXT_LINE);
        config.append("source.password_raw = " + redisSourcePass + ConstUtils.NEXT_LINE);

        //target
        config.append("target.type = " + targetRedisMigrateEnum.getType() + ConstUtils.NEXT_LINE);
        config.append("target.address = " + formatAddress(targetServers, targetRedisMigrateEnum.getIndex()) + ConstUtils.NEXT_LINE);
        config.append("target.auth_type = auth" + ConstUtils.NEXT_LINE);
        config.append("target.password_raw = " + redisTargetPass + ConstUtils.NEXT_LINE);

        return config.toString();
    }

    private String formatAddress(String servers, int migrateType) {
        List<String> sourceServerList = Arrays.asList(servers.split(ConstUtils.NEXT_LINE));
        String address = Joiner.on(ConstUtils.SEMICOLON).join(sourceServerList);
        if (migrateType == AppDataMigrateEnum.sentinel.getIndex()) {
            return address.replaceFirst(ConstUtils.SEMICOLON, ConstUtils.AT);
        }
        return address;
    }

    private String formatAddressForRedisFullCheck(long appId, String servers, int migrateType) {
        if (migrateType == AppDataMigrateEnum.sentinel.getIndex()) {
            List<InstanceInfo> instanceList = appService.getAppInstanceInfo(appId);
            if (CollectionUtils.isNotEmpty(instanceList)) {
                for (int i = 0; i < instanceList.size(); i++) {
                    InstanceInfo instanceInfo = instanceList.get(i);
                    if ("master".equals(instanceInfo.getRoleDesc())) {
                        return instanceInfo.getIp() + ":" + instanceInfo.getPort();
                    }
                }
            }
            return "";
        } else {
            return formatAddress(servers, migrateType);
        }
    }

    private String generateRedisFullCheckConfig(AppDataMigrateStatus appDataMigrateStatus, int nums, int comparemode) {
        StringBuffer cmd = new StringBuffer();
        String migrateId = appDataMigrateStatus.getMigrateId();
        cmd.append(ConstUtils.getRedisFullCheckCmd() + ConstUtils.SPACE);// 异步命令
        cmd.append("-s " + "\"" + formatAddressForRedisFullCheck(appDataMigrateStatus.getSourceAppId(), appDataMigrateStatus.getSourceServers(), appDataMigrateStatus.getSourceMigrateType()) + "\"" + ConstUtils.SPACE);
        cmd.append("-p " + appService.getByAppId(appDataMigrateStatus.getSourceAppId()).getAppPassword() + ConstUtils.SPACE);
        cmd.append("--sourcedbtype=" + getCompareType(appDataMigrateStatus.getSourceMigrateType()) + ConstUtils.SPACE);//源库的类别，0：db(standalone单节点、主从)，1: cluster（集群版），2: 阿里云
        cmd.append("-t " + "\"" + formatAddressForRedisFullCheck(appDataMigrateStatus.getTargetAppId(), appDataMigrateStatus.getTargetServers(), appDataMigrateStatus.getTargetMigrateType()) + "\"" + ConstUtils.SPACE);
        cmd.append("-a " + appService.getByAppId(appDataMigrateStatus.getTargetAppId()).getAppPassword() + ConstUtils.SPACE);
        cmd.append("--targetdbtype=" + getCompareType(appDataMigrateStatus.getTargetMigrateType()) + ConstUtils.SPACE);
        cmd.append("--comparetimes=1" + ConstUtils.SPACE);//比较轮数
        cmd.append("-m " + comparemode + ConstUtils.SPACE);//比较模式，1表示全量比较，2表示只对比value的长度，3只对比key是否存在，4全量比较的情况下，忽略大key的比较
        cmd.append("--batchcount=" + nums + ConstUtils.SPACE);//批量聚合的数量
        cmd.append("--log=" + ConstUtils.getRedisFullCheckResultDir() + "log-" + migrateId + ".log" + ConstUtils.SPACE);//log文件
        //cmd.append("--metric=" + ConstUtils.getRedisFullCheckResultDir() + "metric-" + migrateId + ConstUtils.SPACE); // todo metric文件，报错
        cmd.append("-d " + ConstUtils.getRedisFullCheckResultDir() + "key-" + migrateId + ".db" + ConstUtils.SPACE); //异常数据列表保存的文件名称
        cmd.append("--result=" + ConstUtils.getRedisFullCheckResultDir() + "result-" + migrateId + ".log" + ConstUtils.SPACE);//不一致结果记录到result文件中，格式：'db diff-type key field';
        return cmd.toString();
    }

    private int getCompareType(int migrateType) {
        if (migrateType == AppDataMigrateEnum.standalone.getIndex()
                || migrateType == AppDataMigrateEnum.sentinel.getIndex()
                || migrateType == AppDataMigrateEnum.REDIS_NODE.getIndex()) {
            return 0;
        } else if (migrateType == AppDataMigrateEnum.cluster.getIndex()) {
            return 1;
        }
        return -1;
    }

    /**
     * 创建远程文件
     *
     * @param host
     * @param fileName
     * @param content
     */
    public boolean createRemoteFile(String host, String fileName, String content, SystemResource resource) {
        /**
         * 1. 创建本地文件
         */
        // 确认目录
        String localAbsolutePath = MachineProtocol.TMP_DIR + fileName;
        File tmpDir = new File(MachineProtocol.TMP_DIR);
        if (!tmpDir.exists()) {
            if (!tmpDir.mkdirs()) {
                log.error("cannot create /tmp/cachecloud directory.");
            }
        }
        Path path = Paths.get(MachineProtocol.TMP_DIR + fileName);
        // 将配置文件的内容写到本地
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = Files.newBufferedWriter(path, Charset.forName(MachineProtocol.ENCODING_UTF8));
            bufferedWriter.write(content);
        } catch (IOException e) {
            log.error("write rmt file error, ip: {}, filename: {}, content: {}", host, fileName, content, e);
            return false;
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }

        /**
         * 2. 将配置文件推送到目标机器上
         */
        try {
            String redisShakeConfDir = ConstUtils.getRedisShakeConfDir(resource.getName());
            String redisShakeLogDir = ConstUtils.getRedisShakeLogsDir(resource.getName());
            String redisShakePidDir = ConstUtils.getRedisShakePidDir(resource.getName());
            SSHUtil.execute(host, String.format("mkdir -p %s %s %s", redisShakeConfDir, redisShakeLogDir, redisShakePidDir));
            SSHUtil.scpFileToRemote(host, localAbsolutePath, redisShakeConfDir);
        } catch (SSHException e) {
            log.error("scp rmt file to remote server error: ip: {}, fileName: {}", host, fileName, e);
            return false;
        }

        /**
         * 3. 删除临时文件
         */
        File file = new File(localAbsolutePath);
        if (file.exists()) {
            boolean del = file.delete();
            if (!del) {
                log.warn("file.delete:{}", del);
            }
        }

        return true;
    }

}
