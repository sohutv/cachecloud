package com.sohu.test.instance;

import com.sohu.cache.entity.InstanceCommandStats;
import com.sohu.cache.entity.InstanceStats;
import com.sohu.cache.stats.instance.InstanceStatsCenter;
import com.sohu.test.BaseTest;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by yijunzhang on 14-9-18.
 */
public class InstanceStatsCenterTest extends BaseTest {

    @Resource
    private InstanceStatsCenter instanceStatsCenter;

    @Test
    public void getCommandStatsList() {
        watch.start("getCommandStatsList");
        List<InstanceCommandStats> list = instanceStatsCenter.getCommandStatsList(523L,201506170000L,201506172359L,"get");
        watch.stop();
        System.out.println(watch.prettyPrint());
//        for (InstanceCommandStats stats : list) {
//            System.out.println(stats);
//        }
    }

    @Test
    public void getInstanceStats() {
        InstanceStats instanceStats = instanceStatsCenter.getInstanceStats(51L);
        System.out.println(instanceStats);
        System.out.println(instanceStats.getInfoMap());
        System.out.println(instanceStats.isRun());

        instanceStats = instanceStatsCenter.getInstanceStats(139L);
        System.out.println(instanceStats);
        System.out.println(instanceStats.getInfoMap());
        System.out.println(instanceStats.isRun());
    }

}
