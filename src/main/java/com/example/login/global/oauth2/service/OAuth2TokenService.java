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

    public String createGuestAccessToken(String memberId, String email) {
        return jwtUtil.createAccessToken(
                memberId,   // 실제 멤버 ID 사용
                Role.GUEST, // 임시 역할
                email
        );
    }

    public String createUserAccessToken(String memberId, String email) {
        return jwtUtil.createAccessToken(
                memberId,   // 실제 멤버 ID 사용
                Role.USER,  // 일반 사용자 역할
                email
        );
    }

    public void sendAccessTokenOnly(HttpServletResponse response, String accessToken) {
        response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        log.info("OAuth2 AccessToken 전송 완료");
    }

    public String createRefreshTokenForUser(String memberId, String email) {
        return jwtUtil.createRefreshToken(
                memberId,   // 실제 멤버 ID 사용
                Role.USER,  // 기본 사용자 역할
                email       // 실제 이메일 사용
        );
    }

    public String createGuestRefreshToken(String memberId, String email) {
        return jwtUtil.createRefreshToken(
                memberId,   // 실제 멤버 ID 사용
                Role.GUEST, // 임시 역할
                email       // 실제 이메일 사용
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