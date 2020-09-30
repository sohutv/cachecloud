package com.sohu.cache.web.vo;

import lombok.Data;

import java.util.List;

/**
 * <p>
 * Description: Redis 主-从关系 vo
 * </p>
 *
 * @author chenshi
 * @version 1.0
 * @date 2018/9/21
 */
@Data
public class RedisInfo {

    private int sid;

    private String ip;

    private int port;

    private String version;

    private String role;

    List<RedisInfo> redisInfoList;

    public RedisInfo(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public RedisInfo(String ip, int port, String version, String role) {
        this.ip = ip;
        this.port = port;
        this.version = version;
        this.role = role;
    }

    public RedisInfo(int sid, String ip, int port, String version, String role) {
        this.sid = sid;
        this.ip = ip;
        this.port = port;
        this.version = version;
        this.role = role;
    }

    public RedisInfo(String ip, String role) {
        this.ip = ip;
        this.role = role;
    }

    public RedisInfo(String ip, int port, String version, String role, List<RedisInfo> redisInfoList) {
        this.ip = ip;
        this.port = port;
        this.version = version;
        this.role = role;
        this.redisInfoList = redisInfoList;
    }

    public String getIpAndPortInfo(RedisInfo redisInfo) {
        return redisInfo.getIp() + ":" + redisInfo.getPort();
    }

    public String getRedisInfo(RedisInfo redisInfo) {
        if (redisInfo.getPort() == 0) {
            return redisInfo.getIp() + ":xxxx " + redisInfo.getRole() + " \n";
        }
        return redisInfo.getIp() + ":" + redisInfo.getPort() + " " + redisInfo.getRole() + " \n";
    }

    public String getInfo(RedisInfo redisInfo) {
        return redisInfo.getIp() + ":" + redisInfo.getPort() + " " + redisInfo.getRole() + " " + redisInfo.getVersion() + " \n";
    }

}


