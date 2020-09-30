package com.sohu.cache.web.service;


import java.util.List;
import java.util.Map;

/**
 * Created by rucao on 2018/12/11
 */
public interface ToolService {
    /**
     * <p>
     * Description: 通过appid进行任务检查
     * </p>
     * @param appId 应用id
     */
    List<Map> topologyExamByAppid(long appId);
    /**
     * <p>
     * Description: 所有线上应用进行任务检查
     * </p>
     * @param appidList 线上appid集合
     */
    void topologyExam(List<Long> appidList);

    void restAppDescOfficer();
}
