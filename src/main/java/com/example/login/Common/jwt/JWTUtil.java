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
import java.util.Optional;

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
    public Optional<String> getId(String token) {
        try {
            return Optional.ofNullable(parseClaims(token).get("id", String.class));
        } catch (Exception e) {
            log.warn("ID 추출 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

    // 토큰 파싱: 이메일
    public Optional<String> getEmail(String token) {
        try {
            return Optional.ofNullable(parseClaims(token).get("email", String.class));
        } catch (Exception e) {
            log.warn("Email 추출 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

    // 토큰 파싱: 권한
    public Optional<Role> getRole(String token) {
        try {
            return Optional.of(Role.valueOf(parseClaims(token).get("role", String.class)));
        } catch (Exception e) {
            log.warn("Role 추출 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

    // 토큰 파싱: 타입 구분(access/refresh)
    public Optional<String> getTokenType(String token) {
        return Optional.ofNullable(parseClaims(token))
                .map(claims -> claims.get("type", String.class));
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
    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .filter(header -> header.startsWith("Bearer "))
                .map(header -> header.substring(7));
    }

    // 쿠키에서 RefreshToken 추출
    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) return Optional.empty();
        for (Cookie cookie : request.getCookies()) {
            if ("refresh".equals(cookie.getName())) {
                return Optional.ofNullable(cookie.getValue());
            }
        }
        return Optional.empty();
    }
    public Optional<Long> getExpiration(String token) {
        return Optional.ofNullable(parseClaims(token))
                .map(Claims::getExpiration)
                .map(exp -> exp.getTime() - System.currentTimeMillis());
    }
}
