package com.sohu.cache.web.enums;

/**
 * @Author: rucao
 * @Date: 2020/5/13 5:35 下午
 */
public enum ClientTypeEnum {
    A("A", " 尽可能快地关闭连接"),
    b("b", " 客户端正在等待阻塞事件"),
    c("c", " 在将回复完整地写出之后，关闭链接"),
    d("d", " 一个受监视(watched)的键已被修改，EXEC命令将失败"),
    i("i", " 客户端正在等待VM I/O操作(已废弃)"),
    M("M", " master客户端"),
    N("N", " 普通客户端"),
    O("O", " 客户端是MONITOR模式下的附属节点(slave)"),
    P("P", " Pub/Sub客户端"),
    r("r", " 客户端是只读模式的集群节点"),
    S("S", " slave客户端"),
    u("u", " 客户端未被阻塞(unblocked)"),
    U("U", " 通过Unix套接字连接的客户端"),
    x("x", " 客户端正在执行事务"),
    Method("method", "method");

    private final String flags;
    private final String desc;

    ClientTypeEnum(String flags, String desc) {
        this.flags = flags;
        this.desc = desc;
    }

    public String getFlags() {
        return flags;
    }

    public String getDesc() {
        return desc;
    }

    public String getDesc(String flags) {
        for (ClientTypeEnum e : ClientTypeEnum.values()) {
            if (e.flags.equals(flags)) {
                return e.flags + ":" + e.desc;
            }
        }
        return flags;

    }
}
