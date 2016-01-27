package com.sohu.cache.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.sohu.cache.entity.AppToUser;


/**
 * 用户-应用对应关系
 * 
 * @author leifu
 * @Time 2014年6月5日
 */
public interface AppToUserDao {
    
    List<AppToUser> getByUserId(@Param("userId") Long userId);

    Long save(AppToUser appToUser);

    void deleteByAppId(@Param("appId") Long appId);

    List<AppToUser> getByAppId(@Param("appId") Long appId);

    void deleteAppToUser(@Param("appId") long appId, @Param("userId") long userId);

}
