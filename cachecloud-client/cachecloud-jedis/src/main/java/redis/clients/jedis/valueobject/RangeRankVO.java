package redis.clients.jedis.valueobject;

/**
 * 
 * @author leifu
 * @Date 2017年2月14日
 * @Time 下午4:58:52
 */
public class RangeRankVO {

    private final long min;

    private final long max;

    public RangeRankVO(long min, long max) {
        this.min = min;
        this.max = max;
    }

    public long getMin() {
        return min;
    }

    public long getMax() {
        return max;
    }


}
