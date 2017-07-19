package com.sohu.tv.cachecloud.client.basic.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

/**
 * jackson Json转换工具
 * 
 * @author leifu
 * @Date 2016年1月21日
 * @Time 上午10:55:30
 */
public class JsonUtil {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    public static String toJson(Object entity) {
        if (entity == null) {
            return null;
        }
        return JSONObject.toJSONString(entity);
    }
}
