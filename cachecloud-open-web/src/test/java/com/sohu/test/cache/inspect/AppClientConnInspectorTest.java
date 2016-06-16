package com.sohu.test.cache.inspect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.junit.Test;

import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.inspect.InspectParamEnum;
import com.sohu.cache.inspect.impl.AppClientConnInspector;
import com.sohu.cache.web.service.AppService;
import com.sohu.test.BaseTest;

/**
 * 应用客户端连接数测试
 * @author leifu
 * @Date 2016年6月16日
 * @Time 上午10:31:51
 */
public class AppClientConnInspectorTest extends BaseTest {

    @Resource
    private AppClientConnInspector appClientConnInspector;
    
    @Resource
    private AppService appService;
    
    @Test
    public void testApp() {
        long appId = 10024;
        Map<InspectParamEnum, Object> paramMap = new HashMap<InspectParamEnum, Object>();
        paramMap.put(InspectParamEnum.SPLIT_KEY, appId);
        List<InstanceInfo> instanceInfoList = appService.getAppInstanceInfo(appId);
        paramMap.put(InspectParamEnum.INSTANCE_LIST, instanceInfoList);
        appClientConnInspector.inspect(paramMap);
    }
    
}
