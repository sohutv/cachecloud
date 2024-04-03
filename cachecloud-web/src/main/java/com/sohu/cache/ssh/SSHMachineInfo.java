package com.sohu.cache.ssh;

import lombok.Builder;
import lombok.Data;

import java.util.Objects;

/**
 * @Author: zengyizhao
 * @CreateTime: 2024/4/3 12:38
 * @Description: ssh session key
 * @Version: 1.0
 */
@Data
@Builder
public class SSHMachineInfo {

    /**
     * ip
     */
    private String ip;

    /**
     * 用户名
     */
    private String username;

    /**
     * 登录方式
     */
    private int authType;

    /**
     * 密码
     */
    private String password;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SSHMachineInfo that = (SSHMachineInfo) o;
        return authType == that.authType && Objects.equals(ip, that.ip) && Objects.equals(password, that.password) && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, username, authType, password);
    }
}
