package com.sohu.cache.server.data;
/**
 * 行解析器
 */
public interface LineParser {
	/**
	 * 解析nmon行
	 * @param line     nmon行内容
	 * @param timeKey  时间戳
	 * @throws Exception
	 */
	void parse(String line, String timeKey) throws Exception;
}
