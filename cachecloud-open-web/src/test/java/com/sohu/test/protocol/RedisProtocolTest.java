package com.sohu.test.protocol;

import com.sohu.cache.protocol.RedisProtocol;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yijunzhang on 14-12-5.
 */
public class RedisProtocolTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testRedisProtocolEcho() {
        logger.info(RedisProtocol.getRunShell(6379, true));
        logger.info(RedisProtocol.getSentinelShell(6379));
        logger.info(RedisProtocol.getConfig(6379, false));
    }

}
