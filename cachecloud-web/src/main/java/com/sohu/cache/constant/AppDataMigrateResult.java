package com.sohu.cache.constant;

/**
 * 迁移结果
 * 
 * @author leifu
 * @Date 2016-6-8
 * @Time 下午3:15:08
 */
public class AppDataMigrateResult {

    private int status;

    private String message;

    public AppDataMigrateResult(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public boolean isSuccess() {
        if (status == 1) {
            return true;
        }
        return false;
    }

    public static AppDataMigrateResult success() {
        return new AppDataMigrateResult(1, "所有检查都成功，可以迁移啦!");
    }
    
    public static AppDataMigrateResult success(String message) {
        return new AppDataMigrateResult(1, message);
    }

    public static AppDataMigrateResult fail(String message) {
        return new AppDataMigrateResult(0, message);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "RedisMigrateResult [status=" + status + ", message=" + message + "]";
    }

}
