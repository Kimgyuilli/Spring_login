package com.example.login.Common.config;

import com.example.login.Common.jwt.LoginFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // 🔐 JWT 필터 - 추후 구현
    // private final JWTUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public LoginFilter loginFilter(AuthenticationManager authManager) {
        LoginFilter filter = new LoginFilter(authManager, objectMapper);
        filter.setFilterProcessesUrl("/member/login"); // 로그인 처리 경로 지정
        return filter;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, LoginFilter loginFilter) throws Exception {
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
                        .requestMatchers("/", "/member", "/member/save", "/member/login", "/favicon.ico").permitAll()
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )

                .exceptionHandling(except -> except
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                        )
                );


        http.addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);
        // ✅ 현재는 생략, 나중에 활성화 가능
        // http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        // http.addFilterAfter(authExceptionFilter, CorsFilter.class);
        // http.addFilterAt(logoutFilter, LogoutFilter.class);

        return http.build();
    }
}
