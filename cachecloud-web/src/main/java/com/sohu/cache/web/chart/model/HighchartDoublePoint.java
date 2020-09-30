package com.sohu.cache.web.chart.model;

import com.sohu.cache.entity.AppStats;
import com.sohu.cache.web.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * highchart最简单的点 double y
 *
 * @author leifu
 * @Date 2016年8月1日
 * @Time 下午12:59:29
 */
@Slf4j
public class HighchartDoublePoint {
    /**
     * 时间戳
     */
    private Long x;

    /**
     * 用于表示y轴数量
     */
    private Double y;

    /**
     * 日期
     */
    private String date;

    public HighchartDoublePoint() {

    }

    public HighchartDoublePoint(Long x, Double y, String date) {
        this.x = x;
        this.y = y;
        this.date = date;
    }


    public Long getX() {
        return x;
    }

    public void setX(Long x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public static HighchartDoublePoint getFromAppStats(AppStats appStat, String statName, Date currentDate, int diffDays) throws ParseException {
        Date collectDate = getDateTime(appStat.getCollectTime());
        if (!DateUtils.isSameDay(currentDate, collectDate)) {
            return null;
        }
        //显示用的时间
        String date = null;
        try {
            date = DateUtil.formatDate(collectDate, "yyyy-MM-dd HH:mm");
        } catch (Exception e) {
            date = DateUtil.formatDate(collectDate, "yyyy-MM-dd HH");
        }
        DecimalFormat df = new DecimalFormat("##.##");
        // y坐标
        double count = 0D;
        if ("memFragRatio".equals(statName)) {
            long rss = appStat.getUsedMemoryRss();
            long mem = appStat.getUsedMemory();
            count = Double.parseDouble(df.format(rss * 1.0D / mem));
        }
        //为了显示在一个时间范围内
        if (diffDays > 0) {
            collectDate = DateUtils.addDays(collectDate, diffDays);
        }

        return new HighchartDoublePoint(collectDate.getTime(), count, date);
    }

    private static Date getDateTime(long collectTime) throws ParseException {
        try {
            return DateUtil.parseYYYYMMddHHMM(String.valueOf(collectTime));
        } catch (Exception e) {
            return DateUtil.parseYYYYMMddHH(String.valueOf(collectTime));
        }
    }
}
