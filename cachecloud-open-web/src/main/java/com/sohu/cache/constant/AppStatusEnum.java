package com.sohu.cache.constant;

/**
 * 应用发布状态
 * @author leifu
 * @Time 2014年6月28日
 */
public enum AppStatusEnum {
	STATUS_INITIALIZE(0), /* 应用刚刚初始化，还没有分配云资源 */
	STATUS_ALLOCATED(1), /* 应用被分配了资源 */
	STATUS_PUBLISHED(2), /* 应用已经发布 */
	STATUS_OFFLINE(3); /* 应用下线 */

	private int value;

	private AppStatusEnum(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}

}
