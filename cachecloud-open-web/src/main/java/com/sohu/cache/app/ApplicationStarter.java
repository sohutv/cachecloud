package com.sohu.cache.app;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * Created by zhangyijun on 15/10/26.
 */
@SpringBootApplication
@ImportResource("classpath:spring/spring.xml")
public class ApplicationStarter {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ApplicationStarter.class);
        app.setAdditionalProfiles();
        app.setBannerMode(Banner.Mode.LOG);
        app.run(args);
    }

}
