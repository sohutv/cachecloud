package com.sohu.cache.dao;

import com.sohu.cache.entity.AppCapacityStatisticsResult;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.AppMonitorStatisticsResult;
import com.sohu.cache.entity.AppSearch;
import com.sohu.cache.entity.AppStatisticsSearch;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 基于app的dao操作
 *
 * @author leifu
 * @Date 2014年5月15日
 * @Time 下午1:58:22
 */
public interface AppDao {
    /**
     * 通过appId获取对应的app
     *
     * @param appId
     * @return
     */
    public AppDesc getAppDescById(@Param("appId") long appId);

    AppDesc getOnlineAppDescById(@Param("appId") long appId);

    /**
     * 通过所有在线的应用
     *
     * @return
     */
    public List<AppDesc> getOnlineApps();

    List<AppDesc> getOnlineAppsNonTest();

    /**
     * 通过所有在线的应用
     *
     * @return
     */
    public List<AppDesc> getAllApps();

    /**
     * 通过应用名获取对应app
     *
     * @param appName
     * @return
     */
    public AppDesc getByAppName(@Param("appName") String appName);

    /**
     * 保存app
     *
     * @param appDesc
     * @return
     */
    public int save(AppDesc appDesc);

    /**
     * 更新app
     *
     * @param appDesc
     * @return
     */
    public int update(AppDesc appDesc);

    /**
     * 更新appPwd
     * @param appId
     * @param appPwd
     * @return
     */
    int updateAppPwd(@Param("appId") long appId, @Param("pkey") String appPwd);

    /**
     * 更新app,包含自定义密码
     *
     * @param appDesc
     * @return
     */
    public int updateWithCustomPwd(AppDesc appDesc);


    /**
     * 删除app
     *
     * @param id
     * @return
     */
    public int delete(@Param("id") Long id);

    /**
     * 获取用户拥有的应用
     *
     * @param userId
     * @return
     */
    public List<AppDesc> getAppDescList(@Param("userId") long userId);

    /**
     * 获取应用拥有的应用个数
     *
     * @param userId
     * @return
     */
    public int getUserAppCount(@Param("userId") long userId);

    /**
     * 获取所有应用
     *
     * @param appSearch
     * @return
     */
    public List<AppDesc> getAllAppDescList(AppSearch appSearch);

    /**
     * 获取应用个数(有效状态)
     *
     * @param appSearch
     * @return
     */
    public int getAllAppCount(AppSearch appSearch);

    /**
     * <p>
     * Description:获取应用个数
     * </p>
     * @param
     * @return
     */
    public int getTotalAppCount();

    /**
     * 更新appKey
     *
     * @param appId
     * @param appKey
     */
    public void updateAppKey(@Param("appId") long appId, @Param("appKey") String appKey);

    /**
     * 更新持久化类型
     *
     * @param appId
     * @param persistenceType
     */
    public int updateAppPersistenceType(@Param("appId") long appId, @Param("persistenceType") int persistenceType);

    /**
     * 更新内存策略
     *
     * @param appId
     * @param maxmemoryPolicy
     */
    public int updateAppMaxmemoryPolicy(@Param("appId") long appId, @Param("maxmemoryPolicy") int maxmemoryPolicy);

    /**
     * 获取app安装不同版本的数量
     */
    @Select("SELECT version_id,count(version_id) as num from app_desc where status=2 GROUP BY version_id")
    public List<Map<String,Integer>> getVersionStat();

    public List<AppDesc> getAppDescByIds(@Param("appIds") List<String> appIds);

    public List<AppMonitorStatisticsResult> getMonitorStatistics(AppStatisticsSearch search);

    public List<AppCapacityStatisticsResult> getCapacityStatistics(AppStatisticsSearch search);

}
