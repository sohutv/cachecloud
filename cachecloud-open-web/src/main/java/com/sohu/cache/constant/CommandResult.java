package com.sohu.cache.constant;

import java.util.List;

/**
 * ssh命令简单封装
 * @author leifu
 * @Date 2016年7月27日
 * @Time 下午3:06:42
 */
public class CommandResult {
    private String command;
    
    private String result;
    
    private List<String> resultLines;
    
    public CommandResult(String command, String result) {
        super();
        this.command = command;
        this.result = result;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public List<String> getResultLines() {
        return resultLines;
    }

    public void setResultLines(List<String> resultLines) {
        this.resultLines = resultLines;
    }

    @Override
    public String toString() {
        return "CommandResult [command=" + command + ", result=" + result + ", resultLines=" + resultLines + "]";
    }
    
}
