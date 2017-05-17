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
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by yijunzhang on 15-1-20.
 */
public class InstanceRunInspector extends BaseAlertService implements Inspector {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 实例相关
     */
    private InstanceDao instanceDao;

    /**
     * redis相关
     */
    private RedisCenter redisCenter;

    /**
     * 应用相关dao
     */
    private AppDao appDao;

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
                Boolean isUpdate = updateInstanceByRun(isRun, info);
                if (isUpdate == null) {
                    continue;
                } else if (isUpdate) {
                    redisCenter.deployRedisCollection(appId, host, port);
                    redisCenter.deployRedisSlowLogCollection(appId, host, port);
                } else {
                    redisCenter.unDeployRedisCollection(appId, host, port);
                    redisCenter.unDeployRedisSlowLogCollection(appId, host, port);
                }
                // 错误
                if (isUpdate != null) {
                    alertInstanceInfo(info);
                }

            }
        }

        return true;
    }

    /**
     * 邮箱+短信
     * @param info
     */
    private void alertInstanceInfo(InstanceInfo info){
        sendEmailAlert(info);
        sendPhoneAlert(info);
    }

    /**
     * 发送短信报警
     *
     * @param info
     */
    private void sendPhoneAlert(InstanceInfo info) {
        if (info == null) {
            return;
        }
        String message = generateMessage(info, false);
        mobileAlertComponent.sendPhoneToAdmin(message);
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
        if(isEmail){
            message.append(", appId:");
            message.append(appId + "-" + appDesc.getName());
        }else{
            message.append("-appId(" + appId +"-" + appDesc.getName() +")");
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

    private Boolean updateInstanceByRun(boolean isRun, InstanceInfo info) {
        if (isRun) {
            if (info.getStatus() != InstanceStatusEnum.GOOD_STATUS.getStatus()) {
                info.setStatus(InstanceStatusEnum.GOOD_STATUS.getStatus());
                instanceDao.update(info);
                logger.warn("instance:{} instance is run", info);
                saveFault(info, isRun);
                return true;
            }
        } else {
            if (info.getStatus() != InstanceStatusEnum.ERROR_STATUS.getStatus()) {
                info.setStatus(InstanceStatusEnum.ERROR_STATUS.getStatus());
                instanceDao.update(info);
                logger.error("instance:{} instance failed", info);
                saveFault(info, isRun);
                return false;
            }
        }
        return null;
    }

    public void setInstanceDao(InstanceDao instanceDao) {
        this.instanceDao = instanceDao;
    }

    public void setRedisCenter(RedisCenter redisCenter) {
        this.redisCenter = redisCenter;
    }

    public void setAppDao(AppDao appDao) {
        this.appDao = appDao;
    }

    public void setInstanceFaultDao(InstanceFaultDao instanceFaultDao) {
        this.instanceFaultDao = instanceFaultDao;
    }

}
