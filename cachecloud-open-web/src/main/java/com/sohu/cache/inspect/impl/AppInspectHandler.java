package com.sohu.cache.inspect.impl;

import com.sohu.cache.entity.InstanceInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by yijunzhang on 15-1-20.
 */
public class AppInspectHandler extends AbstractInspectHandler{
    private final String inspectPoolKey="inspector-app-pool";

    @Override
    public String getThreadPoolKey() {
        return inspectPoolKey;
    }

    @Override
    protected Map<String, List<InstanceInfo>> getSplitMap() {
        List<InstanceInfo> list = getAllInstanceList();
        Map<String, List<InstanceInfo>> hostMap = new TreeMap<String, List<InstanceInfo>>();
        for (InstanceInfo instanceInfo : list) {
            String appId = String.valueOf(instanceInfo.getAppId());
            if (hostMap.containsKey(appId)) {
                hostMap.get(appId).add(instanceInfo);
            } else {
                List<InstanceInfo> hostInstances = new ArrayList<InstanceInfo>();
                hostInstances.add(instanceInfo);
                hostMap.put(appId, hostInstances);
            }
        }
        return hostMap;
    }
}
