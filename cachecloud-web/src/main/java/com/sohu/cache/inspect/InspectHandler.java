package com.sohu.cache.inspect;

import java.util.List;

/**
 * Created by yijunzhang on 15-1-20.
 */
public interface InspectHandler {

    public void handle();

    public void setInspectorList(List<Inspector> inspectorList);
}
