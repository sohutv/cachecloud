package com.sohu.cache.web.controller;

import com.sohu.cache.alert.EmailComponent;
import com.sohu.cache.async.AsyncThreadPoolFactory;
import com.sohu.cache.async.KeyCallable;
import com.sohu.cache.constant.AppUserAlertEnum;
import com.sohu.cache.constant.AppUserTypeEnum;
import com.sohu.cache.constant.MachineInfoEnum;
import com.sohu.cache.dao.*;
import com.sohu.cache.entity.*;
import com.sohu.cache.machine.MachineDeployCenter;
import com.sohu.cache.redis.RedisConfigTemplateService;
import com.sohu.cache.task.constant.MachineSyncEnum;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.web.enums.PodStatusEnum;
import com.sohu.cache.web.enums.SuccessEnum;
import com.sohu.cache.web.util.VelocityUtils;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 运维工具
 * Created by rucao on 2018/7/30
 */
@RestController
@RequestMapping("operation")
public class OperationController extends BaseController {
    private final static String DOT = ".";
    private final static String IPREG = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
    private final static String format = "{0}:{1}";

    @Resource
    private MachineDeployCenter machineDeployCenter;
    @Resource
    private MachineDao machineDao;
    @Resource
    private InstanceDao instanceDao;
    @Resource
    private AppDao appDao;
    @Resource
    private MachineRoomDao machineRoomDao;
    @Resource
    private VelocityEngine velocityEngine;
    @Resource
    private EmailComponent emailComponent;
    @Resource
    private RedisConfigTemplateService redisConfigTemplateService;
    @Resource
    private MachineRelationDao machineRelationDao;


