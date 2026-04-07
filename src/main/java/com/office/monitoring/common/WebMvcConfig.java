package com.office.monitoring.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // file.upload-dir 값을 그대로 사용
        // local:  c:/upload  →  file:c:/upload/
        // dev:    /upload    →  file:/upload/
        // prod:   /upload    →  file:/upload/
        registry.addResourceHandler("/uploaded/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}