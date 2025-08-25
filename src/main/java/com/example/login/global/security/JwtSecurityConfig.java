package com.example.login.global.security;

import com.example.login.domain.auth.service.AuthenticationService;
import com.example.login.domain.auth.service.BlacklistService;
import com.example.login.domain.member.security.CustomUserDetailsService;
import com.example.login.global.jwt.JWTUtil;
import com.example.login.global.jwt.JwtAuthenticationFilter;
import com.example.login.global.jwt.LoginFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class JwtSecurityConfig {

    private final JWTUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final BlacklistService blacklistService;
    private final CustomUserDetailsService customUserDetailsService;
    private final AuthenticationService authenticationService;

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
        LoginFilter loginFilter = new LoginFilter(authManager, objectMapper, authenticationService);
        loginFilter.setFilterProcessesUrl("/api/auth/login");
        loginFilter.setAuthenticationManager(authManager);
        return loginFilter;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil, blacklistService, customUserDetailsService);
    }
}