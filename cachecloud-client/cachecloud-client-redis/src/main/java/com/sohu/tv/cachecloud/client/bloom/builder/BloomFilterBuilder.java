package com.sohu.tv.cachecloud.client.bloom.builder;

import com.sohu.tv.cachecloud.client.bloom.hash.HashFunction;
import com.sohu.tv.cachecloud.client.bloom.hash.MurMur3HashFunction;
import com.sohu.tv.cachecloud.client.bloom.impl.CacheCloudBloomFilter;
import com.sohu.tv.cc.client.spectator.util.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.PipelineCluster;

import java.util.HashMap;
import java.util.Map;

/**
 * 布隆过滤器构造器
 * @author leifu
 */
public class BloomFilterBuilder {
    private Logger logger = LoggerFactory.getLogger(BloomFilterBuilder.class);
    
    private static final long MAX_SIZE = Integer.MAX_VALUE * 100L;

    /**
     * 常量
     */
    private final static String EXPECTED_INSERTIONS = "expectedInsertions";
    private final static String FALSE_PROBABILITY = "falseProbability";

    /**
     * 需要PipelineCluster
     */
    private PipelineCluster pipelineCluster;

    /**
     * 布隆过滤器名(位图的key)
     */
    private String name;
    
    /**
     * 大位图总长度
     */
    private long totalSize;
    
    /**
     * 每个小位图(布隆过滤器)长度 10000000位/1024/1024/8=1.2MB
     */
    private int childBloomMaxSize = 10000000;

    /**
     * 布隆过滤器个数
     */
    private int childBloomNumber = 1;

    /**
     * hash函数循环次数
     */
    private int hashIterations;

    /**
     * 预期插入条数
     */
    private long expectedInsertions;

    /**
     * 预期错误概率
     */
    private double falseProbability;

    /**
     * hash函数:默认murmur3
     */
    private HashFunction hashFunction = new MurMur3HashFunction();
    
    /**
     * 是否重写已经存在的布隆过滤器
     */
    private boolean overwriteIfExists = false;
    
    /**
     * 是否完成
     */
    private boolean done = false;

    public BloomFilterBuilder(PipelineCluster pipelineCluster, String name, long expectedInsertions, double falseProbability) {
        this.pipelineCluster = pipelineCluster;
        this.name = name;
        this.expectedInsertions = expectedInsertions;
        this.falseProbability = falseProbability;
    }

    public BloomFilterBuilder setHashFunction(HashFunction hashFunction) {
        this.hashFunction = hashFunction;
        return this;
    }

    public BloomFilterBuilder setOverwriteIfExists(boolean overwriteIfExists) {
        this.overwriteIfExists = overwriteIfExists;
        return this;
    }

    public BloomFilterBuilder setChildBloomMaxSize(int childBloomMaxSize) {
        this.childBloomMaxSize = childBloomMaxSize;
        return this;
    }

    public PipelineCluster getPipelineCluster() {
        return pipelineCluster;
    }

    public String getName() {
        return name;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public int getHashIterations() {
        return hashIterations;
    }

    public long getExpectedInsertions() {
        return expectedInsertions;
    }

    public double getFalseProbability() {
        return falseProbability;
    }

    public HashFunction getHashFunction() {
        return hashFunction;
    }

    public boolean isOverwriteIfExists() {
        return overwriteIfExists;
    }
    
    public <T> CacheCloudBloomFilter<T> build() {
        checkBloomFilterParam();
        return new CacheCloudBloomFilter<T>(this);
    }

    /**
     * 检查布隆过滤器参数
     */
    private void checkBloomFilterParam() {
        if (done) {
            return;
        }
        if (name == null || "".equals(name.trim())) {
            throw new IllegalArgumentException("Bloom filter name is empty");
        }
        if (expectedInsertions < 0 || expectedInsertions > MAX_SIZE) {
            throw new IllegalArgumentException("Bloom filter expectedInsertions can't be greater than " + MAX_SIZE + " or smaller than 0");
        }
        if (falseProbability > 1) {
            throw new IllegalArgumentException("Bloom filter false probability can't be greater than 1");
        }
        if (falseProbability < 0) {
            throw new IllegalArgumentException("Bloom filter false probability can't be negative");
        }
        // 检查布隆过滤器是否已经存在
        boolean isExist = checkBloomFilterExist();
        if (isExist && !overwriteIfExists) {
            // 读取配置
            Map<String, String> configMap = readConfig();
            expectedInsertions = MapUtils.getLongValue(configMap, EXPECTED_INSERTIONS);
            falseProbability = MapUtils.getDoubleValue(configMap, FALSE_PROBABILITY);
        } else {
            // 设置配置
            setConfig();
        }
        // 计算布隆过滤器(位图)长度
        totalSize = optimalNumOfBits();
        logger.info("{} optimalNumOfBits is {}", name, totalSize);
        if (totalSize == 0) {
            throw new IllegalArgumentException("Bloom filter calculated totalSize is " + totalSize);
        }
        if (totalSize > MAX_SIZE) {
            throw new IllegalArgumentException("Bloom filter totalSize can't be greater than " + MAX_SIZE + ". But calculated totalSize is " + totalSize);
        }
        // hash函数迭代次数
        hashIterations = optimalNumOfHashFunctions();
        logger.info("{} hashIterations is {}", name, hashIterations);
        
        // 计算布隆过滤器个数
        childBloomNumber = (int) (totalSize / childBloomMaxSize + 1);
        logger.info("{} bloomNumber is {}", name, childBloomNumber);
        
        done = true;
    }
    
    /**
     * 从Redis中读取布隆过滤器配置
     */
    private Map<String, String> readConfig() {
        return pipelineCluster.hgetAll(getBloomFilterConfigKey());
    }

    /**
     * 向Redis中设置布隆过滤器配置
     */
    private void setConfig() {
        Map<String, String> configMap = new HashMap<String, String>();
        configMap.put(EXPECTED_INSERTIONS, String.valueOf(expectedInsertions));
        configMap.put(FALSE_PROBABILITY, String.valueOf(falseProbability));
        pipelineCluster.hmset(getBloomFilterConfigKey(), configMap);
    }

    /**
     * 布隆过滤器配置key
     */
    public String getBloomFilterConfigKey() {
        return name + ":bloom:cc:config";
    }
    
    /**
     * 根据预期插入条数和概率计算布隆过滤器(位图)长度
     */
    private long optimalNumOfBits() {
        if (falseProbability == 0) {
            falseProbability = Double.MIN_VALUE;
        }
        return (long) (-expectedInsertions * Math.log(falseProbability) / (Math.log(2) * Math.log(2)));
    }

    /**
     * 根据布隆过滤器长度与预期插入长度之比，计算hash函数个数
     */
    private int optimalNumOfHashFunctions() {
        return Math.max(1, (int) Math.round((double) totalSize / expectedInsertions * Math.log(2)));
    }
    
    public int getChildBloomMaxSize() {
        return childBloomMaxSize;
    }

    public int getChildBloomNumber() {
        return childBloomNumber;
    }

    /**
     * 查看布隆过滤器是否存在
     * @return
     */
    public boolean checkBloomFilterExist() {
        return pipelineCluster.exists(name);
    }

}
