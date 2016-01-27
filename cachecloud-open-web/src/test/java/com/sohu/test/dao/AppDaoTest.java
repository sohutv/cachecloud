package com.sohu.test.dao;

import java.util.List;

import com.sohu.cache.dao.AppDao;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.AppSearch;
import com.sohu.test.BaseTest;

import org.junit.Test;

import javax.annotation.Resource;

/**
 * User: lingguo
 * Date: 14-6-10
 * Time: 下午11:36
 */
public class AppDaoTest extends BaseTest{
    @Resource
    private AppDao appDao;

    @Test
    public void testAppDao() {
        long appId = 998L;
        AppDesc appDesc = appDao.getAppDescById(appId);
        logger.info("{}", appDesc.toString());
    }
    
    
    @Test
    public void testGetAllAppDescList(){
        AppSearch vo = new AppSearch();
        vo.setAppName("vrspoll");
        vo.setAppId(10011L);
        List<AppDesc> list = appDao.getAllAppDescList(vo);
        logger.info("list is {}", list);
    }
    
}
