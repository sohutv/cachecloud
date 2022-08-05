package com.sohu.cache.web.controller;


import com.sohu.cache.constant.*;
import com.sohu.cache.dao.AppUserDao;
import com.sohu.cache.dao.ResourceDao;
import com.sohu.cache.entity.*;
import com.sohu.cache.machine.MachineCenter;
import com.sohu.cache.ssh.SSHUtil;
import com.sohu.cache.stats.app.AppDataMigrateCenter;
import com.sohu.cache.stats.app.RedisMigrateToolCenter;
import com.sohu.cache.stats.app.RedisShakeCenter;
import com.sohu.cache.task.constant.ResourceEnum;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.web.enums.SuccessEnum;
import com.sohu.cache.web.service.AppImportService;
import com.sohu.cache.web.service.AppService;
import com.sohu.cache.web.service.ResourceService;
import com.sohu.cache.web.util.Page;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 应用数据迁移入口
 *
 * @author leifu
 * @Date 2016-6-8
 * @Time 下午11:10:34
 */
@Controller
@RequestMapping("/data/migrate")
public class AppDataMigrateController extends BaseController {

    private static Set<String> MIGRATE_SAMPLE_USEFUL_LINES = new HashSet<String>();

    static {
        MIGRATE_SAMPLE_USEFUL_LINES.add("Checked keys");
        MIGRATE_SAMPLE_USEFUL_LINES.add("Inconsistent value keys");
        MIGRATE_SAMPLE_USEFUL_LINES.add("Inconsistent expire keys");
        MIGRATE_SAMPLE_USEFUL_LINES.add("Other check error keys");
        MIGRATE_SAMPLE_USEFUL_LINES.add("Checked OK keys");
    }

    @Autowired
    private AppDataMigrateCenter appDataMigrateCenter;
    @Resource(name = "redisMigrateToolCenter")
    private RedisMigrateToolCenter redisMigrateToolCenter;
    @Autowired
    private RedisShakeCenter redisShakeCenter;
    @Resource(name = "appService")
    private AppService appService;
    @Resource(name = "machineCenter")
    private MachineCenter machineCenter;
    @Autowired
    private ResourceDao resourceDao;
    @Autowired
    private AppUserDao appUserDao;
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private AppImportService appImportService;


