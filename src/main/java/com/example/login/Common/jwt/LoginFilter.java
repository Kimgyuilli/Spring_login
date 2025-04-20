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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper;
    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            // 1. JSON 요청 바디에서 사용자 정보 추출

            MemberLoginReq req = objectMapper.readValue(request.getInputStream(), MemberLoginReq.class);

            // 2. 인증 토큰 생성
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(req.getMemberEmail(), req.getMemberPassword());

            // 3. 인증 처리 (UserDetailsService → loadUserByUsername 호출됨)
            return authenticationManager.authenticate(authToken);

        } catch (IOException e) {
            throw new RuntimeException("로그인 요청 처리 실패", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authentication) {

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtUtil.createAccessToken(
                customUserDetails.getMember().getId().toString(),
                customUserDetails.getMember().getRole(),
                customUserDetails.getMember().getMemberEmail()
        );

        String refreshToken = jwtUtil.createRefreshToken(
                customUserDetails.getMember().getId().toString(),
                customUserDetails.getMember().getRole(),
                customUserDetails.getMember().getMemberEmail()
        );

        // Access Token은 헤더에
        response.addHeader("Authorization", "Bearer " + accessToken);

        // Refresh Token은 HttpOnly Cookie로
        response.addHeader("Set-Cookie", jwtUtil.createRefreshTokenCookie(refreshToken).toString());

        refreshTokenService.saveToken(customUserDetails.getMember().getId().toString(), refreshToken);

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
