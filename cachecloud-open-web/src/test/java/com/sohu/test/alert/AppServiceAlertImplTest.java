package com.sohu.test.alert;

import javax.annotation.Resource;

import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.web.service.AppService;
import org.junit.Test;

import com.sohu.cache.alert.AppAlertService;
import com.sohu.test.BaseTest;
import org.springframework.util.StopWatch;

import java.util.List;

/**
 * app报警测试
 * 
 * @author leifu
 * @Date 2014年12月16日
 * @Time 下午2:58:47
 */
public class AppServiceAlertImplTest extends BaseTest {

    @Resource(name = "appAlertService")
    private AppAlertService appAlertService;

    @Resource
    private AppService appService;

    @Test
    public void testNotNull() {
        assertNotNull(appAlertService);
    }

    @Test
    public void getAppInstanceInfo() {
        watch.start("getAppInstanceInfo1");
        List<InstanceInfo> list = appService.getAppInstanceInfo(10129L);
        watch.stop();
        watch.start("getAppInstanceInfo2");
        list = appService.getAppInstanceInfo(10129L);
        watch.stop();
        logger.info(watch.prettyPrint());
        for (InstanceInfo info : list) {
            logger.warn("{}:{} -> {}:{} id={}", info.getIp(), info.getPort(), info.getMasterHost(), info.getMasterPort(),info.getMasterInstanceId());
        }
    }

}
