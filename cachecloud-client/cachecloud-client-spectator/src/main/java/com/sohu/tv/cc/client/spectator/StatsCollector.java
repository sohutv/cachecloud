package com.sohu.tv.cc.client.spectator;

import com.sohu.tv.cc.client.spectator.stat.CommandStat;

/**
 * @author wenruiwu
 * @create 2019/12/18 18:11
 * @description
 */
public interface StatsCollector {

    void start();

    /**
     * 追加命令调用成功stat
     *
     * @param stat
     */
    boolean appendCommandStat(final CommandStat stat);

    /**
     * 追加命令异常stat
     *
     * @param item
     */
    boolean appendCommandExpStat(final CommandTracker.CommandItem item);

    /**
     * 追加连接异常stat
     *
     * @param node redis节点
     * @param cost 连接耗时
     */
    boolean appendConnectExpStat(final String node, final long cost);

    void shutdown();

}
