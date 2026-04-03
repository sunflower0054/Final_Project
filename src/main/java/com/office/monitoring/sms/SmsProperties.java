package com.office.monitoring.sms;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "coolsms")
public class SmsProperties {
    private String apiKey;
    private String apiSecret;
    private String fromNumber;
}