package com.sohu.cache.velocity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.template.TemplateLocation;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ui.velocity.VelocityEngineFactory;
import org.springframework.ui.velocity.VelocityEngineFactoryBean;

import javax.annotation.PostConstruct;
import java.util.Properties;

/**
 * Created by zhangyijun on 2017/10/10.
 */
@Configuration
@ConditionalOnClass({VelocityEngine.class, VelocityEngineFactory.class})
@EnableConfigurationProperties(VelocityProperties.class)
public class VelocityConfiguration {
    private static final Log logger = LogFactory.getLog(VelocityConfiguration.class);

    private final ApplicationContext applicationContext;

    private final VelocityProperties properties;

    public VelocityConfiguration(ApplicationContext applicationContext,
            VelocityProperties properties) {
        this.applicationContext = applicationContext;
        this.properties = properties;
    }

    @PostConstruct
    public void checkTemplateLocationExists() {
        if (this.properties.isCheckTemplateLocation()) {
            TemplateLocation location = new TemplateLocation(
                    this.properties.getResourceLoaderPath());
            if (!location.exists(this.applicationContext)) {
                logger.warn("Cannot find template location: " + location
                        + " (please add some templates, check your Velocity "
                        + "configuration, or set spring.velocity."
                        + "checkTemplateLocation=false)");
            }
        }
    }

    protected void applyProperties(VelocityEngineFactory factory) {
        factory.setResourceLoaderPath(this.properties.getResourceLoaderPath());
        factory.setPreferFileSystemAccess(this.properties.isPreferFileSystemAccess());
        Properties velocityProperties = new Properties();
        velocityProperties.setProperty("input.encoding",
                this.properties.getCharsetName());
        velocityProperties.putAll(this.properties.getProperties());
        factory.setVelocityProperties(velocityProperties);
    }

    @Bean(name = "velocityEngine")
    public VelocityEngineFactoryBean velocityConfiguration() {
        VelocityEngineFactoryBean velocityEngineFactoryBean = new VelocityEngineFactoryBean();
        applyProperties(velocityEngineFactoryBean);
        return velocityEngineFactoryBean;
    }

}
