package com.sohu.cache.constant;

/**
 * 
 * @author fulei
 */
public enum ChmodEnum {
	
	EXECUTE("x"),
	READ("r"),
	WRITE("w");

	private String op;

	private ChmodEnum(String op) {
		this.op = op;
	}

	public String getOp() {
		return op;
	}
	
	
}
