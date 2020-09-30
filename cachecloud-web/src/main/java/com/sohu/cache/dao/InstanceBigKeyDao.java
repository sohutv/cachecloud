package com.sohu.cache.dao;

import com.sohu.cache.task.entity.InstanceBigKey;
import com.sohu.cache.web.util.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface InstanceBigKeyDao {

	/**
	 * 批量保存bigkey
	 * @param instanceBigKeyList
	 * @return
	 */
    int batchSave(@Param("instanceBigKeyList") List<InstanceBigKey> instanceBigKeyList);

    /**
     * @param appId
     * @param auditId
     * @return
     */
    int getAppBigKeyCount(@Param("appId") long appId, @Param("auditId") long auditId);

    /**
     * @param appId
     * @param auditId
     * @param page
     * @return
     */
	List<InstanceBigKey> getAppBigKeyList(@Param("appId") long appId, @Param("auditId") long auditId,
            @Param("page") Page page);
    
    
}
