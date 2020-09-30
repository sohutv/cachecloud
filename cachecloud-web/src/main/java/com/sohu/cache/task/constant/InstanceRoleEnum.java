package com.sohu.cache.task.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * @author fulei
 * @date 2018年6月27日
 */
public enum InstanceRoleEnum {

	MASTER(1, "master"), 
	SLAVE(2, "slave"),
	SENTINEL(3,"sentinel");

	private static Map<Integer, InstanceRoleEnum> MAP = new HashMap<Integer, InstanceRoleEnum>();

	static {
		for (InstanceRoleEnum instanceRoleEnum : InstanceRoleEnum.values()) {
			MAP.put(instanceRoleEnum.getRole(), instanceRoleEnum);
		}
	}

	public static InstanceRoleEnum getInstanceRoleEnum(int role) {
		return MAP.get(role);
	}

	private int role;

	private String info;

	private InstanceRoleEnum(int role, String info) {
		this.role = role;
		this.info = info;
	}

	public int getRole() {
		return role;
	}

	public String getInfo() {
		return info;
	}

}
