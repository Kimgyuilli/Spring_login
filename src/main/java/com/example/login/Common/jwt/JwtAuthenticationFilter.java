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

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;
    private final BlacklistService blacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. Access Token 추출
        String token = jwtUtil.extractAccessToken(request);
        // 2. 토큰 존재하고 유효한 경우에만 인증 처리
        if (token != null && jwtUtil.validateToken(token, "access")) {

            // 블랙리스트 확인
            if (blacklistService.isBlacklisted(token)) {
                log.warn("블랙리스트에 등록된 토큰입니다.");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "블랙리스트 토큰");
                return;
            }

            // 3. 사용자 정보 추출
            String email = jwtUtil.getEmail(token);
            Role role = jwtUtil.getRole(token);
            log.info("[JwtAuthenticationFilter] 유효한 토큰 - 이메일: {}, 역할: {}", email, role);

            // 4. 인증 객체 생성 (비밀번호 없이 인증된 사용자로 설정)
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()))
                    );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        // 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

}
