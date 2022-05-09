package com.sohu.cache.web.service;

import com.sohu.cache.entity.AppUser;
import com.sohu.cache.web.vo.AppRedisConfigCheckResult;
import com.sohu.cache.web.vo.AppRedisConfigCheckVo;
import com.sohu.cache.web.vo.RedisConfigCheckResult;

import java.util.List;

/**
 * @Author: zengyizhao
 * @DateTime: 2021/9/26 17:24
 * @Description: redis配置检测
 */
public interface AppRedisConfigCheckService {

    RedisConfigCheckResult checkRedisConfig(AppUser appUser, AppRedisConfigCheckVo configCheckVo);

    void saveRedisConfigCheckResult(RedisConfigCheckResult redisConfigCheckResult, List<AppRedisConfigCheckResult> resultList);

    List<RedisConfigCheckResult> getRedisConfigCheckResult();

    List<AppRedisConfigCheckResult> getRedisConfigCheckDetailResult(String uuid);

}
