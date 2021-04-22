package com.sohu.cache.stats.app.impl;

import com.sohu.cache.constant.ImportAppResult;
import com.sohu.cache.constant.InstanceStatusEnum;
import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.dao.InstanceStatsDao;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.entity.InstanceStats;
import com.sohu.cache.entity.MachineInfo;
import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.stats.app.ImportAppCenter;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.util.IdempotentConfirmer;
import com.sohu.cache.web.service.AppService;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;

import java.util.List;
import java.util.Map;

/**
 * 导入应用
 *
 * @author leifu
 * @Date 2016-4-16
 * @Time 下午3:42:49
 */
@Service("importAppCenter")
public class ImportAppCenterImpl implements ImportAppCenter {

    private Logger logger = LoggerFactory.getLogger(ImportAppCenterImpl.class);
    @Autowired
    private AppService appService;
    @Autowired
    @Lazy
    private RedisCenter redisCenter;
    @Autowired
    @Lazy
    private MachineCenter machineCenter;
    @Autowired
    private InstanceDao instanceDao;
    @Autowired
    private InstanceStatsDao instanceStatsDao;

    @Override
    public ImportAppResult check(int type, String appInstanceInfo, String password) {
        // 1.实例信息是否为空
        if (StringUtils.isBlank(appInstanceInfo)) {
            return ImportAppResult.fail("实例详情为空");
        }

        String[] appInstanceDetails = appInstanceInfo.split("\n");

        String masterNameInput = "";
        // 2.检查实例信息格式是否正确
        for (String appInstance : appInstanceDetails) {
            if (StringUtils.isBlank(appInstance)) {
                return ImportAppResult.fail("应用实例信息有空行");
            }
            String[] instanceItems = appInstance.split(":");
            if (instanceItems.length != 2) {
                return ImportAppResult.fail("应用实例信息" + appInstance + "格式错误，必须以冒号分隔");
            }

            // 2.1检查端口是否为整数
            String ip = instanceItems[0];
            String portStr = instanceItems[1];
            boolean portIsDigit = NumberUtils.isDigits(portStr);
            if ((!portIsDigit) && (type != ConstUtils.CACHE_REDIS_SENTINEL)) {
                return ImportAppResult.fail(appInstance + "中的port不是整数");
            } else if ((!portIsDigit) && (type == ConstUtils.CACHE_REDIS_SENTINEL)) {
                masterNameInput = instanceItems[0];
                continue;
            }

            int port = NumberUtils.toInt(portStr);
            // 2.2检查ip:port是否已经在instance_info表和instance_statistics中
            int count = instanceDao.getCountByIpAndPort(ip, port);
            if (count > 0) {
                return ImportAppResult.fail(appInstance + "中ip:port已经在instance_info存在");
            }
            InstanceStats instanceStats = instanceStatsDao.getInstanceStatsByHost(ip, port);
            if (instanceStats != null) {
                return ImportAppResult.fail(appInstance + "中ip:port已经在instance_statistics存在");
            }
            // 3.2检查Redis实例是否存活
            boolean isRun;
            if (StringUtils.isNotEmpty(password)) {
                // 外部导入密码以外部密码为主(cc内部采用一定规则加密)
                isRun = redisCenter.isRun(ip, port, password);
            } else {
                isRun = redisCenter.isRun(ip, port);
            }
            if (!isRun) {
                return ImportAppResult.fail(appInstance + "中的节点不是存活的");
            }

            //3.3判断sentinel模式下，masterName是否正确
            if (StringUtils.isNotEmpty(masterNameInput) && (type == ConstUtils.CACHE_REDIS_SENTINEL)) {
                String masterName = getSentinelMasterName(ip, port);
                if (StringUtils.isEmpty(masterName) || !masterNameInput.equals(masterName)) {
                    return ImportAppResult.fail(ip + ":" + port + ", masterName:" + masterName + "与所填"
                            + masterNameInput + "不一致");
                }
            }

        }
        return ImportAppResult.success();
    }


