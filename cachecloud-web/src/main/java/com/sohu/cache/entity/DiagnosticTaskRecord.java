package com.sohu.cache.entity;

import lombok.Data;

import java.util.Date;

/**
 * @Author: rucao
 * @Date: 2020/6/9 16:42
 */
@Data
public class DiagnosticTaskRecord {
    private long id;
    /**
     * 任务流id
     */
    private long taskId;
    /**
     * 父任务id
     */
    private long parentTaskId;
    /**
     * 审批id
     */
    private long auditId;

    /**
     * 诊断类型：0scan 1bigkey 2idle key 3hotkey 4del key 5slot analysis 6topology exam
     */
    private int type;
    /**
     * 诊断状态：0开始 1结束 2异常
     */
    private int status;
    /**
     * 应用id
     */
    private long appId;
    /**
     * ip:port
     */
    private String node;
    /**
     * 诊断条件
     */
    private String diagnosticCondition;
    /**
     * 备用参数1
     */
    private String param1;
    /**
     * 备用参数2
     */
    private String param2;
    /**
     * 结果的key
     */
    private String redisKey;

    /**
     * 耗时，毫秒
     */
    private long cost;

    private String formatCostTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date modifyTime;

}
