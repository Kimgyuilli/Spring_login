package com.example.login.Common.jwt;

import com.example.login.Common.exception.ErrorCode;
import com.example.login.Common.response.LoginErrorResponse;
import com.example.login.Refresh.service.RefreshTokenService;
import com.example.login.User.dto.request.MemberLoginReq;
import com.example.login.User.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper;
    private final JWTService jwtService;


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        try {
            var loginReq = objectMapper.readValue(request.getInputStream(), MemberLoginReq.class);

            var authToken = new UsernamePasswordAuthenticationToken(
                    loginReq.getMemberEmail(), loginReq.getMemberPassword()
            );

            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            throw new RuntimeException("로그인 요청 처리 실패", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authentication) {

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        jwtService.issueTokens(response, customUserDetails.getMember());

        System.out.println("로그인 성공 - 토큰 발급 완료: {}" + customUserDetails.getUsername());

    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        response.setStatus(ErrorCode.LOGIN_FAIL.getStatus().value());
        response.setContentType("application/json; charset=UTF-8");

        LoginErrorResponse errorResponse = new LoginErrorResponse(ErrorCode.LOGIN_FAIL);
        String json = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(json);
    }
}
