package com.sohu.cache.web.service.impl;

import com.sohu.cache.dao.InstanceDao;
import com.sohu.cache.dao.MachineRoomDao;
import com.sohu.cache.entity.InstanceInfo;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.task.BaseTask;
import com.sohu.cache.task.constant.InstanceRoleEnum;
import com.sohu.cache.task.entity.NutCrackerNode;
import com.sohu.cache.task.entity.RedisSentinelNode;
import com.sohu.cache.task.entity.RedisServerNode;
import com.sohu.cache.task.util.AppWechatUtil;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.web.service.InstancePortService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author fulei
 * @date 2018年7月4日
 * @time 下午3:29:10
 */
@Service
public class InstancePortServiceImpl implements InstancePortService {

    private Logger logger = LoggerFactory.getLogger(InstancePortServiceImpl.class);

	/*@Value("${appEnvName}")
	private String appEnvName;*/

    @Autowired
    private AppWechatUtil appWechatUtil;

    /**
     * 机房dao
     */
    @Autowired
    private MachineRoomDao machineRoomDao;

    /**
     * 实例dao
     */
    @Autowired
    private InstanceDao instanceDao;

    /**
     * redis-port实例dao
     */
//	private RedisPortInstanceDao redisPortInstanceDao;

    /**
     * redis-migrate-tool实例dao
     */
//	private RedisMigrateToolInstanceDao redisMigrateToolInstanceDao;

	/**
	 * redis相关
	 */
	@Autowired
    @Lazy
	private RedisCenter redisCenter;
	
	
	/**
	 * 1. 主从不同机器
	 * 2. 端口从起始端口开始自增1
	 */
	@Override
	public List<RedisServerNode> generateRedisServerNodeList(long appId, List<String> appDeployInfoList,
															 int masterPerMachine, int maxMemory) {

        // 最终结果
        List<RedisServerNode> redisServerNodeList = new ArrayList<RedisServerNode>();
        // 起始节点
        int masterPort = (int) (ConstUtils.REDIS_SERVER_BASE_PORT + (appId % 10) * 10);

        // 记录本次每个机器分配的port
        Map<String, Set<Integer>> ipPortSetMap = new HashMap<String, Set<Integer>>();
        for (int i=0; i < appDeployInfoList.size(); i++) {
            String masterAndSlaveHost = appDeployInfoList.get(i);
            String[] hostInfoArray = masterAndSlaveHost.split(":"); 
            String masterHost = hostInfoArray[0];
            maxMemory = Integer.parseInt(hostInfoArray[1]);
            while (checkHostPortExist(ipPortSetMap, masterHost, masterPort)) {
                    masterPort++;
                }
            redisServerNodeList.add(new RedisServerNode(masterHost, masterPort, InstanceRoleEnum.MASTER.getRole(),
                        maxMemory, "", 0));   
           
            if (hostInfoArray.length >= 2) {
                String slaveHost = hostInfoArray[2];
                int slavePort = masterPort + ConstUtils.SLAVE_PORT_INCREASE;
                while (checkHostPortExist(ipPortSetMap, slaveHost, slavePort)) {
                    slavePort++;
                }
                redisServerNodeList.add(new RedisServerNode(slaveHost, slavePort, InstanceRoleEnum.SLAVE.getRole(),
                      maxMemory, masterHost, masterPort));            
            }
            masterPort += 1;
        }
        /*
        for (int i = 0; i < redisServerMachineList.size(); i++) {

            String masterHost = redisServerMachineList.get(i);
            String slaveHost = redisServerMachineList.get((i + 1) % redisServerMachineList.size());

            for (int j = 0; j < masterPerMachine; j++) {
                // master node
                //如果端口存在就自增
                while (checkHostPortExist(ipPortSetMap, masterHost, masterPort)) {
                    masterPort++;
                }
                redisServerNodeList.add(new RedisServerNode(masterHost, masterPort, InstanceRoleEnum.MASTER.getRole(),
                        maxMemory, "", 0));

                // slave node
                int slavePort = masterPort + ConstUtils.SLAVE_PORT_INCREASE;
                //如果端口存在就自增
                while (checkHostPortExist(ipPortSetMap, slaveHost, slavePort)) {
                    slavePort++;
                }
                redisServerNodeList.add(new RedisServerNode(slaveHost, slavePort, InstanceRoleEnum.SLAVE.getRole(),
                        maxMemory, masterHost, masterPort));

                masterPort += 1;
            }
        }
        */
        return redisServerNodeList;
    }

