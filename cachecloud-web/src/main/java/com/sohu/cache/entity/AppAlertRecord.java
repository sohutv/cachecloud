package com.sohu.cache.entity;

import com.sohu.cache.web.util.DateUtil;
import lombok.Data;

import java.util.Date;

/**
 * @Author: zengyizhao
 * @DateTime: 2021/9/3 12:00
 * @Description: 报警记录
 */
@Data
public class AppAlertRecord {

    /**
     * 记录id
     */
    private long id;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 重要度（0：一般；1：重要；2：紧急）
     */
    private int importantLevel;

    /**
     * 可见类型（0：均可见；1：仅管理员可见；）
     */
    private int visibleType;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 实例id
     */
    private Long instanceId;

    /**
     * ip地址
     */
    private String ip;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 报警标题
     */
    private String title;

    /**
     * 报警内容
     */
    private String content;

    public String getCreateTimeDesc(){
        return DateUtil.formatYYYYMMddHHMMSS(createTime);
    }

}
