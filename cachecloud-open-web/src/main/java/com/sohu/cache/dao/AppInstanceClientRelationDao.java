package com.sohu.cache.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.sohu.cache.entity.AppInstanceClientRelation;

/**
 * 应用下节点和客户端关系Dao
 * 
 * @author leifu
 * @Date 2016年5月4日
 * @Time 上午9:19:44
 */
public interface AppInstanceClientRelationDao {

    int save(AppInstanceClientRelation appInstanceClientRelation);

    int batchSave(@Param("appInstanceClientRelationList") List<AppInstanceClientRelation> appInstanceClientRelationList);

    int isExist(AppInstanceClientRelation appInstanceClientRelation);

    List<AppInstanceClientRelation> getAppInstanceClientRelationList(@Param("appId") Long appId, @Param("day") Date day);

}
