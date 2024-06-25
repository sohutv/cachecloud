package com.sohu.cache.web.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author: zengyizhao
 * @CreateTime: 2023/2/27 18:43
 * @Description: redis node信息（cluster node）
 * @Version: 1.0
 */
@Data
public class RedisClusterNode {

    private String hostPort;

    private String role;

    private boolean fail;

    private boolean connected;

    private List<Integer> slots;

    private String masterHostPort;

}