    @RequestMapping(value = "/machines", method = {RequestMethod.POST})
    public ResponseEntity<String> addMachines(@RequestHeader(value = "token", defaultValue = "addMachine") String token,
                                              @RequestBody List<MachineInfo> machineInfoList) {
        long startTime = System.currentTimeMillis();

        HttpStatus status = HttpStatus.CREATED;
        String message = "";
        List<OperationAlertValueResult> resultList = new ArrayList<OperationAlertValueResult>();
        if (token.isEmpty() || !token.equals("addMachine")) {
            status = HttpStatus.UNAUTHORIZED;
            message = "TOKEN 验证失败";
            OperationAlertValueResult operationResult = new OperationAlertValueResult("", null, "CREATE", status.toString() + status.getReasonPhrase(), message);
            resultList.add(operationResult);
            operationNotice(resultList, "CREATE");
            return ResponseEntity.status(status).body(message);
        }

        for (int i = 0; i < machineInfoList.size(); i++) {
            MachineInfo machineInfo = machineInfoList.get(i);
            logger.info("machineInfo :{}", machineInfo);
            String ip = machineInfo.getIp();
            if (!isIp(ip)) {
                status = HttpStatus.BAD_REQUEST;
                message += "ip为空或格式错误，无法插入\n";
                OperationAlertValueResult operationResult = new OperationAlertValueResult(ip, machineInfo, "CREATE",
                        status.toString() + status.getReasonPhrase(),
                        "ip为空或格式错误，无法插入\n");
                resultList.add(operationResult);
                continue;
            }
            if (isIpRepeat(ip)) {
                message += ip + "已经存在且有效，无法重新插入\n";
                OperationAlertValueResult operationResult = new OperationAlertValueResult(ip, machineInfo, "CREATE",
                        status.toString() + status.getReasonPhrase(),
                        ip + "已经存在且有效，无法重新插入\n");
                resultList.add(operationResult);
                continue;
            }
            message += ip;

            if (machineInfo.getMem() == 0) {
                message += "内存为空,";
            }
            if (machineInfo.getCpu() == 0) {
                message += "cpu为空,";
            }
            if (machineInfo.getVirtual() == 1 && !isIp(machineInfo.getRealIp())) {
                message += "宿主机ip为空或格式错误,";
            }

            if (machineInfo.getUseType() == 0) {
                if (StringUtils.isEmpty(machineInfo.getProjectName())) {
                    message += "Redis专用机器未填写项目名称,";
                } else {
                    machineInfo.setExtraDesc(MessageFormat.format(format, machineInfo.getExtraDesc(), machineInfo.getProjectName()));
                }

            }
            autoInsertRoom(machineInfo);
            Date date = new Date();
            machineInfo.setType(MachineInfoEnum.TypeEnum.REDIS_NODE.getType());
            machineInfo.setCollect(1);

            machineInfo.setSshUser(ConstUtils.USERNAME);
            machineInfo.setSshPasswd(ConstUtils.PASSWORD);
            machineInfo.setServiceTime(date);
            machineInfo.setModifyTime(date);
            machineInfo.setAvailable(MachineInfoEnum.AvailableEnum.YES.getValue());
            try {
                if (!machineDeployCenter.addMachine(machineInfo)) {
                    message += "插入失败\n";
                    OperationAlertValueResult operationResult = new OperationAlertValueResult(ip, machineInfo, "CREATE",
                            "save or deploy machineInfo error",
                            "插入失败\n");
                    resultList.add(operationResult);
                } else {
                    message += "插入成功\n";
                    OperationAlertValueResult operationResult = new OperationAlertValueResult(ip, machineInfo, "CREATE",
                            HttpStatus.CREATED.toString() + HttpStatus.CREATED.getReasonPhrase(),
                            "插入成功\n");
                    resultList.add(operationResult);
                }
                logger.info(message);
                // 更新机器安装redis版本
                if (machineInfo.getAvailable() == MachineInfoEnum.AvailableEnum.YES.getValue()) {
                    // 3.更新线上机器version_install版本状态
                    redisConfigTemplateService.updateMachineInstallRedis(machineInfo.getIp());
                }
            } catch (Exception ex) {
                logger.info("addMultiMachines: {}", ex.getMessage());
            }
        }
        operationNotice(resultList, "CREATE");

        logger.info("add {} machine costtime ={} ms", machineInfoList.size(), (System.currentTimeMillis() - startTime));
        return ResponseEntity.status(status).body(message);
    }