    @Override
    public List<RedisSentinelNode> generateRedisSentinelNodeList(long appId, List<String> redisSentinelMachineList,
                                                                 int sentinelPerMachine) {
        // 最终结果
        List<RedisSentinelNode> redisSentinelNodeList = new ArrayList<RedisSentinelNode>();
        // 起始节点,appid 10000起步 @TODO
        int port = (int) (ConstUtils.REDIS_SENTINEL_BASE_PORT + (appId % 100) * 10);
        for (String ip : redisSentinelMachineList) {
            for (int j = 0; j < sentinelPerMachine; j++) {
                //如果端口存在就自增
                while (checkHostPortExist(ip, port)) {
                    port++;
                }
                redisSentinelNodeList.add(new RedisSentinelNode(ip, port));
                port++;
            }
        }
        return redisSentinelNodeList;
    }

	/*@Override
    public List<PikaNode> generatePikaNodeList(long appId, List<String> pikaMachineList, int masterPerMachine) {
		// 最终结果
		List<PikaNode> pikaNodeList = new ArrayList<PikaNode>();
		// 起始节点
		int masterPort = (int) (ConstUtils.PIKA_BASE_PORT + (appId % 10) * 10);
		// 记录本次每个机器分配的port
		Map<String, Set<Integer>> ipPortSetMap = new HashMap<String, Set<Integer>>();
		// 如果是一对，那只只要主从
		int machineLength = pikaMachineList.size() == 2 ? 1 : pikaMachineList.size();

		for (int i = 0; i < machineLength; i++) {

			String masterHost = pikaMachineList.get(i);
			String slaveHost = pikaMachineList.get((i + 1) % pikaMachineList.size());

			for (int j = 0; j < masterPerMachine; j++) {
				// master node
				while (checkHostPortExist(ipPortSetMap, masterHost, masterPort)) {
					masterPort++;
				}
				pikaNodeList.add(new PikaNode(masterHost, masterPort, InstanceRoleEnum.MASTER.getRole(), "", 0));

				// slave node
				int slavePort = masterPort + ConstUtils.PIKA_SLAVE_PORT_INCREASE;
				while (checkHostPortExist(ipPortSetMap, slaveHost, slavePort)) {
					slavePort++;
				}
				pikaNodeList.add(new PikaNode(slaveHost, slavePort, InstanceRoleEnum.SLAVE.getRole(), masterHost, masterPort));

				masterPort += 1;
			}
		}

		return pikaNodeList;
	}*/

    /**
     * 每台端口从baseport开始自增1
     */
    @Override
    public List<NutCrackerNode> generateNutCrackerNodeList(long appId, List<String> nutCrackerMachineList,
                                                           int nutCrackerPerMachine) {
//		// 最终结果
//		List<NutCrackerNode> nutCrackerNodeList = new ArrayList<NutCrackerNode>();
//		// 起始节点
//		int port =(int) (ConstUtils.NUT_CRACKER_BASE_PORT + (appId % 10) * 10);
//		for (String ip : nutCrackerMachineList) {
//			int initPort = port;
//			for (int j = 0; j < nutCrackerPerMachine; j++) {
//				//如果端口存在就自增
//				while (checkNutCrackerHostPortExist(ip, initPort)) {
//					initPort++;
//				}
//				nutCrackerNodeList.add(new NutCrackerNode(ip, initPort));
//				initPort++;
//			}
//		}
//		return nutCrackerNodeList;
        // @todo fulei
        return null;
    }
	
	/*@Override
	public List<CodisProxyNode> generateCodisProxyList(long appId, List<String> codisProxyMachineList,
			int codisProxyPerMachine) {
		// 最终结果
		List<CodisProxyNode> codisProxyNodeList = new ArrayList<CodisProxyNode>();
		// 起始节点
		int port;
		if (appEnvName.equals(AppEnvNameEnum.afun.getName())) {
			port = ConstUtils.CODIS_PROXY_BASE_PORT;
		} else {
			port = (int) (ConstUtils.CODIS_PROXY_BASE_PORT + (appId % 10) * 10);
		}
		for (String ip : codisProxyMachineList) {
			int initPort = port;
			for (int j = 0; j < codisProxyPerMachine; j++) {
				//如果端口存在就自增
				while (checkCodisProxyHostPortExist(ip, initPort)) {
					initPort++;
				}
				codisProxyNodeList.add(new CodisProxyNode(ip, initPort));
				initPort++;
			}
		}
		return codisProxyNodeList;
	}*/

