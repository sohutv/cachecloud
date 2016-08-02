package com.sohu.cache.web.chart.model;

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.sohu.cache.entity.AppCommandGroup;
import com.sohu.cache.entity.AppCommandStats;
import com.sohu.cache.entity.AppStats;
import com.sohu.cache.web.util.DateUtil;

/**
 * 用于显示chart的简单对象
 * 
 * @author leifu
 * @Time 2014年8月31日
 */
public class SimpleChartData {
	/**
	 * 时间戳
	 */
	private Long x;

	/**
	 * 用于表示y轴数量
	 */
	private Long y;

	/**
	 * 命令名
	 */
	private String commandName;
	
	/**
	 * 日期
	 */
	private String date;

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

	public String getCommandName() {
		return commandName;
	}

	public void setCommandName(String commandName) {
		this.commandName = commandName;
	}

	public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    /**
	 * AppCommandStats转换为SimpleChartData
	 * 
	 * @param appCommandStats
	 * @return
	 * @throws ParseException
	 */
	public static SimpleChartData getFromAppCommandStats(
			AppCommandStats appCommandStats, Integer addDay) throws ParseException {
		SimpleChartData chartData = new SimpleChartData();
		long collectTime = appCommandStats.getCollectTime();
		String commandName = appCommandStats.getCommandName();
		long commandCount = appCommandStats.getCommandCount();
		Date dateTime = null;
		try {
			dateTime = DateUtil.parseYYYYMMddHHMM(String.valueOf(collectTime));
		} catch (Exception e) {
			dateTime = DateUtil.parseYYYYMMddHH(String.valueOf(collectTime));
		}
		Long x = dateTime.getTime();
		if(addDay != null){
		    x += TimeUnit.DAYS.toMillis(1) * addDay;
		}
		Long y = commandCount;
		String date = null;
        try {
            date = DateUtil.formatDate(dateTime, "yyyy-MM-dd HH:mm");
        } catch (Exception e) {
            date = DateUtil.formatDate(dateTime, "yyyy-MM-dd HH");
        }
		chartData.setX(x);
		chartData.setY(y);
		chartData.setDate(date);
		chartData.setCommandName(commandName);
		return chartData;
	}

	/**
	 * AppStats转换为SimpleChartData
	 * 
	 * @param appStat
	 * @param statName
	 *            命中数、丢失数的字段
	 * @return
	 * @throws ParseException
	 */
	public static SimpleChartData getFromAppStats(AppStats appStat, String statName) throws ParseException {
		SimpleChartData chartData = new SimpleChartData();
		long collectTime = appStat.getCollectTime();
		long count = 0;
		if ("hits".equals(statName)) {
			count = appStat.getHits();
		} else if ("misses".equals(statName)) {
			count = appStat.getMisses();
		} else if ("usedMemory".equals(statName)){
		    count = appStat.getUsedMemory() / 1024 / 1024;
		} else if ("netInput".equals(statName)) {
		    count = appStat.getNetInputByte();
		} else if ("netOutput".equals(statName)) {
            count = appStat.getNetOutputByte();
        }
		Date dateTime = null;
		try {
			dateTime = DateUtil.parseYYYYMMddHHMM(String.valueOf(collectTime));
		} catch (Exception e) {
			dateTime = DateUtil.parseYYYYMMddHH(String.valueOf(collectTime));
		}
		Long x = dateTime.getTime();
		Long y = count;
		String date = null;
		try {
            date = DateUtil.formatDate(dateTime, "yyyy-MM-dd HH:mm");
        } catch (Exception e) {
            date = DateUtil.formatDate(dateTime, "yyyy-MM-dd HH");
        }
		chartData.setX(x);
		chartData.setY(y);
		chartData.setDate(date);

		return chartData;
	}

	/**
	 * AppCommandGroup转换为SimpleChartData用于显示pie图
	 * 
	 * @param appCommandGroup
	 * @return
	 */
	public static SimpleChartData getFromAppCommandGroup(
			AppCommandGroup appCommandGroup) {
		SimpleChartData chartData = new SimpleChartData();
		chartData.setCommandName(appCommandGroup.getCommandName());
		chartData.setY(appCommandGroup.getCount());
		return chartData;
	}

	@Override
	public String toString() {
		return "SimpleChartData [x=" + x + ", y=" + y + ", commandName="
				+ commandName + "]";
	}

	


}
