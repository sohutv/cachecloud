package com.sohu.cache.task.tasks;

import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import com.sohu.cache.constant.AppCheckEnum;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.task.BaseTask;
import com.sohu.cache.task.constant.TaskConstants;
import com.sohu.cache.task.constant.TaskStepFlowEnum.TaskFlowStatusEnum;
import com.sohu.cache.task.entity.RedisServerNode;
import com.sohu.cache.util.ConstUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Tuple;

import java.util.*;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * @author fulei
 */
@Component("AppKeyAnalysisTask")
@Scope(SCOPE_PROTOTYPE)
public class AppKeyAnalysisTask extends BaseTask {

    private long appId;

    private long auditId;

    private List<RedisServerNode> redisServerNodes;

    private final static int MAX_ANALYSIS_COUNT = 1;

    private final static long TYPE_SLEEP_BASE = 20000000;

    private final static long TTL_SLEEP_BASE = 20000000;

    private final static long IDLE_SLEEP_BASE = 20000000;

    private final static long BIGKEY_SLEEP_BASE = 20000000;

    private final static long VALUE_SIZE_SLEEP_BASE = 20000000;

    @Override
    public List<String> getTaskSteps() {
        List<String> taskStepList = new ArrayList<String>();
        taskStepList.add(TaskConstants.INIT_METHOD_KEY);
        // 1. 检查集群参数
        taskStepList.add("checkAppParam");
        // 2. redis server key type分析
        taskStepList.add("createRedisServerKeyTypeTask");
        taskStepList.add("waitRedisServerKeyTypeFinish");
        taskStepList.add("showKeyTypeResult");
        // 3. redis server big key分析
        taskStepList.add("createRedisServerBigKeyTask");
        taskStepList.add("waitRedisServerBigKeyFinish");
        // 4. redis server key ttl分析
        taskStepList.add("createRedisServerKeyTtlTask");
        taskStepList.add("waitRedisServerKeyTtlFinish");
        taskStepList.add("showKeyTtlResult");
        // 5. redis server idle key分析
        taskStepList.add("createRedisServerIdleKeyTask");
        taskStepList.add("waitRedisServerIdleKeyFinish");
        taskStepList.add("showIdleKeyResult");
        // 6. redis server key value分析
        taskStepList.add("createRedisServerKeyValueSizeTask");
        taskStepList.add("waitRedisServerKeyValueSizeFinish");
        taskStepList.add("showKeyValueSizeResult");
        // 7. 工单审批
        taskStepList.add("updateAudit");
        return taskStepList;
    }

