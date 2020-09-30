package com.sohu.tv.cachecloud.client.bloom.impl;

import com.sohu.tv.cachecloud.client.bloom.BloomFilter;
import com.sohu.tv.cachecloud.client.bloom.builder.BloomFilterBuilder;
import com.sohu.tv.cachecloud.client.bloom.hash.HashFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.PipelineCluster;
import redis.clients.jedis.exceptions.JedisRedirectionException;
import redis.clients.jedis.util.JedisClusterCRC16;

import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;

/**
 * CacheCloud布隆过滤器
 * 
 * @author leifu
 */
public class CacheCloudBloomFilter<T> implements BloomFilter<T> {
    
    private Logger logger = LoggerFactory.getLogger(CacheCloudBloomFilter.class);

    private BloomFilterBuilder config;
    
    public CacheCloudBloomFilter(BloomFilterBuilder bloomFilterBuilder) {
        this.config = bloomFilterBuilder;
    }

    @Override
    public boolean add(T object) {
        if (object == null) {
            return false;
        }
        // 偏移量列表
        List<Integer> offsetList = hash(object);
        if (offsetList == null || offsetList.isEmpty()) {
            return false;
        }
        String key = genBloomFilterDistributeKey(object);
        return pipelineSetBit(key, new HashSet<Integer>(offsetList));
    }

    @Override
    public boolean batchAdd(List<T> objectList) {
        if (objectList == null || objectList.isEmpty()) {
            return false;
        }
        Map<String, Set<Integer>> keyOffsetSetMap = new HashMap<String, Set<Integer>>();
        for (T object : objectList) {
            // 偏移量列表
            List<Integer> offsetList = hash(object);
            if (offsetList == null || offsetList.isEmpty()) {
                continue;
            }
            String key = genBloomFilterDistributeKey(object);
            if (keyOffsetSetMap.containsKey(key)) {
                keyOffsetSetMap.get(key).addAll(offsetList);
            } else {
                Set<Integer> offsetSet = new HashSet<Integer>();
                offsetSet.addAll(offsetList);
                keyOffsetSetMap.put(key, offsetSet);
            }
        }
        for (Entry<String, Set<Integer>> entry : keyOffsetSetMap.entrySet()) {
            String key = entry.getKey();
            Set<Integer> offsetSet = entry.getValue();
            pipelineSetBit(key, offsetSet);
        }
        return true;
    }

