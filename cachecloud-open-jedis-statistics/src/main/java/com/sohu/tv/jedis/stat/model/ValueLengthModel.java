package com.sohu.tv.jedis.stat.model;

import com.sohu.tv.jedis.stat.enums.ValueSizeDistriEnum;

/**
 * 值分布
 * @author leifu
 * @Date 2015年1月14日
 * @Time 上午10:47:51
 */
public class ValueLengthModel{
    /**
     * 值分布区间枚举
     */
    private ValueSizeDistriEnum redisValueSizeEnum;
    
    /**
     * 命令
     */
    private String command;
    
    /**
     * ip:port
     */
    private String hostPort;
    

    public ValueLengthModel(ValueSizeDistriEnum redisValueSizeEnum, String command, String hostPort) {
        this.redisValueSizeEnum = redisValueSizeEnum;
        this.command = command;
        this.hostPort = hostPort;
    }

    public ValueSizeDistriEnum getRedisValueSizeEnum() {
        return redisValueSizeEnum;
    }

    public String getCommand() {
        return command;
    }

    public String getHostPort() {
        return hostPort;
    }
    
    public String getUniqKey(){
    	return redisValueSizeEnum.getValue() + "_" + hostPort + "_" + command;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((command == null) ? 0 : command.hashCode());
        result = prime * result + ((hostPort == null) ? 0 : hostPort.hashCode());
        result = prime * result + ((redisValueSizeEnum == null) ? 0 : redisValueSizeEnum.hashCode());
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
        ValueLengthModel other = (ValueLengthModel) obj;
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
        if (redisValueSizeEnum != other.redisValueSizeEnum)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ValueLengthModel [redisValueSizeEnum=" + redisValueSizeEnum + ", command=" + command
                + ", hostPort=" + hostPort + "]";
    }



}
