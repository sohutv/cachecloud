package com.sohu.test.app;

import java.util.List;

import com.sohu.cache.entity.AppCommandGroup;
import com.sohu.cache.entity.AppCommandStats;
import com.sohu.cache.stats.app.AppStatsCenter;
import com.sohu.cache.web.vo.AppDetailVO;
import com.sohu.test.BaseTest;

import org.junit.Test;

import javax.annotation.Resource;

/**
 * Created by yijunzhang on 14-8-29.
 */
public class AppStatsCenterImplTest extends BaseTest {

    @Resource
    private AppStatsCenter appStatsCenter;

    @Test
    public void getAppDetail() {
        long appId = 10015L;
        long begin = System.currentTimeMillis();
        AppDetailVO resultVO = appStatsCenter.getAppDetail(appId);
        logger.info("cost=" + (System.currentTimeMillis() - begin));
        logger.info("result={}", resultVO);
    }
    
    @Test
    public void getAppCommandGroup(){
    	long appId = 10016L;
    	long beginTime = 201408260001L;
    	long endTime = 201408261601L;
    	List<AppCommandGroup> list = appStatsCenter.getAppCommandGroup(appId, beginTime, endTime);
        logger.info("list={}", list);
    }
    
    @Test
    public void getTop5AppCommandStatsList(){
    	long appId = 10130L;
    	long beginTime = 201411060000L;
    	long endTime = 201411070000L;
    	List<AppCommandStats> list = appStatsCenter.getTop5AppCommandStatsList(appId, beginTime, endTime);
        logger.info("list={}", list);
    }

    @Test
    public void getCommandStatsListAll(){
        long appId = 999L;
        long beginTime = 201409100001L;
        long endTime = 201409101601L;
        List<AppCommandStats> list = appStatsCenter.getCommandStatsList(appId, beginTime, endTime);
        logger.info("list={}", list);
    }

    @Test
    public void executeCommandRedisTest() {
        long appId = 998L;
        String command = "info all";
        String value = appStatsCenter.executeCommand(appId, command);
        logger.info("value={}", value);
    }

}
