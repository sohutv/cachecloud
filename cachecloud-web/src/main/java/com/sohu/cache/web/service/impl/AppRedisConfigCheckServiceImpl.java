package com.sohu.cache.web.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sohu.cache.constant.AppStatusEnum;
import com.sohu.cache.constant.InstanceStatusEnum;
import com.sohu.cache.dao.AppDao;
import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.AppSearch;
import com.sohu.cache.entity.AppUser;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.redis.AssistRedisService;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.util.StringUtil;
import com.sohu.cache.web.enums.BooleanEnum;
import com.sohu.cache.web.enums.CompareTypeEnum;
import com.sohu.cache.web.service.AppRedisConfigCheckService;
import com.sohu.cache.web.vo.AppRedisConfigCheckResult;
import com.sohu.cache.web.vo.AppRedisConfigCheckVo;
import com.sohu.cache.web.vo.InstanceRedisConfigCheckResult;
import com.sohu.cache.web.vo.RedisConfigCheckResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: zengyizhao
 * @DateTime: 2021/9/26 17:34
 * @Description: redis配置检测
 */
@Slf4j
@Service
public class AppRedisConfigCheckServiceImpl implements AppRedisConfigCheckService {

    @Autowired
    private AppDao appDao;

    @Autowired
    private InstanceDao instanceDao;

    @Autowired
    private RedisCenter redisCenter;

    @Autowired
    private AssistRedisService assistRedisService;

    private static final String REDIS_CHECK_RESULT_SAVE_KEY = "redis:check:result";

    private static final String REDIS_CHECK_RESULT_KEY = "redis:check:result:";

    @Override
    public RedisConfigCheckResult checkRedisConfig(AppUser appUser, AppRedisConfigCheckVo configCheckVo) {
        RedisConfigCheckResult redisConfigCheckResult = new RedisConfigCheckResult();
        redisConfigCheckResult.setUserName(appUser.getChName() == null ? appUser.getName() : appUser.getChName());
        redisConfigCheckResult.setCreateTime(new Date());
        redisConfigCheckResult.setSuccess(true);
        BeanUtils.copyProperties(configCheckVo, redisConfigCheckResult);
        List<AppRedisConfigCheckResult> configCheckResults = new ArrayList<>();
        //获取待检测配置项
        String configName = configCheckVo.getConfigName();
        //trim一下首尾
        String expectValue = configCheckVo.getExpectValue();
        //获取应用（根据redis版本，用户指定应用id，全部）
        List<AppDesc> appDescList = getAppByCondition(configCheckVo.getAppId(), configCheckVo.getVersionId());
        //获取检测规则, 并校验
        int compareType = configCheckVo.getCompareType();
        CompareTypeEnum compareTypeEnum = getCompareType(compareType);
        if(compareTypeEnum == null){
            return null;
        }
        //递归对每个应用进行处理(1.获取应用下实例; 2.根据检测项获取配置值，与检测规则对比，得出结果; 3.每个应用的配置值进行对比，并返回校验失败的信息)
        for(AppDesc appDesc : appDescList){
            AppRedisConfigCheckResult appCheckResult = new AppRedisConfigCheckResult();
            appCheckResult.setAppDesc(appDesc);
            appCheckResult.setCompareType(compareType);
            appCheckResult.setConfigName(configName);
            appCheckResult.setExpectValue(expectValue);
            appCheckResult.setVersionId(configCheckVo.getVersionId());
            appCheckResult.setSuccess(true);
            appCheckResult.setCreateTime(new Date());
            List<InstanceRedisConfigCheckResult> instanceCheckResultList = new ArrayList<>();
            List<InstanceInfo> instanceInfoList = getInstanceInfoListByApp(appDesc.getAppId());
            for (InstanceInfo instanceInfo : instanceInfoList) {
                BooleanEnum master = redisCenter.isMaster(appDesc.getAppId(), instanceInfo.getIp(), instanceInfo.getPort());
                if(master.equals(BooleanEnum.OTHER)){
                    continue;
                }
                InstanceRedisConfigCheckResult instanceCheckResult = new InstanceRedisConfigCheckResult();
                instanceCheckResult.setInstanceInfo(instanceInfo);
                String realValue = getConfigByCommand(appDesc, instanceInfo, configName);
                boolean checkResult = checkConfigSatisfy(expectValue, realValue, compareTypeEnum);
                if(!checkResult){
                    redisConfigCheckResult.setSuccess(false);
                    appCheckResult.setSuccess(false);
                    instanceCheckResult.setSuccess(false);
                    instanceCheckResult.setConfigName(configName);
                    instanceCheckResult.setExpectValue(expectValue);
                    instanceCheckResult.setRealValue(realValue);
                }else{
                    instanceCheckResult.setSuccess(true);
                }
                instanceCheckResultList.add(instanceCheckResult);
            }
            appCheckResult.setInstanceCheckList(instanceCheckResultList);
            configCheckResults.add(appCheckResult);
        }
        UUID uuid = UUID.randomUUID();
        redisConfigCheckResult.setKey(uuid.toString());
        this.saveRedisConfigCheckResult(redisConfigCheckResult, configCheckResults);
        return redisConfigCheckResult;
    }

