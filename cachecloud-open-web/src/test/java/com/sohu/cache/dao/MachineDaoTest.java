package com.sohu.cache.dao;

import com.sohu.cache.entity.MachineInfo;
import com.sohu.test.BaseTest;

import org.junit.Test;

import javax.annotation.Resource;

import java.util.Date;

/**
 * 机器测试
 * 
 * @author leifu
 * @Date 2016年3月17日
 * @Time 下午2:15:02
 */
public class MachineDaoTest extends BaseTest {

    @Resource
    private MachineDao machineDao;

    @Test
    public void testSaveMachine() throws Exception {

        MachineInfo machineInfo = new MachineInfo();
        machineInfo.setCpu(16);
        machineInfo.setIp("127.0.0.1");
        machineInfo.setMem(96);
        machineInfo.setModifyTime(new Date());
        machineInfo.setRealIp("");
        machineInfo.setRoom("北显");
        machineInfo.setServiceTime(new Date());
        machineInfo.setSshPasswd("cachecloud-open");
        machineInfo.setSshUser("cachecloud-open");

        machineDao.saveMachineInfo(machineInfo);
    }

}
