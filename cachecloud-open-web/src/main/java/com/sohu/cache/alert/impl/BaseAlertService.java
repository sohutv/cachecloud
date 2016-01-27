package com.sohu.cache.alert.impl;

import com.sohu.cache.web.component.EmailComponent;
import com.sohu.cache.web.component.MobileAlertComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 报警基类
 * @author leifu
 * @Date 2014年12月16日
 * @Time 下午4:15:11
 */
public class BaseAlertService {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 邮箱报警
     */
    protected EmailComponent emailComponent;

    /**
     * 手机短信报警
     */
    protected MobileAlertComponent mobileAlertComponent;

    public void setEmailComponent(EmailComponent emailComponent) {
        this.emailComponent = emailComponent;
    }

    public void setMobileAlertComponent(MobileAlertComponent mobileAlertComponent) {
        this.mobileAlertComponent = mobileAlertComponent;
    }

}
