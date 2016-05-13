package com.sohu.cache.web.util;

import java.util.Hashtable;

import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

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
        
        // maybe your login code or below ldap
        /*
        if (ConstUtils.IS_DEBUG) {
            logger.warn("isDebug=true return");
            return true;
        }
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
        env.put("java.naming.provider.url", ConstUtils.LDAP_URL);
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
            logger.error("passportCheck: " + e.getMessage(), e);
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        */
        return true;
    }
}
