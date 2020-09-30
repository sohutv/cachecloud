package com.sohu.tv.cachecloud.client.bloom.hash;

import java.io.Serializable;
import java.util.List;

/**
 * hash函数接口类
 * @author leifu
 */
public abstract class HashFunction implements Serializable {
    
    private static final long serialVersionUID = -1074935532939858765L;
    
    protected static final int seed32 = 89478583;
    
    /**
     * Performs rejection sampling on a random 32bit Java int (sampled from Integer.MIN_VALUE to Integer.MAX_VALUE).
     *
     * @param random int
     * @param m     integer output range [1,size]
     * @return the number down-sampled to interval [0, size]. Or -1 if it has to be rejected.
     */
    protected int rejectionSample(int random, int m) {
        random = Math.abs(random);
        if (random > (2147483647 - 2147483647 % m)
                || random == Integer.MIN_VALUE)
            return -1;
        else
            return random % m;
    }

    /**
     * Computes hash values.
     *
     * @param value the byte[] representation of the element to be hashed
     * @param m integer output range [1,size]
     * @param k number of hashes to be computed
     */
    public abstract List<Integer> hash(byte[] value, int m, int k);
}