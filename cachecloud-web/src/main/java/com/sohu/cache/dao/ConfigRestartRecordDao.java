package com.sohu.cache.dao;

import com.sohu.cache.entity.ConfigRestartRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: zengyizhao
 * @DateTime: 2021/10/14 15:58
 * @Description: 重启记录
 */
public interface ConfigRestartRecordDao {

    /**
     * @param configRestartRecord
     */
    void save(ConfigRestartRecord configRestartRecord);

    /**
     * @param id
     * @return
     */
    ConfigRestartRecord getById(@Param("id") long id);

    /**
     * 更新记录状态
     *
     * @param id
     * @param status
     */
    void updateStatus(@Param("id") long id, @Param("status") int status);

    /**
     * 根据条件更新
     *
     * @param configRestartRecord
     */
    void updateByCondition(ConfigRestartRecord configRestartRecord);

    /**
     * 获取重启记录列表
     * @param configRestartRecord
     * @return
     */
    List<ConfigRestartRecord> getListByCondition(ConfigRestartRecord configRestartRecord);

    /**
     * 获取重启记录条数
     * @param configRestartRecord
     * @return
     */
    int getCountByCondition(ConfigRestartRecord configRestartRecord);

}
