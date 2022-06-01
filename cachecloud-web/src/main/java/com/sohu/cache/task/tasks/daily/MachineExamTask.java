package com.sohu.cache.task.tasks.daily;

import com.google.common.collect.Maps;
import com.sohu.cache.entity.MachineStats;
import com.sohu.cache.task.BaseTask;
import com.sohu.cache.task.constant.MachineExamContants;
import com.sohu.cache.task.constant.TaskConstants;
import com.sohu.cache.task.constant.TaskStepFlowEnum;
import com.sohu.cache.task.constant.TaskStepFlowEnum.TaskFlowStatusEnum;
import org.apache.commons.collections.MapUtils;
import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * Created by rucao on 2019/1/17
 */
@Component("MachineExamTask")
@Scope(SCOPE_PROTOTYPE)
public class MachineExamTask extends BaseTask {

    private List<String> machineIpList;
    private Integer useType;
    private List<MachineStats> machineStatsList = Lists.newArrayList();
    private Map<String, Integer> machineInstanceCountMap = Maps.newHashMap();
    private List<Map> examResult = Lists.newArrayList();


    @Override
    public List<String> getTaskSteps() {
        List<String> taskStepList = new ArrayList<String>();
        taskStepList.add(TaskConstants.INIT_METHOD_KEY);
        // 1. 准备任务参数
        taskStepList.add("prepareAppParam");
        // 2. 执行机器故障检查
        taskStepList.add("executeMachineExam");
        // 3. 检查结果展示
        taskStepList.add("showMachineExamResult");
        return taskStepList;
    }

    @Override
    public TaskStepFlowEnum.TaskFlowStatusEnum init() {
        super.init();
        useType = MapUtils.getInteger(paramMap, TaskConstants.USE_TYPE_KEY, null);
        machineIpList = (List) MapUtils.getObject(paramMap, TaskConstants.MACHINE_IP_LIST_KEY);
        return TaskStepFlowEnum.TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 1. 准备任务参数
     *
     * @return
     */
    public TaskFlowStatusEnum prepareAppParam() {
        for (String ip : machineIpList) {
            List<MachineStats> list = machineCenter.getMachineStats(ip, useType, null,null, null, -1, null);
            machineStatsList.addAll(list);
        }
        machineInstanceCountMap = machineCenter.getMachineInstanceCountMap();
        if (machineStatsList.isEmpty() || machineInstanceCountMap.isEmpty()) {
            return TaskFlowStatusEnum.ABORT;
        }
        return TaskFlowStatusEnum.SUCCESS;
    }

    /**
     * 2. 执行机器故障检查
     *
     * @return
     */
    public TaskFlowStatusEnum executeMachineExam() {
        for (MachineStats machineStats : machineStatsList) {
            String ip = machineStats.getIp();
            double mem = (double) machineStats.getInfo().getMem();//G
            double usedMem = machineStats.getMachineMemInfo().getUsedMemRss() / 1024 / 1024 / 1024;//byte
            double applyMem = (double) machineStats.getMachineMemInfo().getApplyMem() / 1024 / 1024 / 1024;//byte
            double usedMemRatio = usedMem / mem;
            double applyMemRatio = applyMem / mem;

            int cpu = machineStats.getInfo().getCpu();
            int usedCpu = machineInstanceCountMap.get(ip);
            double usedCpuRatio = (double) usedCpu / cpu;

            if (judgeMemUsed(usedMemRatio, mem) || judgeMemUsed(applyMemRatio, mem) || judgeCpuUsed(usedCpuRatio)) {
                examResult.add(
                        new HashMap<String, String>() {{
                            put(MachineExamContants.MACHINE_IP, ip);
                            put(MachineExamContants.MEM, String.valueOf(mem));
                            put(MachineExamContants.APPLY_MEM, String.valueOf(applyMem));
                            put(MachineExamContants.APPLY_MEM_RATIO, String.valueOf(applyMemRatio));
                            put(MachineExamContants.USED_MEM, String.valueOf(usedMem));
                            put(MachineExamContants.USED_MEM_RATIO, String.valueOf(usedMemRatio));
                            put(MachineExamContants.CPU, String.valueOf(cpu));
                            put(MachineExamContants.USED_CPU, String.valueOf(usedCpu));
                            put(MachineExamContants.USED_CPU_RATIO, String.valueOf(usedCpuRatio));
                        }}
                );
            }
        }
        return TaskFlowStatusEnum.SUCCESS;
    }


    /**
     * 3. 检查结果展示
     *
     * @return
     */
    public TaskFlowStatusEnum showMachineExamResult() {
        return TaskFlowStatusEnum.SUCCESS;
    }

    private boolean judgeMemUsed(double ratio, double mem) {
        if (mem > 20 && ratio > MachineExamContants.defult_memUseThreshold) {
            return true;
        } else if (mem > 10 && mem <= 20 && ratio > MachineExamContants.middle_memUseThreshold) {
            return true;
        } else if (mem <= 10 && ratio > MachineExamContants.small_memUseThreshold) {
            return true;
        }
        return false;
    }

    private boolean judgeCpuUsed(double ratio) {
        if (ratio >= MachineExamContants.BASE_RATIO) {
            return true;
        }
        return false;
    }
}
