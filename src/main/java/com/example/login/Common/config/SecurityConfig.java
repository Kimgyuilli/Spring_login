package com.example.login.Common.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // 🔐 JWT 필터 - 추후 구현
    // private final JWTUtil jwtUtil;
    // private final FlowfitJWTFilter jwtFilter;

    // 🔐 로그아웃 필터 및 예외 처리 필터 - 추후 구현
    // private final FlowfitLogoutFilter logoutFilter;
    // private final FlowfitAuthExceptionFilter authExceptionFilter;
    // private final ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOriginPatterns(List.of("*"));  // 모든 Origin 허용 (개발용)
                    config.setAllowedMethods(Collections.singletonList("*"));  // 모든 HTTP 메서드 허용
                    config.setAllowCredentials(true);
                    config.setAllowedHeaders(Collections.singletonList("*"));
                    config.setExposedHeaders(Collections.singletonList("Authorization"));
                    config.setMaxAge(3600L);
                    return config;
                }))

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // 세션 사용 안 함
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/member/save",    // 회원가입
                                "/member/login",   // 로그인
                                "/favicon.ico"     // 기본 자원
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                .exceptionHandling(except -> except
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                        )
                );

        // ✅ 현재는 생략, 나중에 활성화 가능
        // http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        // http.addFilterAfter(authExceptionFilter, CorsFilter.class);
        // http.addFilterAt(logoutFilter, LogoutFilter.class);

        return http.build();
    }
}
