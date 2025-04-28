package com.example.login.global.oauth2.handler;

import com.example.login.domain.member.entity.Role;
import com.example.login.domain.member.repository.MemberRepository;
import com.example.login.global.dto.ApiRes;
import com.example.login.global.jwt.JWTService;
import com.example.login.global.oauth2.dto.OAuthLoginRes;
import com.example.login.global.oauth2.user.CustomOAuth2User;
import com.example.login.global.response.SuccessType.MemberSuccessCode;
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

    private final JWTService jwtService;
    private final MemberRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper(); // 직접 생성해도 되고, 빈 주입받아도 됨.

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
        // GUEST는 추가정보 입력을 위해 리다이렉트 유지
        String accessToken = jwtService.createAccessToken(oAuth2User.getEmail());
        jwtService.sendAccessToken(response, accessToken);
        response.sendRedirect("/oauth2/sign-up");
    }

    private void handleNormalLogin(HttpServletResponse response, CustomOAuth2User oAuth2User) throws IOException {
        // ✅ 여기서 JSON으로 ApiRes<OAuthLoginRes> 내려주는 처리
        String accessToken = jwtService.createAccessToken(oAuth2User.getEmail());
        String refreshToken = jwtService.createRefreshToken();

        jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);
        jwtService.updateRefreshToken(oAuth2User.getEmail(), refreshToken);

        // OAuthLoginRes 생성
        OAuthLoginRes oAuthLoginRes = OAuthLoginRes.builder()
                .accessToken(accessToken)
                .email(oAuth2User.getEmail())
                .name(oAuth2User.getName())
                .socialType(oAuth2User.getSocialType().name())
                .build();

        // ApiRes로 감싸기
        ApiRes<OAuthLoginRes> apiRes = ApiRes.success(MemberSuccessCode.SOCIAL_LOGIN_SUCCESS, oAuthLoginRes);

        // JSON 응답 설정
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // JSON으로 쓰기
        response.getWriter().write(objectMapper.writeValueAsString(apiRes));
    }
}
