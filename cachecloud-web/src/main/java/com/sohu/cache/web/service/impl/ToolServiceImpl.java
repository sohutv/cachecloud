package com.sohu.cache.web.service.impl;

import com.sohu.cache.dao.AppDao;
import com.sohu.cache.dao.AppUserDao;
import com.sohu.cache.dao.MachineDao;
import com.sohu.cache.entity.*;
import com.sohu.cache.task.constant.TopoloyExamContants;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.web.service.AppService;
import com.sohu.cache.web.service.ToolService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by rucao on 2018/12/11
 */
@Service
public class ToolServiceImpl implements ToolService {
    private final static String APPID = "appId";
    private final static String TYPE = "type";
    private final static String STATUS = "status";
    private final static String DESC = "desc";
    private final static String SENTINEL_DESC = "instanceId:{0},ip:{1}";
    private final static String MASTER_SALVE = "masterIp:{0},salveIp:{1}";
    private Logger logger = LoggerFactory.getLogger(ToolServiceImpl.class);
    @Resource
    private AppDao appDao;
    @Resource
    private MachineDao machineDao;
    @Resource
    private AppService appService;
    @Resource
    private AppUserDao appUserDao;

    @Override
    public List<Map> topologyExamByAppid(long appId) {
        List<Map> examResult = new ArrayList<Map>();
        checkTopologyByAppid(appId, examResult);
        return examResult;
    }

    @Override
    public void topologyExam(List<Long> appidList) {
        List<Map> examResult = new ArrayList<>();
        for (long appId : appidList) {
            checkTopologyByAppid(appId, examResult);
        }
    }

    @Override
    public void restAppDescOfficer() {
        List<AppDesc> appDescList = appDao.getAllApps();
        appDescList.stream().forEach(appDesc -> {
            try {
                String officer = appDesc.getOfficer();
                if (StringUtils.isNotEmpty(officer)) {
                    List<String> nameList = Arrays.asList(officer.split(";|,| |；|，|、"));
                    String officer_new = String.join(",", getUserIdListByNames(nameList));
                    appDesc.setOfficer(officer_new);
                    if (appDao.update(appDesc) > 0) {
                        logger.info("update appDesc success, appId:{}, officer:{}", appDesc.getAppId(), officer_new);
                    } else {
                        logger.info("update appDesc fail, appId:{}, officer:{}", appDesc.getAppId(), officer_new);
                    }
                }
            }catch (Exception e){
                logger.error("update appDesc error, appId:{}", appDesc.getAppId());
            }
        });
    }

    private List<String> getUserIdListByNames(List<String> nameList) {
        return nameList.stream()
                .map(name -> NumberUtils.isNumber(name) ? name : getUserIdByName(name))
                .collect(Collectors.toList());
    }

    private String getUserIdByName(String name) {
        List<AppUser> appUsers = appUserDao.getUserList(name);
        if (CollectionUtils.isNotEmpty(appUsers) && appUsers.size() > 0) {
            return String.valueOf(appUsers.get(0).getId());
        } else {
            AppUser user = appUserDao.getByName(name);
            if(user!=null){
                return String.valueOf(user.getId());
            }else {
                AppUser appUser = appUserDao.getByEmail(name);
                return appUser == null ? "" : String.valueOf(appUser.getId());
            }
        }
    }


    public void checkTopologyByAppid(long appId, List<Map> examResult) {
        try {
            AppDesc appDesc = appDao.getAppDescById(appId);
            if (appDesc.getType() == ConstUtils.CACHE_REDIS_SENTINEL) {//redis-sentinel
                logger.info("redis-sentinel应用");
                sentinelExam(appId, examResult);
            } else if (appDesc.getType() == ConstUtils.CACHE_TYPE_REDIS_CLUSTER) {//redis-cluster
                logger.info("redis-cluster应用");
                clusterExam(appId, examResult);
            }
        } catch (Exception e) {
            logger.error("appid:{}, exception:{}", appId, e.getMessage());
        }
    }

    private String getRealIp(String ip) {
        MachineInfo machineInfo = machineDao.getMachineInfoByIp(ip);
        if (machineInfo.getVirtual() == 1 && !StringUtils.isEmpty(machineInfo.getRealIp())) {
            return machineInfo.getRealIp();
        } else {
            return ip;
        }
    }

