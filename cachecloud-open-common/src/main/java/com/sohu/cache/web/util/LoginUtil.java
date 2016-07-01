package com.sohu.cache.web.util;

import java.util.Hashtable;

import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.cache.util.ConstUtils;

/**
 * 域账户LDAP登陆简单工具
 * 
 * @author leifu
 * @Time 2014年6月12日
 */
public class LoginUtil {
    
    private final static Logger logger = LoggerFactory.getLogger(LoginUtil.class);

    public static boolean passportCheck(String username, String password) {
        //LDAP登陆地址
        String ldapUrl = ConstUtils.LDAP_URL;
        if (StringUtils.isBlank(ldapUrl)) {
            logger.warn("ldap url is empty!!");
            return true;
        }
        if (ConstUtils.IS_DEBUG) {
            logger.warn("isDebug=true return");
            return true;
        }
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
        env.put("java.naming.provider.url", ldapUrl);
        env.put("java.naming.security.authentication", "simple");
        env.put("java.naming.security.principal", username + ConstUtils.EMAIL_SUFFIX);
        env.put("java.naming.security.credentials", password);
        DirContext ctx = null;
        try {
            ctx = new InitialDirContext(env);
            if (ctx != null) {
                return true;
            }
        } catch (Exception e) {
            logger.error("username {} passportCheck: " + e.getMessage(), username, e);
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return false;
    }
}
