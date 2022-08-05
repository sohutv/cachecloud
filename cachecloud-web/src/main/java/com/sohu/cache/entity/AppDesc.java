package com.sohu.cache.entity;

import com.sohu.cache.constant.AppDescEnum;
import com.sohu.cache.constant.AppDescEnum.AppImportantLevel;
import com.sohu.cache.constant.AppStatusEnum;
import com.sohu.cache.redis.util.AuthUtil;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.web.enums.AppTypeEnum;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 应用的信息
 * User: lingguo
 */
@Data
public class AppDesc implements Serializable {
    private static final long serialVersionUID = -3507970915810652761L;

    /**
     * 应用id
     */
    private long appId;

    /**
     * 应用秘钥
     */
    private String appKey;

    /**
     * 应用名称
     */
    private String name;

    /**
     * 用户id
     */
    private long userId;

    /**
     * 应用状态, 0未分配，1是申请了未审批，2是审批并发布, 3应用下线 4 驳回
     */
    private int status;

    /**
     * 应用描述
     */
    private String intro;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 申请通过的时间
     */
    private Date passedTime;

    /**
     * 类型
     */
    private int type;

    /**
     * 类型描述
     */
    private String typeDesc;

    /**
     * 负责人，用户id，逗号分隔
     */
    private String officer;

    /**
     * 版本号
     */
    private int verId;

    /**
     * 类型：0：正式 1：测试 2：试用
     */
    private int isTest;

    /**
     * 是否有后端数据源: 1是0否
     */
    private int hasBackStore;

    /**
     * 是否需要持久化: 1是0否
     */
    private int needPersistence;

    /**
     * 预估qps
     */
    private int forecaseQps;

    /**
     * 是否需要热备: 1是0否
     */
    private int needHotBackUp;

    /**
     * 预估条目数
     */
    private int forecastObjNum;

    /**
     * 内存报警阀值
     */
    private int memAlertValue;

    /**
     * 客户端连接数报警阀值
     */
    private int clientConnAlertValue;

    /**
     * 客户端命中率报警阀值 0:默认不监控
     */
    private int hitPrecentAlertValue;

    /**
     * 客户端是否接入 全局监控
     */
    private int isAccessMonitor;

    /**
     * 客户端机器机房
     */
    private String clientMachineRoom;

    /**
     * redis密码
     */
    private String pkey;

    /**
     * 重要度，默认重要
     */
    private int importantLevel = AppImportantLevel.IMPORTANT.getValue();

    /**
     * Redis版本id,名称
     */
    private int versionId;
    private String versionName;

    /**
     * Redis小版本是否可以升级 0:最新版本  1:可升小版本
     */
    private int isVersionUpgrade = 0;

    /**
     * 自定义密码，优先级最高
     */
    private String customPassword;

    public String getAuthPassword() {
        if(StringUtils.isNotBlank(customPassword)){
            return customPassword;
        }
        String authPassword = appId + AuthUtil.SPLIT_KEY;
        if (StringUtils.isNotBlank(pkey)) {
            authPassword += getAppPassword();
        }
        return authPassword;
    }

    public boolean isSetCustomPassword(){
        if(StringUtils.isNotBlank(customPassword)){
            return true;
        }
        return false;
    }

    public String getPasswordMd5() {
        if (StringUtils.isNotBlank(pkey)) {
            return AuthUtil.getAppIdMD5(pkey);
        }
        return null;
    }

    public String getAppPassword() {
        if(StringUtils.isNotBlank(customPassword)){
            return customPassword;
        }
        if (StringUtils.isNotBlank(pkey)) {
            return AuthUtil.getAppIdMD5(pkey);
        }
        return null;
    }

    /**
     * 应用运行天数
     */
    public int getAppRunDays() {
        if (createTime == null) {
            return -1;
        }
        Date now = new Date();
        long diff = now.getTime() - createTime.getTime();
        return (int) (diff / TimeUnit.DAYS.toMillis(1));
    }

    public String getTypeDesc() {
        if (type == ConstUtils.CACHE_TYPE_REDIS_CLUSTER) {
            return "redis-cluster";
        } else if (type == ConstUtils.CACHE_REDIS_SENTINEL) {
            return "redis-sentinel";
        } else if (type == ConstUtils.CACHE_REDIS_STANDALONE) {
            return "redis-standalone";
        } else if (type == ConstUtils.CACHE_REDIS_TWEMPROXY) {
            return "redis-twemproxy";
        } else if (type == ConstUtils.CACHE_PIKA_SENTINEL) {
            return "pika-sentinel";
        } else if (type == ConstUtils.CACHE_PIKA_TWEMPROXY) {
            return "pika-twemproxy";
        }
        return "";
    }

    public String getStatusDesc() {
        AppStatusEnum appStatusEnum = AppStatusEnum.getByStatus(status);
        if (appStatusEnum != null) {
            return appStatusEnum.getInfo();
        }
        return "";
    }

    /**
     * 是否上线
     *
     * @return
     */
    public boolean isOnline() {
        return status == AppStatusEnum.STATUS_PUBLISHED.getStatus();
    }

    /**
     * 是否下线
     *
     * @return
     */
    public boolean isOffline() {
        return status == AppStatusEnum.STATUS_OFFLINE.getStatus();
    }

    /**
     * 是否是测试
     *
     * @return
     */
    public boolean isTestOk() {
        return isTest == AppDescEnum.AppTest.IS_TEST.getValue();
    }

    /**
     * 非常重要
     *
     * @return
     */
    public boolean isVeryImportant() {
        return importantLevel == AppDescEnum.AppImportantLevel.VERY_IMPORTANT.getValue();
    }

    /**
     * 超级重要
     *
     * @return
     */
    public boolean isSuperImportant() {
        return importantLevel == AppDescEnum.AppImportantLevel.SUPER_IMPORTANT.getValue();
    }

    /**
     * memcache相关
     *
     * @return
     */
    public boolean isMemcached() {
        return AppTypeEnum.MEMCACHED.getType() == type;
    }

    public Date getCreateTime() {
        if (createTime != null) {
            return (Date) createTime.clone();
        }
        return null;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = (Date) createTime.clone();
    }

    public Date getPassedTime() {
        if (passedTime != null) {
            return (Date) passedTime.clone();
        }
        return null;
    }

    public void setPassedTime(Date passedTime) {
        this.passedTime = (Date) passedTime.clone();
    }
}