	/*@Override
	public List<CodisDashboardNode> generateCodisDashboardList(long appId, List<String> codisDashboardMachineList) {
		// 最终结果
		List<CodisDashboardNode> codisDashboardNodeList = new ArrayList<CodisDashboardNode>();
		int port = ConstUtils.CODIS_DASHBOARD_BASE_PORT;
		for (String ip : codisDashboardMachineList) {
			// 如果端口存在就自增
			while (checkDashboardHostPortExist(ip, port)) {
				port++;
			}
			codisDashboardNodeList.add(new CodisDashboardNode(ip, port));
			port++;
		}
		return codisDashboardNodeList;
	}*/

    @Override
    public boolean checkHostPortExist(String ip, int port) {
        InstanceInfo instanceInfo = instanceDao.getAllInstByIpAndPort(ip, port);
        if (instanceInfo != null) {
            return true;
        }
        //只检测一次
        boolean isRedisRun = redisCenter.isRun(ip, port, 1);
        if (isRedisRun) {
            appWechatUtil.noticeWildInstance(ip, port);
            logger.warn(BaseTask.marker, "{}:{} process is not in instance_info table", ip, port);
            return true;
        }
        return false;
    }

    private boolean checkHostPortExist(Map<String, Set<Integer>> ipPortSetMap, String host, int port) {
        Set<Integer> portSet = ipPortSetMap.get(host);
        if (CollectionUtils.isNotEmpty(portSet) && portSet.contains(port)) {
            return true;
        } else {
            if (portSet == null) {
                Set<Integer> set = new HashSet<Integer>();
                set.add(port);
                ipPortSetMap.put(host, set);
            } else {
                ipPortSetMap.get(host).add(port);
            }
            return checkHostPortExist(host, port);
        }
    }

    /**
     * 暂时和nut cracker一样
     *
     * @return
     */
	/*private boolean checkCodisProxyHostPortExist(String ip, int port) {
		List<InstanceInfo> instanceInfoList = instanceDao.getInstanceByIpAndStatPort(ip, port);
		if (CollectionUtils.isNotEmpty(instanceInfoList)) {
			return true;
		} else {
			return checkHostPortExist(ip, port);
		}
	}*/
    private boolean checkNutCrackerHostPortExist(String ip, int port) {
//		List<InstanceInfo> instanceInfoList = instanceDao.getInstanceByIpAndStatPort(ip, port);
//		if (CollectionUtils.isNotEmpty(instanceInfoList)) {
//			return true;
//		} else {
//			return checkHostPortExist(ip, port);
//		}
        // @todo fulei
        return true;
    }

    public boolean checkDashboardHostPortExist(String ip, int port) {
        InstanceInfo instanceInfo = instanceDao.getAllInstByIpAndPort(ip, port);
        if (instanceInfo != null) {
            return true;
        }
        return false;
    }

    private boolean checkRedisMigrateToolPortExist(String ip, int port) {
        // @todo fulei
        return true;
//		RedisMigrateToolInstance redisMigrateToolInstance = redisMigrateToolInstanceDao.getByHostAndPort(ip, port);
//		if (redisMigrateToolInstance != null) {
//			return true;
//		}
//		//只检测一次
//		boolean isRedisRun = redisCenter.isRun(ip, port, 1);
//		if (isRedisRun) {
//			appWechatUtil.noticeWildInstance(ip, port);
//			logger.warn(BaseTask.marker, "{}:{} process is not in instance_info table", ip, port);
//			return true;
//		}
//		return false;
    }
	
	
	/*@Override
	public List<RedisPortNode> generateRedisPortNodeList(long sourceAppId, long targetAppId,
			List<InstanceInfo> slaveInstanceInfoList, List<InstanceInfo> proxyInstanceInfoList) {
		
		List<RedisPortNode> redisPortNodeList = new ArrayList<RedisPortNode>();
		
		for (int i = 0; i < slaveInstanceInfoList.size(); i++) {
			//proxy索引
			int index = i % proxyInstanceInfoList.size();
			
			InstanceInfo proxyInstanceInfo = proxyInstanceInfoList.get(index);
			InstanceInfo slaveInstanceInfo  = slaveInstanceInfoList.get(i);
			
			String redisPortHost = slaveInstanceInfo.getIp();
			int redisPortPort = slaveInstanceInfoList.get(i).getPort() + ConstUtils.REDIS_PORT_PORT_INCREASE;
			String sourceHost = slaveInstanceInfo.getIp();
			int sourcePort = slaveInstanceInfo.getPort();
			String targetHost = proxyInstanceInfo.getIp();
			int targetPort = proxyInstanceInfo.getPort();
		
			while (checkRedisPortExist(redisPortHost, redisPortPort, sourceHost, sourcePort, targetHost, targetPort)) {
				redisPortPort++;
			}
			
			RedisPortNode redisPortNode = new RedisPortNode();
			redisPortNode.setIp(redisPortHost);
			redisPortNode.setPort(redisPortPort);
			redisPortNode.setSourceInstanceId(slaveInstanceInfo.getId());
			redisPortNode.setSourceIp(sourceHost);
			redisPortNode.setSourcePort(sourcePort);
			redisPortNode.setTargetInstanceId(proxyInstanceInfo.getId());
			redisPortNode.setTargetIp(targetHost);
			redisPortNode.setTargetPort(targetPort);
			
			redisPortNodeList.add(redisPortNode);
		}
		
		return redisPortNodeList;
	}*/

