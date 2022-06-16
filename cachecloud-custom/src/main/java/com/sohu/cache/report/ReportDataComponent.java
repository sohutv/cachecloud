package com.sohu.cache.report;

/**
 * @Author: zengyizhao
 * @DateTime: 2022/2/21 11:16
 * @Description: 上报数据服务
 */
public interface ReportDataComponent {

    //上报命令数据
    void reportCommandData(Object msg);

    //上报异常数据
    void reportExceptionData(Object msg);

    //上报redis info信息
    void reportRedisInfoData(Object msg);

    //上报慢查询数据
    void reportSlowLogData(Object msg);

    //上报延迟事件数据
    void reportLatencyData(Object msg);

    //上报机器监控数据
    void reportMachineData(Object msg);

}
