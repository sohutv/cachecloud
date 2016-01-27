package com.sohu.cache.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 幂等操作器
 * Created by yijunzhang on 14-10-22.
 */
public abstract class IdempotentConfirmer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private int retry = 3;

    protected IdempotentConfirmer(int retry) {
        this.retry = retry;
    }

    public IdempotentConfirmer() {
    }

    public abstract boolean execute();

    public boolean run() {
        while (retry-- > 0) {
            try {
                boolean isOk = execute();
                if (isOk){
                    return true;
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                continue;
            }
        }
        return false;
    }
}
