package com.cosmos.diseaseforecasterpro.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


import org.springframework.web.servlet.config.annotation.CorsRegistry;

//@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/user/SseLink") // 指定 SSE 路径
                .allowedOrigins("http://localhost:5713") // 前端域名和端口
                .allowCredentials(true) // 允许携带凭证（Cookie）
                .allowedMethods("GET") // SSE 通常是 GET 请求
                .maxAge(3600);
    }
}
