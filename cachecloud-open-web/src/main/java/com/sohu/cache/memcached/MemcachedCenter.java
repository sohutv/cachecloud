package com.sohu.cache.memcached;

import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.AppUser;

import java.util.Map;

/**
 * Memcached的相关操作接口
 *
 * User: lingguo
 * Date: 14-6-11
 * Time: 下午4:43
 */
public interface MemcachedCenter {

    /**
     * 为memcached实例创建一个trigger，并启用
     *
     * @return
     */
    public boolean deployMemcachedCollection(final long appId, final String host, final int port);

    /**
     * 取消memcached实例的trigger
     *
     * @param appId
     * @param host
     * @param port
     * @return
     */
    public boolean unDeployMemcachedCollection(long appId, String host, int port);

    /**
     * 收集memcached实例的统计信息，存入到mysql中
     *
     * @param appId
     * @param collectTime
     * @param host
     * @param port
     * @return
     */
    public Map<String, Object> collectMemcachedInfo(final long appId, final long collectTime, final String host, final int port);

    /**
     * 收集memcached统计信息
     *
     * @param host
     * @param port
     * @return
     */
    public Map<String, Object> getInfoStats(final String host, final int port);

    /**
     * 判断实例是否运行
     *
     * @param ip
     * @param port
     * @return
     */
    public boolean isRun(final String ip, final int port);

    /**
     * 执行memcached命令
     * @param appDesc
     * @param command
     * @return
     */
    public String executeCommand(AppDesc appDesc, String command);

    /**
     * 实例执行memcached命令
     *
     * @param host
     * @param port
     * @param command
     * @return
     */
    public String executeCommand(final String host, final int port, String command);

    /**
     * 启动memcache服务
     *
     * @param host
     * @param maxMemory
     * @return
     */
    public boolean deployMemcached(final long appId, final String host, final int maxMemory);

    /**
     * 关闭memcache服务
     * @param host
     * @param port
     * @return
     */
    public boolean shutdown(String host, int port);

    /**
     * 清除数据
     * @param appDesc
     * @param appUser
     * @return
     */
    public boolean cleanAppData(AppDesc appDesc, AppUser appUser);
}
