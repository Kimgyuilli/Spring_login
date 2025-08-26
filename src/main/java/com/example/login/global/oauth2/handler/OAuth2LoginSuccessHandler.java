package com.example.login.global.oauth2.handler;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.login.domain.member.entity.Role;
import com.example.login.global.dto.CommonApiResponse;
import com.example.login.global.oauth2.dto.OAuthLoginResponse;
import com.example.login.global.oauth2.service.OAuth2TokenService;
import com.example.login.global.oauth2.user.CustomOAuth2User;
import com.example.login.global.response.MemberSuccessCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2TokenService oAuth2TokenService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        log.info("OAuth2 Login 성공!");

        try {
            CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

            if (oAuth2User.getRole() == Role.GUEST) {
                handleGuestLogin(response, oAuth2User);
            } else {
                handleNormalLogin(response, oAuth2User);
            }

        } catch (Exception e) {
            log.error("OAuth2 로그인 성공 후 처리 중 에러 발생", e);
            throw e;
        }
    }

    private void handleGuestLogin(HttpServletResponse response, CustomOAuth2User oAuth2User) throws IOException {
        log.info("GUEST 사용자 소셜 로그인 처리: email={}, socialType={}", 
                oAuth2User.getEmail(), oAuth2User.getSocialType());
                
        String accessToken = oAuth2TokenService.createGuestAccessToken(
                oAuth2User.getMemberId(), 
                oAuth2User.getEmail()
        );
        String refreshToken = oAuth2TokenService.createGuestRefreshToken(
                oAuth2User.getMemberId(), 
                oAuth2User.getEmail()
        );
        
        oAuth2TokenService.sendAccessAndRefreshToken(response, accessToken, refreshToken);
        oAuth2TokenService.updateRefreshToken(oAuth2User.getEmail(), refreshToken);
        
        // GUEST 사용자는 추가 정보 입력 페이지로 리다이렉트
        response.sendRedirect("/oauth2/sign-up");
    }

    private void handleNormalLogin(HttpServletResponse response, CustomOAuth2User oAuth2User) throws IOException {
        log.info("일반 사용자 소셜 로그인 성공: email={}, socialType={}", 
                oAuth2User.getEmail(), oAuth2User.getSocialType());
                
        String accessToken = oAuth2TokenService.createUserAccessToken(
                oAuth2User.getMemberId(), 
                oAuth2User.getEmail()
        );
        String refreshToken = oAuth2TokenService.createRefreshTokenForUser(
                oAuth2User.getMemberId(), 
                oAuth2User.getEmail()
        );

        oAuth2TokenService.sendAccessAndRefreshToken(response, accessToken, refreshToken);
        oAuth2TokenService.updateRefreshToken(oAuth2User.getEmail(), refreshToken);

        // 응답 데이터 생성
        OAuthLoginResponse oAuthLoginResponse = OAuthLoginResponse.builder()
                .accessToken(accessToken)
                .email(oAuth2User.getEmail())
                .name(oAuth2User.getName())
                .socialType(oAuth2User.getSocialType().name())
                .build();

        CommonApiResponse<OAuthLoginResponse> apiResponse = CommonApiResponse.success(
                MemberSuccessCode.SOCIAL_LOGIN_SUCCESS, oAuthLoginResponse);

        // JSON 응답 전송
        sendJsonResponse(response, apiResponse);
        
        log.info("소셜 로그인 성공 응답 전송 완료: email={}", oAuth2User.getEmail());
    }
    
    private void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(data));
    }
}
