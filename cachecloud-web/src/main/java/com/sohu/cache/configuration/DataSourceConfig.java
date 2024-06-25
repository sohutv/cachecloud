package com.sohu.cache.configuration;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by zengyizhao on 2022/7/27.
 */
@Configuration
public class DataSourceConfig {

    @Bean(name = "cacheCloudDB")
    @ConfigurationProperties(prefix = "cachecloud.primary")
    public HikariDataSource quartzDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

}
