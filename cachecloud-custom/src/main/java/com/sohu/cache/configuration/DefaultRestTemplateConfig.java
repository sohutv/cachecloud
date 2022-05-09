package com.sohu.cache.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;


/**
 * Created by rucao on 2020/4/2
 */
@Configuration
public class DefaultRestTemplateConfig {
    private static int connectTimeout = 4000;
    private static int readTimeout = 5000;

    @ConditionalOnMissingBean(name = "restTemplate")
    @Bean
    RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory f = new HttpComponentsClientHttpRequestFactory();
        f.setConnectTimeout(connectTimeout);
        f.setReadTimeout(readTimeout);
        f.setConnectionRequestTimeout(connectTimeout);
        RestTemplate restTemplate = new RestTemplate(f);
        return restTemplate;
    }
}
