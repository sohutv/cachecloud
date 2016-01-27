package com.sohu.test.util;

import com.sohu.cache.util.ScheduleUtil;
import com.sohu.test.SimpleBaseTest;

import org.junit.Test;

import java.util.Date;

/**
 * User: lingguo
 * Date: 14-6-30
 */
public class ScheduleUtilTest extends SimpleBaseTest {

    @Test
    public void testGtBeginTimeOfDay() {
        long todayBegin = ScheduleUtil.getBeginTimeOfDay(new Date(), 0);
        long tomorrowBegin = ScheduleUtil.getBeginTimeOfDay(new Date(), 1);
        logger.info("today: {}, tomorrow: {}", todayBegin, tomorrowBegin);
    }


    @Test
    public void testGetHourCronByHostId() {
        String cron1 = ScheduleUtil.getHourCronByHostId(24);
        String cron3 = ScheduleUtil.getHourCronByHostId(25);
//        try {
//            TimeUnit.SECONDS.sleep(5);
//        } catch (InterruptedException e) {
//            logger.error(e.getMessage(), e);
//        }
        String cron2 = ScheduleUtil.getHourCronByHostId(24);
        String cron4 = ScheduleUtil.getHourCronByHostId(25);

        logger.info("cron1: {}, cron2: {}, cron3: {}, cron4: {}", cron1, cron2, cron3, cron4);
        logger.info("time: {}", new Date());

    }

}
