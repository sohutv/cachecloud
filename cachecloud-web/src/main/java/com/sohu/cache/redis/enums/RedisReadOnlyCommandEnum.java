package com.sohu.cache.redis.enums;

import com.sohu.cache.constant.SymbolConstant;
import org.apache.commons.lang.StringUtils;

/**
 * Created by yijunzhang on 14-10-14.
 */
public enum RedisReadOnlyCommandEnum {
//    debug("debug"), //去除debug命令
    exists("exists"),
    object("object"),
    ttl("ttl"),
    type("type"),
    scan("scan"),
    get("get"),
    getbit("getbit"),
    getrange("getrange"),
    mget("mget"),
    setrange("setrange"),
    strlen("strlen"),
    hexists("hexists"),
    hget("hget"),
    hgetall("hgetall"),
    hkeys("hkeys"),
    hlen("hlen"),
    hmget("hmget"),
    hvals("hvals"),
    hscan("hscan"),
    lindex("lindex"),
    llen("llen"),
    lrange("lrange"),
    scard("scard"),
    sismember("sismember"),
    sscan("sscan"),
    srandmember("srandmember"),
    zcard("zcard"),
    zcount("zcount"),
    zrange("zrange"),
    zrangebyscore("zrangebyscore"),
    zrank("zrank"),
    zrevrange("zrevrange"),
    zscore("zscore"),
    zscan("zscan"),
    dbsize("dbsize"),
    info("info"),
    time("time"),
    lastsave("lastsave"),
    memory("memory"),

    JSON_GET("JSON.GET"),
    JSON_MGET("JSON.MGET"),
    JSON_ARRLEN("JSON.ARRLEN"),
    JSON_DEBUG_MEMORY("JSON.DEBUGMEMORY"),
    JSON_OBJKEYS("JSON.OBJKEYS"),
    JSON_OBJLEN("JSON.OBJLEN"),
    JSON_RESP("JSON.RESP"),
    JSON_TOGGLE("JSON.TOGGLE"),
    JSON_TYPE("JSON.TYPE"),

    FT_LIST("FT._LIST"),
    FT_CONFIG_GET("FT.CONFIGGET"),
    FT_EXPLAIN("FT.EXPLAIN"),
    FT_EXPLAINCLI("FT.EXPLAINCLI"),
    FT_INFO("FT.INFO"),
    FT_CURSOR_READ("FT.CURSORREAD"),
    FT_DICTDUMP("FT.DICTDUMP"),
    FT_SEARCH("FT.SEARCH"),
    FT_AGGREGATE("FT.AGGREGATE"),
    FT_PROFILE("FT.PROFILE"),
    FT_SYNDUMP("FT.SYNDUMP"),
    FT_TAGVALS("FT.TAGVALS"),

    BF_EXISTS("BF.EXISTS"),
    BF_MEXISTS("BF.MEXISTS"),
    BF_INFO("BF.INFO"),

    CF_COUNT("CF.COUNT"),
    CF_EXISTS("CF.EXISTS"),
    CF_INFO("CF.INFO"),
    CF_MEXISTS("CF.MEXISTS"),

    CMS_INFO("CMS.INFO"),
    CMS_QUERY("CMS.QUERY");

    private String command;

    RedisReadOnlyCommandEnum(String command){
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public static boolean contains(String command) {
        if (StringUtils.isBlank(command)) {
            return false;
        }
        for (RedisReadOnlyCommandEnum readEnum : RedisReadOnlyCommandEnum.values()) {
            String readCommand = readEnum.getCommand();
            command = StringUtils.remove(command, SymbolConstant.SPACE);
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
