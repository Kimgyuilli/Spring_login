package com.example.login.domain.member.controller;

import com.example.login.global.dto.ApiRes;
import com.example.login.global.response.SuccessType.MemberSuccessCode;
import com.example.login.domain.auth.service.AuthenticationService;
import com.example.login.domain.auth.dto.response.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증 API", description = "JWT 토큰 관리")
public class AuthApiController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "Access Token 재발급")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰")
    })
    @PostMapping("/token/refresh")
    public ResponseEntity<ApiRes<TokenResponse>> refreshAccessToken(
            HttpServletRequest request, 
            HttpServletResponse response) {
        
        TokenResponse tokenResponse = authenticationService.refreshAccessToken(request, response);
        return ResponseEntity.ok(ApiRes.success(MemberSuccessCode.TOKEN_REISSUE_SUCCESS, tokenResponse));
    }

    @Operation(summary = "Access + Refresh Token 모두 재발급")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰")
    })
    @PostMapping("/token/refresh/full")
    public ResponseEntity<ApiRes<TokenResponse>> refreshAllTokens(
            HttpServletRequest request, 
            HttpServletResponse response) {
        
        TokenResponse tokenResponse = authenticationService.refreshAllTokens(request, response);
        return ResponseEntity.ok(ApiRes.success(MemberSuccessCode.TOKEN_REISSUE_FULL_SUCCESS, tokenResponse));
    }

    @Operation(summary = "로그아웃")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiRes<Void>> logout(
            HttpServletRequest request, 
            HttpServletResponse response) {
        
        authenticationService.logout(request, response);
        return ResponseEntity.ok(ApiRes.success(MemberSuccessCode.LOGOUT_SUCCESS));
    }
}
