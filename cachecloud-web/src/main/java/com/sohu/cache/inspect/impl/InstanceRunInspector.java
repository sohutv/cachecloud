package com.sohu.cache.inspect.impl;

import com.sohu.cache.alert.impl.BaseAlertService;
import com.sohu.cache.constant.InstanceStatusEnum;
import com.sohu.cache.dao.AppDao;
import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.dao.InstanceFaultDao;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.InstanceFault;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.inspect.InspectParamEnum;
import com.sohu.cache.inspect.Inspector;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.util.TypeUtil;
import com.sohu.cache.web.enums.AlertTypeEnum;
import com.sohu.cache.web.enums.BooleanEnum;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by yijunzhang on 15-1-20.
 */
public class InstanceRunInspector extends BaseAlertService implements Inspector {

    /**
     * 实例相关
     */
    @Autowired
    private InstanceDao instanceDao;

    /**
     * redis相关
     */
    @Autowired
    private RedisCenter redisCenter;

    /**
     * 应用相关dao
     */
    @Autowired
    private AppDao appDao;
    @Autowired
    private InstanceFaultDao instanceFaultDao;

    @Override
    public boolean inspect(Map<InspectParamEnum, Object> paramMap) {
        String host = MapUtils.getString(paramMap, InspectParamEnum.SPLIT_KEY);
        List<InstanceInfo> list = (List<InstanceInfo>) paramMap.get(InspectParamEnum.INSTANCE_LIST);
        for (InstanceInfo info : list) {
            final int port = info.getPort();
            final int type = info.getType();
            long appId = info.getAppId();
            if (TypeUtil.isRedisType(type)) {
                boolean isRun;
                if (TypeUtil.isRedisSentinel(type)) {
                    isRun = redisCenter.isRun(host, port);
                } else {
                    isRun = redisCenter.isRun(appId, host, port);
                }
                BooleanEnum isUpdate = updateInstanceByRun(isRun, info);
                // 错误
                if (isUpdate != BooleanEnum.OTHER) {
                    alertInstanceInfo(info);
                }

            }
        }

        return true;
    }

    /**
     * 邮箱+短信
     *
     * @param info
     */
    private void alertInstanceInfo(InstanceInfo info) {
        sendEmailAlert(info);
    }

    /**
     * 发送邮箱报警
     *
     * @param info
     */
    private void sendEmailAlert(InstanceInfo info) {
        if (info == null) {
            return;
        }
        String title = "实例(" + info.getIp() + ":" + info.getPort() + ")状态发生变化";
        String message = generateMessage(info, true);
        appAlertRecordService.saveAlertInfoByType(AlertTypeEnum.INSTANCE_RUNNING_STATE_CHANGE, title, message, info);
        emailComponent.sendMailToAdmin(title, message);
    }

    /**
     * 返回示例消息
     *
     * @param info
     * @return
     */
    private String generateMessage(InstanceInfo info, boolean isEmail) {
        StringBuffer message = new StringBuffer();
        long appId = info.getAppId();
        AppDesc appDesc = appDao.getAppDescById(appId);
        message.append("CacheCloud系统-实例(" + info.getIp() + ":" + info.getPort() + ")-");
        if (info.getStatus() == InstanceStatusEnum.ERROR_STATUS.getStatus()) {
            message.append("由运行中变为心跳停止");
        } else if (info.getStatus() == InstanceStatusEnum.GOOD_STATUS.getStatus()) {
            message.append("由心跳停止变为运行中");
        }
        if (isEmail) {
            message.append(", appId:");
            message.append(appId + "-" + appDesc.getName());
        } else {
            message.append("-appId(" + appId + "-" + appDesc.getName() + ")");
        }

        return message.toString();
    }

    private void saveFault(InstanceInfo info, boolean isRun) {
        InstanceFault instanceFault = new InstanceFault();
        instanceFault.setAppId((int) info.getAppId());
        instanceFault.setInstId(info.getId());
        instanceFault.setIp(info.getIp());
        instanceFault.setPort(info.getPort());
        instanceFault.setType(info.getType());
        instanceFault.setCreateTime(new Date());
        if (isRun) {
            instanceFault.setReason("恢复运行");
        } else {
            instanceFault.setReason("心跳停止");
        }
        instanceFaultDao.insert(instanceFault);
    }

    private BooleanEnum updateInstanceByRun(boolean isRun, InstanceInfo info) {
        try {
            InstanceInfo info_new = instanceDao.getInstanceInfoById(info.getId());
            info.setStatus(info_new.getStatus());
            if (isRun) {
                if (info.getStatus() != InstanceStatusEnum.GOOD_STATUS.getStatus()) {
                    info.setStatus(InstanceStatusEnum.GOOD_STATUS.getStatus());
                    instanceDao.update(info);
                    logger.warn("instance:{} instance is run", info);
                    saveFault(info, isRun);
                    return BooleanEnum.TRUE;
                }
            } else {
                if (info.getStatus() != InstanceStatusEnum.ERROR_STATUS.getStatus()
                        && info.getStatus() != InstanceStatusEnum.OFFLINE_STATUS.getStatus()) {
                    info.setStatus(InstanceStatusEnum.ERROR_STATUS.getStatus());
                    instanceDao.update(info);
                    logger.error("instance:{} instance failed", info);
                    saveFault(info, isRun);
                    return BooleanEnum.FALSE;
                }else if(info.getStatus() == InstanceStatusEnum.OFFLINE_STATUS.getStatus()){
                    logger.error("instance:{} instance is offline", info);
                    saveFault(info, isRun);
                    return BooleanEnum.OTHER;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return BooleanEnum.OTHER;
    }

}
