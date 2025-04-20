package com.example.login.Refresh.service;

import com.example.login.Refresh.Entity.RefreshToken;
import com.example.login.Refresh.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public void saveToken(String memberId, String token) {
        refreshTokenRepository.save(new RefreshToken(memberId, token));
    }

    public Optional<RefreshToken> findTokenByMemberId(String memberId) {
        return refreshTokenRepository.findById(memberId);
    }

    public void deleteRefreshToken(String memberId) {
        refreshTokenRepository.deleteById(memberId);
    }


}

