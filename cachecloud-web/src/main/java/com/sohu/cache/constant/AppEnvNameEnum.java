package com.sohu.cache.constant;

/**
 * @author fulei
 */
public enum AppEnvNameEnum {

	local("local"),
	test("test"),
	online_web("online_web"),
	online_stat("online_stat"),
	online_backup("online_backup"),
	afun("afun"),
	brazil("brazil"),
	;
	
	private String name;

	private AppEnvNameEnum(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
}
