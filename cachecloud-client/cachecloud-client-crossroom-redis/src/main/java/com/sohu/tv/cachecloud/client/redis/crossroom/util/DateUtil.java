package com.sohu.tv.cachecloud.client.redis.crossroom.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 简单dateutil
 * @author leifu
 * @Date 2016年9月22日
 * @Time 上午11:03:24
 */
public class DateUtil {

    public static SimpleDateFormat getCrossRoomStatusDateFormat() {
        return new SimpleDateFormat("yyyyMMddHHmm");
    }

    public static String formatDate(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    public static Date add(Date date, int calendarField, int amount) {
        if (date == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(calendarField, amount);
        return c.getTime();
    }

    public static Date addMinutes(Date date, int amount) {
        return add(date, Calendar.MINUTE, amount);
    }
    
}
