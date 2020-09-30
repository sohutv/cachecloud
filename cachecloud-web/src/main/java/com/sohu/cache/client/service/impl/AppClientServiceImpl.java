package com.sohu.cache.client.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sohu.cache.client.command.AppClientCommand;
import com.sohu.cache.client.command.AppClientParams;
import com.sohu.cache.client.service.AppClientService;
import com.sohu.cache.client.service.ClientVersionService;
import com.sohu.cache.dao.AppDao;
import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.util.ConstUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by zhangyijun on 2017/8/4.
 */
public class AppClientServiceImpl implements AppClientService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private InstanceDao instanceDao;

    @Autowired
    private AppDao appDao;

    @Autowired
    private ClientVersionService clientVersionService;

    private ConcurrentMap<Long, Map<String, Object>> appClientMap = Maps.newConcurrentMap();

    @PostConstruct
    public void initApps() {
        long start = System.currentTimeMillis();

        List<AppDesc> onlineApps = appDao.getOnlineApps();
        if (onlineApps == null) {
            return;
        }
        Map<Long, List<InstanceInfo>> cacheInstances = Maps.newHashMap();
        List<InstanceInfo> allInstances = instanceDao.getAllInsts();
        for (InstanceInfo instanceInfo : allInstances) {
            long appId = instanceInfo.getAppId();
            if (instanceInfo.isOnline()) {
                List<InstanceInfo> instances = cacheInstances.get(appId);
                if (instances == null) {
                    instances = Lists.newArrayList();
                    cacheInstances.put(appId, instances);
                }
                instances.add(instanceInfo);
            }
        }
        Map<Long, String> cacheMaxVersions = Maps.newHashMap();
        List<Map<String, Object>> appMaxVersions = clientVersionService.getAllMaxClientVersion();
        for (Map<String, Object> map : appMaxVersions) {
            long appId = MapUtils.getLongValue(map, "appId");
            String clientVersion = MapUtils.getString(map, "clientVersion");
            cacheMaxVersions.put(appId, clientVersion);
        }
        for (AppDesc appDesc : onlineApps) {
            int type = appDesc.getType();
            long appId = appDesc.getAppId();
            AppClientParams appClientParams = new AppClientParams(appId, type, null, null);
            appClientParams.setCacheAppDesc(appDesc);
            appClientParams.setCacheInstanceInfos(cacheInstances.get(appId));
            appClientParams.setCacheMaxVersion(cacheMaxVersions.get(appId));

            AppClientCommand command = new AppClientCommand(appClientParams, appDao, instanceDao, clientVersionService,
                    appClientMap);
            Map<String, Object> model;
            if (type == ConstUtils.CACHE_TYPE_REDIS_CLUSTER) {
                model = command.getRedisClusterInfo(false);
                command.addAppClient(appId, model);
            } else if (type == ConstUtils.CACHE_REDIS_SENTINEL) {
                model = command.getRedisSentinelInfo(false);
                command.addAppClient(appId, model);
            } else if (type == ConstUtils.CACHE_REDIS_STANDALONE) {
                model = command.getRedisStandaloneInfo(false);
                command.addAppClient(appId, model);
            }
        }
        long end = System.currentTimeMillis();
        logger.warn("AppClientService: app-client-size={} cost={} ms", appClientMap.size(), (end - start));
    }

    @Override
    public Map<String, Object> getAppClientInfo(AppClientParams appClientParams) {
        AppClientCommand command = new AppClientCommand(appClientParams, appDao, instanceDao, clientVersionService,
                appClientMap);
        return command.execute();
    }

}