    @Override
    public boolean importAppAndInstance(AppDesc appDesc, String appInstanceInfo) {
        boolean isSuccess = true;
        try {
            // 1.1 保存应用信息
            appService.save(appDesc);
            long appId = appDesc.getAppId();
            // 1.2 更新appKey
            appService.updateAppKey(appId);

            int type = appDesc.getType();
            // 2.保存应用和用户的关系
            appService.saveAppToUser(appId, appDesc.getUserId());
            // 3.保存实例信息并开启统计
            String[] appInstanceDetails = appInstanceInfo.split("\n");
            // 4.检查实例信息格式是否正确
            for (String appInstance : appInstanceDetails) {
                String[] instanceItems = appInstance.split(":");
                String host = instanceItems[0];
                int port = NumberUtils.toInt(instanceItems[1]);

                String memoryOrMasterName = instanceItems[2];
                boolean isSentinelNode = NumberUtils.toInt(memoryOrMasterName) <= 0;
                if (isSentinelNode) {
                    saveInstance(appId, host, port, 0, ConstUtils.CACHE_REDIS_SENTINEL, memoryOrMasterName);
                } else {
                    if (ConstUtils.CACHE_REDIS_STANDALONE == type || ConstUtils.CACHE_REDIS_SENTINEL == type) {
                        saveInstance(appId, host, port, NumberUtils.toInt(memoryOrMasterName), ConstUtils.CACHE_REDIS_STANDALONE, "");
                    } else if (ConstUtils.CACHE_TYPE_REDIS_CLUSTER == type) {
                        saveInstance(appId, host, port, NumberUtils.toInt(memoryOrMasterName), ConstUtils.CACHE_TYPE_REDIS_CLUSTER, "");
                    }
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            isSuccess = false;
        }
        return isSuccess;
    }

    /**
     * 获取sentinel的masterName
     *
     * @param ip
     * @param port
     * @return
     */
    private String getSentinelMasterName(final String ip, final int port) {
        final StringBuilder masterName = new StringBuilder();
        new IdempotentConfirmer() {
            private int timeOutFactor = 1;

            @Override
            public boolean execute() {
                Jedis jedis = null;
                try {
                    // 预留
                    String password = null;
                    jedis = redisCenter.getJedis(ip, port, password);
                    jedis.getClient().setConnectionTimeout(Protocol.DEFAULT_TIMEOUT * (timeOutFactor++));
                    jedis.getClient().setSoTimeout(Protocol.DEFAULT_TIMEOUT * (timeOutFactor++));
                    List<Map<String, String>> mapList = jedis.sentinelMasters();
                    String targetKey = "name";
                    for (Map<String, String> map : mapList) {
                        if (map.containsKey(targetKey)) {
                            masterName.append(MapUtils.getString(map, targetKey, ""));
                        }
                    }
                    return true;
                } catch (Exception e) {
                    logger.warn("{}:{} error message is {} ", ip, port, e.getMessage());
                    return false;
                } finally {
                    if (jedis != null) {
                        jedis.close();
                    }
                }
            }
        }.run();
        return masterName.toString();
    }

    /**
     * 保存实例信息
     *
     * @param appId
     * @param host
     * @param port
     * @param maxMemory
     * @param type
     * @param cmd
     * @return
     */
    private InstanceInfo saveInstance(long appId, String host, int port, int maxMemory, int type,
                                      String cmd) {
        InstanceInfo instanceInfo = new InstanceInfo();
        instanceInfo.setAppId(appId);
        MachineInfo machineInfo = machineCenter.getMachineInfoByIp(host);
        instanceInfo.setHostId(machineInfo.getId());
        instanceInfo.setConn(0);
        instanceInfo.setMem(maxMemory);
        instanceInfo.setStatus(InstanceStatusEnum.GOOD_STATUS.getStatus());
        instanceInfo.setPort(port);
        instanceInfo.setType(type);
        instanceInfo.setCmd(cmd);
        instanceInfo.setIp(host);
        instanceDao.saveInstance(instanceInfo);
        return instanceInfo;
    }
}
