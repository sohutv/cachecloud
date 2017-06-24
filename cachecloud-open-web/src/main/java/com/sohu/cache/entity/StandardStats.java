package com.sohu.cache.entity;

import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 实例基准统计
 * Created by zhangyijun on 15/6/17.
 */
public class StandardStats {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * id
     */
    private long id;

    /**
     * 实例IP
     */
    private String ip;

    /**
     * 实例端口号/hostId
     */
    private int port;

    /**
     * 实例类型
     */
    private String dbType;

    /**
     * 收集时间:格式yyyyMMddHHmm
     */
    private long collectTime;

    /**
     * 实例收集的json数据
     */
    private String infoJson;

    /**
     * 与上一次收集差异的json数据
     */
    private String diffJson;
    
    /**
     * 实例收集的cluster info json数据
     */
    private String clusterInfoJson;

    /**
     * infoJson的Map输出
     */
    private Map<String, Object> infoMap;

    /**
     * diffJson的Map输出
     */
    private Map<String, Object> diffMap;
    
    /**
     * clusterInfoJson的Map输出
     */
    private Map<String, Object> clusterInfoMap;


    private Date createdTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public long getCollectTime() {
        return collectTime;
    }

    public void setCollectTime(long collectTime) {
        this.collectTime = collectTime;
    }

    public String getInfoJson() {
        return infoJson;
    }

    public void setInfoJson(String infoJson) {
        this.infoJson = infoJson;
    }

    public String getDiffJson() {
        return diffJson;
    }

    public void setDiffJson(String diffJson) {
        this.diffJson = diffJson;
    }

    public StandardStats(String diffJson) {
        this.diffJson = diffJson;
    }

    public StandardStats() {
    }

    public Map<String, Object> getInfoMap() {
        if (infoMap != null) {
            return infoMap;
        } else {
            if (StringUtils.isNotBlank(infoJson)) {
                JSONObject jsonObject;
                try {
                    jsonObject = JSONObject.fromObject(infoJson);
                    Map<String, Object> map = transferMapByJson(jsonObject);
                    infoMap = map;
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        }
        return infoMap;
    }

    public void setInfoMap(Map<String, Object> infoMap) {
        if (infoJson == null) {
            JSONObject jsonObject;
            try {
                jsonObject = JSONObject.fromObject(infoMap);
                infoJson = jsonObject.toString();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        this.infoMap = infoMap;
    }

    public Map<String, Object> getDiffMap() {
        if (diffMap != null) {
            return diffMap;
        } else {
            if (StringUtils.isNotBlank(diffJson)) {
                JSONObject jsonObject;
                try {
                    jsonObject = JSONObject.fromObject(diffJson);
                    Map<String, Object> map = transferMapByJson(jsonObject);
                    diffMap = map;
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        }
        return diffMap;
    }

    /**
     * 递归转换JsonObject
     * @param jsonObject
     * @return
     */
    private Map<String, Object> transferMapByJson(JSONObject jsonObject) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        for (Iterator keys = jsonObject.keys(); keys.hasNext(); ) {
            String key = String.valueOf(keys.next());
            Object value = jsonObject.get(key);
            if(value instanceof JSONObject){
                JSONObject subJsonObject = (JSONObject) value;
                Map<String, Object> subMap = transferMapByJson(subJsonObject);
                map.put(key,subMap);
            }else{
                map.put(key, value);
            }
        }
        return map;
    }

    public void setDiffMap(Map<String, Object> diffMap) {
        if (diffJson == null) {
            JSONObject jsonObject;
            try {
                jsonObject = JSONObject.fromObject(diffMap);
                diffJson = jsonObject.toString();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        this.diffMap = diffMap;
    }

    public String getClusterInfoJson() {
        return clusterInfoJson;
    }

    public void setClusterInfoJson(String clusterInfoJson) {
        this.clusterInfoJson = clusterInfoJson;
    }

    public Map<String, Object> getClusterInfoMap() {
        if (clusterInfoMap != null) {
            return clusterInfoMap;
        } else {
            if (StringUtils.isNotBlank(clusterInfoJson)) {
                JSONObject jsonObject;
                try {
                    jsonObject = JSONObject.fromObject(clusterInfoJson);
                    Map<String, Object> map = transferMapByJson(jsonObject);
                    clusterInfoMap = map;
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        }
        return clusterInfoMap;
    }

    public void setClusterInfoMap(Map<String, Object> clusterInfoMap) {
        if (clusterInfoJson == null) {
            JSONObject jsonObject;
            try {
                jsonObject = JSONObject.fromObject(clusterInfoMap);
                clusterInfoJson = jsonObject.toString();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        this.clusterInfoMap = clusterInfoMap;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }
}
