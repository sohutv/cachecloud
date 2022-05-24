package com.sohu.cache.web.controller;

import com.sohu.cache.async.AsyncThreadPoolFactory;
import com.sohu.cache.constant.AppStatusEnum;
import com.sohu.cache.constant.ImportAppResult;
import com.sohu.cache.constant.InstanceStatusEnum;
import com.sohu.cache.dao.AppDataMigrateStatusDao;
import com.sohu.cache.dao.TaskQueueDao;
import com.sohu.cache.entity.*;
import com.sohu.cache.stats.app.ImportAppCenter;
import com.sohu.cache.task.entity.TaskQueue;
import com.sohu.cache.util.TypeUtil;
import com.sohu.cache.web.enums.AppImportStatusEnum;
import com.sohu.cache.web.enums.BooleanEnum;
import com.sohu.cache.web.enums.SuccessEnum;
import com.sohu.cache.web.service.AppImportService;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 已经存在Redis导入
 */
@Controller
@RequestMapping("/import/app")
public class ImportAppController extends BaseController {

    @Resource(name = "importAppCenter")
    private ImportAppCenter importAppCenter;
    @Autowired
    private AppImportService appImportService;
    @Autowired
    private AppDataMigrateStatusDao appDataMigrateStatusDao;
    @Autowired
    private TaskQueueDao taskQueueDao;

