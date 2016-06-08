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
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.cache.constant.RedisMigrateEnum;
import com.sohu.cache.constant.RedisMigrateResult;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.entity.MachineInfo;
import com.sohu.cache.exception.SSHException;
import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.protocol.MachineProtocol;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.ssh.SSHUtil;
import com.sohu.cache.stats.app.RedisMigrateCenter;
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
public class RedisMigrateCenterImpl implements RedisMigrateCenter {

    private Logger logger = LoggerFactory.getLogger(RedisMigrateCenterImpl.class);

    private AppService appService;

    private RedisCenter redisCenter;

    private MachineCenter machineCenter;

    @Override
    public RedisMigrateResult check(String migrateMachineIp, RedisMigrateEnum sourceRedisMigrateEnum,
            String sourceServers, long targetAppId) {
        // 1.检查app是否存在
        AppDesc appDesc = appService.getByAppId(targetAppId);
        if (appDesc == null) {
            return RedisMigrateResult.fail("目标appId=" + targetAppId + "不存在");
        }

        // 2.转换Redis类型
        RedisMigrateEnum targetRedisMigrateEnum = TypeUtil.isRedisCluster(appDesc.getType()) ? RedisMigrateEnum.REDIS_CLUSTER_NODE
                : RedisMigrateEnum.REDIS_NODE;

        // 3.转换servers
        List<InstanceInfo> instanceInfoList = appService.getAppInstanceInfo(targetAppId);
        if (CollectionUtils.isEmpty(instanceInfoList)) {
            return RedisMigrateResult.fail("目标appId=" + targetAppId + "不存在任何redis节点!");
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
    public RedisMigrateResult check(String migrateMachineIp, RedisMigrateEnum sourceRedisMigrateEnum,
            String sourceServers,
            RedisMigrateEnum targetRedisMigrateEnum, String targetServers) {

        // 1. 检查migrateMachineIp是否安装
        RedisMigrateResult migrateMachineResult = checkMigrateMachine(migrateMachineIp);
        if (!migrateMachineResult.isSuccess()) {
            return migrateMachineResult;
        }

        // 2. 检查源配置
        RedisMigrateResult sourceResult = checkMigrateConfig(migrateMachineIp, sourceRedisMigrateEnum, sourceServers);
        if (!sourceResult.isSuccess()) {
            return sourceResult;
        }

        // 3. 检查目标
        RedisMigrateResult targetResult = checkMigrateConfig(migrateMachineIp, targetRedisMigrateEnum, targetServers);
        if (!targetResult.isSuccess()) {
            return targetResult;
        }

        return RedisMigrateResult.success();
    }

    /**
     * 检查迁移的机器是否正常 1. 是否安装redis-migrate-tool
     * 2.是否有redis-migrate-tool进程，暂时运行一个迁移任务
     * 
     * @param migrateMachineIp
     * @return
     */
    private RedisMigrateResult checkMigrateMachine(String migrateMachineIp) {
        // 1. 检查机器是否存在在机器列表中
        if (StringUtils.isBlank(migrateMachineIp)) {
            return RedisMigrateResult.fail("redis-migrate-tool所在机器的IP不能为空");
        }
        try {
            MachineInfo machineInfo = machineCenter.getMachineInfoByIp(migrateMachineIp);
            if (machineInfo == null) {
                return RedisMigrateResult.fail(migrateMachineIp + "没有在机器管理列表中");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return RedisMigrateResult.fail("检测发生异常，请观察日志");
        }
        // 2. 检查是否安装redis-migrate-tool
        try {
            String cmd = ConstUtils.REDIS_MIGRATE_TOOL_CMD;
            String response = SSHUtil.execute(migrateMachineIp, cmd);
            if (StringUtils.isBlank(response) || !response.contains("source") || !response.contains("target")) {
                return RedisMigrateResult.fail(migrateMachineIp + "下，" + cmd + "执行失败，请确保redis-migrate-tool安装正确!");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return RedisMigrateResult.fail("检测发生异常，请观察日志");
        }

        // 3. 检查是否有运行的redis-migrate-tool
        // 3.1 从数据库里检测，每次迁移记录迁移的详情@TODO

        // 3.2 查看进程是否存在
        try {
            String cmd = "/bin/ps -ef | grep redis-migrate-tool | grep -v grep";
            String response = SSHUtil.execute(migrateMachineIp, cmd);
            if (StringUtils.isNotBlank(response)) {
                return RedisMigrateResult.fail(migrateMachineIp + "下有redis-migrate-tool进程，请确保只有一台机器只有一个迁移任务进行");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return RedisMigrateResult.fail("检测发生异常，请观察日志");
        }

        return RedisMigrateResult.success();
    }

    /**
     * 检测配置
     * 
     * @param migrateMachineIp
     * @param redisMigrateEnum
     * @param servers
     * @return
     */
    private RedisMigrateResult checkMigrateConfig(String migrateMachineIp, RedisMigrateEnum redisMigrateEnum,
            String servers) {
        if (StringUtils.isNotBlank(servers)) {
            return RedisMigrateResult.fail("服务器信息不能为空!");
        }
        List<String> serverList = Arrays.asList(servers.split(ConstUtils.NEXT_LINE));
        if (CollectionUtils.isEmpty(serverList)) {
            return RedisMigrateResult.fail("服务器信息格式有问题!");
        }
        for (String server : serverList) {
            if (RedisMigrateEnum.RDB_FILE.equals(redisMigrateEnum)) {
                // 检查文件是否存在
                String filePath = server;
                String cmd = "head " + filePath;
                try {
                    String headResult = SSHUtil.execute(migrateMachineIp, cmd);
                    if (StringUtils.isBlank(headResult)) {
                        return RedisMigrateResult.fail(migrateMachineIp + "上的rdb:" + filePath + "不存在或者为空!");
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    return RedisMigrateResult.fail(migrateMachineIp + "上的rdb:" + filePath + "读取异常!");
                }
            } else {
                // 1. 检查是否为ip:port格式(简单检查一下，无需正则表达式)
                // 2. 检查Redis节点是否存在
                String[] instanceItems = server.split(":");
                if (instanceItems.length != 2) {
                    return RedisMigrateResult.fail("实例信息" + server + "格式错误，必须为ip:port格式");
                }
                String ip = instanceItems[0];
                String portStr = instanceItems[1];
                boolean portIsDigit = NumberUtils.isDigits(portStr);
                if (!portIsDigit) {
                    return RedisMigrateResult.fail(server + "中的port不是整数");
                }
                int port = NumberUtils.toInt(portStr);
                boolean isRun = redisCenter.isRun(ip, port);
                if (!isRun) {
                    return RedisMigrateResult.fail(server + "中的节点不是存活的");
                }
            }
        }

        return RedisMigrateResult.success();
    }

    @Override
    public boolean migrate(String migrateMachineIp, RedisMigrateEnum sourceRedisMigrateEnum, String sourceServers,
            RedisMigrateEnum targetRedisMigrateEnum, String targetServers) {
        // 1. 生成配置
        String configContent = generateConfig(sourceRedisMigrateEnum, sourceServers, targetRedisMigrateEnum,
                targetServers);
        // 2. 上传配置
        String fileName = "rmt-" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".conf";
        boolean uploadConfig = createRemoteFile(migrateMachineIp, fileName, configContent);
        if (!uploadConfig) {
            return false;
        }
        // 3. 开始执行: 指定的配置名、目录、日志名
        String cmd = ConstUtils.REDIS_MIGRATE_TOOL_CMD + " -c " + ConstUtils.REDIS_MIGRATE_TOOL_HOME + fileName
                + " -o " + ConstUtils.REDIS_MIGRATE_TOOL_HOME + "rmt.log" + " -d";
        try {
            SSHUtil.execute(migrateMachineIp, cmd);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }

        // 4. 记录执行记录@TODO

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
    private String generateConfig(RedisMigrateEnum sourceRedisMigrateEnum, String sourceServers,
            RedisMigrateEnum targetRedisMigrateEnum, String targetServers) {
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
        config.append("listen: 0.0.0.0:" + ConstUtils.REDIS_MIGRATE_TOOL_PORT);

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

    public void setRedisCenter(RedisCenter redisCenter) {
        this.redisCenter = redisCenter;
    }

    public void setMachineCenter(MachineCenter machineCenter) {
        this.machineCenter = machineCenter;
    }

    public void setAppService(AppService appService) {
        this.appService = appService;
    }

}
