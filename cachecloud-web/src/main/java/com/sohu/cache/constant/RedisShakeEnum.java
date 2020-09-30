package com.sohu.cache.constant;

/**
 * Created by rucao on 2019/10/30
 */
public enum RedisShakeEnum {
    LOG_WAITING_SOURCE_RDB("waiting source rdb", "等待源端save rdb完毕"),
    LOG_SYNCING("total", "全量同步阶段，显示百分比"),
    LOG_SYNC_RDB_DONE("sync rdb done", "全量同步完成"),
    LOG_FORWARD_COMMANDS("forwardCommands","当前dbSyncer进入增量同步"),
    LOG_ERROR("error","迁移任务发生异常，程序中断，详情请查看日志");


    private String keyword;
    private String description;

    RedisShakeEnum(String keyword, String description) {
        this.keyword = keyword;
        this.description = description;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getDescription() {
        return description;
    }
}
