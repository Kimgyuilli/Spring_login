package com.example.login.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {
    
    @Bean
    public ConcurrentHashMap<String, Long> rateLimitMap() {
        return new ConcurrentHashMap<>();
    }
}