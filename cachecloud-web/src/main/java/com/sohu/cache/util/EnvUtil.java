package com.sohu.cache.util;

import com.google.common.collect.Sets;
import org.springframework.core.env.Environment;

import java.util.Set;

/**
 * Created by yijunzhang
 */
public class EnvUtil {

    public static Set<String> getProfiles(Environment environment) {
        return Sets.newHashSet(environment.getActiveProfiles());
    }

    public static boolean isOnline(Environment environment) {
        return getProfiles(environment).contains("online");
    }

    public static boolean isDev(Environment environment) {
        Set<String> profiles = getProfiles(environment);
        return profiles.contains("test") || profiles.contains("test-sohu") || profiles.contains("local") || profiles.contains("local-sohu");
    }

    public static boolean isLocal(Environment environment) {
        Set<String> profiles = getProfiles(environment);
        return profiles.contains("local") || profiles.contains("local-sohu");
    }

    public static boolean isTest(Environment environment) {
        Set<String> profiles = getProfiles(environment);
        return profiles.contains("test") || profiles.contains("test-sohu");
    }

    public static boolean isOpen(Environment environment) {
        Set<String> profiles = getProfiles(environment);
        return profiles.contains("open") || profiles.contains("open-sohu");
    }

}
