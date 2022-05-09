package com.sohu.cache.web.vo;

import com.sohu.cache.web.util.DateUtil;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: zengyizhao
 * @DateTime: 2021/9/14 11:11
 * @Description: redis 配置校验结果
 */
@Data
public class RedisConfigCheckResult implements Serializable {

    /**
     * 检测时间
     */
    private Date createTime;

    /**
     * redis版本
     */
    private Integer versionId;

    /**
     * 配置项
     */
    private String configName;

    /**
     * 比较类型
     */
    private int compareType;

    /**
     * 比较值
     */
    private String expectValue;

    /**
     * 比较值
     */
    private String userName;

    /**
     * 配置项
     */
    private String key;

    /**
     * redis版本
     */
    private boolean success;

    private String createTimeStr;

    public String getCreateTimeStr(){
        return DateUtil.formatYYYYMMddHHMMSS(createTime);
    }

}
