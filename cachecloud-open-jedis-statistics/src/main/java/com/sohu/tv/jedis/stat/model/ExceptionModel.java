package com.sohu.tv.jedis.stat.model;

import com.sohu.tv.jedis.stat.enums.ClientExceptionType;

/**
 * jedis异常(exceptionClass,hostPort作为唯一哈希)
 * @author leifu
 * @Date 2015年1月13日
 * @Time 下午6:00:35
 */
public class ExceptionModel{
    
    /**
     * 异常类
     */
    private String exceptionClass;
    
    /**
     * ip:port
     */
    private String hostPort;
    
    /**
     * 异常类型
     */
    private ClientExceptionType clientExceptionType;
    
    public String getExceptionClass() {
		return exceptionClass;
	}

	public void setExceptionClass(String exceptionClass) {
		this.exceptionClass = exceptionClass;
	}

    public String getHostPort() {
        return hostPort;
    }

    public void setHostPort(String hostPort) {
        this.hostPort = hostPort;
    }
    
    public ClientExceptionType getClientExceptionType() {
        return clientExceptionType;
    }

    public void setClientExceptionType(ClientExceptionType clientExceptionType) {
        this.clientExceptionType = clientExceptionType;
    }

    public String getUniqKey(){
    	return hostPort + "_" + exceptionClass;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((exceptionClass == null) ? 0 : exceptionClass.hashCode());
		result = prime * result
				+ ((hostPort == null) ? 0 : hostPort.hashCode());
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
		ExceptionModel other = (ExceptionModel) obj;
		if (exceptionClass == null) {
			if (other.exceptionClass != null)
				return false;
		} else if (!exceptionClass.equals(other.exceptionClass))
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
        return "ExceptionModel [exceptionClass=" + exceptionClass + ", hostPort=" + hostPort + ", clientExceptionType="
                + clientExceptionType + "]";
    }


}