    /**
     * k8s pod容器变更:上线 (k8s自身调度/升级)
     *
     * @param token
     * @param machineInfoList
     * @return
     */
    @RequestMapping(value = "/pod/online", method = {RequestMethod.POST})
    public ResponseEntity<String> onlinePod(@RequestHeader(value = "token", defaultValue = "onlinePod") String token,
                                            @RequestBody List<MachineInfo> machineInfoList) {
        long startTime = System.currentTimeMillis();
        HttpStatus status = HttpStatus.CREATED;
        String message = "";
        String type = "POD-ONLINE";
        List<OperationAlertValueResult> resultList = new ArrayList<OperationAlertValueResult>();
        if (token.isEmpty() || !token.equals("onlinePod")) {
            status = HttpStatus.UNAUTHORIZED;
            message = "TOKEN 验证失败";
            OperationAlertValueResult operationResult = new OperationAlertValueResult("", null, type, status.toString() + status.getReasonPhrase(), message);
            resultList.add(operationResult);
            operationNotice(resultList, type);
            return ResponseEntity.status(status).body(message);
        }

        if (machineInfoList != null && machineInfoList.size() > 0) {
            for (MachineInfo machineInfo : machineInfoList) {
                logger.info("machineInfo :{}", machineInfo);
                String ip = machineInfo.getIp();
                String realIp = machineInfo.getRealIp();

                // save machine relation
                try {
                    long podUpdateTime = machineInfo.getPodUpdateTime() == 0 ? System.currentTimeMillis() : machineInfo.getPodUpdateTime();
                    MachineRelation machineRelation = new MachineRelation(ip, realIp, new Date(podUpdateTime), machineInfo.getProjectName(), PodStatusEnum.ONLINE.getValue());
                    machineRelationDao.saveOrUpdateMachineRelation(machineRelation);
                } catch (Exception e) {
                    logger.info("save pod online relation: {}", e.getMessage());
                }

                if (!isIp(ip)) {
                    status = HttpStatus.BAD_REQUEST;
                    message += "ip为空或格式错误，无法插入\n";
                    OperationAlertValueResult operationResult = new OperationAlertValueResult(ip, machineInfo, type,
                            status.toString() + status.getReasonPhrase(),
                            "ip为空或格式错误，无法插入\n");
                    resultList.add(operationResult);
                    continue;
                }
                message += ip;
                if (machineInfo.getMem() == 0) {
                    message += "内存为空,";
                }
                if (machineInfo.getCpu() == 0) {
                    message += "cpu为空,";
                }
                if (machineInfo.getVirtual() == 1 && !isIp(machineInfo.getRealIp())) {
                    message += "宿主机ip为空或格式错误,";
                }
                // 专用机器
                if (machineInfo.getUseType() == 0) {
                    if (StringUtils.isEmpty(machineInfo.getProjectName())) {
                        message += "Redis专用机器未填写项目名称,";
                    } else {
                        machineInfo.setExtraDesc(MessageFormat.format(format, machineInfo.getExtraDesc(), machineInfo.getProjectName()));
                    }

                }
                // 插入机房信息
                autoInsertRoom(machineInfo);
                Date date = new Date();
                // 机器类型: redis/迁移工具   机器部署类型: 专用/混合/测试/sentinel
                MachineInfo machineOldInfo = getMachineInfo(ip);
                if (machineOldInfo != null) {
                    machineInfo.setType(machineOldInfo.getType());
                    machineInfo.setUseType(machineOldInfo.getUseType());
                } else {
                    machineInfo.setType(MachineInfoEnum.TypeEnum.REDIS_NODE.getType());
                }
                machineInfo.setCollect(1);
                machineInfo.setSshUser(ConstUtils.USERNAME);
                machineInfo.setSshPasswd(ConstUtils.PASSWORD);
                machineInfo.setServiceTime(date);
                machineInfo.setModifyTime(date);
                machineInfo.setAvailable(MachineInfoEnum.AvailableEnum.YES.getValue());
                try {
                    boolean saveOrUpdate = isK8sIpRepeat(ip);
                    if (!machineDeployCenter.addMachine(machineInfo)) {
                        message += "插入失败\n";
                        OperationAlertValueResult operationResult = new OperationAlertValueResult(ip, machineInfo, type,
                                "save or deploy machineInfo error",
                                "插入失败\n");
                        resultList.add(operationResult);
                    } else {
                        // 判断机器是新增还是修改
                        if (saveOrUpdate) {
                            message += ip + "修改机器成功\n";
                            OperationAlertValueResult operationResult = new OperationAlertValueResult(ip, machineInfo, type,
                                    status.toString() + status.getReasonPhrase(),
                                    ip + "机器信息修改成功\n");
                            resultList.add(operationResult);
                            // 提交异步任务检测实例
                            asyncExecuteDetectTask(ip);
                        } else {
                            message += "新增机器成功\n";
                            OperationAlertValueResult operationResult = new OperationAlertValueResult(ip, machineInfo, type,
                                    HttpStatus.CREATED.toString() + HttpStatus.CREATED.getReasonPhrase(),
                                    "新增机器成功\n");
                            resultList.add(operationResult);
                        }

                    }
                    logger.info(message);
                } catch (Exception ex) {
                    logger.info("add Machine:{} error: {}", machineInfo, ex.getMessage());
                }
            }
            logger.info("add pod {} machine costtime ={} ms", machineInfoList.size(), (System.currentTimeMillis() - startTime));
        }
        operationNotice(resultList, type);
        return ResponseEntity.status(status).body(message);

    }

