package com.sohu.cache.redis.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wenruiwu
 * @create 2020/4/29 17:24
 * @description
 */
public class LatencyItem {

    private final String event;
    private final long timeStamp;
    private final long latestExecutionTime;
    private final long maxExecutionTime;
    private static final String COMMA = ",";

    @SuppressWarnings("unchecked")
    private LatencyItem(List<Object> properties) {
        super();
        this.event = new String((byte[])properties.get(0));
        this.timeStamp = (Long) properties.get(1);
        this.latestExecutionTime = (Long) properties.get(2);
        this.maxExecutionTime = (Long) properties.get(3);
    }

    @SuppressWarnings("unchecked")
    public static List<LatencyItem> from(List<Object> nestedMultiBulkReply) {
        List<LatencyItem> items = new ArrayList<>(nestedMultiBulkReply.size());
        for (Object obj : nestedMultiBulkReply) {
            List<Object> properties = (List<Object>) obj;
            items.add(new LatencyItem(properties));
        }

        return items;
    }

    public String getEvent() {
        return event;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public long getLatestExecutionTime() {
        return latestExecutionTime;
    }

    public long getMaxExecutionTime() {
        return maxExecutionTime;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(event).append(COMMA).append(timeStamp).append(COMMA)
                .append(latestExecutionTime).append(COMMA).append(maxExecutionTime).toString();
    }
}
