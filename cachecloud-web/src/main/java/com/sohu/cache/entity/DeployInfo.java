package com.sohu.cache.entity;

import com.sohu.cache.web.enums.NodeEnum;
import lombok.Data;

/**
 * Created by rucao
 */
@Data
public class DeployInfo {
    /**
     * 部署形态: 参考 ConstUtils
     * 2: Redis Cluster
     * 5: Redis+Sentinel
     * 7: Redis+Twemproxy
     * 8: Pika+Sentinel
     * 9: Pika+Twemproxy
     * 6: Redis Standalone
     */
    private Integer deployType;
    /**
     * 分配内存大小，单位M
     */
    private Integer memSize;
    /**
     * Redis node
     */
    private String masterIp;
    private String slaveIp;
    /**
     * Pika node
     */
    private String masterPikaIp;
    private String slavePikaIp;
    /**
     * sentinel/twemproxy node
     */
    private String sentinelIp;
    private String twemproxyIp;

    public DeployInfo() {
    }

    public DeployInfo(Integer deployType, String masterIp, Integer memSize) {
        this.deployType = deployType;
        this.masterIp = masterIp;
        this.memSize = memSize;
    }

    public DeployInfo(Integer deployType, String masterIp, Integer memSize, String slaveIp) {
        this.deployType = deployType;
        this.masterIp = masterIp;
        this.memSize = memSize;
        this.slaveIp = slaveIp;
    }

    public DeployInfo(Integer deployType, String sentinelIp) {
        this.deployType = deployType;
        this.sentinelIp = sentinelIp;
    }

    /**
     * 获取Redis实例信息
     */
    public static DeployInfo getRedisInfo(Integer deployType, String masterIp, Integer memSize, String slaveIp) {
        DeployInfo deployInfo = new DeployInfo();
        deployInfo.setDeployType(deployType);
        deployInfo.setMasterIp(masterIp);
        deployInfo.setMemSize(memSize);
        deployInfo.setSlaveIp(slaveIp);
        return deployInfo;
    }

    /**
     * 获取Pika实例信息
     */
    public static DeployInfo getPikaInfo(Integer deployType, String masterPikaIp, Integer memSize, String slavePikaIp) {
        DeployInfo deployInfo = new DeployInfo();
        deployInfo.setDeployType(deployType);
        deployInfo.setMasterPikaIp(masterPikaIp);
        deployInfo.setMemSize(memSize);
        deployInfo.setSlavePikaIp(slavePikaIp);
        return deployInfo;
    }

    /**
     * 获取Sentinel实例信息
     */
    public static DeployInfo getSentinelInfo(Integer deployType, String sentinelIp) {
        DeployInfo deployInfo = new DeployInfo();
        deployInfo.setDeployType(deployType);
        deployInfo.setSentinelIp(sentinelIp);
        return deployInfo;
    }

    /**
     * 获取Twemproxy实例信息
     */
    public static DeployInfo getTwemproxyInfo(Integer deployType, String twemproxyIp) {
        DeployInfo deployInfo = new DeployInfo();
        deployInfo.setDeployType(deployType);
        deployInfo.setTwemproxyIp(twemproxyIp);
        return deployInfo;
    }

    /**
     * 判断是否Redis/Pika节点
     */
    public static Boolean isRedisNode(int type){
        if(type == NodeEnum.REDIS_NODE.getValue()){
            return true;
        }
        return false;
    }

    /**
     * 判断是否Sentinel/Twemproxy节点
     */
    public static Boolean isSentinelNode(int type){
        if(type == NodeEnum.SENTINEL_NODE.getValue() ){
            return true;
        }
        return false;
    }

}