    /**
     * <p>
     * Description: 异步任务: 检测实例状态 & 滚动重启
     * </p>
     */
    public void asyncExecuteDetectTask(final String ip) {

        String key = "detect-instance-" + ip;
        asyncService.submitFuture(AsyncThreadPoolFactory.DEFAULT_ASYNC_POOL, new KeyCallable<Boolean>(key) {
            public Boolean execute() {
                try {
                    // 1.检测pod是否被调度到其他宿主机
                    MachineSyncEnum syncStatus = instanceDeployCenter.podChangeStatus(ip);
                    // 2. pod状态为MachineSyncEnum.NO_CHANGE 或 MachineSyncEnum.SYNC_SUCCESS 自动检测实例
                    List<InstanceAlertValueResult> instanceAlertValueResults = new ArrayList<>();
                    if (syncStatus.getValue() == MachineSyncEnum.NO_CHANGE.getValue() ||
                            syncStatus.getValue() == MachineSyncEnum.SYNC_SUCCESS.getValue()) {
                        instanceAlertValueResults = instanceDeployCenter.checkAndStartExceptionInstance(ip,true);
                    }
                    logger.info("pod ip:{} sync status:{}, scroll redis number:{} ", ip, syncStatus.getDesc(), instanceAlertValueResults.size());
                    return true;
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return false;
                }
            }
        });
    }

    /**
     * k8s pod容器变更:下线 (k8s自身调度/停止/滚动升级)
     *
     * @param token
     * @param machineInfoList
     * @return
     */
    @RequestMapping(value = "/pod/offline", method = {RequestMethod.POST})
    public ResponseEntity<String> offlinePod(@RequestHeader(value = "token", defaultValue = "offlinePod") String token,
                                             @RequestBody List<MachineInfo> machineInfoList) {
        HttpStatus status = HttpStatus.NO_CONTENT;
        String message = "";
        String type = "POD-OFFLINE";

        logger.info("machineinfo list: {}", machineInfoList);
        List<OperationAlertValueResult> resultList = new ArrayList<OperationAlertValueResult>();
        if (token.isEmpty() || !token.equals("offlinePod")) {
            status = HttpStatus.UNAUTHORIZED;
            message = "TOKEN 验证失败";
        }
        // 遍历变更下线的pod
        if (machineInfoList != null && machineInfoList.size() > 0) {
            for (MachineInfo machineInfo : machineInfoList) {
                String ip = machineInfo.getIp();
                String realIp = machineInfo.getRealIp();
                // save machine relation
                try {
                    long podUpdateTime = machineInfo.getPodUpdateTime() == 0 ? System.currentTimeMillis() : machineInfo.getPodUpdateTime();
                    MachineRelation machineRelation = new MachineRelation(ip, realIp, new Date(podUpdateTime), machineInfo.getProjectName(), PodStatusEnum.OFFLINE.getValue());
                    machineRelationDao.saveOrUpdateMachineRelation(machineRelation);
                    //修改机器状态 为下线
                    machineDao.removeMachineInfoByIp(ip);
                } catch (Exception e) {
                    logger.info("save pod offline relation: {}", e.getMessage());
                }

                if (!isIp(ip)) {
                    status = HttpStatus.BAD_REQUEST;
                    message += "ip为空或格式错误\n";
                }
                OperationAlertValueResult operationResult = new OperationAlertValueResult(ip, machineInfo, type, status.toString() + status.getReasonPhrase(), message);
                resultList.add(operationResult);
            }
        }
        operationNotice(resultList, type);
        return ResponseEntity.status(status).body(message);
    }

