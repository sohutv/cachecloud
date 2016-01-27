package com.sohu.tv.jedis.stat.utils;

/**
 * 从commons-lang抄的
 * @author leifu
 * @Date 2015年1月13日
 * @Time 下午5:52:18
 */
public class NumberUtil {
    
    public static int toInt(String str, int defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    public static int toInt(String str) {
        return toInt(str, 0);
    }
    
    public static long toLong(String str, long defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }
    
    public static long toLong(String str) {
        return toLong(str, 0L);
    }
    
    public static double toDouble(final String str) {
        return toDouble(str, 0.0d);
    }
    
    public static double toDouble(final String str, final double defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(str);
        } catch (final NumberFormatException nfe) {
            return defaultValue;
        }
      }
}
