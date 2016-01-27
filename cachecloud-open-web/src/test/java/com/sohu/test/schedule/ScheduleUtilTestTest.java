package com.sohu.test.schedule;

import com.sohu.cache.util.ScheduleUtil;
import com.sohu.test.SimpleBaseTest;

import org.junit.Test;

/**
 * User: lingguo
 * Date: 14-6-12
 * Time: 上午11:08
 */
public class ScheduleUtilTestTest extends SimpleBaseTest {

    @Test
    public void testHourCron() {
        long hostId = 34L;
        logger.info("{}", ScheduleUtil.getHourCronByHostId(hostId));
    }
}
