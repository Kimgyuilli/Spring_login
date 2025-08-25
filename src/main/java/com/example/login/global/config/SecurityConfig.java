package com.example.login.global.config;

import com.example.login.global.config.properties.SecurityProperties;
import com.example.login.global.config.security.CorsConfig;
import com.example.login.global.config.security.OAuth2SecurityConfig;
import com.example.login.global.jwt.JwtAuthenticationFilter;
import com.example.login.global.jwt.LoginFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;



@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final SecurityProperties securityProperties;
    private final CorsConfig corsConfig;
    private final OAuth2SecurityConfig oAuth2SecurityConfig;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, 
                                                   LoginFilter loginFilter,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(securityProperties.getPublicUrlsArray()).permitAll()
                        .requestMatchers(securityProperties.getAdminUrlsArray()).hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(except -> except
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")));

        http.oauth2Login(oAuth2SecurityConfig::configureOAuth2Login);

        http.addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

