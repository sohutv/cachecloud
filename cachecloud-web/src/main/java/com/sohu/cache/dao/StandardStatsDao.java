package com.sohu.cache.dao;

import com.sohu.cache.entity.StandardStats;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 实例统计相关DAO
 */
public interface StandardStatsDao {

    public int mergeStandardStats(StandardStats standardStats);

    public int mergeInstanceMinuteStats(StandardStats standardStats);

    public StandardStats getStandardStats(@Param("collectTime") long collectTime, @Param("ip") String ip,
            @Param("port") int port, @Param("dbType") String dbType);

    public List<StandardStats> getDiffJsonList(@Param("beginTime") long beginTime, @Param("endTime") long endTime,
            @Param("ip") String ip, @Param("port") int port, @Param("dbType") String dbType);

    public int deleteStandardStatsLessCreatedTime(@Param("createdTime") Date createdTime);

    public List<StandardStats> getStandardStatsByCreateTime(@Param("beginTime") Date beginTime,
            @Param("endTime") Date endTime, @Param("dbType") String dbType);

}
