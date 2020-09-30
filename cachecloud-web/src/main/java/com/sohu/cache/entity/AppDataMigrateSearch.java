package com.sohu.cache.entity;

import com.sohu.cache.web.util.Page;
import lombok.Data;

/**
 * 应用数据迁移搜索
 *
 * @author leifu
 */
@Data
public class AppDataMigrateSearch {
    /**
     * 源应用id
     */
    private Long sourceAppId;

    /**
     * 目标应用id
     */
    private Long targetAppId;

    /**
     * 源实例
     */
    private String sourceInstanceIp;

    /**
     * 目标实例
     */
    private String targetInstanceIp;

    /**
     * 迁移机器
     */
    private String migrateMachine;
    /**
     * 操作人
     */
    private Long userId;

    /**
     * 开始时间
     */
    private String startDate;

    /**
     * 结束时间
     */
    private String endDate;

    /**
     * 状态 -2全部，0全量同步，1同步结束，2同步异常，3增量同步
     */
    private int status = 3;

    /**
     * 分页
     */
    private Page page;
}
