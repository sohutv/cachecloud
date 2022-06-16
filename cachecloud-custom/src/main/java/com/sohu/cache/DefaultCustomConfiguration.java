package com.sohu.cache;

import com.sohu.cache.alert.EmailComponent;
import com.sohu.cache.alert.WeChatComponent;
import com.sohu.cache.alert.impl.DefaultEmailComponent;
import com.sohu.cache.alert.impl.DefaultWeChatComponent;
import com.sohu.cache.login.LoginComponent;
import com.sohu.cache.login.impl.DefaultLoginComponent;
import com.sohu.cache.report.ReportDataComponent;
import com.sohu.cache.report.impl.DefaultReportDataComponent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by yijunzhang
 */
@Configuration
public class DefaultCustomConfiguration {

    @Bean("emailComponent")
    @ConditionalOnMissingBean
    public EmailComponent emailComponent() {
        return new DefaultEmailComponent();
    }

    @Bean("weChatComponent")
    @ConditionalOnMissingBean
    public WeChatComponent weChatComponent() {
        return new DefaultWeChatComponent();
    }

    @Bean("loginComponent")
    @ConditionalOnMissingBean
    public LoginComponent loginComponent() {
        return new DefaultLoginComponent();
    }

    @Bean("reportDataComponent")
    @ConditionalOnMissingBean
    public ReportDataComponent reportDataComponent() {
        return new DefaultReportDataComponent();
    }

}
