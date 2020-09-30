package com.sohu.cache.alert.impl;

import com.sohu.cache.alert.EmailComponent;
import com.sohu.cache.alert.WeChatComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 报警基类
 *
 * @author leifu
 * @Date 2014年12月16日
 * @Time 下午4:15:11
 */
public class BaseAlertService {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 邮箱报警
     */
    @Autowired(required = false)
    protected EmailComponent emailComponent;

    /**
     * 微信报警
     */
    @Autowired(required = false)
    protected WeChatComponent weChatComponent;

}
