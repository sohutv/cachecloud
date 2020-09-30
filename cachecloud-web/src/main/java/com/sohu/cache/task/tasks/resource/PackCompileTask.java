package com.sohu.cache.task.tasks.resource;

import com.sohu.cache.entity.SystemResource;
import com.sohu.cache.exception.SSHException;
import com.sohu.cache.ssh.SSHTemplate;
import com.sohu.cache.task.BaseTask;
import com.sohu.cache.task.constant.PushEnum;
import com.sohu.cache.task.constant.TaskConstants;
import com.sohu.cache.task.constant.TaskStepFlowEnum.TaskFlowStatusEnum;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.StringUtil;
import com.sohu.cache.web.enums.SshAuthTypeEnum;
import com.sohu.cache.web.util.DateUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

/**
 * Created by chenshi on 2020/7/13.
 */
@Component("PackCompileTask")
@Scope(SCOPE_PROTOTYPE)
public class PackCompileTask extends BaseTask {

    /**
     * 编译容器ip
     */
    private String containerIp;
    /**
     * 资源id
     */
    private Integer resourceId;
    /**
     * 仓库id
     */
    private Integer repositoryId;
    // 操作人
    private String username;
    // 资源包
    private SystemResource resource;
    // 仓库资源
    private SystemResource repository;

    // 编译
    private static String COMPILE_SH = " mkdir -p %s && cd %s && " +
            " wget -O resource.tar.gz %s && tar zxvf resource.tar.gz --strip-component=1 &&" +
            " rm -f resource.tar.gz && make";
    // 备份
    private static String BACKUP_SH = "mkdir -p %s && cp -r %s %s ";
    // 上传
    private static String PRIVATEKEY_UPLOAD_SH = "cd %s && tar -cvf %s %s && scp -i %s -oStrictHostKeyChecking=no -r %s %s@%s:%s";
    private static String PASSWD_UPLOAD_SH = "cd %s && tar -cvf %s %s && sshpass -p %s scp -oStrictHostKeyChecking=no -r %s %s@%s:%s";

    private static String PACKAGE_SUFFIX = "-make.tar.gz";

    private static String PRIVATE_KEY = "/opt/cachecloud/ssh/id_rsa";

    private static int COMPILE_TIME = 10 * 60 * 1000;

    @Override
    public List<String> getTaskSteps() {

        List<String> taskStepList = new ArrayList<String>();
        //1. 参数初始化
        taskStepList.add(TaskConstants.INIT_METHOD_KEY);
        //2. 检查容器ssh环境&私钥文件&gcc
        taskStepList.add("checkSshEnv");
        //3. 远程仓库下载资源包&解压编译
        taskStepList.add("compile");
        //4. 备份资源
        taskStepList.add("bakResource");
        //5. 二进制包上传到仓库
        taskStepList.add("uploadResource");
        //6. 编译完成
        taskStepList.add("compileOver");
        return taskStepList;
    }

    @Override
    public TaskFlowStatusEnum init() {

        super.init();
        // 1.资源id
        resourceId = MapUtils.getInteger(paramMap, TaskConstants.RESOURCE_ID);
        if (resourceId == null) {
            logger.error(marker, "task {} source machine ip {} is empty", taskId, resourceId);
            return TaskFlowStatusEnum.ABORT;
        }
        // 2.目标宿主机
        repositoryId = MapUtils.getInteger(paramMap, TaskConstants.REPOSITORY_ID);
        if (repositoryId == null) {
            logger.error(marker, "task {} target machine ip {} is empty", taskId, repositoryId);
            return TaskFlowStatusEnum.ABORT;
        }
        // 3.容器ip
        containerIp = MapUtils.getString(paramMap, TaskConstants.CONTAINER_IP);
        if (StringUtils.isEmpty(containerIp)) {
            logger.error(marker, "task {} container ip {} is empty", taskId, containerIp);
            return TaskFlowStatusEnum.ABORT;
        }

        // 4.操作人/资源/仓库信息
        username = MapUtils.getString(paramMap, TaskConstants.USER_INFO_KEY);
        resource = resourceDao.getResourceById(resourceId);
        repository = resourceDao.getResourceById(repositoryId);

        return TaskFlowStatusEnum.SUCCESS;
    }

