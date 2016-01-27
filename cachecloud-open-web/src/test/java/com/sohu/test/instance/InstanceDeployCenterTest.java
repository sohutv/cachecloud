package com.sohu.test.instance;

import com.sohu.cache.stats.instance.InstanceDeployCenter;
import com.sohu.test.BaseTest;
import org.junit.Test;

import javax.annotation.Resource;

/**
 * Created by yijunzhang on 14-11-27.
 */
public class InstanceDeployCenterTest extends BaseTest {

    @Resource
    private InstanceDeployCenter instanceDeployCenter;

    @Test
    public void testStartExistInstance() throws Exception {
        instanceDeployCenter.startExistInstance(631);
    }

    @Test
    public void testShutdownExistInstance() throws Exception {
        instanceDeployCenter.shutdownExistInstance(703);
    }
}
