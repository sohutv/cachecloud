package com.sohu.cache.web.util;

import java.util.Hashtable;

import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.cache.util.ConstUtils;

/**
 * * 域账户LDAP登陆简单工具
 * *
 * * @author leifu
 * * @Time 2014年6月12日
 */
public class LoginUtil {

    private final static Logger logger = LoggerFactory.getLogger(LoginUtil.class);

    public static boolean passportCheck(String username, String password) {

        logger.info(username + "登录尝试");
        String ldapUrl = ConstUtils.LDAP_URL;
        String ldapBaseDN = ConstUtils.LDAP_BASEDN;
        String ldapBindDN = ConstUtils.LDAP_BINDDN;
        String ldapBindPassword = ConstUtils.LDAP_BINDDN_PASSWORD;
        if (StringUtils.isBlank(ldapUrl)) {
            logger.warn("ldap url is empty!!");
            return true;
        } else if (StringUtils.isBlank(ldapBaseDN)) {
            logger.warn("ldap baseDN is empty!!, baseDN={}", ldapBaseDN);
            return false;
        }
        if (ConstUtils.IS_DEBUG) {
            logger.warn("isDebug=true return");
            return true;
        }
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
        env.put("java.naming.provider.url", ldapUrl);
        env.put("java.naming.security.authentication", "simple");
        if (StringUtils.isNotBlank(ldapBindDN)) {
            env.put("java.naming.security.principal", ldapBindDN);
            env.put("java.naming.security.credentials", ldapBindPassword);
        } else {
            env.put("java.naming.security.principal", username + ldapBaseDN);
            env.put("java.naming.security.credentials", password);
        }
        DirContext ctx = null;
        try {
            ctx = new InitialDirContext(env);
            if (ctx != null) {
                if (StringUtils.isBlank(ldapBindDN)) {
                    return true;
                }
                String prefix = "uid=";
                String uid = prefix + username;
                String userDN = getUserDN(uid, ctx, ldapBaseDN);
                return authenricate(username, userDN, password);
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

    public static String getUserDN(String uid, DirContext ctx, String baseDN) {
        String userDN = "";
        try {
            SearchControls sc = new SearchControls();
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
            logger.info("uid={}", uid);
            NamingEnumeration<SearchResult> en = ctx.search(baseDN, uid, sc);
            if (en == null || !en.hasMoreElements()) {
                logger.warn("未找到该用户");
            }
            while (en != null && en.hasMoreElements()) {
                Object obj = en.nextElement();
                if (obj instanceof SearchResult) {
                    SearchResult si = (SearchResult) obj;
                    userDN = si.getName() + "," + baseDN;
                    logger.info("userDN={}", userDN);
                } else {
                    logger.warn(obj.toString());
                }
            }
        } catch (Exception e) {
            logger.warn("查找用户时产生异常。");
            e.printStackTrace();
        }

        return userDN;
    }

    public static boolean authenricate(String UID, String userDN, String password) {
        boolean valide = false;
        DirContext ctx = null;
        try {
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
            env.put("java.naming.provider.url", ConstUtils.LDAP_URL);
            env.put("java.naming.security.authentication", "simple");
            env.put("java.naming.security.principal", userDN);
            env.put("java.naming.security.credentials", password);
            ctx = new InitialDirContext(env);
            logger.info("username {} 验证通过 ", UID);
            valide = true;
        } catch (Exception e) {
            logger.error("username {} 验证失败 ", UID, e);
            valide = false;
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

        return valide;
    }
}
