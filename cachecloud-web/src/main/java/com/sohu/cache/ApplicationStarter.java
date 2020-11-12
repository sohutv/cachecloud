package com.sohu.cache;

import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Created by zhangyijun
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude = {MybatisAutoConfiguration.class})
@ImportResource("classpath:spring/spring.xml")
@ServletComponentScan(basePackages = "com.sohu.cache.web.druid")
@EnableAsync
public class ApplicationStarter {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ApplicationStarter.class);
        app.setAdditionalProfiles();
        app.setBannerMode(Banner.Mode.LOG);
        app.run(args);
    }
}
