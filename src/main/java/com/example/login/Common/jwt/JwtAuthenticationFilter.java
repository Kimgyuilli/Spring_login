package com.example.login.Common.jwt;

import com.example.login.Refresh.service.BlacklistService;
import com.example.login.User.domain.Role;
import com.example.login.User.security.CustomUserDetailsService;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final BlacklistService blacklistService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

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
                .flatMap(jwtUtil::getEmail)
                .ifPresent(email -> {
                    try {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                        Authentication authentication = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.info("SecurityContext에 인증 객체 저장 완료: {}", email);
                    } catch (Exception e) {
                        log.warn("UserDetails 로딩 실패: {}", e.getMessage());
                        try {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "회원 인증 실패");
                        } catch (IOException ioException) {
                            log.error("에러 응답 중 오류 발생", ioException);
                        }
                    }
                });

        filterChain.doFilter(request, response);
    }
}

