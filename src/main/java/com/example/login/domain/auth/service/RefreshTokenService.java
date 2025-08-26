package com.example.login.domain.auth.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.login.domain.auth.entity.RefreshToken;
import com.example.login.domain.auth.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

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

    public boolean existsByMemberId(String memberId) {
        return refreshTokenRepository.existsById(memberId);
    }

    public void deleteRefreshToken(String memberId) {
        refreshTokenRepository.deleteById(memberId);
    }
}

