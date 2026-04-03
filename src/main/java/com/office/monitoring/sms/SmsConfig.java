package com.office.monitoring.sms;

import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SmsConfig {

    private final SmsProperties smsProperties;

    @Bean
    public DefaultMessageService defaultMessageService() {
        return NurigoApp.INSTANCE.initialize(
                smsProperties.getApiKey(),
                smsProperties.getApiSecret(),
                "https://api.coolsms.co.kr"
        );
    }
}