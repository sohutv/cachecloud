package com.sohu.cache.web.service.impl;

import com.sohu.cache.async.AsyncService;
import com.sohu.cache.async.KeyCallable;
import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.dao.InstanceStatsDao;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.exception.SSHException;
import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.redis.RedisDeployCenter;
import com.sohu.cache.ssh.SSHService;
import com.sohu.cache.stats.instance.InstanceDeployCenter;
import com.sohu.cache.task.constant.InstanceRoleEnum;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.web.service.AppService;
import com.sohu.cache.web.service.MigrateService;
import com.sohu.cache.web.vo.RedisClusterNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.HostAndPort;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author: zengyizhao
 * @CreateTime: 2023/2/28 15:33
 * @Description: 迁移服务
 * @Version: 1.0
 */
@Slf4j
@Service("migrateService")
public class MigrateServiceImpl implements MigrateService {

    @Autowired
    private RedisCenter redisCenter;

    @Autowired
    protected AppService appService;

    @Autowired
    private SSHService sshService;

    @Autowired
    private MachineCenter machineCenter;

    @Autowired
    private AsyncService asyncService;

    @Autowired
    private RedisDeployCenter redisDeployCenter;

    @Autowired
    private InstanceDao instanceDao;

    @Autowired
    private InstanceStatsDao instanceStatsDao;

    @Autowired
    private InstanceDeployCenter instanceDeployCenter;

    @Override
    public void forceMigrate(String sourceIp, String targetIp) throws SSHException {

        /**
         *  1. 检查目标容器的连通性
         *  2. 获取需要迁移的实例信息
         *  3. 遍历实例:(只对cluster实例迁移)
         *      3.1 standalone/sentinel节点跳过
         *      3.2 如果实例是master：添加新的从节点；获取master节点slave0；执行failover ；下线老节点
         *      3.3 如果是slave节点：添加新从节点；下线老的从节点
         *  4. 输出报告：下线节点数 ，迁移节点数
         */
        log.info("container forceMigrate start sourceIp:{} targetIp:{}", sourceIp, targetIp);
        //1.检查目标容器的连通性
        String execute = sshService.execute(targetIp, "echo ok");

        //2.获取机器所有实例
        List<InstanceInfo> instanceList = machineCenter.getMachineInstanceInfo(sourceIp);
        log.info("container forceMigrate instances:{} list size:{}", instanceList, instanceList.size());
        List<Object> resultList = null;
        // 4.遍历实例，按照appId分组
        if (!CollectionUtils.isEmpty(instanceList)) {
            //按照appId进行分组
            Map<Long, List<InstanceInfo>> appInstanceMap = new HashMap<>();
            instanceList.forEach(instanceInfo -> {
                if (instanceInfo.getType() != ConstUtils.CACHE_TYPE_REDIS_CLUSTER) {
                    return;
                }
                if (appInstanceMap.containsKey(instanceInfo.getAppId())) {
                    appInstanceMap.get(instanceInfo.getAppId()).add(instanceInfo);
                } else {
                    List<InstanceInfo> instanceInfoList = new ArrayList<>();
                    instanceInfoList.add(instanceInfo);
                    appInstanceMap.put(instanceInfo.getAppId(), instanceInfoList);
                }
            });

            Set<Long> appSet = appInstanceMap.keySet();
            String key = "force-migrate-instance-" + sourceIp + "-" + targetIp;
            List<Future> doForceMigrateResult = appSet.stream().map(appId -> {
                return asyncService.submitFutureWithRst(new KeyCallable<Boolean>(key + appId) {
                    public Boolean execute() {
                        try {
                            //一键强制迁移
                            return forceMigrateAppInstances(targetIp, appId, appInstanceMap.get(appId));
                        } catch (Exception e) {
                            log.error("doForceMigrateInstance ", e.getMessage(), e);
                            return false;
                        }
                    }
                });
            }).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(doForceMigrateResult)) {
                resultList = doForceMigrateResult.stream().map(future -> {
                    try {
                        return future.get();
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                    return false;
                }).collect(Collectors.toList());
            }
        }
        log.info("container forceMigrate end sourceIp:{} targetIp:{}, result:{}", sourceIp, targetIp, resultList);
    }

