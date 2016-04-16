package com.sohu.cache.stats.app;

import java.util.Date;

import javax.annotation.Resource;

import org.junit.Test;

import com.sohu.cache.constant.ImportAppResult;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.util.ConstUtils;
import com.sohu.test.BaseTest;

/**
 * 应用导入测试
 * 
 * @author leifu
 * @Date 2016-4-16
 * @Time 下午5:55:38
 */
public class ImportAppCenterTest extends BaseTest {

    @Resource(name = "importAppCenter")
    private ImportAppCenter importAppCenter;

    @Test
    public void testImport() {
        // 应用信息
        AppDesc appDesc = new AppDesc();
        appDesc.setName("my-old-sentinel");
        appDesc.setIntro("my-old-sentinel desc");
        appDesc.setOfficer("付磊");
        appDesc.setCreateTime(new Date());
        appDesc.setPassedTime(new Date());
        appDesc.setIsTest(1);
        appDesc.setType(ConstUtils.CACHE_TYPE_REDIS_CLUSTER);
        appDesc.setMemAlertValue(80);
        appDesc.setStatus(2);
        appDesc.setUserId(1);
        appDesc.setVerId(1);

        // 实例信息
        String appInstanceInfo =
                        "10.10.53.159:7000:512\n" +
                        "10.10.53.159:7001:512\n" +
                        "10.10.53.159:26379:mymaster\n" +
                        "10.10.53.159:26380:mymaster\n" +
                        "10.10.53.159:26381:mymaster";
        boolean result = importAppCenter.importAppAndInstance(appDesc, appInstanceInfo);
        logger.info("result: {}", result);
    }

    /**
     * 重复应用名
     */
    @Test
    public void testCheckAppDuplicateName() {
        // 应用信息
        AppDesc appDesc = new AppDesc();
        appDesc.setName("redis-cluster-test");
        // 实例信息
        String appInstanceInfo = "";
        ImportAppResult importAppResult = importAppCenter.check(appDesc, appInstanceInfo);
        logger.info("importAppResult: {}", importAppResult);
    }

    /**
     * 应用实例详情为空
     */
    @Test
    public void testCheckEmptyAppInstanceInfo1() {
        // 应用信息
        AppDesc appDesc = new AppDesc();
        appDesc.setName("carlosfu1");
        // 实例信息
        String appInstanceInfo = "";
        ImportAppResult importAppResult = importAppCenter.check(appDesc, appInstanceInfo);
        logger.info("importAppResult: {}", importAppResult);
    }

    /**
     * 应用实例详情格式有问题1
     */
    @Test
    public void testCheckWrongFormatAppInstanceInfo2() {
        // 应用信息
        AppDesc appDesc = new AppDesc();
        appDesc.setName("carlosfu2");
        // 实例信息
        String appInstanceInfo = "\n10.10.53.159:6379:1024";
        ImportAppResult importAppResult = importAppCenter.check(appDesc, appInstanceInfo);
        logger.info("importAppResult: {}", importAppResult);
    }

    /**
     * 应用实例详情格式有问题2
     */
    @Test
    public void testCheckWrongFormatAppInstanceInfo3() {
        // 应用信息
        AppDesc appDesc = new AppDesc();
        appDesc.setName("carlosfu3");
        // 实例信息
        String appInstanceInfo = "10.10.53.159:6379";
        ImportAppResult importAppResult = importAppCenter.check(appDesc, appInstanceInfo);
        logger.info("importAppResult: {}", importAppResult);
    }

    /**
     * 应用实例详情格式有问题4
     */
    @Test
    public void testCheckWrongFormatAppInstanceInfo4() {
        // 应用信息
        AppDesc appDesc = new AppDesc();
        appDesc.setName("carlosfu4");
        // 实例信息
        String appInstanceInfo = "10.10.10.10:6379:1024";
        ImportAppResult importAppResult = importAppCenter.check(appDesc, appInstanceInfo);
        logger.info("importAppResult: {}", importAppResult);
    }

    /**
     * 应用实例详情格式有问题5
     */
    @Test
    public void testCheckWrongFormatAppInstanceInfo5() {
        // 应用信息
        AppDesc appDesc = new AppDesc();
        appDesc.setName("carlosfu5");
        // 实例信息
        String appInstanceInfo = "10.10.53.162:ab:1024";
        ImportAppResult importAppResult = importAppCenter.check(appDesc, appInstanceInfo);
        logger.info("importAppResult: {}", importAppResult);
    }

