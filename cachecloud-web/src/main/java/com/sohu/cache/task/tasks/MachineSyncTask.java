package com.sohu.cache.task.tasks;

import com.sohu.cache.entity.MachineInfo;
import com.sohu.cache.entity.MachineRelation;
import com.sohu.cache.exception.SSHException;
import com.sohu.cache.ssh.SSHTemplate.Result;
import com.sohu.cache.task.BaseTask;
import com.sohu.cache.task.constant.TaskConstants;
import com.sohu.cache.task.constant.TaskStepFlowEnum.TaskFlowStatusEnum;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.web.enums.MachineTaskEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * Created by chenshi on 2019/5/24.
 */
@Component("MachineSyncTask")
@Scope(SCOPE_PROTOTYPE)
public class MachineSyncTask extends BaseTask {

    /**
     * 源宿主机ip
     */
    private String sourceIp;
    /**
     * 目标宿主机ip
     */
    private String targetIp;
    /**
     * 容器ip
     */
    private String containerIp;
    /**
     * k8s 持久化/配置/日志 目录
     */
    private static String baseConfDir = "/data/redis/conf/";
    private static String baseDataDir = "/data/redis/data/";
    private static String baseLogDir = "/data/redis/logs/";
    private static String backupDir = "/data/redis/bak/";

    /**
     * 同步/校验数据 超时时间
     */
    private static int SYNC_DATA_TIMEOUT = 30 * 60 * 1000;
    private static int MD5_CHECK_TIMEOUT = 2 * 60 * 1000;
    private static int BACKUP_DATA_TIMEOUT = 5 * 60 * 1000;


    @Override
    public List<String> getTaskSteps() {

        List<String> taskStepList = new ArrayList<String>();
        //1. 参数初始化
        taskStepList.add(TaskConstants.INIT_METHOD_KEY);
        //2. 检查源主机同步环境
        taskStepList.add("checkSourceMachineEnv");
        //3. 检查目标主机连通性及目录检查
        taskStepList.add("checkTargetMachineEnv");
        //4. scp同步数据 source->target host machine
        taskStepList.add("startSyncData");
        //5. 校验目的文件是否一致
        taskStepList.add("checkDirMd5");
        //6. 备份历史数据
        taskStepList.add("backupSourceData");
        //7. 同步完成
        taskStepList.add("syncOver");
        return taskStepList;
    }

