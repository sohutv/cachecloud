package com.sohu.cache.inspect;

import java.util.Map;

/**
 * Created by yijunzhang on 15-1-20.
 */
public interface Inspector {

    /**
     * 执行检测逻辑
     *
     * @return
     */
    public boolean inspect(Map<InspectParamEnum, Object> paramMap);

}
