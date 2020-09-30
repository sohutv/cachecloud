package com.sohu.cache.task.constant;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * 任务步骤流程
 * @author fulei
 */
public class TaskStepFlowEnum {

    /**
     * 任务流状态
     */
    public enum TaskFlowStatusEnum {

        READY(0, "准备"),
        RUNNING(1, "运行中"),
        ABORT(2, "中断"),
        //		FAIL(3, "失败"),
        SUCCESS(4, "成功"),
        SKIP(5, "跳过");

        private static Map<Integer, TaskFlowStatusEnum> MAP = Maps.newHashMap();

        static {
            for (TaskFlowStatusEnum taskFlowStatusEnum : TaskFlowStatusEnum.values()) {
                MAP.put(taskFlowStatusEnum.getStatus(), taskFlowStatusEnum);
            }
        }

        public static TaskFlowStatusEnum getTaskFlowStatusEnum(int status) {
            return MAP.get(status);
        }

        private int status;

        private String info;

        TaskFlowStatusEnum(int status, String info) {
            this.status = status;
            this.info = info;
        }

        public int getStatus() {
            return status;
        }

        public String getInfo() {
            return info;
        }

    }

}
