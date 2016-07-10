package com.sohu.cache.util;

import java.util.Arrays;

import org.springframework.util.DigestUtils;

/**
 * appkey计算工具
 * 
 * @author leifu
 * @Date 2016-7-9
 * @Time 下午9:23:59
 */
public class AppKeyUtil {

    public static String genSecretKey(long appId) {
        StringBuilder key = new StringBuilder();
        // 相关参数
        key.append(appId).append(ConstUtils.APP_SECRET_BASE_KEY);
        // 转成char[]
        char[] strs = key.toString().toCharArray();
        // 排序
        Arrays.sort(strs);
        // md5
        return MD5(new String(strs));
    }

    private static String MD5(String s) {
        return DigestUtils.md5DigestAsHex(s.getBytes());
    }
    
    public static void main(String[] args) {
        System.out.println(genSecretKey(10010));
    }

}