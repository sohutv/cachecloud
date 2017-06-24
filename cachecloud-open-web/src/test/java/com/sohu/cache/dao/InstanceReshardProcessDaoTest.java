package com.sohu.cache.dao;

import java.util.Date;

import javax.annotation.Resource;

import org.junit.Test;

import com.sohu.cache.entity.InstanceReshardProcess;
import com.sohu.test.BaseTest;

/**
 * @author leifu
 * @Date 2017年6月24日
 * @Time 下午8:44:27
 */
public class InstanceReshardProcessDaoTest extends BaseTest {

    @Resource(name = "instanceReshardProcessDao")
    private InstanceReshardProcessDao instanceReshardProcessDao;
    
    
    @Test
    public void testNotNull() {
        assertNotNull(instanceReshardProcessDao);
    }
    
    @Test
    public void testSave() {
        InstanceReshardProcess instanceReshardProcess = new InstanceReshardProcess();
        instanceReshardProcess.setAppId(10000);
        instanceReshardProcess.setAuditId(123);
        instanceReshardProcess.setCreateTime(new Date());
        instanceReshardProcess.setEndSlot(500);
        instanceReshardProcess.setEndTime(new Date());
        instanceReshardProcess.setFinishSlotNum(0);
        instanceReshardProcess.setMigratingSlot(400);
        instanceReshardProcess.setSourceInstanceId(1);
        instanceReshardProcess.setStartSlot(300);
        instanceReshardProcess.setStartTime(new Date());
        instanceReshardProcess.setStatus(1);
        instanceReshardProcess.setTargetInstanceId(5);
        instanceReshardProcess.setUpdateTime(new Date());
        
        instanceReshardProcessDao.save(instanceReshardProcess);
    }
    
    @Test
    public void testUpdateEndTime() {
        int id = 1;
        instanceReshardProcessDao.updateEndTime(id, new Date());
    }
    
    @Test
    public void testUpdateStatus() {
        int id = 1;
        int status = 2;
        instanceReshardProcessDao.updateStatus(id, status);
    }
    
    @Test
    public void testUpdateFinishSlotNum() {
        int id = 1;
        instanceReshardProcessDao.increaseFinishSlotNum(id);
    }
    
    @Test
    public void testUpdateMigratingSlot() {
        int id = 1;
        instanceReshardProcessDao.updateMigratingSlot(id, 30);
    }
    
}