    @RequestMapping(value = "/machine", method = {RequestMethod.DELETE})
    public ResponseEntity<String> deleteMachine(@RequestHeader(value = "token", defaultValue = "deleteMachine") String token,
                                                @RequestParam(value = "ip") String ip) {

        HttpStatus status = HttpStatus.NO_CONTENT;
        String message = "";
        MachineInfo machineInfo = null;
        List<OperationAlertValueResult> resultList = new ArrayList<OperationAlertValueResult>();
        if (token.isEmpty() || !token.equals("deleteMachine")) {
            status = HttpStatus.UNAUTHORIZED;
            message = "TOKEN 验证失败";
            OperationAlertValueResult operationResult = new OperationAlertValueResult(ip, machineInfo, "DELETE", status.toString() + status.getReasonPhrase(), message);
            resultList.add(operationResult);
            operationNotice(resultList, "DELETE");
            return ResponseEntity.status(status).body(message);
        }
        if (!isIp(ip)) {
            status = HttpStatus.BAD_REQUEST;
            message += "ip为空或格式错误\n";
        } else if (checkMachineActive(ip)) {
            machineInfo = machineDao.getMachineInfoByIp(ip);
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            message += ip + "有redis节点存活，删除失败\n";
        } else {
            machineInfo = machineDao.getMachineInfoByIp(ip);
            if (machineInfo == null) {
                status = HttpStatus.NOT_FOUND;
                message += ip + "机器资源不存在\n";
            } else if (!machineDeployCenter.removeMachine(machineInfo)) {
                message += ip + "删除失败\n";
            }
        }
        OperationAlertValueResult operationResult = new OperationAlertValueResult(ip, machineInfo, "DELETE", status.toString() + status.getReasonPhrase(), message);
        resultList.add(operationResult);
        operationNotice(resultList, "DELETE");
        return ResponseEntity.status(status).body(message);
    }

