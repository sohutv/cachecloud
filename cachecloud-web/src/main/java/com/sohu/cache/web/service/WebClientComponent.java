package com.sohu.cache.web.service;

import com.sohu.cache.web.enums.WebClients;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @Author: rucao
 * @Date: 2020/5/19 3:48 下午
 */
@Component
public class WebClientComponent {
    @Value("${cachecloud.web.clients}")
    private String[] clients;

    public List<String> getWebClientIps() {
        if (CollectionUtils.isEmpty(WebClients.webClientIpList)) {
            WebClients.webClientIpList = Arrays.asList(clients);
        }
        return WebClients.webClientIpList;
    }
}