    public TaskFlowStatusEnum checkSshEnv() {

        try {
            //1. 资源状态更新:编译中
            if (resource != null) {
                resource.setIspush(PushEnum.COMPILEING.getValue());
                resourceDao.update(resource);
            } else {
                logger.error(marker, "resource id:{} is empty,resource {}", resourceId, resource);
                return TaskFlowStatusEnum.ABORT;
            }

            if (ConstUtils.SSH_AUTH_TYPE == SshAuthTypeEnum.PASSWORD.getValue()) {
                // 2.1 用户名密码
                logger.info(marker, "check ssh use username/passwd");
                return TaskFlowStatusEnum.SUCCESS;
            } else if (ConstUtils.SSH_AUTH_TYPE == SshAuthTypeEnum.PUBLIC_KEY.getValue()) {
                // 2.2 检查私钥文件
                String check_cmd = String.format("ls -l %s", PRIVATE_KEY);
                SSHTemplate.Result result = sshService.executeWithResult(containerIp, check_cmd, COMPILE_TIME);
                if (result.isSuccess() && !StringUtil.isBlank(result.getResult())) {
                    logger.info(marker, "check private key exists ,cmd:{} result : {}", check_cmd, result);
                    return TaskFlowStatusEnum.SUCCESS;
                } else {
                    // 下载私钥文件到机器
                    logger.error(marker, "check private key ,cmd:{} result : {}", check_cmd, result);
                    return TaskFlowStatusEnum.ABORT;
                }
            }


        } catch (Exception e) {
            return TaskFlowStatusEnum.ABORT;
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    public TaskFlowStatusEnum compile() {

        String respositoryUrl = resourceService.getRespositoryUrl(resourceId, repositoryId);

        //1. 资源编译
        String compile_dir = ConstUtils.REDIS_COMPILE_BASE_DIR + resource.getName();
        String compile_cmd = String.format(COMPILE_SH, compile_dir, compile_dir, resource.getUrl());

        logger.info(marker, "download from url:{}", respositoryUrl);
        logger.info(marker, "compile cmd : {}", compile_cmd);
        try {
            SSHTemplate.Result result = sshService.executeWithResult(containerIp, compile_cmd, COMPILE_TIME);
            if (result.isSuccess()) {
                logger.info(marker, "compile cmd:{} result : {}", compile_cmd, result);
            } else {
                logger.error(marker, "compile cmd:{} result error: {}", compile_cmd, result.getExcetion());
                return TaskFlowStatusEnum.ABORT;
            }
        } catch (SSHException e) {
            logger.error(marker, "compile error :{}", e.getMessage(), e);
            return TaskFlowStatusEnum.ABORT;
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    public TaskFlowStatusEnum bakResource() {

        if (repository != null) {
            try {
                String resouceFile = repository.getDir() + resource.getDir() + "/" + resource.getName() + PACKAGE_SUFFIX;
                String bakPath = repository.getDir() + resource.getDir() + "/bak/";
                String backupFile = bakPath + resource.getName() + PACKAGE_SUFFIX + "-" + DateUtil.formatYYYYMMddHHMMss(new Date()) + "-" + username;
                String bakcmd = String.format(BACKUP_SH, bakPath, resouceFile, backupFile);

                String repository_ip = repository.getName();//远程仓库ip
                // 源文件如果存在，需要先备份
                SSHTemplate.Result existResult = sshService.executeWithResult(repository_ip, String.format("ls -l %s", resouceFile));
                logger.info(marker, "resouceFile result:{}", existResult.isSuccess(), existResult);
                if (existResult.isSuccess() && !StringUtil.isBlank(existResult.getResult())) {
                    SSHTemplate.Result result = sshService.executeWithResult(repository_ip, bakcmd);
                    if (result.isSuccess()) {
                        logger.info(marker, "bakResource success cmd:{} ,result :{}", bakcmd, result.getResult());
                    } else {
                        logger.error(marker, "bakResource error cmd:{} ,result: {}", bakcmd, result);
                        return TaskFlowStatusEnum.ABORT;
                    }
                }

            } catch (SSHException e) {
                logger.error(marker, "bakResource error :{}", e.getMessage(), e);
                return TaskFlowStatusEnum.ABORT;
            }
        } else {
            logger.error(marker, "repository id:{} is empty,repository {}", repositoryId, repository);
            return TaskFlowStatusEnum.ABORT;
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    public TaskFlowStatusEnum uploadResource() {

        // 1.源资源文件和打包资源文件
        String compile_resourceName = ConstUtils.REDIS_COMPILE_BASE_DIR + resource.getName() + PACKAGE_SUFFIX;
        String upload_path = repository.getDir() + resource.getDir();
        // 2.上传文件
        String upload_cmd = "";
        if (ConstUtils.SSH_AUTH_TYPE == SshAuthTypeEnum.PASSWORD.getValue()) {
            // 2.1 用户名密码
            upload_cmd = String.format(PASSWD_UPLOAD_SH, ConstUtils.REDIS_COMPILE_BASE_DIR, compile_resourceName, resource.getName(), ConstUtils.PASSWORD, compile_resourceName,  ConstUtils.USERNAME, repository.getName(), upload_path);
        } else if (ConstUtils.SSH_AUTH_TYPE == SshAuthTypeEnum.PUBLIC_KEY.getValue()) {
            upload_cmd = String.format(PRIVATEKEY_UPLOAD_SH, ConstUtils.REDIS_COMPILE_BASE_DIR, compile_resourceName, resource.getName(), PRIVATE_KEY, compile_resourceName, ConstUtils.USERNAME, repository.getName(), upload_path);
        }

        try {
            SSHTemplate.Result result = sshService.executeWithResult(containerIp, upload_cmd, COMPILE_TIME);
            if (result.isSuccess()) {
                logger.info(marker, "upload cmd {} success : result:{}  ", upload_cmd, result);
            } else {
                logger.error(marker, "upload cmd {} fail : result:{} ", upload_cmd, result);
            }
        } catch (SSHException e) {
            logger.error(marker, "uploadResource error :{}", e.getMessage(), e);
            return TaskFlowStatusEnum.ABORT;
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    public TaskFlowStatusEnum compileOver() {

        if (resource != null) {
            //1.清理远程目录
            try {
                String clear_cmd = String.format("rm -rf %s", ConstUtils.REDIS_COMPILE_BASE_DIR + resource.getName() + "*");
                logger.info(marker, "clear resource :" + sshService.executeWithResult(containerIp, clear_cmd));
            } catch (SSHException e) {
                e.printStackTrace();
            }
            //2.更新资源状态
            resource.setIspush(PushEnum.YES.getValue());
            resource.setLastmodify(new Date());
            resource.setUsername(username);
            resourceDao.update(resource);
        } else {
            logger.error(marker, "resource id:{} is empty,resource {}", resourceId, resource);
            return TaskFlowStatusEnum.ABORT;
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

}