    /**
     * 已经存在实例信息
     */
    @Test
    public void testCheckExistInstanceInfo() {
        // 应用信息
        AppDesc appDesc = new AppDesc();
        appDesc.setName("carlosfu6");
        // 实例信息
        String appInstanceInfo = "10.10.53.162:6379:1024";
        ImportAppResult importAppResult = importAppCenter.check(appDesc, appInstanceInfo);
        logger.info("importAppResult: {}", importAppResult);
    }

    /**
     * 已经存在实例信息
     */
    @Test
    public void testCheckNotRunInstance() {
        // 应用信息
        AppDesc appDesc = new AppDesc();
        appDesc.setName("carlosfu7");
        // 实例信息
        String appInstanceInfo = "10.10.53.162:6399:1024";
        ImportAppResult importAppResult = importAppCenter.check(appDesc, appInstanceInfo);
        logger.info("importAppResult: {}", importAppResult);
    }

    /**
     * 最大内存检查
     */
    @Test
    public void testCheckWrongMaxMemory() {
        // 应用信息
        AppDesc appDesc = new AppDesc();
        appDesc.setName("carlosfu8");
        // 实例信息
        String appInstanceInfo = "10.10.53.159:6379:aa";
        ImportAppResult importAppResult = importAppCenter.check(appDesc, appInstanceInfo);
        logger.info("importAppResult: {}", importAppResult);
    }

    /**
     */
    @Test
    public void testCheckDataNode() {
        // 应用信息
        AppDesc appDesc = new AppDesc();
        appDesc.setName("carlosfu9");
        // 实例信息
        String appInstanceInfo = "10.10.53.159:6379:1024";
        ImportAppResult importAppResult = importAppCenter.check(appDesc, appInstanceInfo);
        logger.info("importAppResult: {}", importAppResult);
    }

    /**
     * 测试sentinel的masterName
     */
    @Test
    public void testCheckSentinelNodeMasterName() {
        // 应用信息
        AppDesc appDesc = new AppDesc();
        appDesc.setName("carlosfu10");
        // 实例信息
        String appInstanceInfo = "10.10.53.159:26379:1024";
        ImportAppResult importAppResult = importAppCenter.check(appDesc, appInstanceInfo);
        logger.info("importAppResult: {}", importAppResult);
    }

    /**
     * 检测sentinel节点
     */
    @Test
    public void testCheckSentinelNode() {
        // 应用信息
        AppDesc appDesc = new AppDesc();
        appDesc.setName("carlosfu11");
        // 实例信息
        String appInstanceInfo = "10.10.53.159:26379:mymaster";
        ImportAppResult importAppResult = importAppCenter.check(appDesc, appInstanceInfo);
        logger.info("importAppResult: {}", importAppResult);
    }

    /**
     * 检测sentinel节点
     */
    @Test
    public void testCheckSentinelAllNodes() {
        // 应用信息
        AppDesc appDesc = new AppDesc();
        appDesc.setName("carlosfu12");
        // 实例信息
        String appInstanceInfo =
                "10.10.53.159:7000:512\n" +
                        "10.10.53.159:7001:512\n" +
                        "10.10.53.159:26379:mymaster\n" +
                        "10.10.53.159:26380:mymaster\n" +
                        "10.10.53.159:26381:mymaster";
        ImportAppResult importAppResult = importAppCenter.check(appDesc, appInstanceInfo);
        logger.info("importAppResult: {}", importAppResult);
    }

    @Test
    public void testCheckClusterNodes() {
        // 应用信息
        AppDesc appDesc = new AppDesc();
        appDesc.setName("carlosfu13");
        // 实例信息
        String appInstanceInfo =
                "10.10.53.159:8000:512\n" +
                        "10.10.53.159:8001:512\n" +
                        "10.10.53.159:8002:512\n" +
                        "10.10.53.159:8003:512\n" +
                        "10.10.53.159:8004:512\n" +
                        "10.10.53.159:8005:512\n";
        ImportAppResult importAppResult = importAppCenter.check(appDesc, appInstanceInfo);
        logger.info("importAppResult: {}", importAppResult);
    }

}
