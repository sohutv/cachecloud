package com.sohu.cache.util;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/*
 * Create time : 2010-02-04
 */

/**
 */
public enum ConfigUtil {

    I;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private PropertiesConfiguration impl;

    public PropertiesConfiguration getInstance() {
        if (impl == null) {
            try {
                impl = new PropertiesConfiguration("application.properties");
                impl.setAutoSave(true);
            } catch (ConfigurationException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return impl;
    }

    public boolean isDebug() {
        String isdebug = getInstance().getString("isDebug", "false");
        if (isdebug.equals("true")) {
            return true;
        }
        return false;
    }
}
