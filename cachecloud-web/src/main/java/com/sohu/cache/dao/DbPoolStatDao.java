package com.sohu.cache.dao;

import com.sohu.cache.entity.DbPoolStat;

/**
 * @author fulei
 * @date 2018年8月17日
 * @time 上午11:44:51
 */
public interface DbPoolStatDao {

    /**
     * @param dbPoolStat
     * @return
     */
    int save(DbPoolStat dbPoolStat);

}