    @Override
    public RedisServerNode generateRedisServerNode(long appId, String host, int maxMemory, InstanceRoleEnum instanceRoleEnum) {
        synchronized (host.intern()) {
            try {
                //防止端口重复 @TODO也可以用本地缓存做端口限制
                TimeUnit.SECONDS.sleep(3);
                int port = (int) (ConstUtils.REDIS_SERVER_BASE_PORT + (appId % 10) * 10);
                if (InstanceRoleEnum.SLAVE.equals(instanceRoleEnum)) {
                    port += ConstUtils.SLAVE_PORT_INCREASE;
                }
                while (checkHostPortExist(host, port)) {
                    logger.info(BaseTask.marker, "appId {} host {} port is {}", appId, host, port);
                    port++;
                }
                logger.info(BaseTask.marker, "final appId {} host {} port is {}", appId, host, port);
                return new RedisServerNode(host, port, maxMemory);
            } catch (Exception e) {
                logger.error(BaseTask.marker, e.getMessage(), e);
                return null;
            }
        }
    }
	
	/*@Override
	public PikaNode generatePikaNode(long appId, String host, int maxMemory, InstanceRoleEnum instanceRoleEnum) {
		synchronized (host.intern()) {
			try {
				//防止端口重复 @TODO也可以用本地缓存做端口限制
				TimeUnit.SECONDS.sleep(3);
				int port = (int) (ConstUtils.PIKA_BASE_PORT + (appId % 10) * 10);
				if (InstanceRoleEnum.SLAVE.equals(instanceRoleEnum)) {
					port += ConstUtils.PIKA_SLAVE_PORT_INCREASE;
				}
				while (checkHostPortExist(host, port)) {
					logger.info(BaseTask.marker, "appId {} host {} port is {}", appId, host, port);
					port++;
				}
				logger.info(BaseTask.marker, "final appId {} host {} port is {}", appId, host, port);
				return new PikaNode(host, port);
			} catch (Exception e) {
				logger.error(BaseTask.marker, e.getMessage(), e);
				return null;
			}
		}
	}*/
	
	
	/*@Override
	public List<RedisMigrateToolNode> generateRedisMigrateToolNodeList(int rmtCount, String machineLogicName) {
		//最终结果
		List<RedisMigrateToolNode> redisMigrateToolNodeList = new ArrayList<RedisMigrateToolNode>();
		
		//所有rmt机器
		List<MachineInfo> redisMigrateToolMachineList = machineInfoDao.getMachineInfoByKsp(KspNodeEnum.MIGRATE_TOOL.getType());
		if (appEnvName.equals(AppEnvNameEnum.afun.getName())) {
			redisMigrateToolMachineList = machineInfoDao.getMachineInfoByKsp("afun-redis-migrate-tool");
			if (CollectionUtils.isNotEmpty(redisMigrateToolMachineList)) {
				String host = redisMigrateToolMachineList.get(0).getIp();
				int port = ConstUtils.REDIS_MIGRATE_TOOL_PORT;
				
				//检查表和心跳
				if (!checkRedisMigrateToolPortExist(host, port)) {
					redisMigrateToolNodeList.add(new RedisMigrateToolNode(host, port));
				}
			}
		} else {
			redisMigrateToolMachineList = machineInfoDao.getMachineInfoByKsp(KspNodeEnum.MIGRATE_TOOL.getType());
			for (MachineInfo redisMigrateToolMachine : redisMigrateToolMachineList) {
				MachineRoom machineRoom = machineRoomDao.getByName(redisMigrateToolMachine.getMachineRoomName());
				if (!machineLogicName.equals(machineRoom.getLogicName())) {
					continue;
				}
				if (redisMigrateToolNodeList.size() >= rmtCount) {
					break;
				}
				
				String host = redisMigrateToolMachine.getIp();
				int port = ConstUtils.REDIS_MIGRATE_TOOL_PORT;
				
				//检查表和心跳
				if (!checkRedisMigrateToolPortExist(host, port)) {
					redisMigrateToolNodeList.add(new RedisMigrateToolNode(host, port));
				}
			}
		}
		return redisMigrateToolNodeList;
	}*/

}
