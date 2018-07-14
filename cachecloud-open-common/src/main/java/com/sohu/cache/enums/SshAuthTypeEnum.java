package com.sohu.cache.enums;

/**
 * ssh授权方式
 * @author leifu
 * @date 2018年6月15日
 * @time 下午5:56:59
 */
public enum SshAuthTypeEnum {
	
	PASSWORD(1, "用户密码"),
	PUBLIC_KEY(2, "公钥");
	
	private int value;
	
	private String info;

	private SshAuthTypeEnum(int value, String info) {
		this.value = value;
		this.info = info;
	}

	public int getValue() {
		return value;
	}

	public String getInfo() {
		return info;
	}
	
	
}
