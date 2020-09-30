package com.sohu.cache.constant;

/**
 * 应用的配置、节点信息
 *
 * Created by lingguo on 14-6-26.
 */
public enum AppTopology {
    TOTAL_MEMORY("total_memory"),           /* 应用的总内存 */
    MACHINE_COUNT("machine_count"),         /* 应用的机器数量 */
    MASTER_COUNT("master_count"),           /* 主节点的数量 */
    SLAVE_COUNT("slave_count");             /* 从节点的数量 */

    private String value;

    AppTopology(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

}
