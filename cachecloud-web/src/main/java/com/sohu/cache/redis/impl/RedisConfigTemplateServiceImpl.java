package com.sohu.cache.redis.impl;

import com.sohu.cache.constant.InstanceStatusEnum;
import com.sohu.cache.dao.InstanceConfigDao;
import com.sohu.cache.dao.MachineDao;
import com.sohu.cache.dao.ResourceDao;
import com.sohu.cache.entity.*;
import com.sohu.cache.exception.SSHException;
import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.protocol.MachineProtocol;
import com.sohu.cache.protocol.RedisProtocol;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.redis.RedisConfigTemplateService;
import com.sohu.cache.redis.RedisDeployCenter;
import com.sohu.cache.redis.enums.DirEnum;
import com.sohu.cache.redis.enums.RedisClusterConfigEnum;
import com.sohu.cache.redis.enums.RedisConfigEnum;
import com.sohu.cache.redis.enums.RedisSentinelConfigEnum;
import com.sohu.cache.ssh.SSHService;
import com.sohu.cache.ssh.SSHTemplate;
import com.sohu.cache.ssh.SSHUtil;
import com.sohu.cache.stats.instance.InstanceDeployCenter;
import com.sohu.cache.task.constant.ResourceEnum;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.RedisConstUtils;
import com.sohu.cache.util.TypeUtil;
import com.sohu.cache.web.enums.SuccessEnum;
import com.sohu.cache.web.service.AppService;
import com.sohu.cache.web.service.ResourceService;
import com.sohu.cache.web.util.DateUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * redis配置模板服务
 *
 * @author leifu
 * @Date 2016年6月23日
 * @Time 下午2:08:03
 */
@Service("redisConfigTemplateService")
public class RedisConfigTemplateServiceImpl implements RedisConfigTemplateService {

    private Logger logger = LoggerFactory.getLogger(RedisConfigTemplateServiceImpl.class);

    private final static String SPECIAL_EMPTY_STR = "\"\"";
    @Autowired
    private InstanceConfigDao instanceConfigDao;
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private ResourceDao resourceDao;
    @Autowired
    private MachineDao machineDao;
    @Autowired
    private AppService appService;
    @Autowired
    @Lazy
    private RedisCenter redisCenter;
    @Autowired
    private RedisDeployCenter redisDeployCenter;
    @Autowired
    private InstanceDeployCenter instanceDeployCenter;
    @Autowired
    @Lazy
    private MachineCenter machineCenter;
    @Autowired
    SSHService sshService;

    private static int RETRY_TIMES = 30;

    private static long SLEEP_MILLS = 2000;

    private static int WAITING_RESOURCE_SECOND = 5;

    private static int WAITING_RETRY_TIMES = 10;

    private static int DOWNLOAD_SECONDS = 60 * 5 * 1000;

    private static String DOWNLOAD_CMD = "cd %s && wget %s && tar -xvf %s && rm -rf %s ";