    private void sentinelExam(final long appId, List<Map> examResult) {
        List<InstanceInfo> instances = appService.getAppInstanceInfo(appId);
        //sentinel set
        Map<String, String> sent_realIps = new HashMap<String, String>();
        //master_slave
        Map<String, List<String>> master_slave = new HashMap<String, List<String>>();
        for (InstanceInfo inst : instances) {
            if (inst.getStatus() == 1 && inst.getRoleDesc().equals("sentinel")) {
                String desc = MapUtils.getString(sent_realIps, getRealIp(inst.getIp()), "");
                sent_realIps.put(getRealIp(inst.getIp()), desc + "\n" + MessageFormat.format(SENTINEL_DESC, inst.getId(), inst.getIp()));
            } else if (inst.getStatus() == 1 && inst.getRoleDesc().equals("master")) {

            } else if (inst.getStatus() == 1 && inst.getRoleDesc().equals("slave")) {
                String masterHost = inst.getMasterHost();
                List<String> slaves = (ArrayList<String>) MapUtils.getObject(master_slave, masterHost, new ArrayList<String>());
                slaves.add(inst.getIp());
                master_slave.put(masterHost, slaves);
            }
        }
        //sentinel result
        if (sent_realIps.size() < 3) {
            //String tmp = "";
            StringBuilder tmpBuilder = new StringBuilder();
            for (Map.Entry<String, String> realIps : sent_realIps.entrySet()) {
                tmpBuilder.append("realIp::")
                        .append(realIps.getValue())
                        .append("<br/>")
                        .append(realIps.getValue())
                        .append("<br/>");
            }
            final String descStr = tmpBuilder.toString();
            examResult.add(
                    new HashMap<String, String>() {{
                        put(APPID, String.valueOf(appId));
                        put(TYPE, "redis_sentinel");
                        put(STATUS, TopoloyExamContants.NODESNUM_DESC);
                        put(DESC, descStr);
                    }}
            );
            logger.info("sentinel节点分布在少于3个物理机，应用：{0}", appId);
        }
        //master-slave result
        StringBuilder tmpBuilder1 = new StringBuilder();
        for (Map.Entry<String, List<String>> res : master_slave.entrySet()) {
            for (String salveIp : res.getValue()) {
                if (getRealIp(salveIp).equals(getRealIp(res.getKey()))) {
                    tmpBuilder1.append(MessageFormat.format(MASTER_SALVE, res.getKey(), salveIp))
                            .append("<br/>");
                }
            }
        }
        if (tmpBuilder1.length() > 0) {
            final String descStr = tmpBuilder1.toString();
            examResult.add(
                    new HashMap<String, String>() {{
                        put(APPID, String.valueOf(appId));
                        put(TYPE, "redis_sentinel");
                        put(STATUS, "master-slave节点不能在一台物理机上");
                        put(DESC, descStr);
                    }}
            );
            logger.info("master-slave节点不能在一台物理机上，应用: {0}", appId);
        }
    }

