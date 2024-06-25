package com.sohu.cache.web.vo;

import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Author: zengyizhao
 * @DateTime: 2021/9/14 11:11
 * @Description: 应用redis 配置参数名和值
 */
@Data
public class AppRedisConfigVo {

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 修改配置、重启日志记录id
     */
    private Long recordId;

    /**
     * 是否更新配置
     */
    private boolean configFlag;

    /**
     * 是否允许主从切换
     */
    private boolean transferFlag;

    /**
     * 操作的实例
     */
    private List<Integer> instanceList;

    /**
     * 配置名称
     */
    private List<RedisConfigVo> configList;

    /**
     * 是否快速完成滚动重启（修改配置，滚动重启）
     * 如果为是，则在操作每个主从分组中，不再sleep等待（等待的目的是：客户端刷新集群拓扑）
     */
    private boolean quickFinishFlag;

    public List<Integer> getInstanceList() {
        if(CollectionUtils.isEmpty(instanceList)){
            return null;
        }
        for (Integer instanceId : instanceList) {
            if(instanceId == 0){
                return null;
            }
        }
        return instanceList;
    }
}
