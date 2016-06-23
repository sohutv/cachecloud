package com.sohu.test.redis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import com.sohu.cache.redis.impl.RedisDeployCenterImpl;
import com.sohu.test.BaseTest;

/**
 * @author leifu
 * @Date 2016年6月22日
 * @Time 下午6:26:28
 */
public class RedisDeployCenterImplTest extends BaseTest {

    @Resource
    private RedisDeployCenterImpl redisDeployCenter;

    @Test
    public void testSentinelConfig() {
        String masterName = "mymaster";
        String host = "127.0.0.1";
        int port = 6379;
        int sentinelPort = 26379;

        List<String> sentinelList1 = redisDeployCenter.handleSentinelConfig(masterName, host, port, sentinelPort);
        List<String> sentinelList2 = redisDeployCenter.handleSentinelConfig2(masterName, host, port, sentinelPort);

        Collections.sort(sentinelList1);
        Collections.sort(sentinelList2);

        for (int i = 0; i < sentinelList1.size() && i < sentinelList2.size(); i++) {
            System.out.println(sentinelList1.get(i) + "<-->" + sentinelList2.get(i));
            System.out.println(sentinelList1.get(i).equals(sentinelList2.get(i)));
        }
    }

    @Test
    public void testCommonConfig() {
        int port = 6379;
        int maxMemory = 2048;

        List<String> commonList1 = redisDeployCenter.handleCommonConfig(port, maxMemory);
        List<String> commonList2 = redisDeployCenter.handleCommonConfig2(port, maxMemory);

        Collections.sort(commonList1);
        Collections.sort(commonList2);

        List<String> remains = new ArrayList<String>();
        for (String config : commonList2) {
            if (commonList1.contains(config)) {
                System.out.println(config);
            } else {
                remains.add(config);
            }
        }

        System.out.println("===============remain====================");
        for (int i = 0; i < remains.size(); i++) {
            System.out.println(remains.get(i));
        }
    }

    @Test
    public void testClusterConfig() {
        int port = 6379;

        List<String> clusterList1 = redisDeployCenter.handleClusterConfig(port);
        List<String> clusterList2 = redisDeployCenter.handleClusterConfig2(port);

        Collections.sort(clusterList1);
        Collections.sort(clusterList2);

        for (int i = 0; i < clusterList1.size() && i < clusterList2.size(); i++) {
            System.out.println(clusterList1.get(i) + "<-->" + clusterList2.get(i));
            System.out.println(clusterList1.get(i).equals(clusterList2.get(i)));
        }
    }

}
