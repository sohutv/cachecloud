package com.sohu.cache.configuration;

import com.sohu.cache.ssh.SSHSessionPooledObjectFactory;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.sshd.client.session.ClientSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * @Author: zengyizhao
 * @CreateTime: 2024/2/22 15:20
 * @Description: ssh session 连接池配置
 * @Version: 1.0
 */
@Configuration
public class SSHPoolConfig {

    /**
     * ssh连接池配置
     * @return
     */
    @Bean
    public GenericKeyedObjectPool<String, ClientSession> clientSessionPool() throws GeneralSecurityException, IOException {
        GenericKeyedObjectPoolConfig genericKeyedObjectPoolConfig = new GenericKeyedObjectPoolConfig();
        genericKeyedObjectPoolConfig.setTestWhileIdle(true);
        genericKeyedObjectPoolConfig.setTestOnReturn(true);
        genericKeyedObjectPoolConfig.setMaxTotalPerKey(5);
        genericKeyedObjectPoolConfig.setMaxIdlePerKey(1);
        genericKeyedObjectPoolConfig.setMinIdlePerKey(1);
        genericKeyedObjectPoolConfig.setMaxWaitMillis(30000);
        genericKeyedObjectPoolConfig.setTimeBetweenEvictionRunsMillis(20000);
        genericKeyedObjectPoolConfig.setJmxEnabled(false);
        SSHSessionPooledObjectFactory factory = new SSHSessionPooledObjectFactory();
        GenericKeyedObjectPool<String, ClientSession> genericKeyedObjectPool = new GenericKeyedObjectPool<>(
                factory,
                genericKeyedObjectPoolConfig);
        return genericKeyedObjectPool;
    }
}
