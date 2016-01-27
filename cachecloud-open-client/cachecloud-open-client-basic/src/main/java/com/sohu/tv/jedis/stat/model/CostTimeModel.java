package com.sohu.tv.jedis.stat.model;

import com.sohu.tv.jedis.stat.enums.CostTimeDistriEnum;

/**
 * 耗时model,作为AtomicLongMap的key
 * 
 * @author leifu
 * @Date 2015年1月13日
 * @Time 下午5:47:27
 */
public class CostTimeModel{
    /**
     * 耗时区间枚举
     */
    private CostTimeDistriEnum redisCostTimeDistriEnum;

    /**
     * 命令
     */
    private String command;
    
    /**
     * ip:port
     */
    private String hostPort;
    
    public CostTimeModel(CostTimeDistriEnum redisCostTimeDistriEnum, String command, String hostPort) {
        this.redisCostTimeDistriEnum = redisCostTimeDistriEnum;
        this.command = command;
        this.hostPort = hostPort;
    }

    public CostTimeDistriEnum getRedisCostTimeDistriEnum() {
        return redisCostTimeDistriEnum;
    }

    public String getCommand() {
        return command;
    }

    public String getHostPort() {
        return hostPort;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((command == null) ? 0 : command.hashCode());
        result = prime * result + ((hostPort == null) ? 0 : hostPort.hashCode());
        result = prime * result + ((redisCostTimeDistriEnum == null) ? 0 : redisCostTimeDistriEnum.hashCode());
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
        CostTimeModel other = (CostTimeModel) obj;
        if (command == null) {
            if (other.command != null)
                return false;
        } else if (!command.equals(other.command))
            return false;
        if (hostPort == null) {
            if (other.hostPort != null)
                return false;
        } else if (!hostPort.equals(other.hostPort))
            return false;
        if (redisCostTimeDistriEnum != other.redisCostTimeDistriEnum)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CostTimeModel [redisCostTimeDistriEnum=" + redisCostTimeDistriEnum + ", command=" + command
                + ", hostPort=" + hostPort + "]";
    }

    

}
