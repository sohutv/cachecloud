package com.sohu.cache.task.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 任务队列状态枚举
 */
public class TaskQueueEnum {

    public enum TaskStatusEnum {

        NEW(0, "新任务"),
        RUNNING(1, "运行中"),
        ABORT(2, "中断"),
        //FAIL(3, "失败"),
        SUCCESS(4, "成功"),
        READY(5, "准备执行"),
        TERMINATE(6, "终止");

        private static Map<Integer, TaskStatusEnum> MAP = new HashMap<Integer, TaskStatusEnum>();

        static {
            for (TaskStatusEnum taskStatusEnum : TaskStatusEnum.values()) {
                MAP.put(taskStatusEnum.getStatus(), taskStatusEnum);
            }
        }

        public static TaskStatusEnum getTaskStatusEnum(int status) {
            return MAP.get(status);
        }

        private int status;

        private String info;

        private TaskStatusEnum(int status, String info) {
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

    /**
     * 错误代码
     */
    public enum TaskErrorCodeEnum {

        RIGHT(0, "正确");

        private int code;

        private String info;

        TaskErrorCodeEnum(int code, String info) {
            this.code = code;
            this.info = info;
        }

        public int getCode() {
            return code;
        }

        public String getInfo() {
            return info;
        }
    }

}
