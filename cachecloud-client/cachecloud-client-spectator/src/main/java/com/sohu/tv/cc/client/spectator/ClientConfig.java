package com.sohu.tv.cc.client.spectator;

import java.util.Map;

/**
 * @author wenruiwu
 * @create 2019/12/20 14:46
 * @description
 */
public class ClientConfig {

    private Map<String, Object> configMap;
    private boolean clientStatIsOpen;

    public ClientConfig(Map<String, Object> configMap, boolean clientStatIsOpen) {
        this.configMap = configMap;
        this.clientStatIsOpen = clientStatIsOpen;
    }

    public Map<String, Object> getConfigMap() {
        return configMap;
    }

    public void setConfigMap(Map<String, Object> configMap) {
        this.configMap = configMap;
    }

    public boolean isClientStatIsOpen() {
        return clientStatIsOpen;
    }

    public void setClientStatIsOpen(boolean clientStatIsOpen) {
        this.clientStatIsOpen = clientStatIsOpen;
    }
}
