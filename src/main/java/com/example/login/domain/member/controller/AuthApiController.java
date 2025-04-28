package com.example.login.domain.member.controller;

import com.example.login.global.dto.ApiRes;
import com.example.login.global.jwt.JWTService;
import com.example.login.global.jwt.JWTUtil;
import com.example.login.global.response.ErrorType.ErrorCode;
import com.example.login.global.response.SuccessType.MemberSuccessCode;
import com.example.login.domain.auth.Entity.RefreshToken;
import com.example.login.domain.auth.repository.RefreshTokenRepository;
import com.example.login.domain.auth.service.BlacklistService;
import com.example.login.domain.auth.service.RefreshTokenService;
import com.example.login.domain.member.entity.Role;
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

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증 API", description = "JWT 로그인, 재발급, 로그아웃 기능 제공")
public class AuthApiController {

    private final JWTUtil jwtUtil;
    private final JWTService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;
    private final BlacklistService blacklistService;

    /**
     * AccessToken 재발급
     */
    @Operation(
            summary = "Access Token 재발급",
            description = "유효한 Refresh Token으로 Access Token을 재발급합니다. Refresh Token은 쿠키에서 추출됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access Token 재발급 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰")
    })
    @PostMapping("/token/refresh")
    public ResponseEntity<?> reissueAccessToken(HttpServletRequest request, HttpServletResponse response) {
        return jwtUtil.extractRefreshToken(request)
                .filter(jwtUtil::isRefreshToken)
                .flatMap(validRefreshToken -> jwtUtil.getId(validRefreshToken)
                        .flatMap(memberId -> {
                            RefreshToken stored = refreshTokenRepository.findById(memberId).orElse(null);
                            if (stored == null || !stored.getToken().equals(validRefreshToken)) {
                                return Optional.empty();
                            }
                            Optional<String> emailOpt = jwtUtil.getEmail(validRefreshToken);
                            Optional<Role> roleOpt = jwtUtil.getRole(validRefreshToken);

                            if (emailOpt.isEmpty() || roleOpt.isEmpty()) {
                                return Optional.empty();
                            }

                            jwtService.reissueAccessToken(response, memberId, roleOpt.get(), emailOpt.get());
                            return Optional.of(ResponseEntity.ok(ApiRes.success(MemberSuccessCode.TOKEN_REISSUE_SUCCESS)));
                        }))
                .orElseGet(() -> ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED)
                        .body(ApiRes.fail(ErrorCode.INVALID_TOKEN)));
    }

    /**
     * Access + RefreshToken 모두 재발급 (Refresh Rotation)
     */

    @Operation(
            summary = "Access + Refresh Token 재발급 (Refresh Token Rotation)",
            description = "유효한 Refresh Token으로 Access와 Refresh Token 모두 재발급합니다. Refresh Token은 쿠키에서 추출되며, 새로운 Refresh Token으로 교체됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access + Refresh Token 재발급 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰")
    })
    @PostMapping("/token/refresh/full")
    public ResponseEntity<?> reissueAccessAndRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        return jwtUtil.extractRefreshToken(request)
                .filter(jwtUtil::isRefreshToken)
                .flatMap(validRefreshToken -> jwtUtil.getId(validRefreshToken)
                        .flatMap(memberId -> {
                            RefreshToken stored = refreshTokenRepository.findById(memberId).orElse(null);
                            if (stored == null || !stored.getToken().equals(validRefreshToken)) {
                                return Optional.empty();
                            }
                            Optional<String> emailOpt = jwtUtil.getEmail(validRefreshToken);
                            Optional<Role> roleOpt = jwtUtil.getRole(validRefreshToken);

                            if (emailOpt.isEmpty() || roleOpt.isEmpty()) {
                                return Optional.empty();
                            }

                            jwtService.reissueAllTokens(response, memberId, roleOpt.get(), emailOpt.get());
                            return Optional.of(ResponseEntity.ok(ApiRes.success(MemberSuccessCode.TOKEN_REISSUE_FULL_SUCCESS)));
                        }))
                .orElseGet(() -> ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED)
                        .body(ApiRes.fail(ErrorCode.INVALID_TOKEN)));
    }

    /**
     * 로그아웃
     */

    @Operation(
            summary = "로그아웃",
            description = "Access Token을 블랙리스트에 등록하고 Refresh Token을 제거합니다. 로그아웃 처리 후 클라이언트의 쿠키도 만료됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        jwtUtil.extractRefreshToken(request)
                .filter(jwtUtil::isRefreshToken)
                .flatMap(jwtUtil::getId)
                .ifPresent(refreshTokenService::deleteRefreshToken);

        jwtService.expireRefreshCookie(response);

        jwtUtil.extractAccessToken(request)
                .filter(jwtUtil::isAccessToken)
                .flatMap(jwtUtil::getExpiration)
                .ifPresent(exp -> jwtUtil.extractAccessToken(request)
                        .ifPresent(token -> blacklistService.addToBlacklist(token, exp)));

        return ResponseEntity.ok(ApiRes.success(MemberSuccessCode.LOGOUT_SUCCESS));
    }
}
