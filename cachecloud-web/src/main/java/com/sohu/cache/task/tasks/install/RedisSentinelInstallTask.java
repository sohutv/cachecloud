package com.sohu.cache.task.tasks.install;

import com.alibaba.fastjson.JSONArray;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.SystemResource;
import com.sohu.cache.protocol.RedisProtocol;
import com.sohu.cache.task.BaseTask;
import com.sohu.cache.task.constant.InstanceInfoEnum;
import com.sohu.cache.task.constant.TaskConstants;
import com.sohu.cache.task.constant.TaskStepFlowEnum.TaskFlowStatusEnum;
import com.sohu.cache.task.entity.RedisServerNode;
import com.sohu.cache.util.ConstUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * <p>
 * Description: Redis sentinel安装
 * </p>
 *
 * @author chenshi
 * @version 1.0
 * @date 2019/1/11
 */
@Component("RedisSentinelInstallTask")
@Scope(SCOPE_PROTOTYPE)
public class RedisSentinelInstallTask extends BaseTask {

    private long appId;

    private String host;

    private int port;

    private int quorum;

    SystemResource redisResource;

    private List<RedisServerNode> masterRedisServerNodes;

    /**
     * 当前实例类型
     */
    private final InstanceInfoEnum.InstanceTypeEnum currentInstanceTypeEnum = InstanceInfoEnum.InstanceTypeEnum.REDIS_SENTINEL;

    @Override
    public List<String> getTaskSteps() {
        List<String> taskStepList = new ArrayList<String>();
        taskStepList.add(TaskConstants.INIT_METHOD_KEY);
        taskStepList.add("checkIsExist");
        taskStepList.add("prepareRelateDir");
        taskStepList.add("prepareRelateBin");
        taskStepList.add("pushService");
        taskStepList.add("pushConfig");
        taskStepList.add("startServer");
        taskStepList.add("checkIsRun");
        return taskStepList;
    }

