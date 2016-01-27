package com.sohu.test.init;

import com.sohu.cache.init.MachineInitLoad;
import com.sohu.test.BaseTest;

import org.junit.Test;

import javax.annotation.Resource;

/**
 * User: lingguo
 * Date: 14-6-12
 * Time: 下午3:20
 */
public class MachineInitTest extends BaseTest {

    @Resource
    MachineInitLoad machineInitLoad;

    @Test
    public void testInit() {
        machineInitLoad.init();
    }

}
