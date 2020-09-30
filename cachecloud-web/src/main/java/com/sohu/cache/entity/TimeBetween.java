package com.sohu.cache.entity;

import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author leifu
 */
@Data
public class TimeBetween {

    private long startTime;

    private long endTime;

    private Date startDate;

    private Date endDate;

    public TimeBetween() {
    }

    public TimeBetween(long startTime, long endTime, Date startDate, Date endDate) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getFormatStartDate() {
        String dateFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        if (startDate != null) {
            return sdf.format(startDate);
        }
        return "";
    }

    public String getFormatStartDate(String dateFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        if (startDate != null) {
            return sdf.format(startDate);
        }
        return "";
    }

}
