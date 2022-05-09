package com.sohu.cache.redis.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

import java.security.MessageDigest;

/**
 * Created by zhangyijun on 2017/7/31.
 */
public class AuthUtil {
    //客户端埋点SECRET_KEY，如果使用动态密码，建议客户端自行修改
    public final static String SECRET_KEY = "asdfjlajrl2k3jflsdafjal$$$asfdf";
    public final static String SPLIT_KEY = ":cc:";

    /**
     * 密码判定
     * 初始化用例:
     * 1:redis未设置密码，未设置password参数
     * 2:redis未设置密码，设置password参数:原文
     * 3:redis未设置密码，设置password参数:appId
     * 4:redis未设置密码，设置password参数:appId+MD5
     * 5:redis设置密码, 设置password参数:原文
     * 6:redis设置密码, 设置password参数:appId
     * 7:redis设置密码, 设置password参数:appId+MD5
     * 运行期修改密码用例:
     * 1:redis无密码状态，后端设置原文密码后，连接报错重连通信正常
     * 2:redis无密码状态，后端设置appId+MD5密码后，连接报错重连通信正常
     * 3:redis存在appId密码，后端修改为md5密码后通信正常
     * 期望结果:
     * 1:验证通过
     * 2:原文密码,appId,MD5匹配一个，验证通过。
     * 3:密码错误，抛出异常。
     * tip: update this method without reset isbroken to true and it will fail when previous condition
     * @param jedis
     * @return
     */
    public static void auth(Jedis jedis, String password) {
        if (password == null || password.trim().length() == 0) {
            //password为空，认为通过
            return;
        }
        boolean isAuth = authForPassword(jedis, password);
        if (isAuth) {
            if (jedis.getClient().isBroken()) {
                jedis.close();
            }
        } else {
            throw new JedisConnectionException("invalid password");
        }
    }

    private static boolean authForPassword(Jedis jedis, String password) {
        if (password.contains(SPLIT_KEY)) {
            //password layout: appId+SPLIT_KEY+MD5
            String[] split = password.split(SPLIT_KEY);
            String appId;
            String md5 = null;
            if (split.length == 1) {
                appId = split[0];
            } else {
                appId = split[0];
                md5 = split[1];
            }
            // 如果存在md5,优先使用md5验证
            if (md5 != null && md5.length() > 0) {
                boolean auth = checkAuth(jedis, md5, true);
                if (auth) {
                    return true;
                }
            }
            md5 = getAppIdMD5(appId);
            return checkAuth(jedis, md5, false);
        } else {
            //不包含{SPLIT_KEY}，直接认为是密码。
            return "OK".equals(jedis.auth(password));
        }
    }

    public static boolean checkAuth(Jedis jedis, String pass, boolean check) throws JedisDataException {
        try {
            jedis.auth(pass);
            return true;
        } catch (JedisDataException e) {
            if (check && e.getMessage() != null && e.getMessage().contains("invalid password")) {
                // 密码错误，重新验证
                return false;
            }
            throw e;
        }
    }

    public static String getAppIdMD5(String appId) {
        String key = SECRET_KEY + ":" + appId;
        try {
            //确定计算方法
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] md5Bytes = md5.digest(key.getBytes("UTF-8"));
            StringBuffer hexValue = new StringBuffer();
            for (int i = 0; i < md5Bytes.length; i++) {
                int val = ((int) md5Bytes[i]) & 0xff;
                if (val < 16) {
                    hexValue.append("0");
                }
                hexValue.append(Integer.toHexString(val));
            }
            return hexValue.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
