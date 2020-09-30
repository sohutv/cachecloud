package com.sohu.cache.task.tasks.install;

import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.SystemResource;
import com.sohu.cache.protocol.RedisProtocol;
import com.sohu.cache.task.BaseTask;
import com.sohu.cache.task.constant.InstanceInfoEnum.InstanceTypeEnum;
import com.sohu.cache.task.constant.TaskConstants;
import com.sohu.cache.task.constant.TaskStepFlowEnum.TaskFlowStatusEnum;
import com.sohu.cache.util.ConstUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * redis server安装
 */
@Component("RedisServerInstallTask")
@Scope(SCOPE_PROTOTYPE)
public class RedisServerInstallTask extends BaseTask {

    private long appId;

    private String host;

    private int port;

    private int maxMemory;

    private Boolean isCluster;

    SystemResource redisResource;

    /**
     * 当前实例类型
     */
    private final InstanceTypeEnum currentInstanceTypeEnum = InstanceTypeEnum.REDIS_SERVER;


    @Override
    public List<String> getTaskSteps() {
        List<String> taskStepList = new ArrayList<String>();
        taskStepList.add(TaskConstants.INIT_METHOD_KEY);
        /**
         * 1.检查实例是否存在
         * 2.检查服务器安装环境 dir / env param
         * 3.appid | redis version版本验证
         * 4.push config
         * 5.start redis instance
         * 6.validate instance is run
         */
        taskStepList.add("checkIsExist");
        taskStepList.add("prepareRelateDir");
        taskStepList.add("prepareRelateBin");
        taskStepList.add("pushConfig");
        taskStepList.add("pushService");
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

        maxMemory = MapUtils.getIntValue(paramMap, TaskConstants.REDIS_SERVER_MAX_MEMORY_KEY);
        if (maxMemory <= 0) {
            logger.error(marker, "task {} maxMemory {} is wrong", taskId, maxMemory);
            return TaskFlowStatusEnum.ABORT;
        }

        redisResource = resourceService.getResourceById(appService.getByAppId(appId).getVersionId());
        isCluster = MapUtils.getBoolean(paramMap, TaskConstants.IS_CLUSTER_KEY);

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
     * 推配置
     *
     * @return
     */
    public TaskFlowStatusEnum pushConfig() {
        redisConfigTemplateService.checkAndInstallRedisResource(host, redisResource);
        //实例基准目录
        String instanceRemoteBasePath = ConstUtils.CACHECLOUD_BASE_DIR;
        AppDesc appDesc = appService.getByAppId(appId);
        List<String> configList = handleCommonConfig(host, port, maxMemory, appDesc.getVersionId(),isCluster);
        if (CollectionUtils.isEmpty(configList)) {
            logger.error(marker, "appId {} port {} maxmemory {} versionId:{} instanceRemoteBasePath {} configList is empty", appId, port, maxMemory, appDesc.getVersionId(), instanceRemoteBasePath);
            return TaskFlowStatusEnum.ABORT;
        }
        String fileName;
        if (isCluster) {
            fileName = RedisProtocol.getConfig(port, true);
        } else {
            fileName = RedisProtocol.getConfig(port, false);
        }
        //todo
        String pathFile = machineCenter.createRemoteFile(host, fileName, configList);
        if (StringUtils.isBlank(pathFile)) {
            logger.error(marker, "appId {} port {} maxmemory {} instanceRemoteBasePath:{} pathFile:{} is empty", appId, port, maxMemory, instanceRemoteBasePath,pathFile);
            return TaskFlowStatusEnum.ABORT;
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 启动服务
     *
     * @return
     */
    public TaskFlowStatusEnum pushService() {
        //远程和本地目录
        //String instanceRemoteBasePath = machineCenter.getInstanceRemoteBasePath(appId, port, currentInstanceTypeEnum);
//		String instanceLocalTmpPath = machineCenter.getInstanceLocalTempBasePath(appId, host, port, currentInstanceTypeEnum);

        //相关参数
//		int cpuidx = MachineProtocol.getCpuIdx(port);
        /*String startCmd = RedisProtocol.getRedisServerStartCmd();
        String runCmd = RedisProtocol.getRedisServerRunCmd(port);
		String serviceShellFileName = RedisProtocol.getServiceShellFileName(currentInstanceTypeEnum);
		
		logger.info(marker, "cpuidx is {}", cpuidx);
		logger.info(marker, "startCmd is {}", startCmd);
		logger.info(marker, "runCmd is {}", runCmd);
		
		return pushService(appId, currentInstanceTypeEnum, host, port, instanceRemoteBasePath, instanceLocalTmpPath, cpuidx, startCmd, runCmd, serviceShellFileName);
		*/
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 启动服务
     *
     * @return
     */
    public TaskFlowStatusEnum startServer() {
        //资源是否存在
        redisConfigTemplateService.checkAndInstallRedisResource(host, redisResource);
        String redisDir = redisResource == null ? ConstUtils.REDIS_DEFAULT_DIR : ConstUtils.getRedisDir(redisResource.getName());
        String runShell;
        if (isCluster) {
            runShell = redisDeployCenter.getRedisRunShell(true, host, port, redisDir);
        } else {
            runShell = redisDeployCenter.getRedisRunShell(false, host, port, redisDir);
        }
        //启动实例
        logger.info(marker, "masterShell:host={};shell={}", host, runShell);
        boolean isMasterShell = machineCenter.startProcessAtPort(host, port, runShell);
        if (!isMasterShell) {
            logger.error(marker, "runShell={} error,{}:{}", runShell, host, port);
            return TaskFlowStatusEnum.ABORT;
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    public TaskFlowStatusEnum checkIsRun() {
        if (!redisCenter.isRun(host, port)) {
            logger.error(marker, "redis server {}:{} is not run", host, port);
            return TaskFlowStatusEnum.ABORT;
        } else {
            logger.info(marker, "redis server {}:{} is run", host, port);
            return TaskFlowStatusEnum.SUCCESS;
        }
    }

    /**
     * <p>
     * Description:获取redis config
     * </p>
     *
     * @author chenshi
     * @version 1.0
     * @date 2019/1/9
     */
    private List<String> handleCommonConfig(String host, int port, int maxMemory, int versionId, boolean isCluster) {
        try {
            List<String> configs = redisConfigTemplateService.handleCommonConfig(host, port, maxMemory, versionId);
            if (isCluster) {
                configs.addAll(redisConfigTemplateService.handleClusterConfig(port, versionId));
            }
            return configs;
        } catch (Exception e) {
            logger.error(marker, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

}