    @RequestMapping("/index")
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response, Model model, String tabTag) {
        model.addAttribute("tabTag", tabTag);
        model.addAttribute("appImportActive", SuccessEnum.SUCCESS.value());
        List<AppImport> appImportList = appImportService.getImportAppList(-1);
        model.addAttribute("appImportList", appImportList);
        model.addAttribute("appImportStatusMap", Arrays.stream(AppImportStatusEnum.values()).collect(Collectors.toMap(AppImportStatusEnum::getStatus, Function.identity())));


        return new ModelAndView("manage/appImport/list");
    }

    @RequestMapping(value = "init")
    public ModelAndView init(Model model, long importId) {
        //1.获取导入信息
        AppImport appImport = appImportService.get(importId);
        if (appImport == null) {
            return new ModelAndView("");
        }
        Long appId = appImport.getAppId();
        AppDesc appDesc = appService.getByAppId(appId);
        int oldStatus = appImport.getStatus();
        int status = oldStatus;

        if (status == AppImportStatusEnum.PREPARE.getStatus()) {
            //导入申请未处理阶段
            if (appDesc.getVersionId() != -1) {
                appImport.setRedisVersionName(resourceService.getResourceById(appDesc.getVersionId()).getName());
            }
            model.addAttribute("appDesc", appDesc);
        }
        if (status > 10 && status < 20) {
            //创建版本阶段
            if (appDesc.getVersionId() == -1) {
                SystemResource resource = resourceService.getResourceByName(appImport.getRedisVersionName());
                if (resource != null) {
                    appDesc.setVersionId(resource.getId());
                    appService.update(appDesc);
                    status = AppImportStatusEnum.VERSION_BUILD_END.getStatus();
                    model.addAttribute("hasRedisVersion", 1);
                } else {
                    model.addAttribute("hasRedisVersion", 0);
                }
            } else {
                status = AppImportStatusEnum.VERSION_BUILD_END.getStatus();
                model.addAttribute("hasRedisVersion", 1);
            }
        }
        if (status >= 20 && status < 30) {
            //创建应用阶段
            long appBuildTaskId = appImport.getAppBuildTaskId();
            if (appBuildTaskId > 0) {
                TaskQueue appBuildTask = taskQueueDao.getById(appBuildTaskId);
                if (appBuildTask != null && (appBuildTask.getStatus() == 2 || appBuildTask.getStatus() == 3)) {
                    status = AppImportStatusEnum.APP_BUILD_ERROR.getStatus();
                } else if (appBuildTask != null && appBuildTask.getStatus() == 4) {
                    appDesc.setStatus(AppStatusEnum.STATUS_PUBLISHED.getStatus());
                    appService.update(appDesc);
                    status = AppImportStatusEnum.APP_BUILD_END.getStatus();
                } else {
                    status = AppImportStatusEnum.APP_BUILD_START.getStatus();
                }
            } else {
                status = AppImportStatusEnum.APP_BUILD_INIT.getStatus();
            }
        }
        if (status >= 30 && status < 40) {
            //数据迁移阶段
            long migrateId = appImport.getMigrateId();
            if (migrateId > 0) {
                AppDataMigrateStatus appDataMigrateStatus = appDataMigrateStatusDao.getByMigrateId(migrateId);
                int migrateStatus = appDataMigrateStatus.getStatus();
                if (migrateStatus == 1) {
                    status = AppImportStatusEnum.MIGRATE_END.getStatus();
                } else if (migrateStatus == 2) {
                    status = AppImportStatusEnum.MIGRATE_ERROR.getStatus();
                } else {
                    status = AppImportStatusEnum.MIGRATE_START.getStatus();
                }
            }
        }
        if (status == AppImportStatusEnum.MIGRATE_END.getStatus()) {
            model.addAttribute("appId", appId);
        }

        if (oldStatus != status) {
            appImport.setStatus(status);
            appImportService.update(appImport);
        }
        model.addAttribute("appImport", appImport);
        model.addAttribute("appImportStatusMap", Arrays.stream(AppImportStatusEnum.values()).collect(Collectors.toMap(AppImportStatusEnum::getStatus, Function.identity())));
        return new ModelAndView("/manage/appImport/appImport");
    }

    @RequestMapping(value = "/preRebuildApp")
    public ModelAndView preRebuildApp(HttpServletRequest request, HttpServletResponse response, Model model) {
        long importId = NumberUtils.toLong(request.getParameter("importId"));
        long appId = NumberUtils.toLong(request.getParameter("appId"));
        AppImport appImport = appImportService.get(importId);
        appImport.setStatus(AppImportStatusEnum.APP_BUILD_INIT.getStatus());
        appImport.setAppBuildTaskId(0);

        AppDesc appDesc = appService.getByAppId(appId);
        if (appDesc != null) {
            //offline instances
            List<InstanceInfo> instanceInfos = instanceDao.getInstListByAppId(appId);
            int type = appDesc.getType();
            if (instanceInfos != null) {
                instanceInfos.parallelStream().map(instanceInfo -> instanceOffline(appId, instanceInfo, type)).collect(Collectors.toList());
            }
            appDesc.setStatus(AppStatusEnum.STATUS_INITIALIZE.getStatus());
            if (appService.update(appDesc) > 0 && appImportService.update(appImport) > 0) {
                model.addAttribute("success", 1);
            }
        }

        appImportService.update(appImport);
        return new ModelAndView("");
    }

    @RequestMapping(value = "/preReMigrate")
    public ModelAndView preReMigrate(HttpServletRequest request, HttpServletResponse response, Model model) {
        long importId = NumberUtils.toLong(request.getParameter("importId"));
        long appId = NumberUtils.toLong(request.getParameter("appId"));
        AppImport appImport = appImportService.get(importId);
        appImport.setStatus(AppImportStatusEnum.APP_BUILD_END.getStatus());
        appImport.setMigrateId(0);

        AppDesc appDesc = appService.getByAppId(appId);
        if (appDesc != null) {
            //todo 清空数据可能耗时太长
            if (cleanAppData(appId) && appImportService.update(appImport) > 0) {
                model.addAttribute("success", 1);
            }
        }

        appImportService.update(appImport);
        return new ModelAndView("");
    }

    private boolean cleanAppData(long appId) {
        List<InstanceInfo> instanceList = instanceDao.getInstListByAppId(appId);
        // 开始清除
        for (InstanceInfo instance : instanceList) {
            if (instance.getStatus() != InstanceStatusEnum.GOOD_STATUS.getStatus()) {
                continue;
            }
            String host = instance.getIp();
            int port = instance.getPort();
            // master + 非sentinel节点
            BooleanEnum isMater = redisCenter.isMaster(appId, host, port);
            if (isMater == BooleanEnum.TRUE && !TypeUtil.isRedisSentinel(instance.getType())) {
                //异步线程处理
                AsyncThreadPoolFactory.DEFAULT_ASYNC_THREAD_POOL.execute(new Runnable() {
                    @Override
                    public void run() {
                        Jedis jedis = redisCenter.getJedis(appId, host, port);
                        jedis.getClient().setConnectionTimeout(1000);
                        jedis.getClient().setSoTimeout(60000);
                        try {
                            logger.warn("{}:{} start clear data", host, port);
                            long start = System.currentTimeMillis();
                            String result = jedis.flushAll();
                            logger.warn("{}:{} finish clear data :{}, cost time:{} ms", host, port, result,
                                    (System.currentTimeMillis() - start));
                        } catch (Exception e) {
                            logger.error("clear redis: " + e.getMessage(), e);
                        } finally {
                            jedis.close();
                        }
                    }
                });
            }
        }
        return true;
    }

    private boolean instanceOffline(long appId, InstanceInfo instanceInfo, int type) {
        final String ip = instanceInfo.getIp();
        final int port = instanceInfo.getPort();
        boolean isShutdown = TypeUtil.isRedisType(type) ? redisCenter.shutdown(appId, ip, port) : true;
        if(isShutdown){
            isShutdown = redisCenter.checkShutdownSuccess(instanceInfo);
        }
        if (isShutdown) {
            instanceInfo.setStatus(InstanceStatusEnum.OFFLINE_STATUS.getStatus());
            instanceDao.update(instanceInfo);
        } else {
            return false;
        }
        return true;
    }

    @RequestMapping(value = "/goOn")
    public ModelAndView goOn(HttpServletRequest request, HttpServletResponse response, Model model) {
        long importId = NumberUtils.toLong(request.getParameter("importId"));
        int status = NumberUtils.toInt(request.getParameter("status"));
        long migrateId = NumberUtils.toLong(request.getParameter("migrateId"));
        long appBuildTaskId = NumberUtils.toLong(request.getParameter("appBuildTaskId"));
        AppImport appImport = appImportService.get(importId);
        if (status > 0) {
            appImport.setStatus(status);
        }
        if (migrateId > 0) {
            appImport.setMigrateId(migrateId);
        }
        if (appBuildTaskId > 0) {
            appImport.setAppBuildTaskId(appBuildTaskId);
        }
        appImportService.update(appImport);
        model.addAttribute("success", 1);
        model.addAttribute("message", "成功");
        return new ModelAndView("");
    }

    @RequestMapping(value = "/check")
    public ModelAndView check(HttpServletRequest request, HttpServletResponse response, Model model) {

        int type = NumberUtils.toInt(request.getParameter("type"));
        String appInstanceInfo = request.getParameter("appInstanceInfo");
        String password = request.getParameter("password");
        ImportAppResult importAppResult = importAppCenter.check(type, appInstanceInfo, password);
        model.addAttribute("status", importAppResult.getStatus());
        model.addAttribute("message", importAppResult.getMessage());
        return new ModelAndView("");
    }

    @RequestMapping(value = "/add")
    public ModelAndView add(HttpServletRequest request,
                            HttpServletResponse response, Model model) {
        AppDesc appDesc = genAppDesc(request);
        String appInstanceInfo = request.getParameter("appInstanceInfo");
        logger.warn("appDesc:" + appDesc);
        logger.warn("appInstanceInfo: " + appInstanceInfo);

        // 不需要对格式进行检验,check已经做过了。
        boolean isSuccess = importAppCenter.importAppAndInstance(appDesc, appInstanceInfo);
        logger.warn("import app result is {}", isSuccess);

        model.addAttribute("status", isSuccess ? 1 : 0);
        return new ModelAndView("");
    }

    /**
     * 生成AppDesc
     *
     * @param request
     * @return
     */
    private AppDesc genAppDesc(HttpServletRequest request) {
        // 当前用户
        AppUser currentUser = getUserInfo(request);
        // 当前时间
        Date date = new Date();
        // 组装Appdesc
        AppDesc appDesc = new AppDesc();
        appDesc.setName(request.getParameter("name"));
        appDesc.setIntro(request.getParameter("intro"));
        appDesc.setOfficer(request.getParameter("officer"));
        appDesc.setType(NumberUtils.toInt(request.getParameter("type")));
        appDesc.setIsTest(NumberUtils.toInt(request.getParameter("isTest")));
        appDesc.setMemAlertValue(NumberUtils.toInt(request.getParameter("memAlertValue")));
        appDesc.setAppKey(request.getParameter("password"));
        appDesc.setUserId(currentUser.getId());
        appDesc.setStatus(2);
        appDesc.setCreateTime(date);
        appDesc.setPassedTime(date);
        appDesc.setVerId(1);

        return appDesc;
    }

}
