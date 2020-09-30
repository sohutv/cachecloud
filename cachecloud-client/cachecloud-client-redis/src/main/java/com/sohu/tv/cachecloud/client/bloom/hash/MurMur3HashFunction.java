package com.sohu.tv.cachecloud.client.bloom.hash;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author leifu
 */
public class MurMur3HashFunction extends HashFunction {

    private static final long serialVersionUID = 4228200717674617245L;

    @Override
    public List<Integer> hash(byte[] value, int m, int k) {
        List<Integer> hashList = new ArrayList<Integer>();
        int seed = 0;
        int pos = 0;
        while (pos < k) {
            seed = murmur3(seed, value);
            int hash = rejectionSample(seed, m);
            if (hash != -1) {
                hashList.add(hash);
                pos++;
            }
        }
        return hashList;
    }
    
    
    private int murmur3(int seed, byte[] bytes) {
        int h1 = seed; //Standard in Guava
        int c1 = 0xcc9e2d51;
        int c2 = 0x1b873593;
        int len = bytes.length;
        int i = 0;

        while (len >= 4) {
            //process()
            int k1 = bytes[i + 0] & 0xFF;
            k1 |= (bytes[i + 1] & 0xFF) << 8;
            k1 |= (bytes[i + 2] & 0xFF) << 16;
            k1 |= (bytes[i + 3] & 0xFF) << 24;

            k1 *= c1;
            k1 = Integer.rotateLeft(k1, 15);
            k1 *= c2;

            h1 ^= k1;
            h1 = Integer.rotateLeft(h1, 13);
            h1 = h1 * 5 + 0xe6546b64;

            len -= 4;
            i += 4;
        }


        if (len > 0) {
            //processingRemaining()
            int k1 = 0;
            switch (len) {
                case 3:
                    k1 ^= (bytes[i + 2] & 0xFF) << 16;
                    // fall through
                case 2:
                    k1 ^= (bytes[i + 1] & 0xFF) << 8;
                    // fall through
                case 1:
                    k1 ^= (bytes[i] & 0xFF);
                    // fall through
                default:
                    k1 *= c1;
                    k1 = Integer.rotateLeft(k1, 15);
                    k1 *= c2;
                    h1 ^= k1;
            }
            i += len;
        }

        //makeHash()
        h1 ^= i;

        h1 ^= h1 >>> 16;
        h1 *= 0x85ebca6b;
        h1 ^= h1 >>> 13;
        h1 *= 0xc2b2ae35;
        h1 ^= h1 >>> 16;

        return h1;
    }
    
}
