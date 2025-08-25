package com.example.login.global.oauth2.handler;

import com.example.login.global.dto.CommonApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import com.example.login.global.response.ErrorType.ErrorCode;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        log.warn("소셜 로그인 실패 - 에러 메시지: {}", exception.getMessage());

        CommonApiResponse<Void> apiResponse = CommonApiResponse.fail(ErrorCode.OAUTH2_LOGIN_FAILED);

        String errorResponse = objectMapper.writeValueAsString(apiResponse);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(errorResponse);
    }
}
