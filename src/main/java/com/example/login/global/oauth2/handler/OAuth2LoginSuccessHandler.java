package com.example.login.global.oauth2.handler;

import com.example.login.domain.member.entity.Role;
import com.example.login.domain.member.repository.MemberRepository;
import com.example.login.global.dto.CommonApiResponse;
import com.example.login.global.oauth2.service.OAuth2TokenService;
import com.example.login.global.oauth2.dto.OAuthLoginResponse;
import com.example.login.global.oauth2.user.CustomOAuth2User;
import com.example.login.global.response.MemberSuccessCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

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
        String accessToken = oAuth2TokenService.createGuestAccessToken(oAuth2User.getEmail());
        oAuth2TokenService.sendAccessTokenOnly(response, accessToken);
        response.sendRedirect("/oauth2/sign-up");
    }

    private void handleNormalLogin(HttpServletResponse response, CustomOAuth2User oAuth2User) throws IOException {
        String accessToken = oAuth2TokenService.createGuestAccessToken(oAuth2User.getEmail());
        String refreshToken = oAuth2TokenService.createRefreshTokenForUser();

        oAuth2TokenService.sendAccessAndRefreshToken(response, accessToken, refreshToken);
        oAuth2TokenService.updateRefreshToken(oAuth2User.getEmail(), refreshToken);

        OAuthLoginResponse oAuthLoginResponse = OAuthLoginResponse.builder()
                .accessToken(accessToken)
                .email(oAuth2User.getEmail())
                .name(oAuth2User.getName())
                .socialType(oAuth2User.getSocialType().name())
                .build();

        CommonApiResponse<OAuthLoginResponse> apiResponse = CommonApiResponse.success(MemberSuccessCode.SOCIAL_LOGIN_SUCCESS, oAuthLoginResponse);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
