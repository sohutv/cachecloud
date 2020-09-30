package com.sohu.cache.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.sohu.cache.constant.OperateResult;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.redis.AssistRedisService;
import com.sohu.cache.task.TaskService;
import com.sohu.cache.task.entity.TaskQueue;
import com.sohu.cache.task.entity.TaskSearch;
import com.sohu.cache.task.entity.TaskStepFlow;
import com.sohu.cache.task.entity.TaskStepMeta;
import com.sohu.cache.util.ConstUtils;
import com.sohu.cache.web.enums.SuccessEnum;
import com.sohu.cache.web.util.Page;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务管理
 */
@Controller
@RequestMapping("manage/task")
public class TaskController extends BaseController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private AssistRedisService assistRedisService;

    @RequestMapping(value = "/list")
    public ModelAndView taskQueueList(HttpServletRequest request, HttpServletResponse response, Model model,
            TaskSearch taskSearch) {
        List<TaskQueue> taskQueueList = null;

        Long searchTaskId = NumberUtils.toLong(request.getParameter("searchTaskId"));
        int pageNo = NumberUtils.toInt(request.getParameter("pageNo"), 1);
        int pageSize = NumberUtils.toInt(request.getParameter("pageSize"), 30);
        if (searchTaskId != null && searchTaskId > 0) {
            taskQueueList = taskService.getTaskQueueTreeByTaskId(searchTaskId);
            Page page = new Page(pageNo, pageSize, taskQueueList.size());
            model.addAttribute("page", page);
            taskSearch.setPage(page);
        } else {
            // 分页相关:count
            int totalCount = taskService.getTaskQueueCount(taskSearch);
            Page page = new Page(pageNo, pageSize, totalCount);
            model.addAttribute("page", page);
            // 分页相关:list
            taskSearch.setPage(page);
            taskQueueList = taskService.getTaskQueueList(taskSearch);
        }
        //填充任务流
        for (TaskQueue taskQueue : taskQueueList) {
            List<TaskStepFlow> taskStepFlowList = taskService.getTaskStepFlowList(taskQueue.getId());
            taskQueue.setTaskStepFlowList(taskStepFlowList);
        }
        model.addAttribute("searchTaskId", searchTaskId);
        model.addAttribute("taskQueueList", taskQueueList);
        model.addAttribute("taskActive", SuccessEnum.SUCCESS.value());
        return new ModelAndView("manage/task/queueList");
    }

    @RequestMapping(value = "/execute")
    public ModelAndView execute(HttpServletRequest request,
            HttpServletResponse response, Model model) {
        final long taskId = NumberUtils.toLong(request.getParameter("taskId"));
        TaskQueue taskQueue = taskService.getTaskQueueById(taskId);
        //TODO
        if (taskQueue != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    taskService.executeTask(taskId);
                }
            }).start();
        }
        return new ModelAndView("redirect:/manage/task/flow?taskId=" + taskId);
    }

    @RequestMapping(value = "/changeParam")
    public ModelAndView changeParam(HttpServletRequest request,
            HttpServletResponse response, Model model) {
        long taskId = NumberUtils.toLong(request.getParameter("taskId"));
        String prettyParam = request.getParameter("prettyParamText");
        JSONObject jsonObject = JSONObject.parseObject(prettyParam);
        String param = jsonObject.toJSONString();
        OperateResult operateResult = taskService.updateParam(taskId, param);

        model.addAttribute("result", operateResult.isSuccess() ? 1 : 0);
        model.addAttribute("message", operateResult.getMessage());

        return new ModelAndView("");
    }

    @RequestMapping(value = "/changeTaskFlowStatus")
    public ModelAndView changeTaskFlowStatus(HttpServletRequest request,
            HttpServletResponse response, Model model) {
        long taskFlowId = NumberUtils.toLong(request.getParameter("taskFlowId"));
        int status = NumberUtils.toInt(request.getParameter("status"));
        OperateResult operateResult = taskService.updateTaskFlowStatus(taskFlowId, status);

        model.addAttribute("result", operateResult.isSuccess() ? 1 : 0);
        model.addAttribute("message", operateResult.getMessage());

        return new ModelAndView("");
    }

    @RequestMapping(value = "/flow")
    public ModelAndView taskFlowList(HttpServletRequest request,
            HttpServletResponse response, Model model) {
        long taskId = NumberUtils.toLong(request.getParameter("taskId"));

        //任务
        TaskQueue taskQueue = taskService.getTaskQueueById(taskId);
        model.addAttribute("taskQueue", taskQueue);

        long appId = taskQueue.getAppId();
        AppDesc appDesc = appService.getByAppId(appId);
        model.addAttribute("appDesc", appDesc);

        //任务流描述
        String className = taskQueue.getClassName();
        List<TaskStepMeta> taskStepMetaList = taskService.getTaskStepMetaList(className);

        //任务流列表
        List<TaskStepFlow> taskStepFlowList = taskService.getTaskStepFlowList(taskId);
        model.addAttribute("taskStepFlowList", taskStepFlowList);

        List<String> logList = new ArrayList<String>();
        for (TaskStepFlow taskStepFlow : taskStepFlowList) {
            String line = String
                    .format("==========================%s %d======================", taskStepFlow.getStepName(),
                            taskStepFlow.getOrderNo());
            logList.add(line);
            String taskFlowIdKey = ConstUtils.getTaskFlowRedisKey(String.valueOf(taskStepFlow.getId()));
            List<String> tempList = assistRedisService.lrange(taskFlowIdKey, 0, -1);
            logList.addAll(tempList);
        }
        model.addAttribute("logList", logList);

        Map<String, List<String>> stepLogListMap = new HashMap<String, List<String>>();
        for (TaskStepFlow taskStepFlow : taskStepFlowList) {
            String stepName = taskStepFlow.getStepName();
            String taskFlowIdKey = ConstUtils.getTaskFlowRedisKey(String.valueOf(taskStepFlow.getId()));
             List<String> tempList = assistRedisService.lrange(taskFlowIdKey, 0, -1);
            stepLogListMap.put(stepName, tempList);
        }
        model.addAttribute("stepLogListMap", stepLogListMap);

        //task Progress
        taskQueue.setTaskStepFlowList(taskStepFlowList);

        //获取当前执行步骤
        TaskStepFlow currentTaskStepFlow = taskService.getCurrentTaskStepFlow(taskId);
        model.addAttribute("currentTaskStepFlow", currentTaskStepFlow);

        //填充taskStepMeta
        fillTaskStepMeta(taskStepFlowList, taskStepMetaList, currentTaskStepFlow);

        model.addAttribute("taskActive", SuccessEnum.SUCCESS.value());
        return new ModelAndView("manage/task/flowList");
    }

    /**
     * 填充任务流描述
     *
     * @param taskStepFlowList
     * @param taskStepMetaList
     */
    private void fillTaskStepMeta(List<TaskStepFlow> taskStepFlowList, List<TaskStepMeta> taskStepMetaList,
            TaskStepFlow currentTaskStepFlow) {
        //生成Map<class-step,TaskStepMeta>
        Map<String, TaskStepMeta> classStepTaskStepMetaMap = new HashMap<String, TaskStepMeta>();
        for (TaskStepMeta taskStepMeta : taskStepMetaList) {
            String key = generateClassStepKey(taskStepMeta.getClassName(), taskStepMeta.getStepName());
            classStepTaskStepMetaMap.put(key, taskStepMeta);
        }

        //遍历TaskStepFlow
        for (TaskStepFlow taskStepFlow : taskStepFlowList) {
            String key = generateClassStepKey(taskStepFlow.getClassName(), taskStepFlow.getStepName());
            TaskStepMeta taskStepMeta = (TaskStepMeta) MapUtils
                    .getObject(classStepTaskStepMetaMap, key, new TaskStepMeta());
            taskStepFlow.setTaskStepMeta(taskStepMeta);
        }

        //当前TaskStepFlow
        if (currentTaskStepFlow != null) {
            String currentKey = generateClassStepKey(currentTaskStepFlow.getClassName(),
                    currentTaskStepFlow.getStepName());
            TaskStepMeta currentTaskStepMeta = (TaskStepMeta) MapUtils
                    .getObject(classStepTaskStepMetaMap, currentKey, new TaskStepMeta());
            currentTaskStepFlow.setTaskStepMeta(currentTaskStepMeta);
        }
    }

    private String generateClassStepKey(String className, String stepName) {
        return className + "-" + stepName;
    }
}
