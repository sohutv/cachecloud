package com.sohu.cache.constant;

/**
 * 导入结果
 * 
 * @author leifu
 * @Date 2016-4-16
 * @Time 下午3:41:37
 */
public class ImportAppResult {

    private int status;

    private String message;

    public ImportAppResult(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public static ImportAppResult success() {
        return new ImportAppResult(1, "所有检查都成功，可以添加啦!");
    }

    public static ImportAppResult fail(String message) {
        return new ImportAppResult(0, message);
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
        return "ImportAppResult [status=" + status + ", message=" + message + "]";
    }

}
