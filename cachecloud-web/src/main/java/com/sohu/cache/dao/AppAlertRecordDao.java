package com.sohu.cache.dao;

import com.sohu.cache.entity.AppAlertRecord;

import java.util.List;

/**
 * @Author: zengyizhao
 * @DateTime: 2021/9/3 13:38
 * @Description: 报警记录
 */
public interface AppAlertRecordDao {

    /**
     * 保存报警信息
     *
     * @param appAlertRecord
     * @return
     */
    public int save(AppAlertRecord appAlertRecord);

    /**
     * 批量保存报警信息
     *
     * @param appAlertRecordList
     * @return
     */
    public int batchSave(List<AppAlertRecord> appAlertRecordList);

}
