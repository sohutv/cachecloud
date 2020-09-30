package com.sohu.cache.client.command;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.netflix.hystrix.*;
import com.sohu.cache.client.service.ClientVersionService;
import com.sohu.cache.constant.ClientStatusEnum;
import com.sohu.cache.dao.AppDao;
import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.ObjectConvert;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by zhangyijun on 2017/8/7.
 */
public class AppClientCommand extends HystrixCommand<Map<String, Object>> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public final static int DEFAULT_TIMEOUT = 4000;

    public final static int DEFAULT_POOL_SIZE = 30;

    private static final String commandKey = "cacheClient";

    private static final String groupKey = "appClientService";

    private static final String poolKey = "appClientPool";

    private final AppClientParams appClientParams;

    private final InstanceDao instanceDao;

    private final AppDao appDao;

    private final ClientVersionService clientVersionService;

    private final ConcurrentMap<Long, Map<String, Object>> appClientMap;

    public AppClientCommand(AppClientParams appClientParams, AppDao appDao, InstanceDao instanceDao,
            ClientVersionService clientVersionService, ConcurrentMap<Long, Map<String, Object>> appClientMap) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(groupKey))
                .andCommandKey(HystrixCommandKey.Factory.asKey(commandKey))
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(poolKey))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withFallbackIsolationSemaphoreMaxConcurrentRequests(100)
                        .withExecutionTimeoutInMilliseconds(DEFAULT_TIMEOUT))
                .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter().withCoreSize(DEFAULT_POOL_SIZE)));
        this.appClientParams = appClientParams;
        this.instanceDao = instanceDao;
        this.appDao = appDao;
        this.clientVersionService = clientVersionService;
        this.appClientMap = appClientMap;
    }

    @Override
    protected Map<String, Object> run() throws Exception {
        Map<String, Object> model = Maps.newHashMap();
        int type = appClientParams.getType();
        long appId = appClientParams.getAppId();
        boolean isCheck = checkRedisApp(model);
        if (!isCheck) {
            return model;
        }
        if (type == ConstUtils.CACHE_TYPE_REDIS_CLUSTER) {
            model.putAll(getRedisClusterInfo(true));
        } else if (type == ConstUtils.CACHE_REDIS_SENTINEL) {
            model.putAll(getRedisSentinelInfo(true));
        } else if (type == ConstUtils.CACHE_REDIS_STANDALONE) {
            model.putAll(getRedisStandaloneInfo(true));
        }
        //每次数据库操作成功，更新缓存
        addAppClient(appId, model);

        return model;
    }

    public void addAppClient(long appId, Map<String, Object> model) {
        int status = MapUtils.getIntValue(model, "status", ClientStatusEnum.ERROR.getStatus());
        if (status != ClientStatusEnum.ERROR.getStatus()) {
            appClientMap.put(appId, model);
        }
    }

    @Override
    protected Map<String, Object> getFallback() {
        //抛出异常
        if (this.isFailedExecution()) {
            Throwable throwable = this.getFailedExecutionException();
            logger.error(throwable.getMessage(), throwable);
        }
        //判断是否为调用超时
        if (this.isResponseTimedOut()) {
            long time = this.getExecutionTimeInMilliseconds();
            logger.warn("commandKey={} groupKey={} poolKey={} timeout cost={} ms", commandKey, groupKey, poolKey, time);
        }
        Map<String, Object> model = appClientMap.get(appClientParams.getAppId());

        int status = MapUtils.getIntValue(model, "status", ClientStatusEnum.ERROR.getStatus());
        if (status == ClientStatusEnum.ERROR.getStatus()) {
            logger.error("app-fallback-error: appId={} ,clientIp={},stat={}", appClientParams.getAppId(),appClientParams.getAppClientIp(), model);
        }
        return model;
    }

    private boolean checkRedisApp(Map<String, Object> model) {
        long appId = appClientParams.getAppId();
        int type = appClientParams.getType();
        AppDesc appDesc = appDao.getAppDescById(appId);
        if (appDesc == null) {
            model.put("status", ClientStatusEnum.ERROR.getStatus());
            model.put("message", String.format("appId:%s 不存在", appId));
            return false;
        } else if (appDesc.getType() != type) {
            model.put("status", ClientStatusEnum.ERROR.getStatus());
            model.put("message",
                    String.format("appId:%s 类型不符,期望类型:%s,实际类型%s,请联系管理员!", appId, type, appDesc.getType()));
            return false;
        }
        appClientParams.setCacheAppDesc(appDesc);
        return true;
    }

    public Map<String, Object> getRedisStandaloneInfo(boolean clientRequest) {
        long appId = appClientParams.getAppId();

        Map<String, Object> model = Maps.newHashMap();
        boolean isPass = beforeProcess(clientRequest, model);
        if (!isPass) {
            return model;
        }
        List<InstanceInfo> instanceList = appClientParams.getCacheInstanceInfos();
        if (CollectionUtils.isEmpty(instanceList)) {
            instanceList = instanceDao.getInstListByAppId(appId);
        }
        String standalone = null;
        for (InstanceInfo instanceInfo : instanceList) {
            if (instanceInfo.isOffline()) {
                continue;
            }
            standalone = instanceInfo.getIp() + ":" + instanceInfo.getPort();
        }
        model.put("standalone", standalone);

        return model;
    }

    public Map<String, Object> getRedisSentinelInfo(boolean clientRequest) {
        Map<String, Object> model = Maps.newHashMap();
        boolean isPass = beforeProcess(clientRequest, model);
        if (!isPass) {
            return model;
        }

        long appId = appClientParams.getAppId();
        List<InstanceInfo> instanceList = appClientParams.getCacheInstanceInfos();
        if (CollectionUtils.isEmpty(instanceList)) {
            instanceList = instanceDao.getInstListByAppId(appId);
        }
        if (instanceList == null || instanceList.isEmpty()) {
            model.put("status", ClientStatusEnum.ERROR.getStatus());
            model.put("message", "appId: " + appId + " 实例集合为空 ");
            return model;
        }
        String masterName = null;
        List<String> sentinelList = new ArrayList<String>();
        for (InstanceInfo instance : instanceList) {
            if (instance.isOffline()) {
                continue;
            }
            if (instance.getType() == ConstUtils.CACHE_REDIS_SENTINEL
                    && masterName == null
                    && StringUtils.isNotBlank(instance.getCmd())) {
                masterName = instance.getCmd();
            }
            if (instance.getType() == ConstUtils.CACHE_REDIS_SENTINEL) {
                sentinelList.add(instance.getIp() + ":" + instance.getPort());
            }
        }
        String sentinels = StringUtils.join(sentinelList, " ");
        model.put("sentinels", sentinels);
        model.put("masterName", masterName);
        model.put("appId", appId);

        return model;
    }

    public Map<String, Object> getRedisClusterInfo(boolean clientRequest) {
        long appId = appClientParams.getAppId();

        Map<String, Object> model = Maps.newHashMap();
        boolean isPass = beforeProcess(clientRequest, model);
        if (!isPass) {
            return model;
        }

        List<InstanceInfo> instanceList = appClientParams.getCacheInstanceInfos();
        if (CollectionUtils.isEmpty(instanceList)) {
            instanceList = instanceDao.getInstListByAppId(appId);
        }
        if (instanceList == null || instanceList.isEmpty()) {
            model.put("status", ClientStatusEnum.ERROR.getStatus());
            model.put("message", "ERROR: appId:" + appId + "实例集合为空 ");
            return model;
        }
        String shardsInfo = ObjectConvert.assembleInstance(instanceList);
        if (StringUtils.isBlank(shardsInfo)) {
            model.put("status", ClientStatusEnum.ERROR.getStatus());
            model.put("message", "ERROR: appId:" + appId + "shardsInfo为空 ");
            return model;
        }
        int shardNum = shardsInfo.split(" ").length;
        model.put("appId", appId);
        model.put("shardNum", shardNum);
        model.put("shardInfo", shardsInfo);

        return model;
    }

    private boolean beforeProcess(boolean clientRequest, Map<String, Object> model) {
        long appId = appClientParams.getAppId();
        String clientVersion = appClientParams.getClientVersion();
        if (StringUtils.isBlank(clientVersion)) {
            clientVersion = appClientParams.getCacheMaxVersion();
            if (StringUtils.isBlank(clientVersion)) {
                clientVersion = clientVersionService.getAppMaxClientVersion(appClientParams.getAppId());
            }
            if (StringUtils.isBlank(clientVersion)) {
                clientVersion = "1.7-SNAPSHOT";
            }
        }
        boolean isVersionOk = checkClientVersion(clientVersion, model);
        if (!isVersionOk) {
            return false;
        }
        addPkey(clientVersion, model);
        if (clientRequest) {
            //保存版本信息
            try {
                String appClientIp = appClientParams.getAppClientIp();
                clientVersionService.saveOrUpdateClientVersion(appId, appClientIp, clientVersion);
            } catch (Exception e) {
                logger.error("redis heart error:" + e.getMessage(), e);
            }
        }
        return true;
    }

    // 处理pkey
    private void addPkey(String clientVersion, Map<String, Object> model) {
        Double partVersion = NumberUtils.toDouble(clientVersion.substring(0, 3), 1.7);
        if (partVersion >= 1.7) {
            AppDesc appDesc = appClientParams.getCacheAppDesc();
            if (appDesc == null) {
                appDesc = appDao.getAppDescById(appClientParams.getAppId());
            }
            String pkey = appDesc.getPkey();
            if (StringUtils.isNotBlank(pkey)) {
                model.put("pkey", pkey);
            } else {
                model.put("pkey", "");
            }
        }
    }

    private boolean checkClientVersion(String clientVersion, Map<String, Object> model) {
        long appId = appClientParams.getAppId();
        /** 检查客户端的版本 **/
        List<String> goodVersions = Lists.newArrayList(ConstUtils.GOOD_CLIENT_VERSIONS.split(ConstUtils.COMMA));
        List<String> warnVersions = Lists.newArrayList(ConstUtils.WARN_CLIENT_VERSIONS.split(ConstUtils.COMMA));

        boolean versionOk = true;

        if (goodVersions.contains(clientVersion)) {
            model.put("status", ClientStatusEnum.GOOD.getStatus());
            model.put("message", "appId:" + appId + " client is up to date, Cheers!");
        } else if (warnVersions.contains(clientVersion)) {
            model.put("status", ClientStatusEnum.WARN.getStatus());
            model.put("message", "WARN: client is NOT the newest, please update!");
        } else {
            model.put("status", ClientStatusEnum.ERROR.getStatus());
            model.put("message", "ERROR: client is TOO old or NOT recognized, please update NOW!");
            versionOk = false;
        }
        return versionOk;
    }

}
