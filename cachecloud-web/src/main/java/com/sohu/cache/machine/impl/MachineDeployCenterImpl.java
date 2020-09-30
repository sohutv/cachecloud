package com.sohu.cache.machine.impl;

import com.google.common.base.Strings;
import com.sohu.cache.dao.MachineDao;
import com.sohu.cache.dao.MachineRelationDao;
import com.sohu.cache.dao.MachineStatsDao;
import com.sohu.cache.dao.ServerStatusDao;
import com.sohu.cache.entity.MachineInfo;
import com.sohu.cache.entity.MachineRelation;
import com.sohu.cache.machine.MachineDeployCenter;
import com.sohu.cache.web.enums.SuccessEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 机器部署相关
 *
 * @author leifu
 *         changed @Date 2016-4-24
 * @Time 下午5:07:30
 */
@Service("machineDeployCenter")
public class MachineDeployCenterImpl implements MachineDeployCenter {
    private Logger logger = LoggerFactory.getLogger(MachineDeployCenterImpl.class);
    @Autowired
    private MachineDao machineDao;
    @Autowired
    private MachineStatsDao machineStatsDao;
    @Autowired
    private ServerStatusDao serverStatusDao;
    @Autowired
    private MachineRelationDao machineRelationDao;

    /**
     * 将机器加入资源池并统计、监控
     *
     * @param machineInfo
     * @return
     */
    @Override
    public boolean addMachine(MachineInfo machineInfo) {
        boolean success = true;

        if (machineInfo == null || Strings.isNullOrEmpty(machineInfo.getIp())) {
            logger.error("machineInfo is null or ip is valid.");
            return false;
        }
        // 将机器信息保存到db中
        try {
            machineDao.saveMachineInfo(machineInfo);
        } catch (Exception e) {
            logger.error("save machineInfo: {} to db error.", machineInfo.toString(), e);
            return false;
        }

        if (success) {
            logger.info("save and deploy machine ok, machineInfo: {}", machineInfo.toString());
        }
        return success;
    }

    /**
     * 删除机器，并删除相关的定时任务
     *
     * @param machineInfo
     * @return
     */
    @Override
    public boolean removeMachine(MachineInfo machineInfo) {
        if (machineInfo == null || Strings.isNullOrEmpty(machineInfo.getIp())) {
            logger.warn("machineInfo is null or ip is empty.");
            return false;
        }
        String machineIp = machineInfo.getIp();

        // 从db中删除machine和相关统计信息
        try {
            machineDao.removeMachineInfoByIp(machineIp);
            machineStatsDao.deleteMachineStatsByIp(machineIp);
            serverStatusDao.deleteServerInfo(machineIp);
        } catch (Exception e) {
            logger.error("remove machineInfo from db error, machineInfo: {}", machineInfo.toString(), e);
            return false;
        }
        logger.info("remove and undeploy machine ok: {}", machineInfo.toString());
        return true;
    }

    @Override
    public void updateMachineRelation(int id, Long taskid, int is_sync) {
        try {
            machineRelationDao.updateMachineRelation(id, taskid, is_sync);
        } catch (Exception e) {
            logger.error("update machineRelation id:{} taskid :{} error, machineRelation: {}", id, taskid, e.getMessage());
        }
    }

    public List<MachineRelation> getMachineRelationList(String ip) {
        List<MachineRelation> relationList = new ArrayList<MachineRelation>();
        try {
            relationList = machineRelationDao.getRelationList(ip);
        } catch (Exception e) {
            logger.error("get machine relation : containerIp:{} , error message:{}", ip, e.getMessage(), e);
        }
        return relationList;
    }

    public SuccessEnum checkMachineSyncStatus(String containerIp, String sourceIp, int is_sync) {
        List<MachineRelation> machineRelationList = null;
        try {
            machineRelationList = machineRelationDao.getMachineSyncStatus(containerIp, sourceIp, is_sync);
        } catch (Exception e) {
            logger.error("check machine relation error : containerIp: {} sourceIp:{} is_sync:{} ,error message: {}", containerIp, sourceIp, is_sync, e.getMessage(), e);
            return SuccessEnum.ERROR;
        }
        if (machineRelationList != null && machineRelationList.size() > 0) {
            logger.info("check machine relation containerIp: {} sourceIp:{} is_sync:{} size :{} ", containerIp, sourceIp, is_sync, machineRelationList.size());
            return SuccessEnum.REPEAT;
        } else {
            return SuccessEnum.NO_REPEAT;
        }
    }

}
