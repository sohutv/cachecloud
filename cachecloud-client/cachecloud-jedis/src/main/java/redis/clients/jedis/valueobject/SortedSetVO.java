package redis.clients.jedis.valueobject;

/**
 * Created by yijunzhang on 14-5-27.
 */
public class SortedSetVO {

    private final double score;

    private String memberStr;

    private byte[] bytesBytes;

    public SortedSetVO(double score, String memberStr) {
        this.score = score;
        this.memberStr = memberStr;
    }

    public SortedSetVO(double score, byte[] bytesBytes) {
        this.score = score;
        this.bytesBytes = bytesBytes;
    }

    public double getScore() {
        return score;
    }

    public String getMemberStr() {
        return memberStr;
    }

    public byte[] getBytesBytes() {
        return bytesBytes;
    }
}
