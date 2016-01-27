package com.sohu.test.controller;

import com.sohu.test.SimpleBaseTest;

import org.junit.Test;
import org.springframework.web.client.RestTemplate;

/**
 * Created by lingguo on 14-6-26.
 */
public class MemcachedClientControllerTest extends SimpleBaseTest {

    RestTemplate restTemplate = new RestTemplate();

    @Test
    public void testGetAppById() {
        String url = "http://localhost:8585/cache/client/memcached/app/10000.json";
        logger.info("{}", restTemplate.getForObject(url, String.class));
    }

    @Test
    public void testGetAppByIdOld() {
        String url = "http://localhost:8585/memcloud/dns/10000.json";
        logger.info("{}", restTemplate.getForObject(url, String.class));
    }
}
