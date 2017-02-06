package com.sohu.cache.dao;

import com.sohu.test.BaseTest;

import org.junit.Test;

import javax.annotation.Resource;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 机器测试
 * 
 * @author leifu
 * @Date 2016年3月17日
 * @Time 下午2:15:02
 */
public class InstanceDaoTest extends BaseTest {

    @Resource
    private InstanceDao instanceDao;

    @Test
    public void testGetMachineInstanceCountMap() throws Exception {
        System.out.println("================testGetMachineInstanceCountMap start================");
        List<Map<String, Object>> mapList = instanceDao.getMachineInstanceCountMap();
        for(Map<String, Object> map : mapList) {
            System.out.println(map.get("ip"));
            System.out.println(map.get("count"));
        }
        System.out.println("================testGetMachineInstanceCountMap start================");

    }

}