    @Override
    public List<InstanceConfig> getAllInstanceConfig() {
        try {
            return instanceConfigDao.getAllInstanceConfig();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<InstanceConfig> getByType(int type) {
        try {
            return instanceConfigDao.getByType(type);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public List<InstanceConfig> getByVesionAndType(int type, int versionId) {
        try {
            return instanceConfigDao.getByVersionAndType(type, versionId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public List<InstanceConfig> getByVesion(int versionId) {
        try {
            return instanceConfigDao.getByVersion(versionId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public int saveOrUpdate(InstanceConfig instanceConfig) {
        return instanceConfigDao.saveOrUpdate(instanceConfig);
    }

    @Override
    public InstanceConfig getById(long id) {
        try {
            return instanceConfigDao.getById(id);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public InstanceConfig getByConfigKeyAndType(String configKey, int type) {
        try {
            return instanceConfigDao.getByConfigKeyAndType(configKey, type);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public int remove(long id) {
        return instanceConfigDao.remove(id);
    }

    @Override
    public int updateStatus(long id, int status) {
        return instanceConfigDao.updateStatus(id, status);
    }


    @Override
    public List<String> handleCommonConfig(String host, int port, int maxMemory, int versionId) {
        //2018-08-24
        List<InstanceConfig> instanceConfigList = getByVesionAndType(ConstUtils.CACHE_REDIS_STANDALONE, versionId);
        if (CollectionUtils.isEmpty(instanceConfigList)) {
            return Collections.emptyList();
        }
        List<String> configs = new ArrayList<String>();
        for (InstanceConfig instanceConfig : instanceConfigList) {
            // 无效配置过滤
            if (!instanceConfig.isEffective()) {
                continue;
            }
            String configKey = instanceConfig.getConfigKey();
            String configValue = instanceConfig.getConfigValue();
            if (StringUtils.isBlank(configValue)) {
                configValue = SPECIAL_EMPTY_STR;
            }
            if (RedisConfigEnum.MAXMEMORY.getKey().equals(configKey)) {
                configValue = String.format(configValue, maxMemory);
            } else if (RedisConfigEnum.DBFILENAME.getKey().equals(configKey)
                    || RedisConfigEnum.APPENDFILENAME.getKey().equals(configKey) || RedisConfigEnum.PORT.getKey().equals(configKey)) {
                configValue = String.format(configValue, port);
            } else if (RedisConfigEnum.DIR.getKey().equals(configKey)) {
                configValue = machineCenter.getMachineRelativeDir(host, DirEnum.DATA_DIR.getValue());
            } else if (RedisConfigEnum.AUTO_AOF_REWRITE_PERCENTAGE.getKey().equals(configKey)) {
                //随机比例 auto-aof-rewrite-percentage
                int percent = 69 + new Random().nextInt(30);
                configValue = String.format(configValue, percent);
            } else if (RedisConfigEnum.BIND.getKey().equals(configKey)) {
                configValue = String.format(configValue, host);
            }
            configs.add(combineConfigKeyValue(configKey, configValue));
        }
        return configs;
    }

    @Override
    public List<String> handleSentinelConfig(String masterName, String host, int port, String sentinelHost, int sentinelPort, int versionId) {
        List<InstanceConfig> instanceConfigList = getByVesionAndType(ConstUtils.CACHE_REDIS_SENTINEL, versionId);
        if (CollectionUtils.isEmpty(instanceConfigList)) {
            return Collections.emptyList();
        }
        List<String> configs = new ArrayList<String>();
        for (InstanceConfig instanceConfig : instanceConfigList) {
            if (!instanceConfig.isEffective()) {
                continue;
            }
            String configKey = instanceConfig.getConfigKey();
            String configValue = instanceConfig.getConfigValue();
            if (StringUtils.isBlank(configValue)) {
                configValue = SPECIAL_EMPTY_STR;
            }
            if (RedisSentinelConfigEnum.PORT.getKey().equals(configKey)) {
                configValue = String.format(configValue, sentinelPort);
            } else if (RedisSentinelConfigEnum.MONITOR.getKey().equals(configKey)) {
                configValue = String.format(configValue, masterName, host, port);
            } else if (RedisSentinelConfigEnum.DOWN_AFTER_MILLISECONDS.getKey().equals(configKey) || RedisSentinelConfigEnum.FAILOVER_TIMEOUT.getKey().equals(configKey) || RedisSentinelConfigEnum.PARALLEL_SYNCS.getKey().equals(configKey)) {
                configValue = String.format(configValue, masterName);
            } else if (RedisConfigEnum.DIR.getKey().equals(configKey)) {
                configValue = machineCenter.getMachineRelativeDir(sentinelHost, DirEnum.DATA_DIR.getValue());
            }
            configs.add(combineConfigKeyValue(configKey, configValue));
        }
        return configs;
    }

    @Override
    public List<String> handleClusterConfig(int port, int versionId) {
        List<InstanceConfig> instanceConfigList = getByVesionAndType(ConstUtils.CACHE_TYPE_REDIS_CLUSTER, versionId);
        if (CollectionUtils.isEmpty(instanceConfigList)) {
            return Collections.emptyList();
        }
        List<String> configs = new ArrayList<String>();
        for (InstanceConfig instanceConfig : instanceConfigList) {
            if (!instanceConfig.isEffective()) {
                continue;
            }
            String configKey = instanceConfig.getConfigKey();
            String configValue = instanceConfig.getConfigValue();
            if (StringUtils.isBlank(configValue)) {
                configValue = SPECIAL_EMPTY_STR;
            }
            if (RedisClusterConfigEnum.CLUSTER_CONFIG_FILE.getKey().equals(configKey)) {
                configValue = String.format(configValue, port);
            }
            configs.add(combineConfigKeyValue(configKey, configValue));

        }
        return configs;
    }

    @Override
    public List<String> handleCommonDefaultConfig(int port, int maxMemory) {
        List<String> configs = new ArrayList<String>();
        for (RedisConfigEnum config : RedisConfigEnum.values()) {
            if (RedisConfigEnum.MAXMEMORY.equals(config)) {
                configs.add(config.getKey() + " " + String.format(config.getValue(), maxMemory));
            } else if (RedisConfigEnum.DBFILENAME.equals(config) ||
                    RedisConfigEnum.APPENDFILENAME.equals(config) || RedisConfigEnum.PORT.equals(config)) {
                configs.add(config.getKey() + " " + String.format(config.getValue(), port));
            } else if (RedisConfigEnum.DIR.equals(config)) {
                configs.add(config.getKey() + " " + MachineProtocol.DATA_DIR);
            } else if (RedisConfigEnum.AUTO_AOF_REWRITE_PERCENTAGE.equals(config)) {
                //随机比例 auto-aof-rewrite-percentage
                int percent = 69 + new Random().nextInt(30);
                configs.add(config.getKey() + " " + String.format(RedisConfigEnum.AUTO_AOF_REWRITE_PERCENTAGE.getValue(), percent));
            } else {
                configs.add(config.getKey() + " " + config.getValue());
            }
        }
        return configs;
    }

    @Override
    public List<String> handleSentinelDefaultConfig(String masterName, String host, int port, int sentinelPort) {
        List<String> configs = new ArrayList<String>();
        configs.add(RedisSentinelConfigEnum.PORT.getKey() + " " + String.format(RedisSentinelConfigEnum.PORT.getValue(), sentinelPort));
        configs.add(RedisSentinelConfigEnum.DIR.getKey() + " " + RedisSentinelConfigEnum.DIR.getValue());
        configs.add(RedisSentinelConfigEnum.MONITOR.getKey() + " " + String.format(RedisSentinelConfigEnum.MONITOR.getValue(), masterName, host, port, 1));
        configs.add(RedisSentinelConfigEnum.DOWN_AFTER_MILLISECONDS.getKey() + " " + String
                .format(RedisSentinelConfigEnum.DOWN_AFTER_MILLISECONDS.getValue(), masterName));
        configs.add(RedisSentinelConfigEnum.FAILOVER_TIMEOUT.getKey() + " " + String
                .format(RedisSentinelConfigEnum.FAILOVER_TIMEOUT.getValue(), masterName));
        configs.add(RedisSentinelConfigEnum.PARALLEL_SYNCS.getKey() + " " + String
                .format(RedisSentinelConfigEnum.PARALLEL_SYNCS.getValue(), masterName));
        return configs;
    }

    @Override
    public List<String> handleClusterDefaultConfig(int port) {
        List<String> configs = new ArrayList<String>();
        for (RedisClusterConfigEnum config : RedisClusterConfigEnum.values()) {
            if (config.equals(RedisClusterConfigEnum.CLUSTER_CONFIG_FILE)) {
                configs.add(RedisClusterConfigEnum.CLUSTER_CONFIG_FILE.getKey() + " "
                        + String.format(RedisClusterConfigEnum.CLUSTER_CONFIG_FILE.getValue(), port));
            } else {
                configs.add(config.getKey() + " "
                        + config.getValue());
            }
        }
        return configs;
    }

    public SystemResource getRedisVersionByName(String versionName) {
        return resourceDao.getResourceByName(versionName);
    }

    public String copyRedisConfig(int versionCopyId, SystemResource resource) {

        SuccessEnum successEnum = null;
        try {
            //1.获取拷贝redis所有配置项
            List<InstanceConfig> configByRedisVersionId = instanceConfigDao.getConfigByRedisVersionId(versionCopyId);
            int index = 1;
            for (InstanceConfig instanceConfig : configByRedisVersionId) {
                logger.info("[" + (index++) + "] key :" + instanceConfig.getConfigKey() + " , value :" + instanceConfig.getConfigValue());
                //2.插入到新的redis版本模板中
                instanceConfig.setId(0);
                instanceConfig.setVersionId(resource.getId());
                instanceConfigDao.saveOrUpdate(instanceConfig);
            }
            successEnum = SuccessEnum.SUCCESS;
        } catch (Exception e) {
            successEnum = SuccessEnum.FAIL;
            // 需要当前清除状态
            logger.info(e.getMessage(), e);
        }

        return successEnum.info();
    }

    public String updateMachineInstallRedis(String host) {
        // 1.获取所有Redis有效版本
        List<SystemResource> resourceList = resourceDao.getResourceList(ResourceEnum.REDIS.getValue());
        // 2.拼装命令cmd
        String cmds = ""; // 执行命令
        String versions = "";//版本信息
        String result = ""; // ssh结果
        StringBuilder versionsBuilder = new StringBuilder();
        StringBuilder cmdsBuilder = new StringBuilder();
        if (resourceList != null && resourceList.size() > 0) {
            for (SystemResource redisVersion : resourceList) {
                versionsBuilder.append(redisVersion.getName()).append(";");
                cmdsBuilder.append("cat ").append(redisVersion.getDir()).append("/src/redis-server | wc -l ").append(";");
            }
            versions = versionsBuilder.toString();
            cmds = cmdsBuilder.toString();
            versions = versions.substring(0, versions.lastIndexOf(";"));
        }
        // 3.执行ssh命令
        try {
            result = SSHUtil.execute(host, cmds);
            logger.info("execute cmd result：" + result);
        } catch (SSHException e) {
            e.printStackTrace();
            logger.error("check redis-server machine ip:{}  execute cmds:{} error", host, cmds);
            return SuccessEnum.FAIL.info();
        }

        // 4.更新机器版本状态
        MachineInfo machineInfo = machineDao.getMachineInfoByIp(host);
        if (machineInfo != null) {
            StringBuilder versionStrBuilder = new StringBuilder();
            // 版本#flag;
            String[] versionRes = result.split("\n");
            for (int index = 0; index < versionRes.length; index++) {
                try {
                    // 0:未安装 1:安装成功 -1:安装过程中异常
                    int flag = NumberUtils.toInt(versionRes[index]) > 0 ? 1 : 0;
                    versionStrBuilder.append(versions.split(";")[index])
                            .append("#")
                            .append(flag)
                            .append(";");
                } catch (Exception e) {
                    versionStrBuilder.append(versions.split(";")[index])
                            .append("#-1;");
                }
            }
            machineInfo.setVersionInstall(versionStrBuilder.toString());
            // 更新机器的版本情况
            machineDao.saveMachineInfo(machineInfo);
        } else {
            logger.error("machine ip:{}  is not exist", host);
            return SuccessEnum.FAIL.info();
        }
        return SuccessEnum.SUCCESS.info();
    }

    /**
     * 检查redis编译版本
     *
     * @param host
     * @param redisDir
     */
    public Boolean checkMachineRedisVersion(String host, String redisDir) {
        // 1.param check
        if (StringUtils.isEmpty(redisDir)) {
            logger.warn("redisDir is empty :{}", redisDir);
            return false;
        }
        String cmds = "cat " + redisDir + "/src/redis-server | wc -l ";
        // 2.执行ssh命令
        try {
            String result = SSHUtil.execute(host, cmds);
            logger.info("execute cmds:{} result:{}", cmds, result);
            return NumberUtils.toInt(result) > 0 ? true : false;
        } catch (SSHException e) {
            e.printStackTrace();
            logger.error("check redis-server machine ip:{}  execute cmds:{} error", host, cmds);
            return false;
        }
    }

    public Boolean checkMachineRedisTool(String host, String redisDir) {
        // 1.param check
        if (StringUtils.isEmpty(redisDir)) {
            logger.warn("redisDir is empty :{}", redisDir);
            return false;
        }
        String cmds = "ls -l " + redisDir + "/redis-shake.linux | wc -l ";
        // 2.执行ssh命令
        try {
            String result = SSHUtil.execute(host, cmds);
            logger.info("execute cmds:{} result:{}", cmds, result);
            return NumberUtils.toInt(result) > 0 ? true : false;
        } catch (SSHException e) {
            e.printStackTrace();
            logger.error("check checkMachineRedisTool machine ip:{}  execute cmds:{} error", host, cmds);
            return false;
        }
    }

    @Override
    public Boolean checkAndInstallRedisResource(String host, SystemResource redisResource) {

        Boolean redisInstallFlag = true;
        // 检测是否安装需要redis版本
        String redisDir = ConstUtils.getRedisDir(redisResource.getName());
        if (!checkMachineRedisVersion(host, redisDir)) {
            installRedisOnMachine(host, redisResource);
            // 验证安装是否成功,最多重试10次
            for (int retry = 1; retry <= WAITING_RETRY_TIMES; retry++) {
                if (checkMachineRedisVersion(host, redisDir)) {
                    logger.info("checkAndInstallRedisResource machine:{} install {} ok!", host, redisResource.getName());
                    redisInstallFlag = true;
                    break;
                }
                redisInstallFlag = false;
                logger.info("checkAndInstallRedisResource machine:{} install {} fail status:{} ,retry times :{} !", host, redisResource.getName(), redisInstallFlag, retry);
                try {
                    TimeUnit.SECONDS.sleep(WAITING_RESOURCE_SECOND);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return redisInstallFlag;
    }

    public Boolean checkAndInstallRedisTool(String host, SystemResource redisResource) {

        Boolean redisInstallFlag = true;
        // 检测是否安装需要redis版本
        String redisDir = ConstUtils.getRedisDir(redisResource.getName());
        if (!checkMachineRedisTool(host, redisDir)) {
            installRedisOnMachine(host, redisResource);
            // 验证安装是否成功,最多重试10次
            for (int retry = 1; retry <= WAITING_RETRY_TIMES; retry++) {
                if (checkMachineRedisTool(host, redisDir)) {
                    logger.info("checkAndInstallRedisResource machine:{} install {} ok!", host, redisResource.getName());
                    redisInstallFlag = true;
                    break;
                }
                redisInstallFlag = false;
                logger.info("checkAndInstallRedisResource machine:{} install {} fail status:{} ,retry times :{} !", host, redisResource.getName(), redisInstallFlag, retry);
                try {
                    TimeUnit.SECONDS.sleep(WAITING_RESOURCE_SECOND);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return redisInstallFlag;
    }

    /**
     * @param host
     * @param systemResource
     * @return
     */
    public String installRedisOnMachine(String host, SystemResource systemResource) {

        // 1.远程仓库获取资源
        SystemResource repository = resourceService.getRepository();
        if (repository == null) {
            logger.error("repository is empty :{}  ", repository);
            return SuccessEnum.FAIL.info();
        }
        // 2.资源信息拼接
        String fileName = systemResource.getName() + RedisConstUtils.REDIS_INSTALL_MAKE_PACKAGE_SUFFIX;
        String downloadurl = repository.getUrl() + systemResource.getDir() + "/" + fileName;
        String makeFilePath = ConstUtils.REDIS_INSTALL_BASE_DIR + "/" + fileName;
        String download_cmd = String.format(DOWNLOAD_CMD, ConstUtils.REDIS_INSTALL_BASE_DIR, downloadurl, makeFilePath, makeFilePath);
        String temp_file = RedisConstUtils.REDIS_SHELL_DIR + "execute.sh";

        String execute_cmd = "mkdir -p " + RedisConstUtils.REDIS_SHELL_DIR + " && echo \"" + download_cmd + "\" > " + temp_file + " && sh " + temp_file + "> " + String.format(RedisConstUtils.REDIS_INSTALL_LOG, DateUtil.formatYYYYMMddHHMMss(new Date())) + " 2>&1 &";
        // 3.执行ssh
        try {
            SSHTemplate.Result result = sshService.executeWithResult(host, execute_cmd, DOWNLOAD_SECONDS);
            logger.info("execute_cmd :{} result:{}", execute_cmd, result);
        } catch (SSHException e) {
            logger.info(e.getMessage(), e);
            logger.error("install redis machine ip:{}  execute cmd:{} error", host, download_cmd);
            return SuccessEnum.FAIL.info();
        }
        return SuccessEnum.SUCCESS.info();
    }

    public Map<String, Object> slaveUpdateConfig(long appId, Integer upgradeVersionId, String upgradeVersionName) {

        Map<String, Object> resultMap = new HashMap<String, Object>();
        SuccessEnum successEnum = null;
        // 1.获取所有配置项
        AppDesc appDesc = appService.getByAppId(appId);
        /**
         *  2.配置更新步骤
         *  2.1 备份slave配置 -> 2.2 slave shutdown -> 2.3 替换slave配置 -> 2.4 slave start
         */
        List<InstanceInfo> instanceList = appService.getAppInstanceInfo(appId);
        // 2.1 备份配置
        String instanceInfo = "";
        String instanceLog = "";
        StringBuilder instanceInfoBuilder = new StringBuilder();
        StringBuilder instanceLogBuilder = new StringBuilder();
        if (instanceList != null && instanceList.size() > 0) {
            for (InstanceInfo instance : instanceList) {
                if (instance.getStatus() == InstanceStatusEnum.GOOD_STATUS.getStatus() && instance.getRoleDesc().equals("slave")) {
                    // slave节点 & instanceId
                    String ip = instance.getIp();
                    int port = instance.getPort();
                    int mem = instance.getMem();
                    int instanceId = instance.getId();
                    String bakTime = DateUtil.formatYYYYMMddHHMMss(new Date());

                    // redis实例版本检查,比较版本号
                    String redisVersion = redisCenter.getRedisVersion(appId, instance.getIp(), instance.getPort());
                    int versionTag = Integer.parseInt(redisVersion.substring(redisVersion.lastIndexOf(".") + 1));
                    int targetVersionTag = Integer.parseInt(upgradeVersionName.substring(upgradeVersionName.lastIndexOf(".") + 1));
                    logger.info("current redis version:{} , target redis version:{}", versionTag, targetVersionTag);
                    if (versionTag == targetVersionTag) {
                        instanceInfoBuilder.append(instance.getIp())
                                .append(":")
                                .append(instance.getPort())
                                .append(" ")
                                .append(instance.getRoleDesc())
                                .append(" version:")
                                .append(redisVersion)
                                .append(" 等于当前版本,不需升级\n");
                        //instanceLog += "<br/>";
                        instanceLogBuilder.append("<br/>");
                        continue;
                    } else if (versionTag > targetVersionTag) {
                        instanceInfoBuilder.append(instance.getIp())
                                .append(":")
                                .append(instance.getPort())
                                .append(" ")
                                .append(instance.getRoleDesc())
                                .append(" version:")
                                .append(redisVersion)
                                .append(" 高于当前版本,不需升级\n");
                        //instanceLog += "<br/>";
                        instanceLogBuilder.append("<br/>");
                        continue;
                    }

                    // 1).备份配置
                    String bakCommonConfig = "";
                    Boolean isCluster = true;
                    Boolean isInstallModule = false;
                    String moduleCommand = "";
                    try {
                        //1.1) 备份配置
                        String confDir = machineCenter.getMachineRelativeDir(instance.getIp(), DirEnum.CONF_DIR.getValue());
                        String confPath = "";
                        if (TypeUtil.isRedisCluster(instance.getType())) {
                            bakCommonConfig = "cp -rf " + confDir + RedisProtocol.getConfig(port, true) + " " + confDir + RedisProtocol.getConfig(port, true) + ".bak" + bakTime;
                            confPath = confDir + RedisProtocol.getConfig(port, true);
                        } else {
                            bakCommonConfig = "cp -rf " + confDir + RedisProtocol.getConfig(port, false) + " " + ".conf " + confDir + RedisProtocol.getConfig(port, false) + ".bak" + bakTime;
                            confPath = confDir + RedisProtocol.getConfig(port, false);
                            isCluster = false;
                        }
                        SSHUtil.execute(ip, bakCommonConfig);
                        //1.2) 扫描是否有插件
                        String checkModuleCommand = String.format("cat %s | grep loadmodule",confPath);
                        SSHTemplate.Result result = sshService.executeWithResult(ip, checkModuleCommand);
                        if (result.isSuccess() && !StringUtils.isEmpty(result.getResult()) && result.getResult().indexOf("loadmodule") > -1) {
                            isInstallModule = true;
                            moduleCommand = result.getResult();
                        }

                        logger.info("checkModuleCommand :{} isInstallModule:{} moduleCommand:{}", checkModuleCommand, isInstallModule, moduleCommand);
                    } catch (SSHException e) {
                        logger.error(String.format("ip：%s bak config error:%s", ip, e.getMessage()));
                        resultMap.put("message", "备份配置异常,请查看日志!");
                        break;
                    }
                    // 2).关闭redis
                    boolean closeOp = instanceDeployCenter.shutdownExistInstance(appId, instanceId);
                    // 3).生成新配置 & 4).启动redis
                    // 3.1) 是否有module插件, 需要加载配置 loadmodule ${modulepath}
                    try {
                        appDesc.setVersionId(upgradeVersionId);
                        boolean bornConf = redisDeployCenter.bornConfigAndRunNode(appDesc, instance, ip, port, mem, isCluster, isInstallModule, moduleCommand);
                        if (bornConf == false) {
                            resultMap.put("message", "启动失败,查看日志!");
                            break;
                        }
                    } catch (Exception e) {
                        resultMap.put("message", "启动失败,查看日志!");
                        break;
                    }
                    // 5).记录日志
                    redisVersion = redisCenter.getRedisVersion(appId, instance.getIp(), instance.getPort());
                    instanceInfoBuilder.append(instance.getIp())
                            .append(":")
                            .append(instance.getPort())
                            .append(" ")
                            .append(instance.getRoleDesc())
                            .append(" version:")
                            .append(redisVersion)
                            .append(" 更新成功\n");
                    instanceInfo = instanceInfoBuilder.toString();
                    instanceLogBuilder.append("<a target='_blank' href=/manage/instance/log?instanceId=")
                            .append(instance.getId())
                            .append(">日志</a><br/>");
                    instanceLog = instanceLogBuilder.toString();
                }
            }
            successEnum = SuccessEnum.SUCCESS;
        } else {
            // return error! 实例无slave节    -1: slave节点不够
            successEnum = SuccessEnum.ERROR;
            resultMap.put("message", "slave节点少于master节点数!");
        }
        resultMap.put("status", successEnum.value());
        resultMap.put("instanceInfo", instanceInfo);
        resultMap.put("instanceLog", instanceLog);
//        logger.info("result:{}", resultMap);
        return resultMap;
    }

    public Map<String, Object> slaveFailover(long appId) {

        Map<String, Object> resultMap = new HashMap<String, Object>();
        SuccessEnum successEnum = null;
        /**
         *  2.配置更新步骤
         *  2.1 备份slave配置 -> 2.2 slave shutdown -> 2.3 替换slave配置 -> 2.4 slave start
         */
        AppDesc appDesc = appService.getByAppId(appId);
        List<InstanceInfo> instanceList = appService.getAppInstanceInfo(appId);
        // 2.1 备份配置
        if (instanceList != null && instanceList.size() > 0) {
            for (InstanceInfo instance : instanceList) {
                if (instance.getStatus() == InstanceStatusEnum.GOOD_STATUS.getStatus() && instance.getRoleDesc().equals("slave")) {
                    // slave节点 & instanceId
                    String ip = instance.getIp();
                    int port = instance.getPort();

                    int times = 0; //最多重试15次
                    boolean checkFailover = false;//检测是否failover成功  false:轮询检测 true:检测完毕
                    try {
                        boolean failoverStatus = false;
                        if (appDesc.getType() == ConstUtils.CACHE_TYPE_REDIS_CLUSTER) {
                            failoverStatus = redisDeployCenter.clusterFailover(appId, instance.getId(), "force");
                        } else if (appDesc.getType() == ConstUtils.CACHE_REDIS_SENTINEL) {
                            failoverStatus = redisDeployCenter.sentinelFailover(appId);
                        }
                        logger.info("type:{},{} {} slave execute failover, execute {}", appDesc.getTypeDesc(), ip, port, failoverStatus);
                        if (!failoverStatus) {
                            // failover 失败
                            resultMap.put("status", SuccessEnum.ERROR.value());
                            resultMap.put("message", "failover失败，请查看日志!");
                            return resultMap;
                        }

                        // 根据主从偏移量 & 新slave节点状态 & 重试次数30次 判断是否failover完成。
                        while (!checkFailover && times++ <= RETRY_TIMES) {
                            Boolean status = redisCenter.getRedisReplicationStatus(appId, ip, port);
                            if (status) {
                                checkFailover = status;
                            } else {
                                TimeUnit.MILLISECONDS.sleep(SLEEP_MILLS);
                                logger.info(" check slave replication status ,waiting 2s ....");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.error(e.getMessage(), e);
                        resultMap.put("status", SuccessEnum.ERROR.value());
                        resultMap.put("message", "从节点" + ip + ":" + port + " failover异常!");
                        return resultMap;
                    }
                }
            }
        }
        resultMap.put("status", SuccessEnum.SUCCESS.value());
        return resultMap;
    }

    /**
     * <p>
     * Description: 从节点发起psync之后是否load data
     * </p>
     *
     * @author chenshi
     * @version 1.0
     * @date 2018/11/29
     */
    public Boolean slaveIsPsync(long appId, String ip, int port) {
        Boolean psyncFlag = false;// 未开始load data
        int times = 0; //最多重试15次
        // 根据主从偏移量 & 新slave节点状态 & 重试次数30次 判断是否failover完成。
        while (!psyncFlag && times++ <= RETRY_TIMES) {
            try {
                Boolean status = redisCenter.getRedisReplicationStatus(appId, ip, port);
                if (status) {
                    psyncFlag = status;
                } else {
                    TimeUnit.MILLISECONDS.sleep(SLEEP_MILLS);
                    logger.info(" check slave psync replication status ,waiting 2s ....");
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error(e.getMessage(), e);
            }
        }
        return psyncFlag;
    }

    /**
     * 组合
     *
     * @param configKey
     * @param configValue
     * @return
     */
    private String combineConfigKeyValue(String configKey, String configValue) {
        return configKey + ConstUtils.SPACE + configValue;
    }
}