    /**
     * 获取比较类型
     * @param compareType
     * @return
     */
    private CompareTypeEnum getCompareType(int compareType) {
        return CompareTypeEnum.getByType(compareType);
    }

    /**
     * 根据（appId, versionId）查询应用
     * @param appId
     * @param versionId
     * @return
     */
    private List<AppDesc> getAppByCondition(Long appId, Integer versionId){
        AppSearch appSearch =  new AppSearch();
        appSearch.setAppId(appId);
        appSearch.setVersionId(versionId);
        appSearch.setAppStatus(AppStatusEnum.STATUS_PUBLISHED.getStatus());
        List<AppDesc> allAppDescList = appDao.getAllAppDescList(appSearch);
        return allAppDescList;
    }

    /**
     * 查询应用下有效的实例信息
     * @param appId
     * @return
     */
    private List<InstanceInfo> getInstanceInfoListByApp(Long appId){
        // 2.获取当前应用下redis实例信息
        List<InstanceInfo> instanceList = instanceDao.getInstListByAppId(appId);;
        //过滤出运行中的实例
        instanceList = instanceList.stream().filter(instanceInfo -> instanceInfo.getStatus() == InstanceStatusEnum.GOOD_STATUS.getStatus()).collect(Collectors.toList());
        return instanceList;
    }

    /**
     * 通过config get获取配置项的值
     * @param appDesc
     * @param instanceInfo
     * @param configName
     * @return
     */
    private String getConfigByCommand(AppDesc appDesc, InstanceInfo instanceInfo, String configName){
        String configValue = null;
        // 1.获取连接
        final Jedis jedis = redisCenter.getJedis(appDesc.getAppId(), instanceInfo.getIp(), instanceInfo.getPort(), 5000, 5000);
        try {
            List<String> strings = jedis.configGet(configName);
            if(strings != null && strings.size() > 1){
                configValue = strings.get(1);
            }
        } catch (Exception e) {
            if(e instanceof JedisConnectionException){
                configValue = "连接失败，未取到值";
            }else if(e instanceof JedisDataException){
                if(e.getMessage().contains("ERR unknown command `CONFIG`, with args beginning with: `get`, ")){
                    configValue = "无此配置，未取到值";
                }
            }
            if(configValue == null){
                configValue = "异常，未取到值";
            }
            log.error("getConfigByCommand", e);
        } finally {
            if (jedis != null)
                jedis.close();
        }
        return configValue;
    }

