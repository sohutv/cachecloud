package com.sohu.cache.dao;

import com.sohu.cache.entity.SystemResource;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Created by chenshi on 2020/7/6.
 */
public interface ResourceDao {

    List<SystemResource> getResourceList(@Param("resourceType") int resourceType);

    List<SystemResource> getResourceListByName(@Param("resourceType") int resourceType,@Param("searchName") String searchName);

    SystemResource getResourceById(@Param("resourceId") int resourceId);

    SystemResource getResourceByName(@Param("resourceName") String resourceName);

    void save(SystemResource systemResource);

    void update(SystemResource systemResource);

    List<Map<Integer,Integer>> getAppUseRedis();

}
