package redis.clients.jedis.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wenruiwu
 * @create 2020/5/6 10:25
 * @description Latency history event
 */
public class LatencyHistoryItem {

    private final long timeStamp;
    private final long executionTime;
    private static final String COMMA = ",";

    @SuppressWarnings("unchecked")
    private LatencyHistoryItem(List<Object> properties) {
        super();
        this.timeStamp = (Long) properties.get(0);
        this.executionTime = (Long) properties.get(1);
    }

    @SuppressWarnings("unchecked")
    public static List<LatencyHistoryItem> from(List<Object> nestedMultiBulkReply) {
        List<LatencyHistoryItem> items = new ArrayList<>(nestedMultiBulkReply.size());
        for (Object obj : nestedMultiBulkReply) {
            List<Object> properties = (List<Object>) obj;
            items.add(new LatencyHistoryItem(properties));
        }

        return items;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public long getExecutionTime() {
        return executionTime;
    }


    @Override
    public String toString() {
        return new StringBuilder().append(timeStamp).append(COMMA).append(executionTime).toString();
    }
}
