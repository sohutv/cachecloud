package com.sohu.cache.alert.impl;

import com.sohu.cache.alert.WeChatComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by yijunzhang
 */
public class DefaultWeChatComponent implements WeChatComponent {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean sendWeChat(String title, String message, List<String> weChatList) {
        logger.warn("Please implement the sendWeChat logic.");
        return true;
    }

    @Override
    public boolean sendWeChatToAll(String title, String message, List<String> weChatList) {
        logger.warn("Please implement the sendWeChatToAll logic.");
        return true;
    }

    @Override
    public boolean sendWeChatToAdmin(String title, String message) {
        logger.warn("Please implement the sendWeChatToAdmin logic.");
        return true;
    }
}
