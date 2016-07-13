package com.sohu.cache.init;

import com.sohu.cache.dao.MachineDao;
import com.sohu.cache.entity.MachineInfo;
import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.util.ConstUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 初始化，加载所有的host
 *
 * User: lingguo
 * Date: 14-6-12
 * Time: 下午12:40
 */
public class MachineInitLoad extends AsyncLoad{
    private final Logger logger = LoggerFactory.getLogger(MachineInitLoad.class);

    private MachineCenter machineCenter;
    private MachineDao machineDao;

    public void init() {
        if (ConstUtils.IS_DEBUG) {
            logger.warn("isDebug=true return");
            return;
        }

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    initAsync();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });
    }
    /**
     * 为机器部署trigger，主要两类：统计机器的信息以及监控机器的状态
     */
    public void initAsync() {
        List<MachineInfo> machineInfoList = machineDao.getAllMachines();
        for (MachineInfo machineInfo : machineInfoList) {
            long hostId = machineInfo.getId();
            String ip = machineInfo.getIp();
            Assert.hasText(ip);
            Assert.isTrue(hostId > 0);
            machineCenter.deployMachineCollection(hostId, ip);
            machineCenter.deployMachineMonitor(hostId, ip);
            if(machineInfo.getCollect() == 1) {
            	machineCenter.deployServerCollection(hostId, ip);
            }
        }
        logger.info("init deploy all host and monitor done.");
    }

    public void setMachineCenter(MachineCenter machineCenter) {
        this.machineCenter = machineCenter;
    }

    public void setMachineDao(MachineDao machineDao) {
        this.machineDao = machineDao;
    }

}