    @RequestMapping(value = "/users", method = RequestMethod.POST)
    @ApiResponses({
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public ResponseEntity<String> addUsers(@RequestHeader(value = "token", defaultValue = "addUsers") String token,
                                           @RequestParam(value = "appId") Long appId,
                                           @RequestBody List<AppUser> userList) {
        HttpStatus status = HttpStatus.CREATED;
        String message = "";
        if (token.isEmpty() || !token.equals("addUsers")) {
            status = HttpStatus.UNAUTHORIZED;
            message = "TOKEN 验证失败";
            return ResponseEntity.status(status).body(message);
        }
        try {
            if (appDao.getOnlineAppDescById(appId) == null) {
                status = HttpStatus.BAD_REQUEST;
                message = "appId应用状态无效";
                return ResponseEntity.status(status).body(message);
            }
            if (userList.size() == 0) {
                status = HttpStatus.BAD_REQUEST;
                message = "userList为空";
                return ResponseEntity.status(status).body(message);
            }

            for (AppUser appUser : userList) {
                String userName = appUser.getName();
                if (StringUtils.isNotBlank(userName)) {
                    try {
                        AppUser needAddAppUser = userService.getByName(userName);
                        if (needAddAppUser == null) {
                            if (StringUtils.isBlank(appUser.getChName())) {
                                appUser.setChName("");
                            }
                            if (StringUtils.isBlank(appUser.getEmail())) {
                                appUser.setEmail("");
                            }
                            if (StringUtils.isBlank(appUser.getMobile())) {
                                appUser.setMobile("");
                            }
                            if (StringUtils.isBlank(appUser.getWeChat())) {
                                appUser.setWeChat("");
                            }
                            appUser.setType(AppUserTypeEnum.REGULAR_USER.value());
                            appUser.setIsAlert(AppUserAlertEnum.YES.value());
                            if (userService.save(appUser) == SuccessEnum.SUCCESS) {
                                needAddAppUser = userService.getByName(userName);
                            } else {
                                status = HttpStatus.INTERNAL_SERVER_ERROR;
                                message += "用户:" + userName + "不存在，且用户创建失败\n";
                            }
                        }
                        if (needAddAppUser != null) {
                            if (appService.saveAppToUser(appId, needAddAppUser.getId())) {
                                message += "用户:" + userName + "添加成功\n";
                            } else {
                                status = HttpStatus.INTERNAL_SERVER_ERROR;
                                message += "用户:" + userName + "添加失败\n";
                            }

                        }
                    } catch (Exception ex) {
                        logger.info("addUsers: {}", ex.getMessage());
                    }
                } else {
                    status = HttpStatus.BAD_REQUEST;
                    message += "用户名(英文，域账户)为空\n";
                }
            }

        } catch (Exception ex) {
            logger.info("addUsers: {}", ex.getMessage());
        }
        return ResponseEntity.status(status).body(message);
    }

    /**
     * 根据网段自动识别机房并填入
     *
     * @param machineInfo
     */
    private void autoInsertRoom(MachineInfo machineInfo) {
        String ip = machineInfo.getIp();
        List<MachineRoom> roomList = machineRoomDao.getEffectiveRoom();
        for (MachineRoom room : roomList) {
            String ipNetwork = room.getIpNetwork();
            String ipSub1 = ipNetwork.substring(0, ipNetwork.indexOf(DOT, 3));
            String ipSub2 = ip.substring(0, ip.indexOf(DOT, 3));
            if (ipSub1.equals(ipSub2)) {
                machineInfo.setRoom(room.getName());
            }
        }
    }

    /**
     * 正则验证ip格式
     *
     * @param ipAddress
     * @return
     */
    private boolean isIp(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty())
            return false;
        Pattern pattern = Pattern.compile(IPREG);
        Matcher matcher = pattern.matcher(ipAddress);
        return matcher.matches();
    }

    /**
     * ip是否重复 available==1
     *
     * @param ip
     * @return
     */
    private boolean isIpRepeat(String ip) {
        List<MachineInfo> machineInfos = machineDao.getMachineInfoByLikeIp(ip);
        if (machineInfos == null || machineInfos.size() == 0) {
            return false;
        }
        return true;
    }

    /**
     * ip,realIp 是否存在
     *
     * @param ip
     * @return
     */
    private boolean isK8sIpRepeat(String ip) {
        MachineInfo machineInfo = machineDao.existk8sMachine(ip);
        if (machineInfo == null) {
            return false;
        }
        return true;
    }

    /**
     * 机器类型(type/useType) 不覆盖
     *
     * @param ip
     * @return
     */
    private MachineInfo getMachineInfo(String ip) {
        return machineDao.getMachineInfoByIp(ip);
    }

    /**
     * machine的可删除状态，false表示不存活，可删；true表示有存活节点，不可删
     *
     * @param ip
     * @return
     */
    private boolean checkMachineActive(String ip) {
        List<InstanceInfo> instancelist = instanceDao.getInstListByIp(ip);
        if (instancelist == null || instancelist.size() == 0)
            return false;
        return true;
    }

    /**
     * 操作给管理员发邮件通知
     *
     * @param operationAlertValueResultList
     * @param type
     */
    private void operationNotice(List<OperationAlertValueResult> operationAlertValueResultList, String type) {
        String title = "【CacheCloud】";
        if (type.equalsIgnoreCase("DELETE")) {
            title += "销毁机器";
        } else if (type.equalsIgnoreCase("CREATE")) {
            title += "添加机器";
        } else if (type.equalsIgnoreCase("POD-ONLINE")) {
            title += "POD状态变更:上线";
        } else if (type.equalsIgnoreCase("POD-OFFLINE")) {
            title += "POD状态变更:下线";
        }
        String mailContent = VelocityUtils.createText(velocityEngine,
                null, null, null,
                null,
                operationAlertValueResultList,
                null,
                "OperationAlert.vm", "UTF-8");
        logger.info("send mail content: {}" + operationAlertValueResultList);
        emailComponent.sendMailToAdmin(title, mailContent);
    }

}
