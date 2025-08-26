package com.example.login.global.config;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {
    
    @Bean
    public ConcurrentHashMap<String, RateLimiter> rateLimiterMap() {
        return new ConcurrentHashMap<>();
    }
}