    private void clusterExam(final long appId, List<Map> examResult) {
        List<InstanceInfo> instances = appService.getAppInstanceInfo(appId);
        //mater节点数
        List<String> masters = new ArrayList<>();
        //master物理机数量
        Map<String, String> master_realIps = new HashMap<String, String>();
        Map<String, String> realIpMap = new HashMap<String, String>();

        int slaveNum = 0;
        //master_slave
        Map<String, List<String>> master_slave = new HashMap<String, List<String>>();
        for (InstanceInfo inst : instances) {
            if (inst.getStatus() == 1) {//实例正常启用
                if (inst.getRoleDesc().equals("master")) {
                    //master_realIps.add(getRealIp(inst.getIp()));
                    masters.add(inst.getIp());
                    String desc = MapUtils.getString(master_realIps, getRealIp(inst.getIp()), "");
                    master_realIps.put(getRealIp(inst.getIp()), desc + "\n" + MessageFormat.format(SENTINEL_DESC, inst.getId(), inst.getIp()));

                    String desc1 = MapUtils.getString(realIpMap, getRealIp(inst.getIp()), "");
                    realIpMap.put(getRealIp(inst.getIp()), desc1 + "," + inst.getId());

                    String masterId = String.valueOf(inst.getId());
                    String masterHost = inst.getIp();
                    String key = masterId + "_" + masterHost;
                    List<String> slaves = (ArrayList<String>) MapUtils.getObject(master_slave, key, new ArrayList<String>());
                    master_slave.put(key, slaves);
                } else if (inst.getRoleDesc().equals("slave")) {
                    slaveNum++;
                    String masterHost = inst.getMasterHost();
                    String masterId = String.valueOf(inst.getMasterInstanceId());
                    String slaveIp = inst.getIp();
                    String key = masterId + "_" + masterHost;
                    List<String> slaves = (ArrayList<String>) MapUtils.getObject(master_slave, key, new ArrayList<String>());
                    if (!slaves.contains(slaveIp)) {
                        slaves.add(slaveIp);
                    }
                    master_slave.put(key, slaves);
                }
            }
        }

        if (master_realIps.size() < 3) {
            StringBuilder tmpBuilder = new StringBuilder();
            for (Map.Entry<String, String> realIps : master_realIps.entrySet()) {
                tmpBuilder.append("realIp::")
                        .append(realIps.getKey())
                        .append("\n")
                        .append(realIps.getValue())
                        .append("\n");
            }
            final String descStr = tmpBuilder.toString();
            examResult.add(
                    new HashMap<String, String>() {{
                        put(APPID, String.valueOf(appId));
                        put(TYPE, TopoloyExamContants.REDIS_CLUSTER);
                        put(STATUS, TopoloyExamContants.NODESNUM_DESC);
                        put(DESC, descStr);
                    }}
            );
        }
        //2. master-slave result
        StringBuilder tmpBuilder1 = new StringBuilder();
        for (Map.Entry<String, List<String>> res : master_slave.entrySet()) {
            for (String salveIp : res.getValue()) {
                String masterHost = res.getKey().split("_")[1];
                if (getRealIp(salveIp).equals(masterHost)) {
                    tmpBuilder1.append(MessageFormat.format(MASTER_SALVE, res.getKey(), salveIp))
                            .append("\n");
                }
            }
            //检查master是否有slave
            if (slaveNum != 0) {
                if (res.getValue().size() == 0) {
                    examResult.add(
                            new HashMap<String, String>() {{
                                put(APPID, String.valueOf(appId));
                                put(TYPE, TopoloyExamContants.REDIS_CLUSTER);
                                put(STATUS, TopoloyExamContants.SLAVE_NOT_EXIST);
                                put(DESC, MessageFormat.format(MASTER_SALVE, res.getKey(), "null"));
                            }}
                    );
                }
            }
        }
        if (tmpBuilder1.length() > 0) {
            final String descStr = tmpBuilder1.toString();
            examResult.add(
                    new HashMap<String, String>() {{
                        put(APPID, String.valueOf(appId));
                        put(TYPE, TopoloyExamContants.REDIS_CLUSTER);
                        put(STATUS, TopoloyExamContants.MASTER_SLAVE_DESC);
                        put(DESC, descStr);
                    }}
            );
        }
        //3. 故障转移
        for (Map.Entry<String, String> entry : realIpMap.entrySet()) {
            String[] instList = entry.getValue().split(",");
            int num = instList.length;
            //String desc = "";
            StringBuilder descBuilder = new StringBuilder();
            int count = 0;
            for (int i = 0; i < num; i++) {
                //desc += "instanceId::" + instList[i] + "   ";
                descBuilder.append("instanceId::")
                        .append(instList[i])
                        .append("   ");
                if (!instList[i].isEmpty()) {
                    count++;
                }
            }
            descBuilder.append("   物理机ip::").append(entry.getKey());
            final String descStr = descBuilder.toString();
            if (masters.size() - count < masters.size() / 2 + 1) {
                examResult.add(
                        new HashMap<String, String>() {{
                            put(APPID, String.valueOf(appId));
                            put(TYPE, "redis_cluster");
                            put(STATUS, "集群中一台物理机下线不满足自动故障转移条件");
                            put(DESC, descStr);
                        }}
                );
                logger.info("集群中某一台物理机下线不满足自动故障转移条件，应用: " + appId);
            }
        }
    }

}
