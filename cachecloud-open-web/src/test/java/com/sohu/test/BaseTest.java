package com.sohu.test;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StopWatch;

/**
 * Created by yijunzhang on 14-6-4.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring-test.xml"})
public class BaseTest  extends Assert {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected StopWatch watch = new StopWatch();

}
