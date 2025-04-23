package com.example.login.User.controller;

import com.example.login.Common.dto.ApiRes;
import com.example.login.Common.jwt.JWTService;
import com.example.login.Common.jwt.JWTUtil;
import com.example.login.Common.response.SuccessType.MemberSuccessCode;
import com.example.login.Refresh.Entity.RefreshToken;
import com.example.login.Refresh.repository.RefreshTokenRepository;
import com.example.login.Refresh.service.BlacklistService;
import com.example.login.Refresh.service.RefreshTokenService;
import com.example.login.User.domain.Role;
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
public class AuthApiController {

    private final JWTUtil jwtUtil;
    private final JWTService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;
    private final BlacklistService blacklistService;

    /**
     * AccessToken 재발급
     */
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
                        .body(ApiRes.fail(com.example.login.Common.response.ErrorType.ErrorCode.INVALID_TOKEN)));
    }

    /**
     * Access + RefreshToken 모두 재발급 (Refresh Rotation)
     */
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
                        .body(ApiRes.fail(com.example.login.Common.response.ErrorType.ErrorCode.INVALID_TOKEN)));
    }

    /**
     * 로그아웃
     */
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
