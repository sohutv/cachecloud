package com.sohu.cache.web.controller;

import com.google.common.collect.Lists;
import com.sohu.cache.dao.MachineDao;
import com.sohu.cache.entity.AppDesc;
import com.sohu.cache.entity.MachineInfo;
import com.sohu.cache.task.TaskService;
import com.sohu.cache.task.tasks.daily.TopologyExamTask;
import com.sohu.cache.web.enums.ExamToolEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by rucao on 2019/1/25
 */
@Controller
@RequestMapping("/manage/tool")
public class ExamToolController extends BaseController{
    @Autowired
    private TaskService taskService;
    @Autowired
    private MachineDao machineDao;
    @Autowired
    TopologyExamTask topologyExamTask;

    @RequestMapping(value = "/topologyExam")
    public ModelAndView startTopologyExam(Model model,
                                          long appId){

        ArrayList<AppDesc> applist = new ArrayList<AppDesc>();
        AppDesc appDesc = appService.getByAppId(appId);
        if(appDesc.getAppId() > 0){
            applist.add(appDesc);
            Map<String, Object> info = topologyExamTask.check(applist);
            model.addAttribute("checkInfo",info);
        }
        return new ModelAndView("manage/appTool/appKeysDealTool");
    }

    @RequestMapping(value = "/topologyExam/online",method = RequestMethod.POST)
    public long startAllTopologyExam(){
        long taskId=taskService.addAppTopologyExamTask(true, ExamToolEnum.EXAM_NON_TEST.getValue(),0,0);
        return taskId;
    }

    @RequestMapping(value = "/machineExam",method = RequestMethod.GET)
    public ModelAndView startMachineExam(Model model,
                                         @RequestParam Integer useType,
                                         @RequestParam String ipLike){
        List<String> machineIpList=Lists.newArrayList();
        List<MachineInfo> machineInfoByCondition = machineDao.getMachineInfoByCondition(ipLike, useType, -1, null, -1, null);
        for (MachineInfo machineInfo:machineInfoByCondition){
            machineIpList.add(machineInfo.getIp());
        }
        taskService.addMachineExamTask(machineIpList,useType,0);
        return new ModelAndView("");
    }

}
