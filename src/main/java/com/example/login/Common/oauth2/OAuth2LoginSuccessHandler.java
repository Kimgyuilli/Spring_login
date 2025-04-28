package com.example.login.Common.oauth2;

import com.example.login.Common.jwt.JWTService;
import com.example.login.User.domain.Role;
import com.example.login.User.repository.MemberRepository;
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

    private final JWTService jwtService;
    private final MemberRepository userRepository;

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
        String accessToken = jwtService.createAccessToken(oAuth2User.getEmail());
        jwtService.sendAccessToken(response, accessToken); // AccessToken만 발급
        response.sendRedirect("/oauth2/sign-up"); // 추가정보 입력 폼으로 이동
    }

    private void handleNormalLogin(HttpServletResponse response, CustomOAuth2User oAuth2User) throws IOException {
        String accessToken = jwtService.createAccessToken(oAuth2User.getEmail());
        String refreshToken = jwtService.createRefreshToken();

        jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);
        jwtService.updateRefreshToken(oAuth2User.getEmail(), refreshToken);
    }
}
