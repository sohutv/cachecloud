package com.sohu.cache.web.vo;

import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.web.util.DateUtil;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Author: zengyizhao
 * @DateTime: 2021/9/14 11:11
 * @Description: 应用redis 配置校验
 */
@Data
public class AppRedisConfigCheckResult implements Serializable {

    /**
     * 检测时间
     */
    private Date createTime;

    /**
     * redis版本
     */
    private Integer versionId;

    /**
     * 应用id
     */
    private AppDesc appDesc;

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
     * 配置项
     */
    private List<InstanceRedisConfigCheckResult> instanceCheckList;

    /**
     * redis版本
     */
    private boolean success;

    private Long appId;

    private String createTimeStr;

    public Long getAppId(){
        if(appDesc != null){
            return appDesc.getAppId();
        }
        return null;
    }

    private String instanceIds;

    public String getInstanceIds(){
        final StringBuilder sb = new StringBuilder();
        if(CollectionUtils.isNotEmpty(instanceCheckList)){
            instanceCheckList.forEach(instanceCheckResult -> {
                if(!instanceCheckResult.isSuccess()){
                    sb.append(instanceCheckResult.getInstanceInfo().getId());
                    sb.append(",");
                }
            });
            if(sb.length() > 1){
                sb.deleteCharAt(sb.length() - 1);
            }
        }
        return sb.toString();
    }

    public String getCreateTimeStr(){
        return DateUtil.formatYYYYMMddHHMMSS(createTime);
    }

}
