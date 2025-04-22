package com.example.login.User.controller;

import com.example.login.Common.jwt.JWTUtil;
import com.example.login.Refresh.Entity.RefreshToken;
import com.example.login.Refresh.repository.RefreshTokenRepository;
import com.example.login.Refresh.service.BlacklistService;
import com.example.login.Refresh.service.RefreshTokenService;
import com.example.login.User.domain.Role;
import com.example.login.User.dto.request.MemberLoginReq;
import com.example.login.User.dto.response.MemberLoginRes;
import com.example.login.User.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/login-page")
    public String loginForm(Model model) {
        model.addAttribute("memberLoginReq", new MemberLoginReq());
        return "login";
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody @Valid MemberLoginReq req, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("입력값 오류");
        }

        MemberLoginRes loginResult = memberService.login(req);
        if (loginResult == null) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body("이메일 또는 비밀번호가 틀렸습니다.");
        }

        String accessToken = jwtUtil.createAccessToken(
                loginResult.getId().toString(), Role.ADMIN, loginResult.getMemberEmail()
        );
        String refreshToken = jwtUtil.createRefreshToken(
                loginResult.getId().toString(), Role.ADMIN, loginResult.getMemberEmail()
        );
        ResponseCookie refreshCookie = jwtUtil.createRefreshTokenCookie(refreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body("로그인 성공");
    }

    // Access Token 재발급
    @PostMapping("/token/refresh")
    public ResponseEntity<?> reissueAccessToken(HttpServletRequest request) {

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

                            String email = emailOpt.get();
                            Role role = roleOpt.get();

                            String newAccessToken = jwtUtil.createAccessToken(memberId, role, email);

                            return Optional.of(ResponseEntity.ok()
                                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken)
                                    .body("Access 토큰 재발급 완료"));
                        }))
                .orElseGet(() -> ResponseEntity
                        .status(HttpServletResponse.SC_UNAUTHORIZED)
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

                            String email = emailOpt.get();
                            Role role = roleOpt.get();

                            String newAccessToken = jwtUtil.createAccessToken(memberId, role, email);
                            String newRefreshToken = jwtUtil.createRefreshToken(memberId, role, email);

                            // Redis 갱신
                            refreshTokenService.saveToken(memberId, newRefreshToken);

                            // 쿠키 갱신
                            ResponseCookie refreshCookie = jwtUtil.createRefreshTokenCookie(newRefreshToken);

                            return Optional.of(ResponseEntity.ok()
                                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken)
                                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                                    .body("토큰 재발급 완료"));
                        }))
                .orElseGet(() -> ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED)
                        .body("유효하지 않거나 일치하지 않는 리프레시 토큰입니다."));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 리프레시 토큰 추출
        jwtUtil.extractRefreshToken(request)
                .filter(token -> jwtUtil.validateToken(token, "refresh"))
                .flatMap(jwtUtil::getId)
                .ifPresent(refreshTokenService::deleteRefreshToken);

        // 클라이언트 쿠키에서 제거
        ResponseCookie deleteCookie = jwtUtil.invalidateRefreshToken();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        // access token 블랙리스트 등록
        jwtUtil.extractAccessToken(request)
                .filter(token -> jwtUtil.validateToken(token, "access"))
                .flatMap(jwtUtil::getExpiration)
                .ifPresent(expiration -> {
                    String token = jwtUtil.extractAccessToken(request).get();
                    blacklistService.addToBlacklist(token, expiration);
                });

        // (선택) Authorization 헤더 제거
        response.setHeader(HttpHeaders.AUTHORIZATION, "");

        return ResponseEntity.ok("로그아웃 완료");
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "index";
    }

}