    /**
     * 0.初始化参数
     *
     * @return
     */
    @Override
    public TaskFlowStatusEnum init() {
        super.init();

        appId = MapUtils.getLongValue(paramMap, TaskConstants.APPID_KEY);
        if (appId <= 0) {
            logger.error(marker, "task {} appId {} is wrong", taskId, appId);
            return TaskFlowStatusEnum.ABORT;
        }

        auditId = MapUtils.getLongValue(paramMap, TaskConstants.AUDIT_ID_KEY);
        if (auditId <= 0) {
            logger.error(marker, "task {} auditId {} is wrong", taskId, auditId);
            return TaskFlowStatusEnum.ABORT;
        }

        //redis server list
        String redisServerNodesStr = MapUtils.getString(paramMap, TaskConstants.REDIS_SERVER_NODES_KEY);
        if (StringUtils.isNotBlank(redisServerNodesStr)) {
            redisServerNodes = JSONArray.parseArray(redisServerNodesStr, RedisServerNode.class);
            if (CollectionUtils.isEmpty(redisServerNodes)) {
                logger.error(marker, "task {} redisServerNodes is empty", taskId);
                return TaskFlowStatusEnum.ABORT;
            }
            logger.info(marker, "user paramMap node: {}", redisServerNodesStr);
        }

        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 1.检查应用参数
     *
     * @return
     */
    public TaskFlowStatusEnum checkAppParam() {
        AppDesc appDesc = appDao.getAppDescById(appId);
        if (appDesc == null) {
            logger.error(marker, "appId {} appDesc is null", appId);
            return TaskFlowStatusEnum.ABORT;
        }
        if (!appDesc.isOnline()) {
            logger.error(marker, "appId {} is must be online, ", appId);
            return TaskFlowStatusEnum.ABORT;
        }
        //@TODO 其他比如考虑qps之类
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 6.1.创建分析闲置key子任务(master)
     */
    public TaskFlowStatusEnum createRedisServerIdleKeyTask() {
        redisServerNodes = buildRedisServerNodes();

        // 每个server的dbsize
        Map<String, Long> redisServerDbSizeMap = new HashMap<String, Long>();
        for (RedisServerNode redisServerNode : redisServerNodes) {
            String host = redisServerNode.getIp();
            int port = redisServerNode.getPort();
            long dbSize = redisCenter.getDbSize(appId, redisServerNode.getIp(), redisServerNode.getPort());
            logger.info(marker, "appId {} {}:{} dbSize is {} ", appId, host, port, dbSize);
            if (dbSize < 0) {
                return TaskFlowStatusEnum.ABORT;
            }
            redisServerDbSizeMap.put(host + ":" + port, dbSize);
        }

        long keyCounter = 0;
        int factor = 1;
        for (RedisServerNode redisServerNode : redisServerNodes) {
            //可能已经执行过
            if (redisServerNode.getTaskId() > 0) {
                continue;
            }
            long sleepCounter = factor * IDLE_SLEEP_BASE;

            String host = redisServerNode.getIp();
            int port = redisServerNode.getPort();

            try {
                long dbSize = redisServerDbSizeMap.get(host + ":" + port);
                keyCounter += dbSize;

                long childTaskId = taskService.addRedisServerIdleKeyAnalysisTask(appId, auditId, host, port, taskId);
                redisServerNode.setTaskId(childTaskId);
                paramMap.put(TaskConstants.REDIS_SERVER_NODES_KEY, redisServerNodes);
                logger.info(marker, "appId {} {}:{}  redis server idle key analysis task create successfully", appId,
                        host, port);

                // 超额就sleep
                if (keyCounter > sleepCounter) {
                    factor++;
                    sleepSeconds(300);
                }
            } catch (Exception e) {
                logger.error(marker, "appId {} {}:{}  redis server idle key analysis create fail", appId, host, port);
                logger.error(marker, e.getMessage(), e);
                return TaskFlowStatusEnum.ABORT;
            }

        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 6.2.等待分析闲置key子任务(master)
     */
    public TaskFlowStatusEnum waitRedisServerIdleKeyFinish() {
        for (RedisServerNode redisServerNode : redisServerNodes) {
            String host = redisServerNode.getIp();
            int port = redisServerNode.getPort();

            long childTaskId = redisServerNode.getTaskId();
            TaskFlowStatusEnum taskFlowStatusEnum = waitTaskFinish(childTaskId,
                    TaskConstants.REDIS_SERVER_IDLE_KEY_ANALYSIS_TIMEOUT);
            if (taskFlowStatusEnum.equals(TaskFlowStatusEnum.ABORT)) {
                logger.error(marker, "appId {} {}:{} redis server idle key analysis task execute fail", appId, host,
                        port);
                return TaskFlowStatusEnum.ABORT;
            } else {
                logger.info(marker, "appId {} {}:{} redis server idle key analysis task execute successfully", appId,
                        host, port);
            }
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 6.3.展示闲置key分析结果
     */
    public TaskFlowStatusEnum showIdleKeyResult() {
        String idleKeyResultKey = ConstUtils.getRedisServerIdleKey(appId, auditId);
        Set<Tuple> tuples = assistRedisService.zrangeWithScores(idleKeyResultKey, 0, -1);
        for (Tuple tuple : tuples) {
            String member = tuple.getElement();
            double score = tuple.getScore();
            logger.info(marker, "{} {} idle distri {} {}", idleKeyResultKey, appId, member, score);
            assistRedisService.zadd(idleKeyResultKey, (long) score, member);
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 2.1.创建分析key类型子任务
     */
    public TaskFlowStatusEnum createRedisServerKeyTypeTask() {
        redisServerNodes = buildRedisServerNodes();

        Map<String, Long> redisServerDbSizeMap = new HashMap<String, Long>();
        for (RedisServerNode redisServerNode : redisServerNodes) {
            String host = redisServerNode.getIp();
            int port = redisServerNode.getPort();
            long dbSize = redisCenter.getDbSize(appId, redisServerNode.getIp(), redisServerNode.getPort());
            logger.info(marker, "appId {} {}:{} dbSize is {} ", appId, host, port, dbSize);
            redisServerDbSizeMap.put(host + ":" + port, dbSize);
        }

        long keyCounter = 0;
        int factor = 1;
        for (RedisServerNode redisServerNode : redisServerNodes) {
            //可能已经执行过
            if (redisServerNode.getTaskId() > 0) {
                continue;
            }
            long sleepCounter = factor * TYPE_SLEEP_BASE;

            String host = redisServerNode.getIp();
            int port = redisServerNode.getPort();

            try {
                long dbSize = redisServerDbSizeMap.get(host + ":" + port);
                keyCounter += dbSize;

                long childTaskId = taskService.addRedisServerKeyTypeAnalysisTask(appId, auditId, host, port, taskId);
                redisServerNode.setTaskId(childTaskId);
                paramMap.put(TaskConstants.REDIS_SERVER_NODES_KEY, redisServerNodes);
                logger.info(marker, "appId {} {}:{}  redis server key type analysis task create successfully", appId,
                        host, port);

                // 超额就sleep
                if (keyCounter > sleepCounter) {
                    factor++;
                    sleepSeconds(120);
                }
            } catch (Exception e) {
                logger.error(marker, "appId {} {}:{}  redis server key type analysis create fail", appId, host, port);
                logger.error(marker, e.getMessage(), e);
                return TaskFlowStatusEnum.ABORT;
            }

        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 2.2.等待key类分析结束
     */
    public TaskFlowStatusEnum waitRedisServerKeyTypeFinish() {
        for (RedisServerNode redisServerNode : redisServerNodes) {
            String host = redisServerNode.getIp();
            int port = redisServerNode.getPort();

            long childTaskId = redisServerNode.getTaskId();
            TaskFlowStatusEnum taskFlowStatusEnum = waitTaskFinish(childTaskId,
                    TaskConstants.REDIS_SERVER_KEY_TYPE_ANALYSIS_TIMEOUT);
            if (taskFlowStatusEnum.equals(TaskFlowStatusEnum.ABORT)) {
                logger.error(marker, "appId {} {}:{} redis server key type analysis task execute fail", appId, host,
                        port);
                return TaskFlowStatusEnum.ABORT;
            } else {
                logger.info(marker, "appId {} {}:{} redis server key type analysis task execute successfully", appId,
                        host, port);
            }
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 2.3.展示key类分析结束
     */
    public TaskFlowStatusEnum showKeyTypeResult() {
        String keyTypeResultKey = ConstUtils.getRedisServerTypeKey(appId, auditId);
        Set<Tuple> tuples = assistRedisService.zrangeWithScores(keyTypeResultKey, 0, -1);
        for (Tuple tuple : tuples) {
            String member = tuple.getElement();
            double score = tuple.getScore();
            logger.info(marker, "{} {} type distri {} {}", keyTypeResultKey, appId, member, score);
            assistRedisService.zadd(keyTypeResultKey, (long) score, member);
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 5.1.创建分析key ttl子任务
     */
    public TaskFlowStatusEnum createRedisServerKeyTtlTask() {
        redisServerNodes = buildRedisServerNodes();

        Map<String, Long> redisServerDbSizeMap = new HashMap<String, Long>();
        for (RedisServerNode redisServerNode : redisServerNodes) {
            String host = redisServerNode.getIp();
            int port = redisServerNode.getPort();
            long dbSize = redisCenter.getDbSize(appId, redisServerNode.getIp(), redisServerNode.getPort());
            logger.info(marker, "appId {} {}:{} dbSize is {} ", appId, host, port, dbSize);
            redisServerDbSizeMap.put(host + ":" + port, dbSize);
        }

        long keyCounter = 0;
        int factor = 1;
        for (RedisServerNode redisServerNode : redisServerNodes) {
            //可能已经执行过
            if (redisServerNode.getTaskId() > 0) {
                continue;
            }
            long sleepCounter = factor * TTL_SLEEP_BASE;

            String host = redisServerNode.getIp();
            int port = redisServerNode.getPort();

            try {
                long dbSize = redisServerDbSizeMap.get(host + ":" + port);
                keyCounter += dbSize;

                long childTaskId = taskService.addRedisServerKeyTtlAnalysisTask(appId, auditId, host, port, taskId);
                redisServerNode.setTaskId(childTaskId);
                paramMap.put(TaskConstants.REDIS_SERVER_NODES_KEY, redisServerNodes);
                logger.info(marker, "appId {} {}:{}  redis server key ttl analysis task create successfully", appId,
                        host, port);

                // 超额就sleep
                if (keyCounter > sleepCounter) {
                    factor++;
                    sleepSeconds(120);
                }
            } catch (Exception e) {
                logger.error(marker, "appId {} {}:{}  redis server key ttl analysis create fail", appId, host, port);
                logger.error(marker, e.getMessage(), e);
                return TaskFlowStatusEnum.ABORT;
            }

        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 5.2.等待key ttl分析结束
     */
    public TaskFlowStatusEnum waitRedisServerKeyTtlFinish() {
        for (RedisServerNode redisServerNode : redisServerNodes) {
            String host = redisServerNode.getIp();
            int port = redisServerNode.getPort();

            long childTaskId = redisServerNode.getTaskId();
            TaskFlowStatusEnum taskFlowStatusEnum = waitTaskFinish(childTaskId,
                    TaskConstants.REDIS_SERVER_KEY_TTL_ANALYSIS_TIMEOUT);
            if (taskFlowStatusEnum.equals(TaskFlowStatusEnum.ABORT)) {
                logger.error(marker, "appId {} {}:{} redis server key ttl analysis task execute fail", appId, host,
                        port);
                return TaskFlowStatusEnum.ABORT;
            } else {
                logger.info(marker, "appId {} {}:{} redis server key ttl analysis task execute successfully", appId,
                        host, port);
            }
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 5.3.展示key ttl分析结束
     */
    public TaskFlowStatusEnum showKeyTtlResult() {
        String keyTtlResultKey = ConstUtils.getRedisServerTtlKey(appId, auditId);
        Set<Tuple> tuples = assistRedisService.zrangeWithScores(keyTtlResultKey, 0, -1);
        for (Tuple tuple : tuples) {
            String member = tuple.getElement();
            double score = tuple.getScore();
            logger.info(marker, "{} {} ttl distri {} {}", keyTtlResultKey, appId, member, score);
            assistRedisService.zadd(keyTtlResultKey, (long) score, member);
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 3.1.创建分析big key子任务
     */
    public TaskFlowStatusEnum createRedisServerBigKeyTask() {
        redisServerNodes = buildRedisServerNodes();

        // 每个server的dbsize
        Map<String, Long> redisServerDbSizeMap = new HashMap<String, Long>();
        for (RedisServerNode redisServerNode : redisServerNodes) {
            String host = redisServerNode.getIp();
            int port = redisServerNode.getPort();
            long dbSize = redisCenter.getDbSize(appId, redisServerNode.getIp(), redisServerNode.getPort());
            logger.info(marker, "appId {} {}:{} dbSize is {} ", appId, host, port, dbSize);
            redisServerDbSizeMap.put(host + ":" + port, dbSize);
        }

        long keyCounter = 0;
        int factor = 1;
        for (RedisServerNode redisServerNode : redisServerNodes) {
            //可能已经执行过
            if (redisServerNode.getTaskId() > 0) {
                continue;
            }
            long sleepCounter = factor * BIGKEY_SLEEP_BASE;

            String host = redisServerNode.getIp();
            int port = redisServerNode.getPort();

            try {
                long dbSize = redisServerDbSizeMap.get(host + ":" + port);
                keyCounter += dbSize;

                long childTaskId = taskService.addRedisServerBigKeyAnalysisTask(appId, auditId, host, port, taskId);
                redisServerNode.setTaskId(childTaskId);
                paramMap.put(TaskConstants.REDIS_SERVER_NODES_KEY, redisServerNodes);
                logger.info(marker, "appId {} {}:{}  redis server big key analysis task create successfully", appId,
                        host, port);

                // 超额就sleep
                if (keyCounter > sleepCounter) {
                    factor++;
                    sleepSeconds(120);
                }

            } catch (Exception e) {
                logger.error(marker, "appId {} {}:{}  redis server big key analysis create fail", appId, host, port);
                logger.error(marker, e.getMessage(), e);
                return TaskFlowStatusEnum.ABORT;
            }

        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 3.2.等待bigkey子任务完成
     *
     * @return
     */
    public TaskFlowStatusEnum waitRedisServerBigKeyFinish() {
        for (RedisServerNode redisServerNode : redisServerNodes) {
            String host = redisServerNode.getIp();
            int port = redisServerNode.getPort();

            long childTaskId = redisServerNode.getTaskId();
            TaskFlowStatusEnum taskFlowStatusEnum = waitTaskFinish(childTaskId,
                    TaskConstants.REDIS_SERVER_BIG_KEY_ANALYSIS_TIMEOUT);
            if (taskFlowStatusEnum.equals(TaskFlowStatusEnum.ABORT)) {
                logger.error(marker, "appId {} {}:{} redis server big key analysis task execute fail", appId, host,
                        port);
                return TaskFlowStatusEnum.ABORT;
            } else {
                logger.info(marker, "appId {} {}:{} redis server big key analysis task execute successfully", appId,
                        host, port);
            }
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 6.1.创建分析key value size子任务
     */
    public TaskFlowStatusEnum createRedisServerKeyValueSizeTask() {
        redisServerNodes = buildRedisServerNodes();

        Map<String, Long> redisServerDbSizeMap = new HashMap<String, Long>();
        for (RedisServerNode redisServerNode : redisServerNodes) {
            String host = redisServerNode.getIp();
            int port = redisServerNode.getPort();
            long dbSize = redisCenter.getDbSize(appId, redisServerNode.getIp(), redisServerNode.getPort());
            logger.info(marker, "appId {} {}:{} dbSize is {} ", appId, host, port, dbSize);
            redisServerDbSizeMap.put(host + ":" + port, dbSize);
        }

        long keyCounter = 0;
        int factor = 1;
        for (RedisServerNode redisServerNode : redisServerNodes) {
            //可能已经执行过
            if (redisServerNode.getTaskId() > 0) {
                continue;
            }
            long sleepCounter = factor * VALUE_SIZE_SLEEP_BASE;

            String host = redisServerNode.getIp();
            int port = redisServerNode.getPort();

            try {
                long dbSize = redisServerDbSizeMap.get(host + ":" + port);
                keyCounter += dbSize;

                long childTaskId = taskService.addRedisServerKeyValueAnalysisTask(appId, auditId, host, port, taskId);
                redisServerNode.setTaskId(childTaskId);
                paramMap.put(TaskConstants.REDIS_SERVER_NODES_KEY, redisServerNodes);
                logger.info(marker, "appId {} {}:{}  redis server key value size analysis task create successfully",
                        appId, host, port);

                // 超额就sleep
                if (keyCounter > sleepCounter) {
                    factor++;
                    sleepSeconds(120);
                }
            } catch (Exception e) {
                logger.error(marker, "appId {} {}:{}  redis server value size analysis create fail", appId, host, port);
                logger.error(marker, e.getMessage(), e);
                return TaskFlowStatusEnum.ABORT;
            }

        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 6.2.等待key value size分析结束
     */
    public TaskFlowStatusEnum waitRedisServerKeyValueSizeFinish() {
        for (RedisServerNode redisServerNode : redisServerNodes) {
            String host = redisServerNode.getIp();
            int port = redisServerNode.getPort();
            long childTaskId = redisServerNode.getTaskId();
            TaskFlowStatusEnum taskFlowStatusEnum = waitTaskFinish(childTaskId,
                    TaskConstants.REDIS_SERVER_KEY_VALUE_SIZE_ANALYSIS_TIMEOUT);
            if (taskFlowStatusEnum.equals(TaskFlowStatusEnum.ABORT)) {
                logger.error(marker, "appId {} {}:{} redis server key value size analysis task execute fail", appId,
                        host, port);
                return TaskFlowStatusEnum.ABORT;
            } else {
                logger.info(marker, "appId {} {}:{} redis server key value size analysis task execute successfully",
                        appId, host, port);
            }
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 6.3.展示key value size分析结束
     */
    public TaskFlowStatusEnum showKeyValueSizeResult() {
        String keyValueSizeResultKey = ConstUtils.getRedisServerValueSizeKey(appId, auditId);
        Set<Tuple> tuples = assistRedisService.zrangeWithScores(keyValueSizeResultKey, 0, -1);
        for (Tuple tuple : tuples) {
            String member = tuple.getElement();
            double score = tuple.getScore();
            logger.info(marker, "{} {} value size distri {} {}", keyValueSizeResultKey, appId, member, score);
            assistRedisService.zadd(keyValueSizeResultKey, (long) score, member);
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 7.通过初审：资源分配
     */
    public TaskFlowStatusEnum updateAudit() {
        try {
            AppDesc appDesc = appService.getByAppId(appId);
            appAuditDao.updateAppAudit(auditId, AppCheckEnum.APP_PASS.value());
            StringBuffer content = new StringBuffer();
            content.append(String.format("集群(%s-%s)的键值分析完成", appDesc.getAppId(), appDesc.getName()));
            //appWechatUtil.noticeAuditFinish(auditId, content.toString());
            return TaskFlowStatusEnum.SUCCESS;
        } catch (Exception e) {
            logger.error(marker, e.getMessage(), e);
            return TaskFlowStatusEnum.ABORT;
        }
    }

    private List<RedisServerNode> transformRedisServerFromInstance(List<InstanceInfo> instanceInfoList, int nodeCount) {
        List<RedisServerNode> redisServerNodes = new ArrayList<RedisServerNode>();
        for (int i = 0; i < instanceInfoList.size() && i < nodeCount; i++) {
            InstanceInfo instanceInfo = instanceInfoList.get(i);
            RedisServerNode redisServerNode = new RedisServerNode();
            redisServerNode.setIp(instanceInfo.getIp());
            redisServerNode.setPort(instanceInfo.getPort());
            redisServerNodes.add(redisServerNode);
        }
        return redisServerNodes;
    }

    private List<RedisServerNode> buildRedisServerNodes() {
        List<RedisServerNode> list = Lists.newArrayList();
        if (CollectionUtils.isEmpty(redisServerNodes)) {
            List<InstanceInfo> instanceInfoList = appService.getAppMasterInstanceInfoList(appId);
            redisServerNodes = transformRedisServerFromInstance(instanceInfoList, MAX_ANALYSIS_COUNT);
            list.addAll(redisServerNodes);
        } else {
            for (RedisServerNode redisServerNode : redisServerNodes) {
                list.add(new RedisServerNode(redisServerNode.getIp(), redisServerNode.getPort()));
            }
        }
        return list;
    }

}
