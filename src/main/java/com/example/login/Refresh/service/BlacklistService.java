package com.example.login.Refresh.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class BlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    public void addToBlacklist(String accessToken, long expirationMillis) {
        redisTemplate.opsForValue().set("blacklist:" + accessToken, "logout", Duration.ofMillis(expirationMillis));
    }

    public boolean isBlacklisted(String accessToken) {
        return redisTemplate.hasKey("blacklist:" + accessToken);
    }
}