    public boolean forceMigrateAppInstances(String targetIp, Long appId, List<InstanceInfo> instanceList) throws SSHException {
        log.info("container forceMigrate AppInstances start appId:{} targetIp:{}, instance:{}", appId, targetIp, instanceList);
        List<InstanceInfo> failInstanceList = new ArrayList<>();
        instanceList = instanceList.stream()
                .filter(instanceInfo -> instanceInfo.getType() == ConstUtils.CACHE_TYPE_REDIS_CLUSTER)
                .collect(Collectors.toList());
        //先执行能正确获取角色的节点，如为执行成功，则添加到非正常节点中
        for (InstanceInfo instanceInfo : instanceList) {
            String role = redisCenter.getInstanceRole(instanceInfo.getAppId(), instanceInfo.getIp(), instanceInfo.getPort());
            log.info("instanceInfo:{} {} role:{} start migrate", instanceInfo.getIp(), instanceInfo.getPort(),role);
            if (instanceInfo.isOnline()) {
                // a)当前为master节点
                if (InstanceRoleEnum.MASTER.getInfo().equals(role)) {
                    if (!handleMasterMigrate(instanceInfo, targetIp)) {
                        failInstanceList.add(instanceInfo);
                    }
                }
                // b).当前为slave节点
                if (InstanceRoleEnum.SLAVE.getInfo().equals(role)) {
                    if (!handleSlaveMigrate(instanceInfo, targetIp)) {
                        failInstanceList.add(instanceInfo);
                    }
                }
            } else {
                failInstanceList.add(instanceInfo);
            }
        }
        log.info("container forceMigrate AppInstances appId:{} targetIp:{}, need to retry instances:{}", appId, targetIp, failInstanceList);
        //强制迁移非正常节点
        int retryTimes = 3;
        while (failInstanceList.size() > 0 && retryTimes > 0) {
            //获取拓扑信息
            Map<String, RedisClusterNode> appClusterNode = this.getAppClusterNode(appId);
            //每个实例传入拓扑信息进行处理
            for (int i = 0; i < failInstanceList.size(); ) {
                InstanceInfo instanceInfo = failInstanceList.get(i);
                RedisClusterNode clusterNode = appClusterNode.get(instanceInfo.getHostPort());
                if (clusterNode != null) {
                    boolean handlerResult = false;
                    if ("master".equals(clusterNode.getRole())) {
                        try {
                            Optional<RedisClusterNode> slaveNodeOptional = appClusterNode.values().stream()
                                    .filter(node -> clusterNode.getHostPort().equals(node.getMasterHostPort()) && node.isConnected() && !node.isFail())
                                    .findFirst();
                            if (slaveNodeOptional.isPresent()) {
                                String hostPort = slaveNodeOptional.get().getHostPort();
                                String[] hostPortArray = hostPort.split(":");
                                InstanceInfo slaveInstance = instanceDao.getInstByIpAndPort(hostPortArray[0], Integer.valueOf(hostPortArray[1]));
                                handlerResult = handleUnknownInstanceMigrate(instanceInfo, slaveInstance, true, targetIp, true);
                            }
                        } catch (Exception e) {
                            log.error("container forceMigrate AppInstances appId:{} targetIp:{}, forceMigrate master fail instance:{}, error: {}", appId, targetIp, instanceInfo, e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            Optional<RedisClusterNode> masterNodeOptional = appClusterNode.values().stream()
                                    .filter(node -> clusterNode.getMasterHostPort().equals(node.getHostPort()) && node.isConnected() && !node.isFail())
                                    .findFirst();
                            if (masterNodeOptional.isPresent()) {
                                String hostPort = masterNodeOptional.get().getHostPort();
                                Set<RedisClusterNode> slaves = appClusterNode.values().stream()
                                        .filter(node -> hostPort.equals(node.getMasterHostPort())
                                                && !node.getHostPort().equals(clusterNode.getHostPort()))
                                        .collect(Collectors.toSet());
                                boolean addSlave = true;
                                if (CollectionUtils.isNotEmpty(slaves)) {
                                    addSlave = false;
                                }
                                String[] hostPortArray = hostPort.split(":");
                                InstanceInfo masterInstance = instanceDao.getInstByIpAndPort(hostPortArray[0], Integer.valueOf(hostPortArray[1]));
                                handlerResult = handleUnknownInstanceMigrate(masterInstance, instanceInfo, false, targetIp, addSlave);
                            }
                        } catch (Exception e) {
                            log.error("container forceMigrate AppInstances appId:{} targetIp:{}, forceMigrate slave fail instance:{}, error: {}", appId, targetIp, instanceInfo, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    if (handlerResult) {
                        failInstanceList.remove(i);
                    } else {
                        i++;
                    }
                }
            }
            log.warn("container forceMigrate AppInstances appId:{} targetIp:{}, after one more retry, fail instance left:{}", appId, targetIp, failInstanceList);
            retryTimes--;
        }
        if (CollectionUtils.isNotEmpty(failInstanceList)) {
            log.warn("container forceMigrate AppInstances appId:{} targetIp:{}, forceMigrate fail instance:{}", appId, targetIp, failInstanceList);
            return false;
        }
        return true;
    }

    private boolean handleMasterMigrate(InstanceInfo instanceInfo, String targetIp) {
        try {
            //1)获取master节点slave0
            AppDesc appdesc = appService.getByAppId(instanceInfo.getAppId());
//            HostAndPort slave0 = redisCenter.getSlave0(instanceInfo.getIp(), instanceInfo.getPort(), appdesc.getAppPassword());
            HostAndPort slave0 = null;
            if (instanceInfo.getType() == ConstUtils.CACHE_TYPE_REDIS_CLUSTER) {
                slave0 = redisCenter.getSlave0(instanceInfo.getIp(), instanceInfo.getPort(), appdesc.getAppPassword());
            }
            //2)执行failover force
            if (slave0 == null) {
                return false;
            }
            InstanceInfo masterInst = instanceDao.getInstByIpAndPort(slave0.getHost(), slave0.getPort());
            boolean checkFailover = this.failoverAndCheck(masterInst);
            if (!checkFailover) {
                // 如果failover失败 ，则不下线源节点，继续轮训下个节点
                return false;
            }
            TimeUnit.SECONDS.sleep(5);
            //3) 下线节点
            boolean isOffline = instanceDeployCenter.shutdownExistInstance(instanceInfo.getAppId(), instanceInfo.getId());
            log.info("forceMigrate handleMasterMigrate appid:{} offline node master:{} to {}, result :{}", instanceInfo.getAppId(), instanceInfo.getHostPort(), targetIp, isOffline);
            //4) 添加新的从节点
            Boolean isSuccess = false;
            if (instanceInfo.getType() == ConstUtils.CACHE_TYPE_REDIS_CLUSTER) {
                isSuccess = redisDeployCenter.addSlave(instanceInfo.getAppId(), masterInst.getId(), targetIp);
            }
//            Boolean isSuccess = redisDeployCenter.addSlave(instanceInfo.getAppId(), masterInst.getId(), targetIp);
            log.info("forceMigrate handleMasterMigrate appid:{} offline node master:{} result:{}， add new slave :{} result:{}", instanceInfo.getAppId(), instanceInfo.getHostPort(), isOffline, targetIp, isSuccess);
            if (isSuccess) {
                return true;
            } else {
                // 添加从节点 失败，则退出 检查原因
                log.error("forceMigrate handleMasterMigrate add slave {}:{} fail", instanceInfo.getAppId(), masterInst.getId());
            }
        } catch (Exception e) {
            log.error("container forceMigrate handleMasterMigrate targetIp:{}, forceMigrate fail instance:{}, error: {}", targetIp, instanceInfo, e.getMessage());
        }
        return false;
    }

    private boolean handleSlaveMigrate(InstanceInfo instanceInfo, String targetIp) {
        AppDesc appdesc = appService.getByAppId(instanceInfo.getAppId());
        try {
            HostAndPort masterInfo = redisCenter.getMaster(instanceInfo.getIp(), instanceInfo.getPort(), appdesc.getAppPassword());
            if (masterInfo == null) {
                return false;
            }
            if (StringUtils.isNotEmpty(masterInfo.getHost()) && masterInfo.getPort() > 0) {
                InstanceInfo masterInst = instanceDao.getInstByIpAndPort(masterInfo.getHost(), masterInfo.getPort());
                if (masterInst == null) {
                    return false;
                }
                //下线当前slave节点
                boolean isOffline = instanceDeployCenter.shutdownExistInstance(instanceInfo.getAppId(), instanceInfo.getId());
                log.info("forceMigrate handleSlaveMigrate appid:{} offline slave:{}, result :{}", instanceInfo.getAppId(), instanceInfo.getHostPort(), isOffline);
                //添加新的slave节点
                Boolean isSuccess = false;
                if (instanceInfo.getType() == ConstUtils.CACHE_TYPE_REDIS_CLUSTER) {
                    isSuccess = redisDeployCenter.addSlave(instanceInfo.getAppId(), masterInst.getId(), targetIp);
                }
                //boolean isSuccess = redisDeployCenter.addSlave(instanceInfo.getAppId(), masterInst.getId(), targetIp);
                // sleep 5s
                TimeUnit.SECONDS.sleep(5);
                log.info("forceMigrate handleSlaveMigrate appid:{} offline slave:{} result:{},add slave:{} result:{}", instanceInfo.getAppId(), instanceInfo.getHostPort(), isOffline, targetIp, isSuccess);
                if (isSuccess) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("container forceMigrate handleSlaveMigrate targetIp:{}, forceMigrate fail instance:{}, error: {}", targetIp, instanceInfo, e.getMessage());
        }
        return false;
    }

    private Map<String, RedisClusterNode> getAppClusterNode(long appId) {
        List<InstanceInfo> appInstances = appService.getAppBasicInstanceInfo(appId);
        Set<String> hostPortSet = appInstances.stream().filter(instanceInfo -> !instanceInfo.isOffline())
                .map(instanceInfo -> instanceInfo.getHostPort()).collect(Collectors.toSet());
        appInstances = appInstances.stream()
                .filter(instanceInfo -> instanceInfo.isOnline()).collect(Collectors.toList());
        Map<String, RedisClusterNode> clusterNode = new HashMap<>();
        for (int i = 0; i < appInstances.size(); i++) {
            clusterNode = this.getClusterNodeByInstance(hostPortSet, appInstances.get(i));
            if (MapUtils.isNotEmpty(clusterNode)) {
                break;
            }
        }
        return clusterNode;
    }

    private Map<String, RedisClusterNode> getClusterNodeByInstance(Set<String> hostPortSet, InstanceInfo instanceInfo) {
        try {
            String clusterNodes = redisCenter.getClusterNodes(instanceInfo.getAppId(), instanceInfo.getIp(), instanceInfo.getPort());
            if (StringUtils.isBlank(clusterNodes)) {
                return null;
            }
            final Map<String, RedisClusterNode> nodeMap = new HashMap<>();
            hostPortSet.forEach(hostPort -> nodeMap.put(hostPort, null));
            String[] clusterNodeArray = clusterNodes.split("\n");
            for (int i = 0; i < clusterNodeArray.length; i++) {
                final String nodeInfo = clusterNodeArray[i];
                Optional<String> optional = hostPortSet.stream().filter(hostPort -> nodeInfo.contains(hostPort)).findFirst();
                if (!optional.isPresent()) {
                    continue;
                }
                if (StringUtils.isNotBlank(nodeInfo)) {
                    String[] nodeInfoPartArray = nodeInfo.split(" ");
                    RedisClusterNode clusterNode = new RedisClusterNode();
                    clusterNode.setHostPort(optional.get());
                    if (nodeInfo.contains("connected")) {
                        clusterNode.setConnected(true);
                    }
                    if (nodeInfo.contains("fail")) {
                        clusterNode.setFail(true);
                    }
                    if (nodeInfo.contains("master")) {
                        clusterNode.setRole("master");
                    }
                    if (nodeInfo.contains("slave")) {
                        clusterNode.setRole("slave");
                        String masterId = nodeInfoPartArray[3];
                        for (String clusterNodeStr : clusterNodeArray) {
                            if (clusterNodeStr.contains("master") && clusterNodeStr.contains(masterId)) {
                                String[] masterNodeInfoPartArray = clusterNodeStr.split(" ");
                                String masterHostPort = masterNodeInfoPartArray[1];
                                if (masterHostPort.contains("@")) {
                                    masterHostPort = masterHostPort.substring(0, masterHostPort.indexOf("@"));
                                }
                                clusterNode.setMasterHostPort(masterHostPort);
                            }
                        }
                    }
                    nodeMap.put(optional.get(), clusterNode);
                }
            }
            return nodeMap;
        } catch (Exception e) {
            log.error("forceMigrate getClusterNodeByInstance instance:{}, error:{}", instanceInfo, e.getMessage());
        }
        return null;
    }

    private boolean handleUnknownInstanceMigrate(InstanceInfo masterInstance, InstanceInfo slaveInstance, boolean currentIsMaster, String targetIp, boolean addSlave) {
        try {
            if (currentIsMaster) {
                //1) failover
                boolean failoverRst = this.failoverAndCheck(slaveInstance);
                if (!failoverRst) {
                    return false;
                }
                TimeUnit.SECONDS.sleep(5);
                //2) 下线节点
                boolean isOffline = instanceDeployCenter.shutdownExistInstance(masterInstance.getAppId(), masterInstance.getId());
                log.info("forceMigrate handleUnknownInstanceMigrate appid:{} offline node master:{} to {}, result :{}", masterInstance.getAppId(), masterInstance.getHostPort(), targetIp, isOffline);
                //3) 添加新的从节点
                Boolean isSuccess = redisDeployCenter.addSlave(slaveInstance.getAppId(), slaveInstance.getId(), targetIp);
                log.info("forceMigrate handleUnknownInstanceMigrate appid:{} offline node master:{} result:{}, add new slave:{} result:{}", masterInstance.getAppId(), masterInstance.getHostPort(), isOffline, targetIp, isSuccess);
                if (isSuccess) {
                    return true;
                } else {
                    // 添加从节点 失败，则退出 检查原因
                    log.error("forceMigrate handleUnknownInstanceMigrate add slave {}:{} fail", slaveInstance.getAppId(), slaveInstance.getId());
                }
            } else {
                //1) 下线节点
                boolean isOffline = instanceDeployCenter.shutdownExistInstance(slaveInstance.getAppId(), slaveInstance.getId());
                log.info("forceMigrate handleUnknownInstanceMigrate appid:{} offline node slave:{}, result :{}", slaveInstance.getAppId(), slaveInstance.getHostPort(), isOffline);
                //2) 添加新的从节点
                Boolean isSuccess = false;
                if (addSlave) {
                    isSuccess = redisDeployCenter.addSlave(masterInstance.getAppId(), masterInstance.getId(), targetIp);
                }
                log.info("forceMigrate handleUnknownInstanceMigrate appid:{} offline node slave:{} result:{}， addSlave Flag:{}, add new slave:{} result:{}",
                        masterInstance.getAppId(), masterInstance.getHostPort(), isOffline, addSlave, targetIp, isSuccess);
                if ((addSlave && isSuccess) || !addSlave) {
                    return true;
                } else {
                    // 添加从节点失败，则退出 检查原因
                    log.error("forceMigrate handleUnknownInstanceMigrate add slave {}:{} fail", masterInstance.getAppId(), masterInstance.getId());
                }
            }
        } catch (Exception e) {
            log.error("forceMigrate handleUnknownInstanceMigrate appid:{} master node:{}， slave node:{}, currentIsMaster:{}, targetIp:{}, error:{}",
                    masterInstance.getAppId(), masterInstance.getHostPort(), slaveInstance.getHostPort(), currentIsMaster, targetIp, e.getMessage());
        }
        return false;
    }

    private boolean failoverAndCheck(InstanceInfo slaveInstance) {
        try {

            boolean isFailover = redisDeployCenter.clusterFailover(slaveInstance.getAppId(), slaveInstance.getId(), "force");
            int times = 0;
            boolean checkFailover = false;
            while (!checkFailover && times++ <= 5) {
                Boolean status = false;
//                Boolean status = redisCenter.getRedisFailoverForceStatus(slaveInstance.getAppId(), slaveInstance.getIp(), slaveInstance.getPort());
                if (slaveInstance.getType() == ConstUtils.CACHE_TYPE_REDIS_CLUSTER) {
                    status = redisCenter.getRedisReplicationStatus(slaveInstance.getAppId(), slaveInstance.getIp(), slaveInstance.getPort());
                }
                if (status) {
                    checkFailover = status;
                } else {
                    TimeUnit.SECONDS.sleep(6);
                    log.info(" check slave replication status ,waiting 6s ....");
                }
            }
            if (!checkFailover) {
                // 如果failover失败 ，则不下线源节点，继续轮训下个节点
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("forceMigrate failoverAndCheck appId:{}, instance:{}, error: {}", slaveInstance.getAppId(), slaveInstance.getHostPort(), e.getMessage());
        }
        return false;
    }

}