    @Override
    public TaskFlowStatusEnum init() {

        super.init();
        // 1.源宿主机
        sourceIp = MapUtils.getString(paramMap, TaskConstants.SOURCE_HOST_KEY);
        if (StringUtils.isEmpty(sourceIp)) {
            logger.error(marker, "task {} source machine ip {} is empty", taskId, sourceIp);
            return TaskFlowStatusEnum.ABORT;
        }
        // 2.目标宿主机
        targetIp = MapUtils.getString(paramMap, TaskConstants.TARGET_HOST_KEY);
        if (StringUtils.isEmpty(targetIp)) {
            logger.error(marker, "task {} target machine ip {} is empty", taskId, targetIp);
            return TaskFlowStatusEnum.ABORT;
        }
        // 3.容器ip
        containerIp = MapUtils.getString(paramMap, TaskConstants.CONTAINER_IP);
        if (!StringUtils.isEmpty(containerIp)) {
            MachineInfo machineInfo = machineDao.getMachineInfoByIp(containerIp);
            if (machineInfo == null) {
                logger.error(marker, "task {} container ip {} is not exist", taskId, containerIp);
                return TaskFlowStatusEnum.ABORT;
            }
        } else {
            logger.error(marker, "task {} container ip {} is empty", taskId, containerIp);
            return TaskFlowStatusEnum.ABORT;
        }

        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 2.源主机:
     * 2.1.机器连通性
     * 2.2.sshpass安装
     * 2.3.源数据目录是否存在
     */
    public TaskFlowStatusEnum checkSourceMachineEnv() {

        // 1.连通性 + sshpass
        String sshpass_command = "sshpass -V | head -1 ";
        // 源数据目录
        String checkDir_command = "ls -l " + baseConfDir.concat(containerIp) + " | wc -l && ls -l " + baseDataDir.concat(containerIp) + " | wc -l && ls -l " + baseLogDir.concat(containerIp) + " | wc -l";

        try {
            Result checkResult = sshService.executeWithResult(sourceIp, sshpass_command);
            if (checkResult.isSuccess() && checkResult.getResult().indexOf("sshpass") > -1) {
                logger.info(marker, "sourceMachine ip :{} check sshpass env result:{}", sourceIp, checkResult.getResult());
            } else {
                logger.error(marker, "sourceMachine ip :{} check sshpass env error message:{}", sourceIp, checkResult.getResult());
                return TaskFlowStatusEnum.ABORT;
            }

            Result checkDirResult = sshService.executeWithResult(sourceIp, checkDir_command);
            if (checkDirResult.isSuccess()) {
                logger.info(marker, "sourceMachine ip :{} check dir env command:{} , result:{}", sourceIp, checkDir_command, checkDirResult.getResult());
                if (checkDirResult.getResult() != null && checkDirResult.getResult().indexOf("0 0 0") > -1) {
                    logger.error(marker, "sourceMachine ip :{} check dir not exist result:{}", sourceIp, checkDirResult.getResult());
                    return TaskFlowStatusEnum.ABORT;
                }
            } else {
                logger.error(marker, "sourceMachine ip :{} check dir env error message:{}", sourceIp, checkDirResult.getResult());
                return TaskFlowStatusEnum.ABORT;
            }

        } catch (SSHException e) {
            logger.error(marker, "sourceMachine ip :{} check env error message:{}", sourceIp, e.getMessage());
            e.printStackTrace();
            return TaskFlowStatusEnum.ABORT;
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 3.目标主机检查:
     * 3.1. 机器连通性
     * 3.2. cachecloud用户是否存在
     * 3.3. 检测容器上是否有redis进程
     * 3.4. 同步目录存在且无redis进程 删除数据目录
     */
    public TaskFlowStatusEnum checkTargetMachineEnv() {

        String checkUser_command = "cat /etc/passwd | grep cachecloud ";
        String checkRedis_command = "ps -ef | grep redis | grep -v \"grep redis\" | wc -l ";
        String delDir_command = "rm -rf " + baseConfDir.concat(containerIp) + " " + baseDataDir.concat(containerIp) + " " + baseLogDir.concat(containerIp);

        try {
            // 等待镜像启动
            TimeUnit.SECONDS.sleep(30);

            // 1.检查用户是否存在
            Result checkResult = sshService.executeWithResult(targetIp, checkUser_command);
            if (checkResult.isSuccess() && checkResult.getResult().indexOf("cachecloud") > -1) {
                logger.info(marker, "targetMachine ip :{} check user:{} , result:{}", targetIp, checkUser_command, checkResult.getResult());
            } else {
                logger.error(marker, "targetMachine ip :{} check user:{} , error message:{}", targetIp, checkUser_command, checkResult.getResult());
                return TaskFlowStatusEnum.ABORT;
            }
            // 2.检测容器是否还有redis进程
            Result checkRedisResult = sshService.executeWithResult(containerIp, checkRedis_command);
            if (checkRedisResult.isSuccess() && checkRedisResult.getResult().equals("0")) {
                logger.info(marker, "container ip :{} check redis:{} size:{} is not exist ", containerIp, checkRedis_command, checkRedisResult.getResult());
            } else {
                logger.error(marker, "container ip :{} check redis:{} size:{} , message:{}", containerIp, checkRedis_command, checkRedisResult.getResult());
                return TaskFlowStatusEnum.ABORT;
            }
            // 3.清除目标主机的目录数据(防止冲突)
            Result delResult = sshService.executeWithResult(targetIp, delDir_command);
            if (delResult.isSuccess()) {
                logger.info(marker, "targetMachine ip :{} , del success command:{} ", targetIp, delDir_command, delResult.getResult());
            } else {
                logger.error(marker, "targetMachine ip :{} , del command:{}, error message:{}", targetIp, delDir_command, delResult.getResult());
                return TaskFlowStatusEnum.ABORT;
            }
        } catch (SSHException e) {
            logger.error(marker, "sourceMachine ip :{} check env error message:{}", sourceIp, e.getMessage(),e);
            return TaskFlowStatusEnum.ABORT;
        } catch (InterruptedException e) {
            return TaskFlowStatusEnum.ABORT;
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 4.持久化数据、配置目录同步任务
     */
    public TaskFlowStatusEnum startSyncData() {

        /*String sync_command = " sshpass -p " + ConstUtils.PASSWORD + " scp -oStrictHostKeyChecking=no -r " + baseConfDir.concat(containerIp) + " " + ConstUtils.USERNAME + "@" + targetIp + ":" + baseConfDir +
                " && sshpass -p " + ConstUtils.PASSWORD + " scp -oStrictHostKeyChecking=no -r " + baseDataDir.concat(containerIp) + " " + ConstUtils.USERNAME + "@" + targetIp + ":" + baseDataDir +
                " && sshpass -p " + ConstUtils.PASSWORD + " scp -oStrictHostKeyChecking=no -r " + baseLogDir.concat(containerIp) + " " + ConstUtils.USERNAME + "@" + targetIp + ":" + baseLogDir;
        */
        String sync_command = " scp -i "+ConstUtils.DEFAULT_PUBLIC_KEY_PEM+" -oStrictHostKeyChecking=no -r " + baseConfDir.concat(containerIp) + " " + ConstUtils.USERNAME + "@" + targetIp + ":" + baseConfDir +
                " && scp -i "+ConstUtils.DEFAULT_PUBLIC_KEY_PEM+"  -oStrictHostKeyChecking=no -r " + baseDataDir.concat(containerIp) + " " + ConstUtils.USERNAME + "@" + targetIp + ":" + baseDataDir +
                " && scp -i "+ConstUtils.DEFAULT_PUBLIC_KEY_PEM+"  -oStrictHostKeyChecking=no -r " + baseLogDir.concat(containerIp) + " " + ConstUtils.USERNAME + "@" + targetIp + ":" + baseLogDir;
        logger.info(marker, "source ip :{} ,execute command :{}", sourceIp, sync_command);
        Result syncResult = null;
        try {
            long start = System.currentTimeMillis();
            syncResult = sshService.executeWithResult(sourceIp, sync_command, SYNC_DATA_TIMEOUT);
            if (syncResult.isSuccess() || syncResult.getResult().indexOf("Permission denied") == -1) {
                logger.info(marker, "sync data result:{} costTime:{} s", syncResult.getResult(), (System.currentTimeMillis() - start) / 1000);
            } else {
                logger.error(marker, "sync data error message:{}", syncResult.getResult());
                return TaskFlowStatusEnum.ABORT;
            }
        } catch (SSHException e) {
            logger.error(marker, "sync data error message:{} {}", syncResult,e.getMessage(),e);
            return TaskFlowStatusEnum.ABORT;
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 5.同步数据目录文件md5校验
     */
    public TaskFlowStatusEnum checkDirMd5() {

//        String sourceShell = "find " + baseConfDir.concat(containerIp) + " " + baseDataDir.concat(containerIp) + " " + baseLogDir.concat(containerIp) + " -type f -exec md5sum {} \\; | sort -k 2";
//        String targetShell = "find " + baseConfDir.concat(containerIp) + " " + baseDataDir.concat(containerIp) + " " + baseLogDir.concat(containerIp) + " -type f -exec md5sum {} \\; | sort -k 2";
        String sourceShell = "find " + baseConfDir.concat(containerIp) + "/*.conf " + baseDataDir.concat(containerIp) + "/*.conf -type f -exec md5sum {} \\; | sort -k 2";
        String targetShell = "find " + baseConfDir.concat(containerIp) + "/*.conf " + baseDataDir.concat(containerIp) + "/*.conf -type f -exec md5sum {} \\; | sort -k 2";

        //1.源宿主机和目标宿主机 目录文件列表的md5值
        Result sourceMd5Result;
        Result targetMd5Result;
        try {
            sourceMd5Result = sshService.executeWithResult(sourceIp, sourceShell, MD5_CHECK_TIMEOUT);
            if(sourceMd5Result == null){
                logger.error(marker, " source ip:{} validate md5 result is null", sourceIp);
                return TaskFlowStatusEnum.ABORT;
            }
            if (sourceMd5Result.isSuccess()) {
                logger.info(marker, "source ip:{} validate md5 result: ", sourceIp);
                for (String sourceMd5 : sourceMd5Result.getResult().split("\n")) {
                    logger.info(marker, "source file md5 {} : ", sourceMd5);
                }
            } else {
                logger.error(marker, " source ip:{} validate md5 error:{}", sourceIp, sourceMd5Result.getExcetion());
                return TaskFlowStatusEnum.ABORT;
            }
        } catch (SSHException e) {
            logger.error(marker, " source ip:{} validate md5 error:{}", sourceIp, e.getMessage());
            return TaskFlowStatusEnum.ABORT;
        }
        try {
            targetMd5Result = sshService.executeWithResult(targetIp, targetShell, MD5_CHECK_TIMEOUT);
            if(targetMd5Result == null){
                logger.error(marker, " target ip:{} validate md5 result is null", targetIp);
                return TaskFlowStatusEnum.ABORT;
            }
            if (targetMd5Result.isSuccess()) {
                logger.info(marker, "target ip:{} validate md5 result:{} ", targetIp, targetMd5Result.getResult());
                for (String targetMd5 : targetMd5Result.getResult().split("\n")) {
                    logger.info(marker, "target file md5 {} : ", targetMd5);
                }
            } else {
                logger.error(marker, " target ip:{} validate md5 error:{}", targetIp, targetMd5Result.getResult());
                return TaskFlowStatusEnum.ABORT;
            }
        } catch (SSHException e) {
            logger.error(marker, " target ip:{} validate md5 error:{}", sourceIp, e.getMessage());
            return TaskFlowStatusEnum.ABORT;
        }

        // 2.print md5 compare result
        if (sourceMd5Result != null && targetMd5Result != null && targetMd5Result.getResult().equals(sourceMd5Result.getResult())) {
            logger.info(marker, "===== compare source ip:{} & target ip:{} is equal =====", sourceIp, targetIp);
        } else {
            logger.error(marker, "===== compare source ip:{} & target ip:{} is diffrent =====", sourceIp, targetIp);
            return TaskFlowStatusEnum.ABORT;
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    public TaskFlowStatusEnum backupSourceData() {

        // 1. backup data :  /data/redis/bak/${ip}_${time}/...
        SimpleDateFormat time = new SimpleDateFormat("yyyyMMddHHmm");
        String timeFormat = time.format(new Date());
        // 2. backup command
        String backup_command = " mkdir -p " + backupDir.concat(containerIp + "_" + timeFormat) + " && " +
                " mv " + baseConfDir.concat(containerIp) + " " + backupDir.concat(containerIp + "_" + timeFormat).concat("/conf") + " && " +
                " mv " + baseDataDir.concat(containerIp) + " " + backupDir.concat(containerIp + "_" + timeFormat).concat("/data") + " && " +
                " mv " + baseLogDir.concat(containerIp) + " " + backupDir.concat(containerIp + "_" + timeFormat).concat("/logs");

        Result backupResult;
        try {
            backupResult = sshService.executeWithResult(sourceIp, backup_command, BACKUP_DATA_TIMEOUT);
            if (backupResult.isSuccess()) {
                logger.info(marker, "source ip:{} backup date success ,result:{} ", sourceIp, backupResult.getResult());
            } else {
                logger.error(marker, " source ip:{} backup date error:{}", sourceIp, backupResult.getResult());
                return TaskFlowStatusEnum.ABORT;
            }
        } catch (SSHException e) {
            logger.error(marker, " source ip:{} backup date error:{}", sourceIp, e.getMessage());
            return TaskFlowStatusEnum.ABORT;
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 7.更新机器同步状态
     */
    public TaskFlowStatusEnum syncOver() {
        // update machine SYNC status
        try {
            List<MachineRelation> relationList = machineRelationDao.getUnSyncRelationList(containerIp, sourceIp);
            if (!CollectionUtils.isEmpty(relationList)) {
                for (MachineRelation machineRelation : relationList) {
                    machineRelationDao.updateMachineSyncStatus(machineRelation.getId(), MachineTaskEnum.SYNCED.getValue());
                    logger.info(marker, "update machine realtion id:{}, ip:{},realIp:{} sync status success !", machineRelation.getId(), sourceIp, containerIp);
                }
            }
        } catch (Exception e) {
            logger.error(marker, "update machine relation  ", sourceIp, targetIp);
            return TaskFlowStatusEnum.ABORT;
        }

        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 执行一个ls命令，确认ssh以及不是只读盘
     *
     * @return
     */
    protected boolean checkMachineIsConnect(String ip) {
        try {
            Result result = sshService.executeWithResult(ip, "ls / | wc -l");
            if (result.isSuccess()) {
                return true;
            }
        } catch (Exception e) {
            logger.error(marker, e.getMessage());
        }
        return false;
    }
}
