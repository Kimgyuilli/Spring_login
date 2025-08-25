package com.example.login.global.oauth2.service;

import com.example.login.domain.auth.service.RefreshTokenService;
import com.example.login.domain.member.entity.Role;
import com.example.login.global.jwt.JWTUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2TokenService {

    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public String createGuestAccessToken(String email) {
        return jwtUtil.createAccessToken(
                "",         // GUEST는 임시 ID 없음
                Role.GUEST, // 임시 역할
                email
        );
    }

    public void sendAccessTokenOnly(HttpServletResponse response, String accessToken) {
        response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        log.info("OAuth2 AccessToken 전송 완료");
    }

    public String createRefreshTokenForUser() {
        return jwtUtil.createRefreshToken(
                "",         // 빈 ID (추후 확장 가능)
                Role.USER,  // 기본 사용자 역할
                ""          // 빈 이메일 (추후 확장 가능)
        );
    }

    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken) {
        setAccessToken(response, accessToken);
        setRefreshCookie(response, refreshToken);
        log.info("OAuth2 AccessToken과 RefreshToken 전송 완료");
    }

    public void updateRefreshToken(String email, String refreshToken) {
        refreshTokenService.saveToken(email, refreshToken);
        log.info("OAuth2 RefreshToken 갱신 완료 - Email: {}", email);
    }

    private void setAccessToken(HttpServletResponse response, String accessToken) {
        response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
    }

    private void setRefreshCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = jwtUtil.createRefreshTokenCookie(refreshToken);
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}