    /**
     * 初始化参数
     *
     * @return
     */
    @Override
    public TaskFlowStatusEnum init() {
        super.init();

        appId = MapUtils.getLongValue(paramMap, TaskConstants.APPID_KEY);
        if (appId <= 0) {
            logger.error(marker, "task {} appId {} is wrong", taskId, appId);
            return TaskFlowStatusEnum.ABORT;
        }

        host = MapUtils.getString(paramMap, TaskConstants.HOST_KEY);
        if (StringUtils.isBlank(host)) {
            logger.error(marker, "task {} host is empty", taskId);
            return TaskFlowStatusEnum.ABORT;
        }

        port = MapUtils.getIntValue(paramMap, TaskConstants.PORT_KEY);
        if (port <= 0) {
            logger.error(marker, "task {} port {} is wrong", taskId, port);
            return TaskFlowStatusEnum.ABORT;
        }

        quorum = MapUtils.getIntValue(paramMap, TaskConstants.REDIS_SENTINEL_QUORUM_KEY);
        if (quorum <= 0) {
            logger.error(marker, "task {} quorum {} is wrong", taskId, quorum);
            return TaskFlowStatusEnum.ABORT;
        }

        //parse
        String masterRedisNodeStr = MapUtils.getString(paramMap, TaskConstants.MASTER_REDIS_SERVER_NODES);
        masterRedisServerNodes = JSONArray.parseArray(masterRedisNodeStr, RedisServerNode.class);
        if (CollectionUtils.isEmpty(masterRedisServerNodes)) {
            logger.error(marker, "task {} masterRedisNodes is empty", taskId);
            return TaskFlowStatusEnum.ABORT;
        }
        redisResource = resourceService.getResourceById(appService.getByAppId(appId).getVersionId());
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 检查实例是否已经存在
     *
     * @return
     */
    public TaskFlowStatusEnum checkIsExist() {
        return checkInstanceIsExist(appId, host, port, currentInstanceTypeEnum);
    }

    /**
     * 准备相关目录
     *
     * @return
     */
    public TaskFlowStatusEnum prepareRelateDir() {
        return prepareRelateDir(appId, host, port, currentInstanceTypeEnum);
    }

    /**
     * 准备二进制执行文件
     *
     * @return
     */
    public TaskFlowStatusEnum prepareRelateBin() {
        return prepareRelateBin(appId, host, port, currentInstanceTypeEnum, redisResource.getName());
    }

    /**
     * 启动服务
     *
     * @return
     */
    public TaskFlowStatusEnum pushService() {
        //远程和本地目录
        /*String instanceRemoteBasePath = machineCenter.getInstanceRemoteBasePath(appId, port, currentInstanceTypeEnum);
        String instanceLocalTmpPath = machineCenter.getInstanceLocalTempBasePath(appId, host, port, currentInstanceTypeEnum);

        //相关参数
        int cpuidx = MachineProtocol.getCpuIdx(port);
        String startCmd = RedisProtocol.getRedisSentinelStartCmd();
        String runCmd = RedisProtocol.getRedisSentinelRunCmd(port);
        String serviceShellFileName = RedisProtocol.getServiceShellFileName(currentInstanceTypeEnum);

        logger.info(marker, "cpuidx is {}", cpuidx);
        logger.info(marker, "startCmd is {}", startCmd);
        logger.info(marker, "runCmd is {}", runCmd);

        return pushService(appId, currentInstanceTypeEnum, host, port, instanceRemoteBasePath, instanceLocalTmpPath, cpuidx, startCmd, runCmd, serviceShellFileName);
        */
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 推配置
     *
     * @return
     */
    public TaskFlowStatusEnum pushConfig() {
        //资源是否存在
        redisConfigTemplateService.checkAndInstallRedisResource(host, redisResource);
        String instanceRemoteBasePath = ConstUtils.CACHECLOUD_BASE_DIR;
        AppDesc appDesc = appService.getByAppId(appId);

        List<String> masterSentinelConfigs;
        // 获取masterName
        if (masterRedisServerNodes.size() == 1) {
            RedisServerNode redisServerNode = masterRedisServerNodes.get(0);
            masterSentinelConfigs = handleSentinelConfig(redisServerNode.getMasterName(), redisServerNode.getIp(), redisServerNode.getPort(), host, port, appDesc.getVersionId());
            logger.info("sentinel configs :" + masterSentinelConfigs);
        } else {
            logger.error(marker, "appId {} masterRedisServerNodes {} is empty", appId, masterRedisServerNodes);
            return TaskFlowStatusEnum.ABORT;
        }

        if (CollectionUtils.isEmpty(masterSentinelConfigs)) {
            logger.error(marker, "appId {} host:{} port:{} versionId:{} instanceRemoteBasePath {} configList is empty", appId, host, port, appDesc.getVersionId(), instanceRemoteBasePath);
            return TaskFlowStatusEnum.ABORT;
        }

        String masterSentinelFileName = RedisProtocol.getConfig(port, false);
        String sentinelPathFile = machineCenter
                .createRemoteFile(host, masterSentinelFileName, masterSentinelConfigs);
        if (StringUtils.isBlank(sentinelPathFile)) {
            return TaskFlowStatusEnum.ABORT;
        }

        return TaskFlowStatusEnum.SUCCESS;
    }

    private String getMasterName(String host, int port) {
        String masterSentinelName = String.format("sentinel-%s-%s", host, port);
        return masterSentinelName;
    }

    private void printConfig(List<String> masterConfigs) {
        logger.info("==================redis-{}-config==================", masterConfigs);
        for (String line : masterConfigs) {
            logger.info(line);
        }
    }

    /**
     * 启动服务
     *
     * @return
     */
    public TaskFlowStatusEnum startServer() {
        redisConfigTemplateService.checkAndInstallRedisResource(host, redisResource);
        String redisDir = redisResource == null ? ConstUtils.REDIS_DEFAULT_DIR : ConstUtils.getRedisDir(redisResource.getName());
        String sentinelShell = redisDeployCenter.getSentinelRunShell(host, port, redisDir);
        logger.info(marker, "sentinelMasterShell:{}", sentinelShell);
        boolean isSentinelMasterShell = machineCenter.startProcessAtPort(host, port, sentinelShell);
        if (!isSentinelMasterShell) {
            logger.error(marker, "sentinelMasterShell={} error", sentinelShell);
            return TaskFlowStatusEnum.ABORT;
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * @return
     */
    public TaskFlowStatusEnum checkIsRun() {
        if (!redisCenter.isRun(host, port)) {
            logger.error(marker, "sentinel {}:{} is not run", host, port);
            return TaskFlowStatusEnum.ABORT;
        } else {
            logger.info(marker, "sentinel {}:{} is run", host, port);
            return TaskFlowStatusEnum.SUCCESS;
        }
    }

    /**
     * <p>
     * Description: 获取sentinel config
     * </p>
     *
     * @author chenshi
     * @version 1.0
     * @date 2019/1/11
     */
    private List<String> handleSentinelConfig(String masterName, String host, int port, String sentinelHost, int sentinelPort, int versionId) {
        try {
            // todo  masterHost
            return redisConfigTemplateService.handleSentinelConfig(masterName, host, port, sentinelHost, sentinelPort, versionId);
        } catch (Exception e) {
            logger.error(marker, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

}
