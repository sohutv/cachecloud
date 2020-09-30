package redis.clients.jedis.valueobject;

/**
 * Created by yijunzhang on 14-5-28.
 */
public class RangeScoreVO {

    private final double max;

    private final double min;

    public RangeScoreVO(double max, double min) {
        this.max = max;
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }
}
