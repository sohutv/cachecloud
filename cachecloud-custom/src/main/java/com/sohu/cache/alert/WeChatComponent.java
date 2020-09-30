package com.sohu.cache.alert;

import java.util.List;

/**
 * 微信报警
 * Created by rucao
 */
public interface WeChatComponent {

    /**
     * 发送微信报警
     * @param message
     * @param weChatList
     * @return
     */
    boolean sendWeChat(String title, String message, List<String> weChatList);

    /**
     * 发送微信报警给所有相关人员
     * @param title
     * @param message
     * @param weChatList
     * @return
     */
    boolean sendWeChatToAll(String title, String message, List<String> weChatList);

    /**
     * 发送微信报警给管理员
     * @param message
     * @return
     */
    boolean sendWeChatToAdmin(String title, String message);

}
