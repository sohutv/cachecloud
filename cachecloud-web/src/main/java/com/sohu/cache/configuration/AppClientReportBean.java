package com.sohu.cache.configuration;

import com.sohu.cache.client.service.DealClientReportService;
import com.sohu.cache.client.service.impl.DealClientReportServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by rucao on 2019/12/16
 */
@Configuration
public class AppClientReportBean {
    @Bean(initMethod = "init")
    DealClientReportService dealClientReportService() {
        return new DealClientReportServiceImpl();
    }
}
