package com.sohu.cache.entity;
import com.sohu.cache.constant.InstanceStatusEnum;
import com.sohu.cache.task.constant.InstanceInfoEnum;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.web.enums.BooleanEnum;
import lombok.Data;
import redis.clients.jedis.Module;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 实例信息
 * User: lingguo
 */
@Data
public class InstanceInfo implements Serializable {
    private static final long serialVersionUID = -903896025243493024L;
    /**
     * 实例id
     */
    private int id;
    /**
     * 应用id
     */
    private long appId;
    /**
     * host id
     */
    private long hostId;
    /**
     * ip
     */
    private String ip;
    /**
     * 端口
     */
    private int port;
    /**
     * 是否启用 0:节点异常,1:正常启用,2:节点下线
     */
    private int status;
    /**
     * 开启的内存
     */
    private int mem;
    /**
     * 连接数
     */
    private int conn;
    /**
     * 启动命令 或者 redis-sentinel的masterName
     */
    private String cmd;
    private int type;
    private String typeDesc;
    private int masterInstanceId;
    private String masterHost;
    private int masterPort;
    private String roleDesc;
    private int groupId;
    private Date updateTime;

    private List<Module> modules;

    public String getTypeDesc() {
        if (type <= 0) {
            typeDesc = "";
        } else if (type == ConstUtils.CACHE_TYPE_REDIS_CLUSTER) {
            typeDesc = "redis-cluster";
        } else if (type == ConstUtils.CACHE_REDIS_SENTINEL) {
            typeDesc = "redis-sentinel";
        } else if (type == ConstUtils.CACHE_REDIS_STANDALONE) {
            typeDesc = "redis-standalone";
        }
        return typeDesc;
    }
    public String getStatusDesc() {
        InstanceStatusEnum instanceStatusEnum = InstanceStatusEnum.getByStatus(status);
        if (instanceStatusEnum != null) {
            return instanceStatusEnum.getInfo();
        }
        return "";
    }
    /**
     * 判断当前节点是否下线
     *
     * @return
     */
    public boolean isOffline() {
        return status == InstanceStatusEnum.OFFLINE_STATUS.getStatus() || status == InstanceStatusEnum.FORGET_STATUS.getStatus();
    }
    /**
     * 判断当前节点是否在线
     */
    public boolean isOnline() {
        return status == InstanceStatusEnum.GOOD_STATUS.getStatus();
    }
    public String getRoleDesc() {
        if (type == ConstUtils.CACHE_REDIS_SENTINEL) {
            return "sentinel";
        } else {
            return roleDesc;
        }
    }
    public void setRoleDesc(BooleanEnum isMaster) {
        if (isMaster == BooleanEnum.OTHER) {
            roleDesc = "未知";
        } else if (isMaster == BooleanEnum.TRUE) {
            roleDesc = "master";
        } else if (isMaster == BooleanEnum.FALSE) {
            roleDesc = "slave";
        }
    }
    public String getHostPort() {
        return ip + ":" + port;
    }
    public boolean isMemcached() {
        return InstanceInfoEnum.InstanceTypeEnum.MEMCACHE.getType() == type;
    }
    public boolean isNutCracker() {
        return InstanceInfoEnum.InstanceTypeEnum.NUTCRACKER.getType() == type;
    }
    public boolean isPika() {
        return InstanceInfoEnum.InstanceTypeEnum.PIKA.getType() == type;
    }
    /**
     * 是否是redis数据节点
     *
     * @return
     */
    public boolean isRedisData() {
        if (type == InstanceInfoEnum.InstanceTypeEnum.REDIS_SERVER.getType()
                || type == InstanceInfoEnum.InstanceTypeEnum.REDIS_CLUSTER.getType()) {
            return true;
        }
        return false;
    }
    public Date getUpdateTime() {
        if(updateTime != null){
            return (Date) updateTime.clone();
        }
        return null;
    }
    public String getUpdateTimeDesc() {
        if(updateTime != null){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format((Date) updateTime.clone());
        } else {
            return "";
        }
    }
    public void setUpdateTime(Date updateTime) {
        this.updateTime = (Date) updateTime.clone();
    }
}