package redis.clients.jedis.valueobject;

/**
 * @author leifu
 * @Date 2017年4月7日
 * @Time 下午4:01:19
 */
public class BitOffsetValue {

	private long offset;
	
	private boolean value;

	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	public boolean getValue() {
		return value;
	}

	public void setValue(boolean value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "BitOffsetValue [offset=" + offset + ", value=" + value + "]";
	} 
	
	
}
