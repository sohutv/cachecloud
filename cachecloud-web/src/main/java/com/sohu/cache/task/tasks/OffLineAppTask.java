package com.sohu.cache.task.tasks;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sohu.cache.constant.AppStatusEnum;
import com.sohu.cache.constant.InstanceStatusEnum;
import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.AppUser;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.task.BaseTask;
import com.sohu.cache.task.constant.TaskConstants;
import com.sohu.cache.task.constant.TaskStepFlowEnum.TaskFlowStatusEnum;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.TypeUtil;
import com.sohu.cache.web.service.AppService;
import com.sohu.cache.web.util.AppEmailUtil;
import org.apache.commons.collections.MapUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * Created by rucao on 2019/11/13
 */
@Component("OffLineAppTask")
@Scope(SCOPE_PROTOTYPE)
public class OffLineAppTask extends BaseTask {
    private long appId;
    private AppDesc appDesc;
    private AppUser userInfo;
    private long auditId;
    @Autowired
    private AppService appService;
    @Autowired
    private InstanceDao instanceDao;
    @Resource(name = "appEmailUtil")
    private AppEmailUtil appEmailUtil;

    @Override
    public List<String> getTaskSteps() {
        List<String> taskStepList = Lists.newArrayList();
        taskStepList.add(TaskConstants.INIT_METHOD_KEY);
        // 2. 执行应用下线处理
        taskStepList.add("executeOffLineApp");
        // 3. 更新应用信息
        taskStepList.add("updateAppStatus");

        return taskStepList;
    }

    /**
     * 初始化参数
     *
     * @return
     */
    @Override
    public TaskFlowStatusEnum init() {
        TaskFlowStatusEnum taskFlowStatusEnum = super.init();
        appId = MapUtils.getLongValue(paramMap, TaskConstants.APPID_KEY);
        if (appId <= 0) {
            logger.error(marker, "task {} appId {} is wrong", taskId, appId);
            taskFlowStatusEnum = TaskFlowStatusEnum.ABORT;
        }

        auditId = MapUtils.getLongValue(paramMap, TaskConstants.AUDIT_ID_KEY, -1);
        if (auditId <= 0) {
            logger.info(marker, "task {} auditId {} is wrong", taskId, auditId);
        }

        appDesc = appService.getByAppId(appId);
        if (appDesc == null) {
            logger.error(marker, "task {} appId {} appDesc is not exist", taskId, appId);
            taskFlowStatusEnum = TaskFlowStatusEnum.ABORT;
        }
        Object userObject = MapUtils.getObject(paramMap, TaskConstants.USER_INFO_KEY, null);
        if (userObject instanceof JSONObject) {
            JSONObject userJson = (JSONObject) userObject;
            userInfo = JSONObject.parseObject(JSON.toJSONString(userJson), AppUser.class);
        }
        if (userInfo == null) {
            logger.error(marker, "task {} appId {} userInfo is not exist", taskId, appId);
            taskFlowStatusEnum = TaskFlowStatusEnum.ABORT;
        } else {
            if (!ConstUtils.SUPER_MANAGER.contains(userInfo.getName())) {
                logger.error("task {} appId {} user {} who hope to offline hasn't privilege", taskId, appId, userInfo.getName());
                taskFlowStatusEnum = TaskFlowStatusEnum.ABORT;
            }
        }
        if (taskFlowStatusEnum == TaskFlowStatusEnum.ABORT) {
            appEmailUtil.noticeOfflineApp(userInfo, appId, false);
        }
        return taskFlowStatusEnum;
    }

    /**
     * 2. 执行应用下线操作
     *
     * @return
     */
    public TaskFlowStatusEnum executeOffLineApp() {
        logger.info("executeOffLineApp");
        if (auditId > 0) {
            appAuditDao.updateAppAuditUser(auditId, 2, userInfo.getId());
        }
        List<InstanceInfo> instanceInfos = instanceDao.getInstListByAppId(appId);
        int type = appDesc.getType();
        List<Boolean> isShutDownList = Lists.newArrayList();
        if (instanceInfos != null) {
            isShutDownList = instanceInfos.parallelStream()
                    .map(instanceInfo -> instanceOffline(instanceInfo, type))
                    .collect(Collectors.toList());
        }
        if (isShutDownList.contains(false)) {
            appEmailUtil.noticeOfflineApp(userInfo, appId, false);
            return TaskFlowStatusEnum.ABORT;
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    private boolean instanceOffline(InstanceInfo instanceInfo, int type) {
        final String ip = instanceInfo.getIp();
        final int port = instanceInfo.getPort();
        boolean isShutdown = TypeUtil.isRedisType(type) ? redisCenter.shutdown(appId, ip, port) : true;
        if(isShutdown){
            isShutdown = redisCenter.checkShutdownSuccess(instanceInfo);
        }
        if (isShutdown) {
            instanceInfo.setStatus(InstanceStatusEnum.OFFLINE_STATUS.getStatus());
            instanceDao.update(instanceInfo);
        } else {
            logger.error("task {} appId {} {}:{} redis not shutdown!", taskId, appId, ip, port);
            return false;
        }
        return true;
    }

    /**
     * 3. 更新应用信息，发送邮件
     *
     * @return
     */
    public TaskFlowStatusEnum updateAppStatus() {
        logger.info("updateAppStatus");
        appDesc.setStatus(AppStatusEnum.STATUS_OFFLINE.getStatus());
        if (auditId > 0) {
            appAuditDao.updateAppAudit(auditId, 1);
        }
        int count = appService.update(appDesc);
        if (count > 0) {
            appEmailUtil.noticeOfflineApp(userInfo, appId, true);
            return TaskFlowStatusEnum.SUCCESS;
        } else {
            appEmailUtil.noticeOfflineApp(userInfo, appId, false);
            return TaskFlowStatusEnum.ABORT;
        }

    }
}
