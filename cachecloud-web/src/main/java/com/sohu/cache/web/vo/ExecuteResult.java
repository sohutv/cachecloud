package com.sohu.cache.web.vo;

import lombok.Data;

/**
 * @Author: zengyizhao
 * @DateTime: 2021/9/17 16:13
 * @Description: 执行结果及信息提示
 */
@Data
public class ExecuteResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 操作结果提示
     */
    private String message;

    public ExecuteResult(){

    }

    public ExecuteResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static ExecuteResult ok(){
        return new ExecuteResult(true, null);
    }

    public static <T> ExecuteResult ok(String message){
        return new ExecuteResult(true, message);
    }

    public static ExecuteResult error(){
        return new ExecuteResult(false, null);
    }

    public static ExecuteResult error(String message){
        return new ExecuteResult(false, message);
    }

}
