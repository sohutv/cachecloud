package com.sohu.cache.dao;

import com.sohu.cache.entity.AppBiz;
import com.sohu.cache.entity.AppUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 业务组管理dao
 * @author zengyizhao
 * @Time 2023年1月16日
 */
public interface AppBizDao {

	public AppBiz get(@Param("id") Long id);
	
	public int save(AppBiz appBiz);
	
	public int update(AppBiz appBiz);
	
	public int delete(@Param("id") Long id);

    public List<AppBiz> getBizList();
}
