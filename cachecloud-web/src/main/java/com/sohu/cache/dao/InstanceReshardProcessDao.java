package com.sohu.cache.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.sohu.cache.entity.InstanceReshardProcess;


/**
 * 实例Reshard进度保存
 * @author leifu
 * @Date 2017年6月24日
 * @Time 下午7:17:36
 */
public interface InstanceReshardProcessDao {

    int save(InstanceReshardProcess instanceReshardProcess);
    
    List<InstanceReshardProcess> getByAuditId(@Param("auditId") long auditId);
    
    int updateStatus(@Param("id") long id, @Param("status") int status);
    
    int updateEndTime(@Param("id") long id, @Param("endTime") Date endTime);
    
    int increaseFinishSlotNum(@Param("id") long id);

    int updateMigratingSlot(@Param("id") int id, @Param("migratingSlot") int migratingSlot);

    InstanceReshardProcess get(@Param("id") int id);


}
