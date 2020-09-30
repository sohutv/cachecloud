package com.sohu.tv.cachecloud.client.bloom;

import java.util.List;
import java.util.Map;

/**
 * 布隆过滤器接口
 * @author leifu
 */
public interface BloomFilter<T> {

    /**
     * 添加
     * @param object
     * @return 是否添加成功
     */
    boolean add(T object);
    
    /**
     * 批量添加
     * @param objectList
     * @return 是否添加成功
     */
    boolean batchAdd(List<T> objectList);

    /**
     * 是否包含
     * @param object
     */
    boolean contains(T object);
    
    /**
     * 是否包含
     * @param object
     */
    Map<T, Boolean> batchContains(List<T> objectList);
    
    /**
     * 清除
     */
    void clear();
    
    /**
     * 预期插入数量
     */
    long getExpectedInsertions();

    /**
     * 预期错误概率
     */
    double getFalseProbability();

    /**
     * 布隆过滤器总长度
     */
    long getSize();

    /**
     * hash函数迭代次数
     */
    int getHashIterations();
    
    /**
     * 子布隆过滤器个数
     */
    int getChildBloomNumber();

}