    private boolean checkConfigSatisfy(String expectValue, String realValue, CompareTypeEnum compareTypeEnum){
        boolean resultFlag = false;
        if(StringUtil.isBlank(expectValue)){
            expectValue = "";
        }
        if(CompareTypeEnum.EQUAL.equals(compareTypeEnum)){
            if(StringUtil.isBlank(realValue) && StringUtil.isBlank(expectValue)){
                resultFlag = true;
            }else {
                resultFlag = expectValue.equals(realValue);
            }
            return resultFlag;
        }else if(CompareTypeEnum.NOT_EQUAL.equals(compareTypeEnum)){
            if(StringUtil.isBlank(realValue) && StringUtil.isBlank(expectValue)){
                resultFlag = false;
            }else {
                resultFlag = !expectValue.equals(realValue);
            }
            return resultFlag;
        }else {
            if(StringUtil.isBlank(realValue) || StringUtil.isBlank(expectValue)){
                return resultFlag;
            }
            Integer compare = null;
            try{
                long l = Long.parseLong(realValue);
                long l1 = Long.parseLong(expectValue);
                compare = Long.compare(l, l1);
            }catch (Exception e){

            }
            if(compare == null){
                try{
                    double v = Double.parseDouble(realValue);
                    double v1 = Double.parseDouble(expectValue);
                    compare = Double.compare(v, v1);
                }catch (Exception e){

                }
            }
            if(compare == null){
                return resultFlag;
            }
            if(CompareTypeEnum.LESS_THAN.equals(compareTypeEnum)){
                return compare < 0;
            }
            if(CompareTypeEnum.MORE_THAN.equals(compareTypeEnum)){
                return compare > 0;
            }
            return resultFlag;
        }
    }

    @Override
    public void saveRedisConfigCheckResult(RedisConfigCheckResult redisConfigCheckResult, List<AppRedisConfigCheckResult> resultList) {
        if(redisConfigCheckResult != null){
            Long llen = assistRedisService.llen(REDIS_CHECK_RESULT_SAVE_KEY);
            if(llen >= 20){
                String configResult = assistRedisService.lpop(REDIS_CHECK_RESULT_SAVE_KEY);
                RedisConfigCheckResult toRemoveResult = JSONObject.parseObject(configResult, RedisConfigCheckResult.class);
                assistRedisService.remove(REDIS_CHECK_RESULT_KEY + toRemoveResult.getKey());
            }
            assistRedisService.rpush(REDIS_CHECK_RESULT_SAVE_KEY, JSONObject.toJSONString(redisConfigCheckResult));
            if(CollectionUtils.isNotEmpty(resultList)){
                assistRedisService.set(REDIS_CHECK_RESULT_KEY + redisConfigCheckResult.getKey(), JSONObject.toJSONString(resultList));
            }
        }
    }

    @Override
    public List<RedisConfigCheckResult> getRedisConfigCheckResult() {
        List<RedisConfigCheckResult> lists = new ArrayList<>();
        List<String> lrange = assistRedisService.lrange(REDIS_CHECK_RESULT_SAVE_KEY, 0, 20);
        if(CollectionUtils.isNotEmpty(lrange)){
            for (String str : lrange) {
                lists.add(JSONObject.parseObject(str, RedisConfigCheckResult.class));
            }
        }
        lists = lists.stream().sorted(Comparator.comparing(RedisConfigCheckResult::getCreateTime).reversed()).collect(Collectors.toList());
        return lists;
    }

    @Override
    public List<AppRedisConfigCheckResult> getRedisConfigCheckDetailResult(String uuid) {
        List<AppRedisConfigCheckResult> configCheckResults = new ArrayList<>();
        String resultStr = assistRedisService.get(REDIS_CHECK_RESULT_KEY + uuid);
        if(!StringUtil.isBlank(resultStr)){
            configCheckResults = JSONObject.parseArray(resultStr, AppRedisConfigCheckResult.class);
        }
        return configCheckResults;
    }

}
