package com.sohu.cache.web.vo;

import lombok.Data;

/**
 * @Author: zengyizhao
 * @DateTime: 2021/9/17 16:13
 * @Description: 返回结果类
 */
@Data
public class GeneralResponse<T> {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 操作状态码
     */
    private int status;

    /**
     * 操作结果提示
     */
    private String error;

    /**
     * 对象结果
     */
    private T data;

    public GeneralResponse(){

    }

    public GeneralResponse(boolean success, int status, String error, T data) {
        this.success = success;
        this.status = status;
        this.error = error;
        this.data = data;
    }

    public static GeneralResponse ok(){
        return new GeneralResponse(true, 200, null, null);
    }

    public static <T> GeneralResponse ok(T data){
        return new GeneralResponse(true, 200, null, data);
    }

    public static GeneralResponse error(int status, String error){
        return new GeneralResponse(false, status, error, null);
    }

}
