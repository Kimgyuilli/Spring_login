package com.example.login.Common.jwt;

import com.example.login.Refresh.service.RefreshTokenService;
import com.example.login.User.domain.MemberEntity;
import com.example.login.User.domain.Role;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class JWTService {

    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    // Access Token 헤더 설정
    private void setAccessToken(HttpServletResponse response, String accessToken) {
        response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
    }

    // Refresh Token 쿠키 설정
    private void setRefreshCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = jwtUtil.createRefreshTokenCookie(refreshToken);
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    // 로그인 시 토큰 발급
    public void issueTokens(HttpServletResponse response, MemberEntity member) {
        String memberId = member.getId().toString();
        String accessToken = jwtUtil.createAccessToken(memberId, member.getRole(), member.getMemberEmail());
        String refreshToken = jwtUtil.createRefreshToken(memberId, member.getRole(), member.getMemberEmail());

        refreshTokenService.saveToken(memberId, refreshToken);

        setAccessToken(response, accessToken);
        setRefreshCookie(response, refreshToken);

        log.info("Access / Refresh 토큰 발급 완료");
    }

    // Access Token만 재발급
    public void reissueAccessToken(HttpServletResponse response, String memberId, Role role, String email) {
        String accessToken = jwtUtil.createAccessToken(memberId, role, email);
        setAccessToken(response, accessToken);
        log.info("Access 토큰 재발급 완료");
    }

    // Access + Refresh 토큰 모두 재발급
    public void reissueAllTokens(HttpServletResponse response, String memberId, Role role, String email) {
        String accessToken = jwtUtil.createAccessToken(memberId, role, email);
        String refreshToken = jwtUtil.createRefreshToken(memberId, role, email);

        refreshTokenService.saveToken(memberId, refreshToken);

        setAccessToken(response, accessToken);
        setRefreshCookie(response, refreshToken);

        log.info("Access / Refresh 토큰 모두 재발급 완료");
    }

    // 로그아웃 시 Refresh 쿠키 삭제
    public void expireRefreshCookie(HttpServletResponse response) {
        ResponseCookie expired = jwtUtil.invalidateRefreshToken();
        response.setHeader(HttpHeaders.SET_COOKIE, expired.toString());
    }
}