    @Override
    public boolean contains(T object) {
        if (object == null) {
            return false;
        }
        // 偏移量列表
        List<Integer> offsetList = hash(object);
        if (offsetList == null || offsetList.isEmpty()) {
            return false;
        }
        String key = genBloomFilterDistributeKey(object);
        // 获取位图值，只要有非true的就证明不包含
        Map<Integer, Boolean> offsetResultMap = pipelineGetBit(key, offsetList);
        for (Boolean bit : offsetResultMap.values()) {
            if (bit == null || !bit) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Map<T, Boolean> batchContains(List<T> objectList) {
        if (objectList == null || objectList.isEmpty()) {
            return Collections.emptyMap();
        }
        // 最终结果
        Map<T, Boolean> resultMap = new HashMap<T, Boolean>();

        // 按照object和offsetList做分组
        Map<T, List<Integer>> objectOffsetListMap = new HashMap<T, List<Integer>>();
        Map<String, List<Integer>> keyOffsetSetMap = new HashMap<String, List<Integer>>();
        
        // 分组
        for (T object : objectList) {
            List<Integer> offsetList = hash(object);
            if (offsetList == null || offsetList.isEmpty()) {
                continue;
            }
            String key = genBloomFilterDistributeKey(object);
            if (keyOffsetSetMap.containsKey(key)) {
                keyOffsetSetMap.get(key).addAll(offsetList);
            } else {
                List<Integer> offsetListTemp = new ArrayList<Integer>();
                offsetListTemp.addAll(offsetList);
                keyOffsetSetMap.put(key, offsetListTemp);
            }
            objectOffsetListMap.put(object, offsetList);
        }
        
        Map<Integer, Boolean> totalOffsetResultMap = new HashMap<Integer, Boolean>();
        for (Entry<String, List<Integer>> entry : keyOffsetSetMap.entrySet()) {
            String key = entry.getKey();
            List<Integer> offsetList = entry.getValue();
            Map<Integer, Boolean> offsetResultMap = pipelineGetBit(key, offsetList);
            totalOffsetResultMap.putAll(offsetResultMap);
        }
        
        for (Entry<T, List<Integer>> entry : objectOffsetListMap.entrySet()) {
            T object = entry.getKey();
            List<Integer> offsetList = entry.getValue();
            Boolean result = true;
            for (Integer offset : offsetList) {
                Boolean t = totalOffsetResultMap.get(offset);
                if (t == null || !t) {
                    result = false;
                    break;
                }
            }
            resultMap.put(object, result);
        }
        return resultMap;
    }

    /**
     * pipeline setbit
     */
    private boolean pipelineSetBit(String key, Set<Integer> offsetSet) {
        int slot = JedisClusterCRC16.getSlot(key);
        JedisPool jedisPool = getPipelineCluster().getConnectionHandler().getJedisPoolFromSlot(slot);
        Jedis jedis = null;
        Pipeline pipeline = null;
        try {
            jedis = jedisPool.getResource();
            pipeline = jedis.pipelined();
            for (int offset : offsetSet) {
                pipeline.setbit(key, offset, true);
            }
            pipeline.sync();
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        } finally {
            if (pipeline != null)
                pipeline.clear();
            if (jedis != null)
                jedis.close();
        }
    }

    /**
     * pipeline get
     */
    private Map<Integer, Boolean> pipelineGetBit(String key, List<Integer> offsetList) {
        Map<Integer, Boolean> offsetResultMap = new HashMap<Integer, Boolean>();
        int slot = JedisClusterCRC16.getSlot(key);
        JedisPool jedisPool = getPipelineCluster().getConnectionHandler().getJedisPoolFromSlot(slot);
        Jedis jedis = null;
        Pipeline pipeline = null;
        try {
            jedis = jedisPool.getResource();
            pipeline = jedis.pipelined();
            for (int offset : offsetList) {
                pipeline.getbit(key, offset);
            }
            List<Object> objectList = null;
            try {
                objectList = pipeline.syncAndReturnAll();
            } catch (JedisRedirectionException e) {
                // ignore
            }
            int i = 0;
            if(objectList != null){
                for (Object object : objectList) {
                    offsetResultMap.put(offsetList.get(i), (Boolean) object);
                    i++;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (pipeline != null)
                pipeline.clear();
            if (jedis != null)
                jedis.close();
        }
        return offsetResultMap;
    }
    
    /**
     * 生成子布隆过滤器对应的key，使用crc16作为分组
     * @param object
     */
    private String genBloomFilterDistributeKey(T object) {
        int hashcode = JedisClusterCRC16.getCRC16(object.toString());
        int segement = hashcode % getChildBloomNumber();
        return getBloomFilterKey(segement);
    }
    
    public BloomFilterBuilder getConfig() {
        return config;
    }

    @Override
    public long getExpectedInsertions() {
        return getConfig().getExpectedInsertions();
    }

    @Override
    public double getFalseProbability() {
        return getConfig().getFalseProbability();
    }

    @Override
    public long getSize() {
        return getConfig().getTotalSize();
    }

    @Override
    public int getHashIterations() {
        return getConfig().getHashIterations();
    }

    public String getName() {
        return getConfig().getName();
    }

    public PipelineCluster getPipelineCluster() {
        return getConfig().getPipelineCluster();
    }
    
    public HashFunction getHashFunction() {
        return getConfig().getHashFunction();
    }
    
    @Override
    public int getChildBloomNumber() {
        return getConfig().getChildBloomNumber();
    }

    public List<Integer> hash(Object object) {
        byte[] bytes = object.toString().getBytes(Charset.forName("UTF-8"));
        return getHashFunction().hash(bytes, getConfig().getChildBloomMaxSize(), getConfig().getHashIterations());
    }

    /**
     * 获取布隆过滤器key
     * @param index
     * @return
     */
    private String getBloomFilterKey(int index) {
        return getName() + ":" + index;
    }
    
    @Override
    public void clear() {
        // 删除所有位图
        List<String> keys = new ArrayList<String>();
        for (int i = 0; i <= getChildBloomNumber(); i++) {
            keys.add(getBloomFilterKey(i));
        }
        getPipelineCluster().mdel(keys);
        // 删除配置
        String configKey = getConfig().getBloomFilterConfigKey();
        getPipelineCluster().del(configKey);
    }

}
