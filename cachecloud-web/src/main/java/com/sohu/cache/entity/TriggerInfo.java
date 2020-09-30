package com.sohu.cache.entity;

import com.sohu.cache.web.util.DateUtil;
import lombok.Data;

import java.util.Date;

/**
 * trigger的信息及状态
 */
@Data
public class TriggerInfo {
    private String schedName;
    private String triggerName;
    private String triggerGroup;
    private String jobName;
    private String jobGroup;
    private String description;
    private long nextFireTime;
    private String nextFireDate;
    private long prevFireTime;
    private String prevFireDate;
    private int priority;
    private String triggerState;
    private String triggerType;
    private long startTime;
    private String startDate;
    private long endTime;
    private String endDate;
    private String calendarName;
    private short misfireInstr;
    private String cron;

    public String getNextFireDate() {
        if (nextFireTime > 0) {
            return DateUtil.formatYYYYMMddHHMMSS(new Date(nextFireTime));
        }
        return "";
    }

    public String getPrevFireDate() {
        if (prevFireTime > 0) {
            return DateUtil.formatYYYYMMddHHMMSS(new Date(prevFireTime));
        }
        return "";
    }

    public String getStartDate() {
        if (startTime > 0) {
            return DateUtil.formatYYYYMMddHHMMSS(new Date(startTime));
        }
        return "";
    }

    public String getEndDate() {
        if (endTime > 0) {
            return DateUtil.formatYYYYMMddHHMMSS(new Date(endTime));
        }
        return "";
    }
}
