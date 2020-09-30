package com.sohu.cache.entity;

import lombok.Data;

/**
 * Created by rucao on 2019/12/13
 */
@Data
public class AppClientLatencyCommand {
    private long id;
    /**
     * 长命令裁剪后明文
     */
    private String command;
    /**
     * args 截取的命令明文
     */
    private String args;
    /**
     * 命令参数大小
     */
    private long size;

    /**
     * 命令执行时间
     */
    private long invokeTime;
}
