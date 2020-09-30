package com.sohu.cache.util;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Description: 总览统计
 * </p>
 * @author chenshi
 * @version 1.0
 * @date 2017/8/14
 * @param
 * @return
 */
public class PandectUtil {

    private static Logger logger = LoggerFactory.getLogger(PandectUtil.class);

    //最后一次从mysql获取map时间
    public final static String KEY_LASTTIME = "lastTime";

    private static Map<String,Object> PANDECT_MAP = new HashMap<String,Object>();

    /**
     * <p>
     * Description:1小时自动失效，重新从数据库获取
     * </p>
     * @author chenshi
     * @version 1.0
     * @date 2017/8/14
     * @param
     * @return true:从sql获取 false：从map获取
     */
    public static Boolean getFromMysql(){
        if(!MapUtils.isEmpty(PANDECT_MAP)){
            Long lastTime = MapUtils.getLong(PANDECT_MAP, KEY_LASTTIME, 0l);
            long expire = System.currentTimeMillis()-lastTime;
            logger.info("expire = {}ms",expire);
            if(expire >= 2*60*1000){
                return true;
            }
            return false;
        }
        return true;
    }

    // 清除map
    public static void clearMap(){
        PANDECT_MAP.clear();
    }

    public static Map<String, Object> getPandectMap() {
        return PANDECT_MAP;
    }

    public static void setPandectMap(Map<String, Object> pandectMap) {
        PANDECT_MAP = pandectMap;
    }
}
