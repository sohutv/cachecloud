package com.sohu.cache.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.sohu.cache.constant.AppCheckEnum;
import com.sohu.cache.constant.DiagnosticTypeEnum;
import com.sohu.cache.dao.AppDao;
import com.sohu.cache.dao.ResourceDao;
import com.sohu.cache.entity.*;
import com.sohu.cache.redis.RedisCenter;
import com.sohu.cache.stats.admin.CoreAppsStatCenter;
import com.sohu.cache.task.TaskService;
import com.sohu.cache.util.StringUtil;
import com.sohu.cache.web.enums.SuccessEnum;
import com.sohu.cache.web.service.DiagnosticToolService;
import com.sohu.cache.web.service.ToolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 应用诊断工具
 * Created by rucao on 2018/11/9
 */
@Controller
@RequestMapping("/manage/app/tool")
public class AppToolController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(AppToolController.class);

    @Resource
    private RedisCenter redisCenter;
    @Resource
    private ToolService toolService;
    @Resource
    private AppDao appDao;
    @Resource
    private CoreAppsStatCenter coreAppsStatCenter;
    @Autowired
    private DiagnosticToolService diagnosticToolService;
    @Autowired
    private ResourceDao resourceDao;
    @Autowired
    private TaskService taskService;


    /**
     * 跳转到主页
     *
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping("/index")
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response,
                              Model model, String tabTag, Long appId, Long parentTaskId, Long auditId, Integer diagnosticStatus) {
        model.addAttribute("tabTag", tabTag);
        model.addAttribute("appId", appId);
        model.addAttribute("parentTaskId", parentTaskId);
        model.addAttribute("auditId", auditId);
        model.addAttribute("diagnosticStatus", diagnosticStatus);

        model.addAttribute("diagnosticActive", SuccessEnum.SUCCESS.value());
        return new ModelAndView("manage/diagnosticTool/list");
    }

    @RequestMapping(value = "/diagnostic/tool")
    public ModelAndView diagnosticToolIndex(HttpServletRequest request,
                                            HttpServletResponse response, Model model, String tabTag,
                                            Long appId, Long parentTaskId, Long auditId, Integer diagnosticStatus) {

        List<AppDesc> appDescList = appDao.getOnlineApps();
        appDescList.forEach(appDesc -> {
            String versionName = Optional.ofNullable(resourceService.getResourceById(appDesc.getVersionId())).map(ver -> ver.getName()).orElse("");
            appDesc.setVersionName(versionName);
        });
        Map<Long, AppDesc> appDescMap = appDescList.stream().collect(Collectors.toMap(AppDesc::getAppId, Function.identity()));
        model.addAttribute("appDescMap", appDescMap);


        if (tabTag == null || "redis-cli".equals(tabTag)) {
            return new ModelAndView("manage/diagnosticTool/diagnosticTool");
        }


        int type = DiagnosticTypeEnum.getDescKey(tabTag);
        List<DiagnosticTaskRecord> diagnosticTaskRecordList = diagnosticToolService.getDiagnosticTaskRecords(appId, parentTaskId, auditId, type, diagnosticStatus);
        model.addAttribute("diagnosticTaskRecordList", diagnosticTaskRecordList);
        model.addAttribute("appId", appId);
        model.addAttribute("parentTaskId", parentTaskId);
        model.addAttribute("auditId", auditId);
        model.addAttribute("type", type);
        model.addAttribute("diagnosticStatus", diagnosticStatus);

        if (DiagnosticTypeEnum.SCAN_KEY.getDesc().equals(tabTag)) {
            return new ModelAndView("manage/diagnosticTool/diagnosticScan");
        } else if (DiagnosticTypeEnum.BIG_KEY.getDesc().equals(tabTag)) {
            return new ModelAndView("manage/diagnosticTool/diagnosticMemUsed");
        } else if (DiagnosticTypeEnum.IDLE_KEY.getDesc().equals(tabTag)) {
            return new ModelAndView("manage/diagnosticTool/diagnosticIdleKey");
        } else if (DiagnosticTypeEnum.HOT_KEY.getDesc().equals(tabTag)) {
            return new ModelAndView("manage/diagnosticTool/diagnosticHotKey");
        } else if (DiagnosticTypeEnum.DEL_KEY.getDesc().equals(tabTag)) {
            return new ModelAndView("manage/diagnosticTool/diagnosticDelKey");
        } else if (DiagnosticTypeEnum.SLOT_ANALYSIS.getDesc().equals(tabTag)) {
            return new ModelAndView("manage/diagnosticTool/diagnosticSlot");
        }

        return new ModelAndView("");
    }


    @RequestMapping(value = "/diagnostic/appInstances")
    public ModelAndView getAppInstances(HttpServletRequest request,
                                        HttpServletResponse response, Model model, Long appId) {
        JSONObject json = new JSONObject();

        AppDesc appDesc = appService.getByAppId(appId);
        if (appDesc == null) {
            json.put("status", String.valueOf(SuccessEnum.FAIL.value()));
        } else {
            List<InstanceInfo> instanceInfos = appService.getAppOnlineInstanceInfo(appId);
            json.put("appInstanceList", instanceInfos);
            json.put("status", String.valueOf(SuccessEnum.SUCCESS.value()));
        }
        sendMessage(response, json.toString());
        return null;
    }


    @RequestMapping(value = "/diagnostic/submit")
    public ModelAndView submitDiagnostic(HttpServletRequest request,
                                         HttpServletResponse response, Model model, int type,
                                         Long auditId, Long appId, String nodes, String params) {


        if (auditId == null) {
            AppUser appUser = getUserInfo(request);
            AppDesc appDesc = appService.getByAppId(appId);
            AppAudit appAudit = appService.saveAppDiagnostic(appDesc, appUser, "应用诊断任务:" + DiagnosticTypeEnum.getKeyDesc(type));
            auditId = appAudit.getId();
        }
        appAuditDao.updateAppAuditUser(auditId, AppCheckEnum.APP_ALLOCATE_RESOURCE.value(), getUserInfo(request).getId());

        long taskId = -1l;
        if (type == DiagnosticTypeEnum.SCAN_KEY.getType()) {
            String[] paramArray = params.split(",");
            String pattern = paramArray.length > 0 ? paramArray[0] : "";
            int size = paramArray.length > 1 ? Integer.parseInt(paramArray[1]) : 20;

            taskId = taskService.addAppScanKeyTask(appId, auditId, nodes, pattern, size, 0);
        } else if (type == DiagnosticTypeEnum.BIG_KEY.getType()) {
            String[] paramArray = params.split(",");
            long fromBytes = paramArray.length > 0 ? Long.parseLong(paramArray[0]) : 10;
            int size = paramArray.length > 1 ? Integer.parseInt(paramArray[1]) : 20;

            taskId = taskService.addAppBigKeyTask(appId, nodes, fromBytes, -1, size, auditId, 0);
        } else if (type == DiagnosticTypeEnum.IDLE_KEY.getType()) {
            String[] paramArray = params.split(",");
            long idleTime = paramArray.length > 0 ? Integer.parseInt(paramArray[0]) : 7;
            int size = paramArray.length > 1 ? Integer.parseInt(paramArray[1]) : 20;

            taskId = taskService.addAppIdleKeyTask(appId, nodes, idleTime, size, auditId, 0);
        } else if (type == DiagnosticTypeEnum.HOT_KEY.getType()) {
            String[] paramArray = params.split(",");
            String command = paramArray.length > 0 ? paramArray[0] : "hotkey";

            taskId = taskService.addAppHotKeyTask(appId, nodes, command, auditId, 0);
        } else if (type == DiagnosticTypeEnum.DEL_KEY.getType()) {
            String[] paramArray = params.split(",");
            String pattern = paramArray.length > 0 ? paramArray[0] : "";

            taskId = taskService.addAppDelKeyTask(appId, nodes, pattern, auditId, 0);
        } else if (type == DiagnosticTypeEnum.SLOT_ANALYSIS.getType()) {
            taskId = taskService.addAppSlotAnalysisTask(appId, nodes, auditId, 0);
        }
        JSONObject json = new JSONObject();
        json.put("status", "success");
        json.put("taskId", taskId);
        sendMessage(response, json.toString());
        return null;
    }


    @RequestMapping(value = "/diagnostic/result")
    public ModelAndView diagnosticResultList(HttpServletRequest request,
                                             HttpServletResponse response, Model model,
                                             Long appId, Long parentTaskId, Long auditId, Integer type, Integer diagnosticStatus) {

        try {
            Map<Integer, String> diagnosticTypeMap = new HashMap<>();
            for (DiagnosticTypeEnum diagnosticType : DiagnosticTypeEnum.values()) {
                diagnosticTypeMap.put(diagnosticType.getType(), diagnosticType.getDesc());
            }
            model.addAttribute("diagnosticTypeMap", diagnosticTypeMap);

            List<AppDesc> appDescList = appDao.getOnlineApps();
            Map<Long, AppDesc> appDescMap = appDescList.stream().collect(Collectors.toMap(AppDesc::getAppId, Function.identity()));
            model.addAttribute("appDescMap", appDescMap);

            List<DiagnosticTaskRecord> diagnosticTaskRecordList = diagnosticToolService.getDiagnosticTaskRecords(appId, parentTaskId, auditId, type, diagnosticStatus);
            model.addAttribute("diagnosticTaskRecordList", diagnosticTaskRecordList);

            model.addAttribute("appId", appId);
            model.addAttribute("parentTaskId", parentTaskId);
            model.addAttribute("auditId", auditId);
            model.addAttribute("type", type);
            model.addAttribute("diagnosticStatus", diagnosticStatus);
            model.addAttribute("diagnosticActive", SuccessEnum.SUCCESS.value());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return new ModelAndView("manage/diagnosticTool/resultList");
    }

    @RequestMapping(value = "/diagnostic/data")
    public ModelAndView diagnosticResult(HttpServletRequest request,
                                         HttpServletResponse response, Model model, String redisKey, int type, boolean err) {
        JSONObject json = new JSONObject();
        if (!StringUtil.isBlank(redisKey)) {

            if (type == DiagnosticTypeEnum.SCAN_KEY.getType()) {
                List<String> result = diagnosticToolService.getScanDiagnosticData(redisKey);
                json.put("count", result.size());
                json.put("result", result);
            } else if (type == DiagnosticTypeEnum.BIG_KEY.getType()
                    || type == DiagnosticTypeEnum.IDLE_KEY.getType()
                    || type == DiagnosticTypeEnum.SLOT_ANALYSIS.getType()) {
                Map<String, String> result = diagnosticToolService.getDiagnosticDataMap(redisKey, type, err);
                json.put("count", result.size());
                json.put("result", result);
            } else if (type == DiagnosticTypeEnum.HOT_KEY.getType()) {
                String result = diagnosticToolService.getHotkeyDiagnosticData(redisKey);
                json.put("result", result == null ? "" : result.replaceAll("(\\r\\n|\\n|\\n\\r)", "<br/>"));
            }
        }
        json.put("status", String.valueOf(SuccessEnum.SUCCESS.value()));
        sendMessage(response, json.toString());
        return null;
    }

    @RequestMapping("/commandExecute")
    public ModelAndView getCommandExecute(HttpServletRequest request,
                                          HttpServletResponse response,
                                          Model model, Long appId, String node, String command, Integer timeout) {
        String result;
        if (appId != null && appId > 0 && !StringUtil.isBlank(node)) {
            model.addAttribute("appId", appId);
            model.addAttribute("node", node);

            String host = node.split(":")[0];
            int port = Integer.parseInt(node.split(":")[1]);
            result = redisCenter.executeAdminCommand(appId, host, port, command, timeout);
        } else {
            result = "error";
        }
        model.addAttribute("result", result);

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("status", SuccessEnum.SUCCESS.value());
        resultMap.put("result", result);
        sendMessage(response, JSONObject.toJSONString(resultMap));
        return null;
    }

    @PostMapping(value = "/diagnostic/sampleScan")
    public ModelAndView getSampleScan(HttpServletRequest request,
                                      HttpServletResponse response,
                                      Model model,
                                      Long appId, String nodes, String pattern) {

        List<String> sampleScanData = diagnosticToolService.getSampleScanData(appId, nodes, pattern);
        JSONObject json = new JSONObject();
        json.put("count", sampleScanData.size());
        json.put("result", sampleScanData);
        json.put("status", "success");
        sendMessage(response, json.toString());
        return null;
    }


    @PostMapping(value = "findInstancePatternKeys")
    public ModelAndView findInstancePatternKeys(Model model,
                                                long appId,
                                                String ip,
                                                int port,
                                                String pattern) {

        model.addAttribute("result", 1);
        return new ModelAndView("");
    }

    @PostMapping(value = "findInstanceBigKey")
    public ModelAndView findInstanceBigKey(Model model,
                                           long appId,
                                           String ip,
                                           int port,
                                           long startBytes,
                                           long endBytes) {
        List<String> instanceBigKeyList = redisCenter.findInstanceBigKey(appId, ip, port, startBytes, endBytes);
        model.addAttribute("instanceBigKeyList", instanceBigKeyList);
        return new ModelAndView("");
    }

    @PostMapping(value = "findClusterBigKey")
    public ModelAndView findClusterBigKey(Model model,
                                          long appId,
                                          long startBytes,
                                          long endBytes) {
        List<String> clusterBigKeyList = redisCenter.findClusterBigKey(appId, startBytes, endBytes);
        model.addAttribute("clusterBigKeyList", clusterBigKeyList);
        return new ModelAndView("");
    }

    @PostMapping(value = "findInstanceIdleKeys")
    public ModelAndView findInstanceIdleKeys(Model model,
                                             long appId,
                                             String ip,
                                             int port,
                                             long idleDays) {
        List<String> instanceIdleKeyList = redisCenter.findInstanceIdleKeys(appId, ip, port, idleDays);
        model.addAttribute("instanceIdleKeyList", instanceIdleKeyList);
        return new ModelAndView("");
    }

    @PostMapping(value = "findClusterIdleKeys")
    public ModelAndView findClusterIdleKeys(Model model,
                                            long appId,
                                            long idleDays) {
        List<String> clusterIdleKeyList = redisCenter.findClusterIdleKeys(appId, idleDays);
        model.addAttribute("clusterIdleKeyList", clusterIdleKeyList);
        return new ModelAndView("");
    }

    @PostMapping(value = "delInstancePatternKeys")
    public ModelAndView delInstancePatternKeys(Model model,
                                               long appId,
                                               String ip,
                                               int port,
                                               String pattern) {
        redisCenter.delInstancePatternKeys(appId, ip, port, pattern);
        model.addAttribute("result", 1);
        return new ModelAndView("");
    }

    @PostMapping(value = "delClusterPatternKey")
    public ModelAndView delClusterPatternKey(Model model,
                                             long appId,
                                             String pattern) {
        redisCenter.delClusterPatternKey(appId, pattern);
        model.addAttribute("result", 1);
        return new ModelAndView("");
    }


    @GetMapping(value = "topologyExam/{appid}")
    public ModelAndView topologyExamByAppid(HttpServletRequest request,
                                            HttpServletResponse response,
                                            Model model,
                                            @PathVariable("appid") long appid) {
        List<Map> res = toolService.topologyExamByAppid(appid);
        model.addAttribute("result", res);
        write(response, res.toString());
        return null;
    }

    @PostMapping(value = "topologyExam")
    public ModelAndView topologyExam(Model model,
                                     @RequestBody List<Long> appidList) {
        toolService.topologyExam(appidList);
        return null;
    }

    @PostMapping(value = "topologyExam/all")
    public ModelAndView topologyExamAll(@RequestParam Boolean examTest) {
        List<AppDesc> appDescList = appDao.getOnlineApps();
        List<Long> appidList = new ArrayList<Long>();
        for (AppDesc appDesc : appDescList) {
            if (examTest == false) {
                if (appDesc.getIsTest() == 0) {
                    appidList.add(appDesc.getAppId());
                }
            } else {
                appidList.add(appDesc.getAppId());
            }
        }
        toolService.topologyExam(appidList);
        return null;
    }

    @GetMapping("/restAppDescOfficer")
    public void restAppDescOfficer() {
        toolService.restAppDescOfficer();
    }


    @GetMapping("/sendExpAppsStatDataEmail")
    public ModelAndView sendExpAppsStatDataEmail(@RequestParam("searchDate") String searchDate) {
        logger.info("begin-sendExpAppsStatDataEmail");
        coreAppsStatCenter.sendExpAppsStatDataEmail(searchDate);
        logger.info("end-sendExpAppsStatDataEmail");
        return null;
    }
}
