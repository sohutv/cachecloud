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
    public void testGetStandardStatsList(){
        long appId1 = 10004;
        watch.start("memcached");
        Map<Integer, Map<String, List<InstanceCommandStats>>> map = instanceStatsCenter.getStandardStatsList(appId1, 201506170000L,
                201506172359L, Arrays.asList("get_hits"));
        watch.stop();
        System.out.println(map.size());
        long appId2 = 10129;
        watch.start("redis-cluster");
        map = instanceStatsCenter.getStandardStatsList(appId2, 201506170000L,
                201506172359L, Arrays.asList("total_net_input_bytes","total_net_output_bytes"));
        watch.stop();
        System.out.println(watch.prettyPrint());
        System.out.println(map.size());
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
