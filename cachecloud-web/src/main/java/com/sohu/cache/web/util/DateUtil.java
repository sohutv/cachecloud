package com.sohu.cache.web.util;

import com.sohu.cache.entity.TimeBetween;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author leifu
 * @Time 2014年8月31日
 */
public class DateUtil {
    private final static Logger logger = LoggerFactory.getLogger(DateUtil.class);
    private final static String COLLECT_TIME_FORMAT = "yyyyMMddHHmmss";

    /*
     * yyyyMMddHHmm格式format
     */
    public static String formatYYYYMMddHHMM(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        return sdf.format(date);
    }

    /*
     * yyyyMMddHHmmss格式format
     */
    public static String formatYYYYMMddHHMMss(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(date);
    }

    /*
     * yyyy-MM-dd HH:mm:ss格式format
     */
    public static String formatYYYYMMddHHMMSS(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    /*
     * yyyyMMddHHmm格式parse
     */
    public static Date parse(String dateStr, String format) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.parse(dateStr);
    }

    /*
     * yyyyMMddHHmm格式parse
     */
    public static Date parseYYYYMMddHHMM(String dateStr) throws ParseException {
        return parse(dateStr, "yyyyMMddHHmm");
    }

    /**
     * yyyyMMddHH格式parse
     *
     * @throws ParseException
     */
    public static Date parseYYYYMMddHH(String dateStr) throws ParseException {
        return parse(dateStr, "yyyyMMddHH");
    }


    /*
     * yyyy-MM-dd格式parse
     */
    public static Date parseYYYY_MM_dd(String dateStr) throws ParseException {
        return parse(dateStr, "yyyy-MM-dd");
    }

    /**
     * yyyyMMdd格式parse
     */
    public static Date parseYYYYMMdd(String dateStr) throws ParseException {
        return parse(dateStr, "yyyyMMdd");
    }


    public static Date getDateByFormat(String date, String format) {
        SimpleDateFormat sf = new SimpleDateFormat(format);
        Date result = null;
        try {
            result = sf.parse(date);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    public static String formatDate(Date date, String format) {
        SimpleDateFormat sf = new SimpleDateFormat(format);
        return sf.format(date);
    }


    public static String formatYYYYMMdd(Date date) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
        return sf.format(date);
    }

    public static String formatYYYY_MM_dd(Date date) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        return sf.format(date);
    }

    public static String formatHHMM(Date date) {
        SimpleDateFormat sf = new SimpleDateFormat("HHmm");
        return sf.format(date);
    }

    public static TimeBetween fillWithDateFormat(String searchDate) throws ParseException {
        final String dateFormat = "yyyy-MM-dd";
        Date startDate;
        Date endDate;
        if (StringUtils.isBlank(searchDate)) {
            // 如果为空默认取今天
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            startDate = sdf.parse(sdf.format(new Date()));
        } else {
            startDate = com.sohu.cache.web.util.DateUtil.parse(searchDate, dateFormat);
        }
        endDate = DateUtils.addDays(startDate, 1);
        // 查询后台需要
        long startTime = NumberUtils.toLong(com.sohu.cache.web.util.DateUtil.formatDate(startDate, COLLECT_TIME_FORMAT));
        long endTime = NumberUtils.toLong(com.sohu.cache.web.util.DateUtil.formatDate(endDate, COLLECT_TIME_FORMAT));
        return new TimeBetween(startTime, endTime, startDate, endDate);
    }

    public static TimeBetween fillWithMinDateFormat(String searchDate, String dateFormat) throws ParseException {
        Date startDate;
        Date endDate;
        if (StringUtils.isBlank(searchDate)) {
            // 如果为空默认取今天
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            startDate = sdf.parse(sdf.format(new Date()));
        } else {
            startDate = com.sohu.cache.web.util.DateUtil.parse(searchDate, dateFormat);
        }
        endDate = DateUtils.addMinutes(startDate, 1);
        // 查询后台需要
        long startTime = NumberUtils.toLong(com.sohu.cache.web.util.DateUtil.formatDate(startDate, COLLECT_TIME_FORMAT));
        long endTime = NumberUtils.toLong(com.sohu.cache.web.util.DateUtil.formatDate(endDate, COLLECT_TIME_FORMAT));
        return new TimeBetween(startTime, endTime, startDate, endDate);
    }

    public static Date customDateByHHMM(int hh, int mm){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hh);
        calendar.set(Calendar.MINUTE, mm);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    public static boolean timeLessDiffAfter(Date date1, Date date2, int diffSecond){
        return (date2.getTime()/1000 > date1.getTime()/1000) && (date2.getTime() - date1.getTime()) / 1000 <= diffSecond;
    }

    public static boolean timeMoreDiffAfter(Date date1, Date date2, int diffSecond){
        return (date2.getTime() - date1.getTime()) / 1000 >= diffSecond;
    }

    public static Date getBeginOfDay(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date getBeginOfTomorrow(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static String getTimeBeforePointedMinute(Date date, int mm){
        Date newDate = DateUtils.addMinutes(date, 0 - mm);
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmm00");
        return sf.format(newDate);
    }

}
