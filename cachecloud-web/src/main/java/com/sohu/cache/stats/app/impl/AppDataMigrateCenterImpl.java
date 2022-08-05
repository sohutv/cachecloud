package com.sohu.cache.stats.app.impl;

import com.sohu.cache.constant.AppDataMigrateStatusEnum;
import com.sohu.cache.constant.MachineInfoEnum;
import com.sohu.cache.constant.RedisMigrateToolConstant;
import com.sohu.cache.constant.RedisShakeEnum;
import com.sohu.cache.dao.AppDataMigrateStatusDao;
import com.sohu.cache.entity.AppDataMigrateSearch;
import com.sohu.cache.entity.AppDataMigrateStatus;
import com.sohu.cache.entity.MachineInfo;
import com.sohu.cache.exception.SSHException;
import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.ssh.SSHUtil;
import com.sohu.cache.stats.app.AppDataMigrateCenter;
import com.sohu.cache.util.ConstUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by rucao on 2019/10/24[]
 */
@Slf4j
@Service
public class AppDataMigrateCenterImpl implements AppDataMigrateCenter {
    @Autowired
    private AppDataMigrateStatusDao appDataMigrateStatusDao;
    @Autowired
    private MachineCenter machineCenter;

    @Override
    public String showDataMigrateLog(long id, int pageSize) {
        AppDataMigrateStatus appDataMigrateStatus = appDataMigrateStatusDao.get(id);
        if (appDataMigrateStatus == null) {
            return "";
        }

        try {
            int migrateStatus = appDataMigrateStatus.getStatus();
            String logPath = appDataMigrateStatus.getLogPath();
            String host = appDataMigrateStatus.getMigrateMachineIp();

            MachineInfo machineInfo = machineCenter.getMachineInfoByIp(host);
            if (machineInfo != null && machineInfo.getAvailable() == MachineInfoEnum.AvailableEnum.YES.getValue()) {
                StringBuilder command = new StringBuilder();
                command.append("tail -n").append(pageSize).append(" ").append(logPath);

                String result = SSHUtil.execute(host, command.toString());
                int logStatus = migrateStatus;
                if (StringUtils.isNotEmpty(result) && result.contains(RedisShakeEnum.LOG_ERROR.getKeyword())) {
                    logStatus = AppDataMigrateStatusEnum.ERROR.getStatus();
                } else if (StringUtils.isNotEmpty(result) && (result.contains(RedisShakeEnum.LOG_SYNC_RDB_DONE.getKeyword()) || result.contains(RedisShakeEnum.LOG_FORWARD_COMMANDS.getKeyword()))) {
                    logStatus = AppDataMigrateStatusEnum.FULL_END.getStatus();
                } else if (StringUtils.isNotEmpty(result) && result.contains(RedisShakeEnum.LOG_SYNCING.getKeyword())) {
                    logStatus = AppDataMigrateStatusEnum.START.getStatus();
                } else if (StringUtils.isNotEmpty(result) && result.contains(RedisShakeEnum.LOG_WAITING_SOURCE_RDB.getKeyword())) {
                    logStatus = AppDataMigrateStatusEnum.PREPARE.getStatus();
                }
                if (migrateStatus != logStatus) {
                    appDataMigrateStatusDao.updateStatus(id, logStatus);
                }
                return result;
            } else {
                log.warn("machine ip:{} is offline", host);
                return "";
            }
        } catch (SSHException e) {
            log.error(e.getMessage(), e);
            return "";
        }
    }

    @Override
    public String showCheckDataLog(long id, int pageSize) {
        AppDataMigrateStatus appDataMigrateStatus = appDataMigrateStatusDao.get(id);
        if (appDataMigrateStatus == null) {
            return "";
        }
        String migrateId = appDataMigrateStatus.getMigrateId();
        String logPath = ConstUtils.getRedisFullCheckResultDir() + "log-" + migrateId + ".log";
        String host = appDataMigrateStatus.getMigrateMachineIp();
        StringBuilder command = new StringBuilder();
        command.append("tail -n").append(pageSize).append(" ").append(logPath);
        try {
            return SSHUtil.execute(host, command.toString());
        } catch (SSHException e) {
            log.error(e.getMessage(), e);
            return "";
        }
    }

    @Override
    public String showDataMigrateConf(long id) {
        AppDataMigrateStatus appDataMigrateStatus = appDataMigrateStatusDao.get(id);
        if (appDataMigrateStatus == null) {
            return "";
        }
        String configPath = appDataMigrateStatus.getConfigPath();
        String host = appDataMigrateStatus.getMigrateMachineIp();
        String command = "cat " + configPath;
        try {
            return SSHUtil.execute(host, command);
        } catch (SSHException e) {
            log.error(e.getMessage(), e);
            return "";
        }
    }

    @Override
    public Map<RedisMigrateToolConstant, Map<String, Object>> showMiragteToolProcess(long id) {
        AppDataMigrateStatus appDataMigrateStatus = appDataMigrateStatusDao.get(id);
        if (appDataMigrateStatus == null) {
            return Collections.emptyMap();
        }
        String info = "";
        String host = appDataMigrateStatus.getMigrateMachineIp();
        int port = appDataMigrateStatus.getMigrateMachinePort();
        Jedis jedis = null;
        try {
            jedis = new Jedis(host, port, 5000);
            info = jedis.info();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        if (StringUtils.isBlank(info)) {
            return Collections.emptyMap();
        }
        return processRedisMigrateToolStats(info);
    }

    @Override
    public List<AppDataMigrateStatus> search(AppDataMigrateSearch appDataMigrateSearch) {
        try {
//            List<Long> onMigrateIds = appDataMigrateStatusDao.getAllOnMigrateId();
//            onMigrateIds.parallelStream().map(migrateId -> showDataMigrateLog(migrateId, 100)).collect(Collectors.toList());
            return appDataMigrateStatusDao.search(appDataMigrateSearch);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public int getMigrateTaskCount(AppDataMigrateSearch appDataMigrateSearch) {
        try {
            return appDataMigrateStatusDao.getMigrateTaskCount(appDataMigrateSearch);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 处理迁移工具状态
     *
     * @param statResult
     * @return
     */
    private Map<RedisMigrateToolConstant, Map<String, Object>> processRedisMigrateToolStats(String statResult) {
        Map<RedisMigrateToolConstant, Map<String, Object>> redisStatMap = new HashMap<RedisMigrateToolConstant, Map<String, Object>>();
        String[] data = statResult.split("\r\n");
        String key;
        int i = 0;
        int length = data.length;
        while (i < length) {
            if (data[i].contains("#")) {
                int index = data[i].indexOf('#');
                key = data[i].substring(index + 1);
                ++i;
                RedisMigrateToolConstant redisMigrateToolConstant = RedisMigrateToolConstant.value(key.trim());
                if (redisMigrateToolConstant == null) {
                    continue;
                }
                Map<String, Object> sectionMap = new LinkedHashMap<String, Object>();
                while (i < length && data[i].contains(":")) {
                    String[] pair = data[i].split(":");
                    sectionMap.put(pair[0], pair[1]);
                    i++;
                }
                redisStatMap.put(redisMigrateToolConstant, sectionMap);
            } else {
                i++;
            }
        }
        return redisStatMap;
    }
}
