package com.sohu.cache.web.vo;

import com.sohu.cache.web.enums.CheckEnum;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by chenshi on 2021/1/12.
 */
@Data
public class MachineEnv {

    private static Logger logger = LoggerFactory.getLogger(MachineEnv.class);
    /**
     * 容器环境参数
     */
    //内存分配策略设置为1:表示内核允许分配所有的物理内存
    private String overcommit_memory;
    private final static String overcommit_memory_warnning = "1";
    //swap策略设置为0：关闭swap，避免内存io转换磁盘io导致阻塞
    private String swappines = "0";
    private final static String swappines_warnning = "0";
    //thp设置：never,防止fork过程中消耗大内存拷贝导致阻塞
    private String transparent_hugepage_enable;
    private final static String transparent_hugepage_enable_warnning = "always madvise [never]";
    private String transparent_hugepage_defrag;
    private final static String transparent_hugepage_defrag_warnning = "always defer madvise [never]";
    private final static String thp_judge = "[never]";
    //nproc用户线程数: * soft nproc 4096
    private String nproc;
    private final static String nproc_warnning = "*          soft    nproc     4096";

    /**
     * 宿主环境参数
     */
    //tcp连接队列数:512
    private String somaxconn;
    private final static String somaxconn_warnning = "511";
    //redis fsync slow log
    private int fsync_delay_times;
    private final static int fsync_delay_times_warnning = 10;
    //cachecloud用户最大线程数量,当前宿主环境为4096
    private int nproc_threads = 1024;
    private final static int nproc_threads_warnning = 1024;
    // ssh pass版本
    private String sshPass;
    private final static String sshPass_warnning = "";
    // 文件句柄数量
    private int unlimit_used;
    private int unlimit;
    private final static int unlimit_warnning = 40000;
    // 磁盘信息
    private String diskUsed;
    private final static int diskUsed_warnning = 80;
    // 实例数量
    private int instanceNum;

    /**
     * 容器资源
     */
    public MachineEnv(String overcommit_memory, String swappines, String transparent_hugepage_enable, String transparent_hugepage_defrag, String nproc) {
        this.overcommit_memory = overcommit_memory;
        this.swappines = swappines;
        this.transparent_hugepage_enable = transparent_hugepage_enable;
        this.transparent_hugepage_defrag = transparent_hugepage_defrag;
        this.nproc = nproc;
    }

    /**
     * 宿主资源
     */
    public MachineEnv(String somaxconn, int fsync_delay_times, int nproc_threads, String sshPass, int unlimit_used, int unlimit, String diskUsed, int instanceNum) {
        this.somaxconn = somaxconn;
        this.fsync_delay_times = fsync_delay_times;
        this.nproc_threads = nproc_threads;
        this.sshPass = sshPass;
        this.unlimit_used = unlimit_used;
        this.unlimit = unlimit;
        this.diskUsed = diskUsed;
        this.instanceNum = instanceNum;
    }

    public static MachineEnv getDefaultEnv() {
       return new MachineEnv("-1","-1","-1","-1","-1","-1",-1,-1,"-1",-1,-1,"-1",-1);
    }

    public MachineEnv(String overcommit_memory, String swappines, String transparent_hugepage_enable, String transparent_hugepage_defrag, String nproc, String somaxconn, int fsync_delay_times, int nproc_threads, String sshPass, int unlimit_used, int unlimit, String diskUsed, int instanceNum) {
        this.overcommit_memory = overcommit_memory;
        this.swappines = swappines;
        this.transparent_hugepage_enable = transparent_hugepage_enable;
        this.transparent_hugepage_defrag = transparent_hugepage_defrag;
        this.nproc = nproc;
        this.somaxconn = somaxconn;
        this.fsync_delay_times = fsync_delay_times;
        this.nproc_threads = nproc_threads;
        this.sshPass = sshPass;
        this.unlimit_used = unlimit_used;
        this.unlimit = unlimit;
        this.diskUsed = diskUsed;
        this.instanceNum = instanceNum;
    }

    public static int checkContainer(MachineEnv env) {

        try {
            if (env.overcommit_memory.equals(overcommit_memory_warnning) && env.transparent_hugepage_enable.contains(thp_judge) &&
                    env.transparent_hugepage_defrag.contains(thp_judge) && env.swappines.equals(swappines_warnning) &&
                    env.nproc.equals(nproc_warnning)) {
                return CheckEnum.CONSISTENCE.getValue();
            }
            return CheckEnum.INCONSISTENCE.getValue();
        } catch (Exception e) {
            logger.error("MachineEnvUtil checkContainer error  env:{} {}", env, e.getMessage());
            return CheckEnum.EXCEPTION.getValue();
        }
    }

    public static int checkHost(MachineEnv env) {
        try {
            int diskuse_precent = 0;
            try {
                diskuse_precent = Integer.parseInt(env.diskUsed.split("%")[0]);
            } catch (Exception e) {
                logger.error("disk used {} parse error:{}", env.diskUsed,e.getMessage());
            }

            if (env.nproc_threads <= nproc_threads_warnning && env.fsync_delay_times <= fsync_delay_times_warnning &&
                    env.somaxconn.equals(somaxconn_warnning) && env.unlimit_used < unlimit_warnning && diskuse_precent < diskUsed_warnning) {
                return CheckEnum.CONSISTENCE.getValue();
            }
            return CheckEnum.INCONSISTENCE.getValue();
        } catch (Exception e) {
            logger.error("MachineEnvUtil checkMachine error  env:{} {}", env, e.getMessage(), e);
            return CheckEnum.EXCEPTION.getValue();
        }
    }
}
