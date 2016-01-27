package com.sohu.cache.memcached.enums;

import org.apache.commons.lang.StringUtils;

/**
 * Created by yijunzhang on 14-10-14.
 */
public enum  MemcachedReadOnlyCommandEnum {
    get,
    version,
    stats;

    public static boolean contains(String command) {
        if (StringUtils.isBlank(command)) {
            return false;
        }
        for (MemcachedReadOnlyCommandEnum readEnum : MemcachedReadOnlyCommandEnum.values()) {
            String readCommand = readEnum.toString();
            command = StringUtils.trim(command);
            if (command.length() < readCommand.toString().length()) {
                continue;
            }
            String head = StringUtils.substring(command, 0, readCommand.length());
            if (readCommand.equalsIgnoreCase(head)) {
                return true;
            }
        }
        return false;
    }

}
