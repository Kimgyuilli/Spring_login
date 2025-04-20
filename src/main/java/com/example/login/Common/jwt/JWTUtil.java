package com.example.login.Common.jwt;

import com.example.login.User.domain.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JWTUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Access Token 생성
    public String createAccessToken(String id, Role role, String email) {
        return createJWT(id, role, email, "access", accessTokenExpiration);
    }

    // Refresh Token 생성
    public String createRefreshToken(String id, Role role, String email) {
        return createJWT(id, role, email, "refresh", refreshTokenExpiration);
    }

    // Refresh Token 무효화
    public ResponseCookie invalidateRefreshToken() {
        return ResponseCookie.from("refresh", "")
                .path("/")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .maxAge(0)
                .build();
    }

    // Refresh Token을 쿠키로 발급
    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return createRefreshCookie(refreshToken, refreshTokenExpiration);
    }

    private ResponseCookie createRefreshCookie(String value, Long maxAge) {
        return ResponseCookie.from("refresh", value)
                .maxAge(maxAge)
                .path("/")
                .httpOnly(true)
                .secure(false) // 운영환경에서는 true, SameSite=Strict 또는 None
                .sameSite("Lax")
                .build();
    }

    // JWT 생성 내부 메서드
    private String createJWT(String id, Role role, String email, String type, long expireMs) {
        return Jwts.builder()
                .claim("type", type)
                .claim("id", id)
                .claim("role", role.name())
                .claim("email", email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expireMs))
                .signWith(secretKey)
                .compact();
    }

    // 토큰 파싱: ID
    public String getId(String token) {
        return parseClaims(token).get("id", String.class);
    }

    // 토큰 파싱: 이메일
    public String getEmail(String token) {
        return parseClaims(token).get("email", String.class);
    }

    // 토큰 파싱: 권한
    public Role getRole(String token) {
        return Role.valueOf(parseClaims(token).get("role", String.class));
    }

    // 토큰 파싱: 타입 구분(access/refresh)
    public String getTokenType(String token) {
        return parseClaims(token).get("type", String.class);
    }

    // 토큰 검증 (만료 포함)
    public boolean validateToken(String token, String expectedType) {
        try {
            Claims claims = parseClaims(token);
            return expectedType.equals(claims.get("type", String.class));
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    // 토큰에서 Claims 파싱
    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }

    // 헤더에서 AccessToken 추출
    public String extractAccessToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    // 쿠키에서 RefreshToken 추출
    public String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("refresh".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
