package com.sohu.test.util;

import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.util.ObjectConvert;
import com.sohu.test.BaseTest;

import org.junit.Test;

import java.util.List;
import javax.annotation.Resource;

/**
 * User: lingguo
 * Date: 14-6-19
 * Time: 下午2:35
 */
public class ObjectConvertTest extends BaseTest {

    @Resource
    InstanceDao instanceDao;

    @Test
    public void testAssembleInst() {
        long appId = 10000L;
        List<InstanceInfo> infoList = instanceDao.getInstListByAppId(appId);
        String shardInfo = ObjectConvert.assembleInstance(infoList);
        logger.warn("shards: {}", shardInfo);
    }

}