    @RequestMapping("/index")
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response, Model model,
                              String tabTag,
                              AppDataMigrateSearch appDataMigrateSearch) {
        List<AppUser> adminList = appUserDao.getAdminList();
        model.addAttribute("adminList", adminList);

        // 分页相关
        int totalCount = appDataMigrateCenter.getMigrateTaskCount(appDataMigrateSearch);
        int pageNo = NumberUtils.toInt(request.getParameter("pageNo"), 1);
        Page page = new Page(pageNo, 15, totalCount);
        appDataMigrateSearch.setPage(page);

        List<AppDataMigrateStatus> appDataMigrateStatusList = appDataMigrateCenter.search(appDataMigrateSearch);
        model.addAttribute("page", page);
        model.addAttribute("appDataMigrateStatusList", appDataMigrateStatusList);
        model.addAttribute("appDataMigrateSearch", appDataMigrateSearch);
        model.addAttribute("tabTag", tabTag);
        model.addAttribute("appMigrateActive", SuccessEnum.SUCCESS.value());

        return new ModelAndView("manage/migrate/list");
    }

    /**
     * 初始化界面
     *
     * @return
     */
    @RequestMapping(value = "/init")
    public ModelAndView init(HttpServletRequest request, Model model) {
        List<MachineInfo> machineInfoList = machineCenter.getMachineInfoByType(MachineInfoEnum.TypeEnum.REDIS_MIGRATE_TOOL);
        Map<String, Integer> machineInfoMap = machineInfoList.stream().collect(Collectors.toMap(
                machineInfo -> machineInfo.getIp(),
                machineInfo -> getMigrateMachineUsed(machineInfo.getIp())));
        List<SystemResource> resourcelist = resourceService.getResourceList(ResourceEnum.TOOL.getValue());
        model.addAttribute("resourcelist", resourcelist);
        model.addAttribute("machineInfoMap", machineInfoMap);

        long importId = NumberUtils.toLong(request.getParameter("importId"));
        if (importId > 0) {
            AppImport appImport = appImportService.get(importId);
            model.addAttribute("importId", importId);
            model.addAttribute("targetAppId", appImport.getAppId());
            model.addAttribute("sourceServers", appImport.getInstanceInfo());
            model.addAttribute("redisSourcePass", appImport.getRedisPassword());
            model.addAttribute("sourceType", appImport.getSourceType());
            model.addAttribute("sourceDataType", 0);
            model.addAttribute("redisSourceVersion", appImport.getRedisVersionName());
        }

        return new ModelAndView("migrate/init");
    }

    private int getMigrateMachineUsed(String migrateMachineIp) {
        try {
            String cmd = "ps -ef | grep redis-shake | grep -v grep | grep -v tail";
            String response = SSHUtil.execute(migrateMachineIp, cmd);
            if (StringUtils.isNotEmpty(response)) {
                String[] redis_shake_count = response.split(ConstUtils.NEXT_LINE);
                return redis_shake_count.length;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return 0;
    }

    /**
     * 检查配置
     *
     * @return
     */
    @RequestMapping(value = "/check")
    public ModelAndView check(HttpServletRequest request, Model model) {
        //相关参数
        String migrateMachineIp = request.getParameter("migrateMachineIp");
        String sourceRedisMigrateIndex = request.getParameter("sourceRedisMigrateIndex");
        AppDataMigrateEnum sourceRedisMigrateEnum = AppDataMigrateEnum.getByIndex(NumberUtils.toInt(sourceRedisMigrateIndex, -1));
        String sourceServers = request.getParameter("sourceServers");
        String targetRedisMigrateIndex = request.getParameter("targetRedisMigrateIndex");
        AppDataMigrateEnum targetRedisMigrateEnum = AppDataMigrateEnum.getByIndex(NumberUtils.toInt(targetRedisMigrateIndex, -1));
        String targetServers = request.getParameter("targetServers");
        String redisSourcePass = request.getParameter("redisSourcePass");
        String redisTargetPass = request.getParameter("redisTargetPass");

        //检查返回结果
//        int migrateTool = NumberUtils.toInt(request.getParameter("migrateTool"), 0);
        int versionid = NumberUtils.toInt(request.getParameter("versionid"));
        SystemResource resource = resourceService.getResourceById(versionid);

        if (resource == null) {
            return null;
        }
        // 检验机器安装环境
        redisConfigTemplateService.checkAndInstallRedisTool(migrateMachineIp, resource);

        AppDataMigrateResult redisMigrateResult = null;
        if (resource.getName().indexOf("redis-shake") > -1) {
            redisMigrateResult = redisShakeCenter.check(migrateMachineIp, sourceRedisMigrateEnum, sourceServers, targetRedisMigrateEnum, targetServers, redisSourcePass, redisTargetPass, resource);
        } else if (resource.getName().indexOf("redis-migrate-tool") > -1) {
            redisMigrateResult = redisMigrateToolCenter.check(migrateMachineIp, sourceRedisMigrateEnum, sourceServers, targetRedisMigrateEnum, targetServers, redisSourcePass, redisTargetPass, resource);
        }
        model.addAttribute("status", redisMigrateResult.getStatus());
        model.addAttribute("message", redisMigrateResult.getMessage());
        /*AppDataMigrateResult redisMigrateResult = migrateTool == 1 ?
                redisMigrateToolCenter.check(migrateMachineIp, sourceRedisMigrateEnum, sourceServers, targetRedisMigrateEnum, targetServers, redisSourcePass, redisTargetPass)
                : redisShakeCenter.check(migrateMachineIp, sourceRedisMigrateEnum, sourceServers, targetRedisMigrateEnum, targetServers, redisSourcePass, redisTargetPass);
        model.addAttribute("status", redisMigrateResult.getStatus());
        model.addAttribute("message", redisMigrateResult.getMessage());*/

        return new ModelAndView("");
    }

    /**
     * 开始迁移
     *
     * @return
     */
    @RequestMapping(value = "/start")
    public ModelAndView start(HttpServletRequest request, Model model) {
        //相关参数
        String migrateMachineIp = request.getParameter("migrateMachineIp");
        String sourceRedisMigrateIndex = request.getParameter("sourceRedisMigrateIndex");
        AppDataMigrateEnum sourceRedisMigrateEnum = AppDataMigrateEnum.getByIndex(NumberUtils.toInt(sourceRedisMigrateIndex, -1));
        String sourceServers = request.getParameter("sourceServers");
        String targetRedisMigrateIndex = request.getParameter("targetRedisMigrateIndex");
        AppDataMigrateEnum targetRedisMigrateEnum = AppDataMigrateEnum.getByIndex(NumberUtils.toInt(targetRedisMigrateIndex, -1));
        String targetServers = request.getParameter("targetServers");
        long sourceAppId = NumberUtils.toLong(request.getParameter("sourceAppId"));
        long targetAppId = NumberUtils.toLong(request.getParameter("targetAppId"));
        String redisSourcePass = request.getParameter("redisSourcePass");
        String redisTargetPass = request.getParameter("redisTargetPass");
        String redisSourceVersion = request.getParameter("redisSourceVersion");
        String redisTargetVersion = request.getParameter("redisTargetVersion");
        int source_rdb_parallel = NumberUtils.toInt(request.getParameter("source_rdb_parallel"), 8);
        int parallel = NumberUtils.toInt(request.getParameter("parallel"), 16);

        AppUser appUser = getUserInfo(request);
        long userId = appUser == null ? 0 : appUser.getId();

        // 不需要对格式进行检验,check已经做过了，开始迁移
//        int migrateTool = NumberUtils.toInt(request.getParameter("migrateTool"), 0);
        int versionid = NumberUtils.toInt(request.getParameter("versionid"));
        SystemResource resource = resourceService.getResourceById(versionid);

        if (resource == null) {
            return null;
        }
        AppDataMigrateStatus appDataMigrateStatus = new AppDataMigrateStatus();
        if (resource.getName().indexOf("redis-shake") > -1) {
            appDataMigrateStatus = redisShakeCenter.migrate(migrateMachineIp, source_rdb_parallel, parallel,
                    sourceRedisMigrateEnum, sourceServers,
                    targetRedisMigrateEnum, targetServers,
                    sourceAppId, targetAppId,
                    redisSourcePass, redisTargetPass,
                    redisSourceVersion, redisTargetVersion,
                    userId, resource);
        } else if (resource.getName().indexOf("redis-migrate-tool") > -1) {
            appDataMigrateStatus = redisMigrateToolCenter.migrate(migrateMachineIp, sourceRedisMigrateEnum, sourceServers,
                    targetRedisMigrateEnum, targetServers, sourceAppId, targetAppId, redisSourcePass, redisTargetPass, userId, resource);
        }
        model.addAttribute("status", 1);
        model.addAttribute("migrateId", appDataMigrateStatus.getMigrateId());

        return new ModelAndView("");
    }

    /**
     * 停掉迁移任务
     *
     * @return
     */
    @RequestMapping(value = "/stop")
    public ModelAndView stop(HttpServletRequest request, Model model) {
        //任务id：查到任务相关信息
        long id = NumberUtils.toLong(request.getParameter("id"));
        int migrateTool = NumberUtils.toInt(request.getParameter("migrateTool"), 0);


        AppDataMigrateResult stopMigrateResult = migrateTool == 1 ?
                redisMigrateToolCenter.stopMigrate(id)
                : redisShakeCenter.stopMigrate(id);
        model.addAttribute("status", stopMigrateResult.getStatus());
        model.addAttribute("message", stopMigrateResult.getMessage());
        return new ModelAndView("");
    }

    /**
     * 查看迁移日志
     *
     * @return
     */
    @RequestMapping(value = "/log")
    public ModelAndView log(HttpServletRequest request, Model model) {
        //任务id：查到任务相关信息
        long id = NumberUtils.toLong(request.getParameter("id"));
        int pageSize = NumberUtils.toInt(request.getParameter("pageSize"), 0);
        if (pageSize == 0) {
            pageSize = 100;
        }

        String log = appDataMigrateCenter.showDataMigrateLog(id, pageSize);
        model.addAttribute("logList", Arrays.asList(log.split(ConstUtils.NEXT_LINE)));
        return new ModelAndView("migrate/log");
    }

    /**
     * 查看迁移配置
     *
     * @return
     */
    @RequestMapping(value = "/config")
    public ModelAndView config(HttpServletRequest request, Model model) {
        //任务id：查到任务相关信息
        long id = NumberUtils.toLong(request.getParameter("id"));
        String config = appDataMigrateCenter.showDataMigrateConf(id);
        model.addAttribute("configList", Arrays.asList(config.split(ConstUtils.NEXT_LINE)));
        return new ModelAndView("migrate/config");
    }

    /**
     * 查看迁移进度
     *
     * @return
     */
    @RequestMapping(value = "/process")
    public ModelAndView showProcess(HttpServletRequest request, Model model) {
        long id = NumberUtils.toLong(request.getParameter("id"));
        int migrateTool = NumberUtils.toInt(request.getParameter("migrateTool"), 0);
        if (migrateTool == 0) {
            String process = redisShakeCenter.showProcess(id);
            model.addAttribute("process", process);
            return new ModelAndView("");
        } else {
            Map<RedisMigrateToolConstant, Map<String, Object>> migrateToolStatMap = appDataMigrateCenter.showMiragteToolProcess(id);
            model.addAttribute("migrateToolStatMap", migrateToolStatMap);
            return new ModelAndView("migrate/process");
        }
    }

    /**
     * 数据校验
     *
     * @return
     */
    @RequestMapping(value = "/checkData")
    public ModelAndView checkData(HttpServletRequest request, Model model) {
        long id = NumberUtils.toLong(request.getParameter("id"));
        int migrateTool = NumberUtils.toInt(request.getParameter("migrateTool"), 0);
        int comparemode = NumberUtils.toInt(request.getParameter("comparemode"), 3);
        int nums = 1000 + new Random().nextInt(2000);
        CommandResult commandResult;
        List<String> checkDataResultList = new ArrayList<String>();
        if (migrateTool == 1) {
            // redis-migrate-tool 采样校验
            commandResult = redisMigrateToolCenter.sampleCheckData(id, nums);
            String message = commandResult.getResult();
            checkDataResultList.add("一共随机检验了" + nums + "个key" + ",检查结果如下:");
            String[] lineArr = message.split(ConstUtils.NEXT_LINE);
            for (String line : lineArr) {
                if (StringUtils.isBlank(line)) {
                    continue;
                }
                // 行数太多显示会有问题
                if (lineArr.length > 100 && !isUsefulLine(line)) {
                    continue;
                }
                //message格式显示有点问题
                line = line.replace("[0m", "");
                line = line.replace("[31m", "");
                line = line.replace("[33m", "");
                checkDataResultList.add(line.trim());
            }
            model.addAttribute("checkDataResultList", checkDataResultList);
            model.addAttribute("checkDataCommand", commandResult.getCommand());
        } else {
            // redis-full-check
            commandResult = redisShakeCenter.checkData(id, 1000, comparemode);
            model.addAttribute("checkDataCommand", commandResult.getCommand());
        }
        return new ModelAndView("migrate/checkData");
    }

    /**
     * 查看迁移日志
     *
     * @return
     */
    @RequestMapping(value = "/checkData/log")
    public ModelAndView checkDatalog(HttpServletRequest request, Model model) {
        //任务id：查到任务相关信息
        long id = NumberUtils.toLong(request.getParameter("id"));
        int pageSize = NumberUtils.toInt(request.getParameter("pageSize"), 0);
        if (pageSize == 0) {
            pageSize = 100;
        }

        String log = appDataMigrateCenter.showCheckDataLog(id, pageSize);
        model.addAttribute("checkDatalogList", Arrays.asList(log.split(ConstUtils.NEXT_LINE)));
        return new ModelAndView("migrate/checkDataLog");
    }

    private boolean isUsefulLine(String line) {
        for (String usefulLine : MIGRATE_SAMPLE_USEFUL_LINES) {
            if (line.contains(usefulLine)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 通过应用id获取可用的Redis实例信息
     *
     * @return
     */
    @RequestMapping(value = "/appInstanceList")
    public ModelAndView appInstanceList(HttpServletRequest request, Model model) {
        String appIdStr = request.getParameter("appId");
        String migrateToolStr = request.getParameter("migrateTool");
        long appId = NumberUtils.toLong(appIdStr);
        int migrateTool = NumberUtils.toInt(migrateToolStr);
        AppDesc appDesc = appService.getByAppId(appId);
        String instances = migrateTool == 1 ?
                redisMigrateToolCenter.getAppInstanceListForRedisMigrateTool(appId)
                : redisShakeCenter.getAppInstanceListForRedisShake(appId);
        model.addAttribute("instances", instances);
        model.addAttribute("password", appDesc == null ? "" : appDesc.getAppPassword());
        model.addAttribute("appType", appDesc == null ? -1 : appDesc.getType());
        model.addAttribute("appName", appDesc == null ? "" : appDesc.getName());
        model.addAttribute("redisVersion", appDesc == null ? "" : resourceDao.getResourceById(appDesc.getVersionId()).getName());
        return new ModelAndView("");
    }
}
