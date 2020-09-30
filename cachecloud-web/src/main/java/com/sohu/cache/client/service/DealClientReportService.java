package com.sohu.cache.client.service;

import com.sohu.cache.client.AppClientReportModel;

/**
 * Created by rucao on 2019/12/13
 */
public interface DealClientReportService {
    void init();

    /**
     * 处理上报数据
     *
     * @param
     * @return
     */
    boolean deal(AppClientReportModel appClientReportModel);
}
