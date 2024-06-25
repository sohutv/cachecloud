package com.sohu.cache.client.service;

/**
 * Created by rucao on 2019/12/29
 */
public interface AppClientStatisticGatherService {
    void bathSave(long startTime, long endTime);
    void bathAdd(long startTime, long endTime);
    void bathAddServerCmdCount(long startTime, long endTime);
}
