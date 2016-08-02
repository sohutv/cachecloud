package com.sohu.cache.web.chart.model;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

import com.sohu.cache.entity.AppCommandStats;
import com.sohu.cache.entity.AppStats;
import com.sohu.cache.web.util.DateUtil;

/**
 * highchart最简单的点
 * 
 * @author leifu
 * @Date 2016年8月1日
 * @Time 下午12:59:29
 */
public class HighchartPoint {
    /**
     * 时间戳
     */
    private Long x;

    /**
     * 用于表示y轴数量
     */
    private Long y;
    
    /**
     * 日期
     */
    private String date;

    public HighchartPoint() {

    }

    public HighchartPoint(Long x, Long y, String date) {
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

    public Long getY() {
        return y;
    }

    public void setY(Long y) {
        this.y = y;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public static HighchartPoint getFromAppCommandStats(AppCommandStats appCommandStats, Date currentDate, int diffDays) throws ParseException {
        Date collectDate = getDateTime(appCommandStats.getCollectTime());
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
        // y坐标
        long commandCount = appCommandStats.getCommandCount();
        // x坐标
        //为了显示在一个时间范围内
        if (diffDays > 0) {
            collectDate = DateUtils.addDays(collectDate, diffDays);
        }
        
        return new HighchartPoint(collectDate.getTime(), commandCount, date);
    }

    public static HighchartPoint getFromAppStats(AppStats appStat, String statName, Date currentDate, int diffDays) throws ParseException {
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
        // y坐标
        long count = 0;
        if ("hits".equals(statName)) {
            count = appStat.getHits();
        } else if ("misses".equals(statName)) {
            count = appStat.getMisses();
        } else if ("usedMemory".equals(statName)) {
            count = appStat.getUsedMemory() / 1024 / 1024;
        } else if ("netInput".equals(statName)) {
            count = appStat.getNetInputByte();
        } else if ("netOutput".equals(statName)) {
            count = appStat.getNetOutputByte();
        } else if ("connectedClient".equals(statName)) {
            count = appStat.getConnectedClients();
        } else if ("objectSize".equals(statName)) {
            count = appStat.getObjectSize();
        }
        //为了显示在一个时间范围内
        if (diffDays > 0) {
            collectDate = DateUtils.addDays(collectDate, diffDays);
        }
        
        return new HighchartPoint(collectDate.getTime(), count, date);
    }

    private static Date getDateTime(long collectTime) throws ParseException {
        try {
            return DateUtil.parseYYYYMMddHHMM(String.valueOf(collectTime));
        } catch (Exception e) {
            return DateUtil.parseYYYYMMddHH(String.valueOf(collectTime));
        }
    }

}
