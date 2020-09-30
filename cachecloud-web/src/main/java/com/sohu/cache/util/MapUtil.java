package com.sohu.cache.util;

import org.apache.commons.beanutils.BeanUtils;

import java.util.Map;

/**
 * Created by rucao on 2019/12/16
 */
public class MapUtil {

    public static Object mapToObject(Map<String, Object> map, Class<?> beanClass) throws Exception {
        if (map == null){
            return null;
        }
        Object obj = beanClass.newInstance();
        BeanUtils.populate(obj, map);
        return obj;
    }
}
