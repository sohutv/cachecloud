package com.sohu.tv.jedis.stat.model;

/**
 * 耗时详细统计key
 * 
 * @author leifu
 * @Date 2015年1月23日
 * @Time 上午11:24:58
 */
public class CostTimeDetailStatKey {

    /**
     * 当前分钟 yyyyMMddHHmm00
     */
    private String currentMinute;

    /**
     * 命令
     */
    private String command;

    /**
     * ip:port
     */
    private String hostPort;

    public CostTimeDetailStatKey() {
    }

    public CostTimeDetailStatKey(String currentMinute, String command, String hostPort) {
        this.currentMinute = currentMinute;
        this.command = command;
        this.hostPort = hostPort;
    }

    public String getCurrentMinute() {
        return currentMinute;
    }

    public void setCurrentMinute(String currentMinute) {
        this.currentMinute = currentMinute;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getHostPort() {
        return hostPort;
    }

    public void setHostPort(String hostPort) {
        this.hostPort = hostPort;
    }

    public String getUiqueKey() {
        return currentMinute + "_" + hostPort + "_" + command;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((command == null) ? 0 : command.hashCode());
        result = prime * result + ((currentMinute == null) ? 0 : currentMinute.hashCode());
        result = prime * result + ((hostPort == null) ? 0 : hostPort.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CostTimeDetailStatKey other = (CostTimeDetailStatKey) obj;
        if (command == null) {
            if (other.command != null)
                return false;
        } else if (!command.equals(other.command))
            return false;
        if (currentMinute == null) {
            if (other.currentMinute != null)
                return false;
        } else if (!currentMinute.equals(other.currentMinute))
            return false;
        if (hostPort == null) {
            if (other.hostPort != null)
                return false;
        } else if (!hostPort.equals(other.hostPort))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CostTimeDetailStatKey [currentMinute=" + currentMinute + ", command=" + command + ", hostPort="
                + hostPort + "]";
    }

}
