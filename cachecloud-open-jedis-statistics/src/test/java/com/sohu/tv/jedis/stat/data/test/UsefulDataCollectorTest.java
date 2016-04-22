package com.sohu.tv.jedis.stat.data.test;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.tv.jedis.stat.data.UsefulDataCollector;
import com.sohu.tv.jedis.stat.model.CostTimeDetailStatModel;
import com.sohu.tv.jedis.stat.utils.AtomicLongMap;

/**
 * @author leifu
 * @Date 2015年1月23日
 * @Time 下午3:23:34
 */
public class UsefulDataCollectorTest {
    private final static Logger logger = LoggerFactory.getLogger(UsefulDataCollectorTest.class);

    @Test
    public void testGenerateCostTimeDetailStatKey() {
        AtomicLongMap<Integer> map = AtomicLongMap.create();
        map.addAndGet(5, 300);
        map.addAndGet(2, 100);
        map.addAndGet(1, 500);
        map.addAndGet(4, 300);
        map.addAndGet(10, 30);
        map.addAndGet(30, 2);

        CostTimeDetailStatModel model = UsefulDataCollector.generateCostTimeDetailStatKey(map);
        logger.info(model.toString());
    }

}
