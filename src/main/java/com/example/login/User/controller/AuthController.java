package com.example.login.User.controller;

import com.example.login.Common.jwt.JWTService;
import com.example.login.Common.jwt.JWTUtil;
import com.example.login.Refresh.Entity.RefreshToken;
import com.example.login.Refresh.repository.RefreshTokenRepository;
import com.example.login.Refresh.service.BlacklistService;
import com.example.login.Refresh.service.RefreshTokenService;
import com.example.login.User.domain.Role;
import com.example.login.User.dto.request.MemberLoginReq;
import com.example.login.User.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class AuthController {

    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistService blacklistService;
    private final MemberService memberService;
    private final JWTService jwtService;

    @GetMapping("/login-page")
    public String loginForm(Model model) {
        model.addAttribute("memberLoginReq", new MemberLoginReq());
        return "login";
    }

    // Access Token 재발급
    @PostMapping("/token/refresh")
    public ResponseEntity<?> reissueAccessToken(HttpServletRequest request, HttpServletResponse response) {
        return jwtUtil.extractRefreshToken(request)
                .filter(token -> jwtUtil.validateToken(token, "refresh"))
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
                            return Optional.of(ResponseEntity.ok("Access 토큰 재발급 완료"));
                        }))
                .orElseGet(() -> ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED)
                        .body("리프레시 토큰 검증 실패 또는 정보 추출 실패"));
    }

    // Refresh Token을 포함한 Access Token 재발급
    @PostMapping("/token/refresh/full")
    public ResponseEntity<?> reissueAccessAndRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        return jwtUtil.extractRefreshToken(request)
                .filter(token -> jwtUtil.validateToken(token, "refresh"))
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
                            return Optional.of(ResponseEntity.ok("Access/Refresh 토큰 재발급 완료"));
                        }))
                .orElseGet(() -> ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED)
                        .body("유효하지 않거나 일치하지 않는 리프레시 토큰입니다."));
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        jwtUtil.extractRefreshToken(request)
                .filter(token -> jwtUtil.validateToken(token, "refresh"))
                .flatMap(jwtUtil::getId)
                .ifPresent(refreshTokenService::deleteRefreshToken);

        jwtService.expireRefreshCookie(response);

        jwtUtil.extractAccessToken(request)
                .filter(token -> jwtUtil.validateToken(token, "access"))
                .flatMap(jwtUtil::getExpiration)
                .ifPresent(expiration -> {
                    jwtUtil.extractAccessToken(request).ifPresent(token -> {
                        blacklistService.addToBlacklist(token, expiration);
                    });
                });

        response.setHeader("Authorization", "");

        return ResponseEntity.ok("로그아웃 완료");
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "index";
    }

}