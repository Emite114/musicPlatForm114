package com.example.musicplatform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /media/** 路径映射到磁盘目录
        registry.addResourceHandler("/media/**")
                .addResourceLocations("file:D:/AAAmusicplatformMedia/");
    }
}
