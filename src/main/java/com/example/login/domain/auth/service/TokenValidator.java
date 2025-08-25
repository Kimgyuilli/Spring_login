package com.example.login.domain.auth.service;

import com.example.login.global.jwt.JWTUtil;
import com.example.login.domain.auth.entity.RefreshToken;
import com.example.login.domain.auth.repository.RefreshTokenRepository;
import com.example.login.domain.member.entity.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 토큰 검증을 담당하는 컴포넌트
 * 재사용 가능한 토큰 검증 로직을 중앙화
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenValidator {
    
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    
    /**
     * Refresh Token의 종합적 검증
     */
    public TokenValidationResult validateRefreshToken(String refreshToken) {
        // 1. JWT 형식 및 만료시간 검증
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            log.warn("Invalid refresh token format or expired");
            return TokenValidationResult.invalid("Invalid or expired refresh token");
        }
        
        // 2. 토큰에서 사용자 ID 추출
        Optional<String> memberIdOpt = jwtUtil.getId(refreshToken);
        if (memberIdOpt.isEmpty()) {
            log.warn("Cannot extract member ID from refresh token");
            return TokenValidationResult.invalid("Invalid token payload");
        }
        
        String memberId = memberIdOpt.get();
        
        // 3. Redis에 저장된 토큰과 일치 여부 확인
        RefreshToken storedToken = refreshTokenRepository.findById(memberId).orElse(null);
        if (storedToken == null || !storedToken.getToken().equals(refreshToken)) {
            log.warn("Refresh token not found or mismatched for member: {}", memberId);
            return TokenValidationResult.invalid("Token not found or mismatched");
        }
        
        // 4. 토큰에서 추가 정보 추출
        Optional<String> emailOpt = jwtUtil.getEmail(refreshToken);
        Optional<Role> roleOpt = jwtUtil.getRole(refreshToken);
        
        if (emailOpt.isEmpty() || roleOpt.isEmpty()) {
            log.warn("Cannot extract email or role from refresh token");
            return TokenValidationResult.invalid("Invalid token claims");
        }
        
        log.debug("Refresh token validation successful for member: {}", memberId);
        return TokenValidationResult.valid(memberId, emailOpt.get(), roleOpt.get());
    }
    
    /**
     * Access Token의 블랙리스트 및 만료 검증
     */
    public boolean isValidAccessToken(String accessToken) {
        return jwtUtil.isAccessToken(accessToken);
    }
    
    /**
     * 토큰 검증 결과를 캡슐화하는 클래스
     */
    public static class TokenValidationResult {
        private final boolean valid;
        private final String memberId;
        private final String email;
        private final Role role;
        private final String errorMessage;
        
        private TokenValidationResult(boolean valid, String memberId, String email, Role role, String errorMessage) {
            this.valid = valid;
            this.memberId = memberId;
            this.email = email;
            this.role = role;
            this.errorMessage = errorMessage;
        }
        
        public static TokenValidationResult valid(String memberId, String email, Role role) {
            return new TokenValidationResult(true, memberId, email, role, null);
        }
        
        public static TokenValidationResult invalid(String errorMessage) {
            return new TokenValidationResult(false, null, null, null, errorMessage);
        }
        
        // Getters
        public boolean isValid() { return valid; }
        public String getMemberId() { return memberId; }
        public String getEmail() { return email; }
        public Role getRole() { return role; }
        public String getErrorMessage() { return errorMessage; }
    }
}