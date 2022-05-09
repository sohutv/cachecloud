package com.sohu.cache.web.service;

import com.sohu.cache.entity.AppAlertRecord;
import com.sohu.cache.web.enums.AlertTypeEnum;

import java.util.List;

/**
 * 保存报警信息
 * @author zengyizhao
 * @Date 2021年9月3日
 */
public interface AppAlertRecordService {
    
    /**
     * 保存报警信息
     *
     * @param appAlertRecord
     * @return
     */
    int saveAlertInfo(AppAlertRecord appAlertRecord);

    /**
     * 批量保存报警信息
     * @param appAlertRecordList
     * @return
     */
    int saveBatchAlertInfo(List<AppAlertRecord> appAlertRecordList);

    /**
     * 异步保存报警信息
     *
     * @param appAlertRecord
     * @return
     */
    void asyncSaveAlertInfo(AppAlertRecord appAlertRecord);

    /**
     * 异步批量保存报警信息
     * @param appAlertRecordList
     * @return
     */
    void asyncSaveBatchAlertInfo(List<AppAlertRecord> appAlertRecordList);

    /**
     * 根据报警类型，保存报警信息
     * @param type 类型
     * @param title 邮件标题
     * @param message 邮件内容
     * @param object
     * @return
     */
    int saveAlertInfoByType(AlertTypeEnum type, String title, String message, Object... object);

}
