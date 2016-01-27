package com.sohu.cache.constant;

/**
 * Memcached需要统计的命令
 *
 * User: lingguo
 * Date: 14-6-14
 * Time: 下午3:34
 */
public enum MemcachedCommand {
    CmdGet("cmd_get"),
    CmdSet("cmd_set"),
    CmdFlush("cmd_flush"),
    CmdIncr("cmd_incr"),
    CmdDecr("cmd_decr"),
    CmdTouch("cmd_touch"),
    CmdDelete("cmd_delete"),
    CmdCas("cmd_cas");

    private String value;

    MemcachedCommand(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
