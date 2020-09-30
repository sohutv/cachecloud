package com.sohu.tv.cc.client.spectator.util;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Map;

/**
 * from org.apache.commons.collections4.MapUtils
 * @author leifu
 * @Date 2017年4月17日
 * @Time 下午3:05:09
 */
public class MapUtils {
    
    /**
     * Gets a long from a Map in a null-safe manner.
     * <p>
     * The long is obtained from the results of {@link #getNumber(Map,Object)}.
     *
     * @param <K>  the key type
     * @param map  the map to use
     * @param key  the key to look up
     * @return the value in the Map as a long, <code>0L</code> if null map input
     */
    public static <K> long getLongValue(final Map<? super K, ?> map, final K key) {
        final Long longObject = getLong(map, key);
        if (longObject == null) {
            return 0L;
        }
        return longObject.longValue();
    }
    
    /**
     * Gets a Long from a Map in a null-safe manner.
     * <p>
     * The Long is obtained from the results of {@link #getNumber(Map,Object)}.
     *
     * @param <K>  the key type
     * @param map  the map to use
     * @param key  the key to look up
     * @return the value in the Map as a Long, <code>null</code> if null map input
     */
    public static <K> Long getLong(final Map<? super K, ?> map, final K key) {
        final Number answer = getNumber(map, key);
        if (answer == null) {
            return null;
        }
        if (answer instanceof Long) {
            return (Long) answer;
        }
        return Long.valueOf(answer.longValue());
    }
    
    /**
     * Gets a double from a Map in a null-safe manner.
     * <p>
     * The double is obtained from the results of {@link #getNumber(Map,Object)}.
     *
     * @param <K>  the key type
     * @param map  the map to use
     * @param key  the key to look up
     * @return the value in the Map as a double, <code>0.0</code> if null map input
     */
    public static <K> double getDoubleValue(final Map<? super K, ?> map, final K key) {
        final Double doubleObject = getDouble(map, key);
        if (doubleObject == null) {
            return 0d;
        }
        return doubleObject.doubleValue();
    }
    
    /**
     * Gets a Double from a Map in a null-safe manner.
     * <p>
     * The Double is obtained from the results of {@link #getNumber(Map,Object)}.
     *
     * @param <K>  the key type
     * @param map  the map to use
     * @param key  the key to look up
     * @return the value in the Map as a Double, <code>null</code> if null map input
     */
    public static <K> Double getDouble(final Map<? super K, ?> map, final K key) {
        final Number answer = getNumber(map, key);
        if (answer == null) {
            return null;
        }
        if (answer instanceof Double) {
            return (Double) answer;
        }
        return Double.valueOf(answer.doubleValue());
    }
    
    /**
     * Gets a Number from a Map in a null-safe manner.
     * <p>
     * If the value is a <code>Number</code> it is returned directly.
     * If the value is a <code>String</code> it is converted using
     * {@link NumberFormat#parse(String)} on the system default formatter
     * returning <code>null</code> if the conversion fails.
     * Otherwise, <code>null</code> is returned.
     *
     * @param <K>  the key type
     * @param map  the map to use
     * @param key  the key to look up
     * @return the value in the Map as a Number, <code>null</code> if null map input
     */
    public static <K> Number getNumber(final Map<? super K, ?> map, final K key) {
        if (map != null) {
            final Object answer = map.get(key);
            if (answer != null) {
                if (answer instanceof Number) {
                    return (Number) answer;
                }
                if (answer instanceof String) {
                    try {
                        final String text = (String) answer;
                        return NumberFormat.getInstance().parse(text);
                    } catch (final ParseException e) { // NOPMD
                        // failure means null is returned
                    }
                }
            }
        }
        return null;
    }

}
