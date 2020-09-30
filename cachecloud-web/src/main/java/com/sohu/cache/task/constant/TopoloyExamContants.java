package com.sohu.cache.task.constant;

/**
 * Created by rucao on 2019/1/22
 */
public class TopoloyExamContants {
    public final static String APPID="appId";
    public final static String TYPE="type";
    public final static String STATUS="status";
    public final static String DESC="desc";

    public final static String REDIS_STANDALONE="redis-standalone";
    public final static String REDIS_CLUSTER="redis-cluster";
    public final static String REDIS_SENTINEL="redis-sentinel";

    public final static String INSTANCE_FORMAT="{0}:{1}:{2} 宿主机:{3}<br/>";
    public final static String CLUSTER_INSTANCE_FORMAT="{0}:{1}:{2} 宿主机:{3}<br/>";

    public final static String MASTER_SLAVE_DESC="主从节点分布同一台物理机";
    public final static String SLAVE_NOT_EXIST="主节点没有从节点";
    public final static String NODESNUM_DESC="集群节点分布在少于3台物理机";
    public final static String CLUSTER_FAILOVER_DESC="集群中一台物理机宕机不满足故障转移条件";


}
