package com.sohu.tv.cc.client.spectator.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 简易DateUtils
 * @author leifu
 */
public class DateUtils {

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

    public static void main(String[] args) throws InterruptedException {
       long start = System.currentTimeMillis();
       long nextMin = start + 1 * 1000;
       long count = 0L;
       List<String> list = new ArrayList<>(10);
       StringBuilder sb = new StringBuilder(128);
       while (System.currentTimeMillis() < nextMin){
           count++;
           list.add("hello");
           sb.append("hello").append(",");
       }
       System.out.println(count);
       System.out.println(list.size());
       System.out.println(sb.length());
    }

}
