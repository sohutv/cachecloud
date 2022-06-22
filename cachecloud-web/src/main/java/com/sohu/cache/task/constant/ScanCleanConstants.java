package com.sohu.cache.task.constant;

/**
 * 键匹配相关常量
 *
 * @author zengyizhao
 */
public class ScanCleanConstants {

    /**
     * 操作类型，仅分析，分析清理，分析重置ttl
     */
    public final static String OPERATE_TYPE = "operateType";

    /**
     * 指定节点
     */
    public final static String POINTED_NODES = "nodes";

    /**
     * 匹配串
     */
    public final static String PATTERN = "pattern";

    /**
     * ttl 超时
     */
    public final static String TTL_LESS = "ttlLess";

    public final static String TTL_MORE = "ttlMore";

    /**
     * 重置ttl超时设置
     */
    public final static String TTL_RESET_LESS = "ttlResetLess";

    public final static String TTL_RESET_MORE = "ttlResetMore";

    /**
     * 每次scan数量
     */
    public final static String PER_COUNT = "perCount";

    /**
     * 单个实例最大处理数量
     */
    public final static String MAX_HANDLE_COUNT = "maxHandleCount";

    public final static String OPERATE_ANALYSE = "0";

    public final static String OPERATE_CLEAN = "1";

    public final static String OPERATE_TTL_RESET = "2";

    public final static Integer COMPARE_TYPE_LESS_THAN = 0;

    public final static Integer COMPARE_TYPE_MORE_THAN = 1;

    public final static String NODE_TYPE_SLAVE = "allSlave";

    public final static String NODE_TYPE_MASTER = "allMaster";

    public final static int REDIS_SCAN_CLEAN_TIMEOUT = 7200;

}
