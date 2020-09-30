package com.sohu.cache.task.entity;

import com.sohu.cache.web.util.Page;
import lombok.Data;

/**
 * @author fulei
 * @date 2018年7月11日
 */
@Data
public class TaskSearch {

    /**
     * appId
     */
    private Long appId;

    /**
     * 类名
     */
    private String className;

    /**
     * 状态
     */
    private int status = -1;

    /**
     * 分页
     */
    private Page page;

}
