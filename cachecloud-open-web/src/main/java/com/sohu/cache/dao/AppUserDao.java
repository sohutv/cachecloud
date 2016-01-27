package com.sohu.cache.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.sohu.cache.entity.AppUser;

/**
 * 用户管理dao
 * @author leifu
 * @Time 2014年6月5日
 */
public interface AppUserDao {

	public AppUser get(@Param("id") Long id);
	
	public int save(AppUser user);
	
	public int update(AppUser user);
	
	public int delete(@Param("id") Long id);
	
	public AppUser getByName(@Param("name") String name);

    public List<AppUser> getUserList(@Param("chName") String chName);
}
