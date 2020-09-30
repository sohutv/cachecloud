package com.sohu.cache.constant;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by yijunzhang on 14-6-25.
 */
public class RedisExcludeCommand {

    private static Set<String> excludeCommands = new HashSet<String>();

    static {
        //排除这些redis命令
        excludeCommands.add("ping");
        excludeCommands.add("cluster");
        excludeCommands.add("config");
        excludeCommands.add("ttl");
        excludeCommands.add("client");
        excludeCommands.add("bgrewriteaof");
        excludeCommands.add("bgsave");
        excludeCommands.add("dbsize");
        excludeCommands.add("debug");
        excludeCommands.add("flushall");
        excludeCommands.add("flushdb");
        excludeCommands.add("flush");
        excludeCommands.add("info");
        excludeCommands.add("lastsave");
        excludeCommands.add("monitor");
        excludeCommands.add("psync");
        excludeCommands.add("save");
        excludeCommands.add("shutdown");
        excludeCommands.add("slaveof");
        excludeCommands.add("slowlog");
        excludeCommands.add("sync");
        excludeCommands.add("time");
        excludeCommands.add("replconf");
        excludeCommands.add("asking");
        excludeCommands.add("restore-asking");
        excludeCommands.add("restore");
        excludeCommands.add("select");
    }

    public static boolean isExcludeCommand(String command) {
        return excludeCommands.contains(command);
    }


}
