package com.sohu.cache.entity;

/**
 * 命令分布
 */
public class AppCommandGroup {
	/**
	 * 命令名
	 */
	private String commandName;
	
	/**
	 * 调用次数
	 */
	private long count;

	public String getCommandName() {
		return commandName;
	}

	public void setCommandName(String commandName) {
		this.commandName = commandName;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	@Override
	public String toString() {
		return "AppCommandGroupVO [commandName=" + commandName + ", count="
				+ count + "]";
	}
	
}
