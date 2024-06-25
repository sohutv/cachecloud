package com.sohu.cache.configuration;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: zengyizhao
 * @CreateTime: 2023/4/10 15:23
 * @Description: freemaker 定制配置
 * @Version: 1.0
 */
@Configuration
public class FreemakerCustomConfig {

    @Bean
    public freemarker.template.Configuration configuration(freemarker.template.Configuration configuration) {
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.IGNORE_HANDLER);
        DefaultObjectWrapper objectWrapper = (DefaultObjectWrapper) configuration.getObjectWrapper();
        objectWrapper.setUseAdaptersForContainers(true);
        return configuration;
    }
}
