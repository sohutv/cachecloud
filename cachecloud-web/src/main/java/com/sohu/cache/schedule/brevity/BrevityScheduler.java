package com.sohu.cache.schedule.brevity;

/**
 * 短频任务调度
 * Created by yijunzhang
 */
public interface BrevityScheduler {

    /**
     * 维护全量定时任务逻辑
     */
    void maintainTasks();

    /**
     * 异步竞争执行短频任务
     *
     * @return
     */
    void dispatcherTasks();

}
