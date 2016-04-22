package com.sohu.tv.cache.client.common.jmx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.tv.jedis.stat.data.UsefulDataCollector;
import com.sohu.tv.jedis.stat.model.CostTimeDetailStatKey;
import com.sohu.tv.jedis.stat.model.ExceptionModel;
import com.sohu.tv.jedis.stat.model.ValueLengthModel;
import com.sohu.tv.jedis.stat.utils.AtomicLongMap;

import java.util.*;
import java.util.Map.Entry;

/**
 * 监控cachecloud数据收集
 * @author leifu
 * @Date 2015年1月28日
 * @Time 下午2:02:04
 */
public class CachecloudDataWatcher implements CachecloudDataWatcherMBean {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public Map<String, Map<Integer, Long>> getCostTimeMap() {
		Map<CostTimeDetailStatKey, AtomicLongMap<Integer>> map = UsefulDataCollector.getDataCostTimeMapAll();
		if (map == null || map.isEmpty()) {
			return Collections.emptyMap();
		}

		Map<String, Map<Integer, Long>> result = new HashMap<String, Map<Integer, Long>>();
		for (Entry<CostTimeDetailStatKey, AtomicLongMap<Integer>> entry : map.entrySet()) {
			CostTimeDetailStatKey costTimeDetailStatKey = entry.getKey();
			String key = costTimeDetailStatKey.getUiqueKey();
			result.put(key, entry.getValue().asMap());
		}

		return result;
	}
	
	@Override
    public Map<String, Long> getCostTimeGroupByMinute() {
        Map<CostTimeDetailStatKey, AtomicLongMap<Integer>> map = UsefulDataCollector.getDataCostTimeMapAll();
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Long> result = new HashMap<String, Long>();
        for (Entry<CostTimeDetailStatKey, AtomicLongMap<Integer>> entry : map.entrySet()) {
            String minute = entry.getKey().getCurrentMinute();
            Long totalCount = 0L;
            for (Long count : entry.getValue().asMap().values()) {
                totalCount += count;
            }
            if (result.containsKey(minute)) {
                result.put(minute, result.get(minute) + totalCount);
            } else {
                result.put(minute, totalCount);
            }
        }
        return result;
    }
	
	@Override
    public Map<String, Map<String, Long>> getCostTimeGroupByMinuteAndCommand() {
        Map<CostTimeDetailStatKey, AtomicLongMap<Integer>> map = UsefulDataCollector.getDataCostTimeMapAll();
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }
        // 20150130113700_127.0.0.1:6381_del={0=4, 1=4}
        Map<String, Map<String, Long>> result = new HashMap<String, Map<String, Long>>();
        for (Entry<CostTimeDetailStatKey, AtomicLongMap<Integer>> entry : map.entrySet()) {
            String minute = entry.getKey().getCurrentMinute();
            String command = entry.getKey().getCommand();
            Long totalCount = 0L;
            for (Long count : entry.getValue().asMap().values()) {
                totalCount += count;
            }
            if (result.containsKey(minute)) {
                Map<String, Long> tempMap = result.get(minute);
                if (tempMap.containsKey(command)) {
                    tempMap.put(command, tempMap.get(command) + totalCount);
                } else {
                    tempMap.put(command, totalCount);
                }
                result.put(minute, tempMap);
            } else {
                Map<String, Long> tempMap = new HashMap<String, Long>();
                tempMap.put(command, totalCount);
                result.put(minute, tempMap);
            }
        }
        return result;
    }

	@Override
	public Map<String, Map<String, Long>> getExceptionMap() {
		Map<String, AtomicLongMap<ExceptionModel>> map = UsefulDataCollector.getDataExceptionMapAll();
		if (map == null || map.isEmpty()) {
			return Collections.emptyMap();
		}

		Map<String, Map<String, Long>> result = new HashMap<String, Map<String, Long>>();
		for (Entry<String, AtomicLongMap<ExceptionModel>> entry : map.entrySet()) {
			String key = entry.getKey();
			Map<String, Long> tempMap = new HashMap<String, Long>();
			for (Entry<ExceptionModel, Long> exceptionEntry : entry.getValue().asMap().entrySet()) {
				tempMap.put(exceptionEntry.getKey().getUniqKey(), exceptionEntry.getValue());
			}
			result.put(key, tempMap);
		}
		return result;
	}

	@Override
	public Map<String, Map<String, Long>> getValueLengthMap() {
		Map<String, AtomicLongMap<ValueLengthModel>> map = UsefulDataCollector
				.getDataValueLengthDistributeMapAll();
		if (map == null || map.isEmpty()) {
			return Collections.emptyMap();
		}

		Map<String, Map<String, Long>> result = new HashMap<String, Map<String, Long>>();
		for (Entry<String, AtomicLongMap<ValueLengthModel>> entry : map.entrySet()) {
			String key = entry.getKey();
			Map<String, Long> tempMap = new HashMap<String, Long>();
			for (Entry<ValueLengthModel, Long> valueLengthEntry : entry.getValue().asMap().entrySet()) {
				tempMap.put(valueLengthEntry.getKey().getUniqKey(), valueLengthEntry.getValue());
			}
			result.put(key, tempMap);
		}
		return result;
	}

    @Override
    public Map<String, Map<Long, Long>> getCollectionCostTimeMap() {
        Map<String, AtomicLongMap<Long>> map = UsefulDataCollector.getCollectionCostTimeMapAll();
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Map<Long, Long>> result = new HashMap<String, Map<Long, Long>>();
        for (Entry<String, AtomicLongMap<Long>> entry : map.entrySet()) {
            String key = entry.getKey();
            result.put(key, entry.getValue().asMap());
        }
        return result;
    }



}
