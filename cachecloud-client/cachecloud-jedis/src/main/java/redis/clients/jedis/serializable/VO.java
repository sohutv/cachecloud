package redis.clients.jedis.serializable;

import java.io.Serializable;

/**
 * Created by yijunzhang on 14-4-2.
 */
public class VO<T> implements Serializable {

    private T value;

    public VO(T value) {
        this.value = value;
    }

    public VO() {
    }

    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "VO{" +
                "value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VO)) return false;
        VO vo = (VO) o;
        if (value != null ? !value.equals(vo.value) : vo.value != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
