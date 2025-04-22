package com.example.login.Common.jwt;

import com.example.login.Refresh.service.BlacklistService;
import com.example.login.User.domain.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final BlacklistService blacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        jwtUtil.extractAccessToken(request)
                .filter(token -> jwtUtil.validateToken(token, "access"))
                .filter(token -> {
                    if (blacklistService.isBlacklisted(token)) {
                        log.warn("블랙리스트에 등록된 토큰입니다.");
                        try {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "블랙리스트 토큰");
                        } catch (IOException e) {
                            log.error("에러 응답 중 오류 발생", e);
                        }
                        return false;
                    }
                    return true;
                })
                .ifPresent(token -> {
                    Optional<String> optionalEmail = jwtUtil.getEmail(token);
                    Optional<Role> optionalRole = jwtUtil.getRole(token);

                    if (optionalEmail.isEmpty() || optionalRole.isEmpty()) {
                        log.warn("토큰에서 사용자 정보 추출 실패");
                        try {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰 정보");
                        } catch (IOException e) {
                            log.error("에러 응답 중 오류 발생", e);
                        }
                        return;
                    }

                    String email = optionalEmail.get();
                    Role role = optionalRole.get();
                    log.info("[JwtAuthenticationFilter] 유효한 토큰 - 이메일: {}, 역할: {}", email, role);

                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()))
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });

        filterChain.doFilter(request, response);
    }
}
