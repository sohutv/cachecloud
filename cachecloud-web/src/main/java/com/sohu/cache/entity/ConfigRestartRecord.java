package com.sohu.cache.entity;

import com.alibaba.fastjson.JSONObject;
import com.sohu.cache.web.util.Page;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author: zengyizhao
 * @DateTime: 2021/10/14 15:52
 * @Description: 配置重启记录
 */
@Data
public class ConfigRestartRecord {

    /**
     *
     */
    private long id;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 重启类型（滚动重启，修改配置强制重启）
     */
    private int operateType;

    /**
     * 初始化任务参数(json):不变
     */
    private String param;

    /**
     * 状态：0等待，1运行，2失败
     */
    private Integer status;

    /**
     * 日志信息
     */
    private String log;

    /**
     * 实例信息
     */
    private String instances;

    /**
     * 操作人员id
     */
    private long userId;

    /**
     * 操作人员名称
     */
    private String userName;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 分页参数
     */
    private Page page;

    private List<String> logList;

    private List<Integer> instanceIdList;

    public List<String> getLogList(){
        List<String> logList = JSONObject.parseArray(log, String.class);
        return logList;
    }

    public List<Integer> getInstanceIdList(){
        List<Integer> logList = JSONObject.parseArray(instances, Integer.class);
        return logList;
    }


}
