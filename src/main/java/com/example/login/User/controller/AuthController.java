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

    @PostMapping("/token/refresh")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtUtil.extractRefreshToken(request);

        if (refreshToken == null || !jwtUtil.validateToken(refreshToken, "refresh")) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body("유효하지 않은 리프레시 토큰");
        }

        String memberId = jwtUtil.getId(refreshToken);
        RefreshToken stored = refreshTokenRepository.findById(memberId).orElse(null);

        if (stored == null || !stored.getToken().equals(refreshToken)) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body("저장된 리프레시 토큰과 일치하지 않습니다.");
        }

        // 새 Access + Refresh 토큰 발급
        String email = jwtUtil.getEmail(refreshToken);
        Role role = jwtUtil.getRole(refreshToken);

        String newAccessToken = jwtUtil.createAccessToken(memberId, role, email);
        String newRefreshToken = jwtUtil.createRefreshToken(memberId, role, email);

        // Redis 갱신
        refreshTokenService.saveToken(memberId, newRefreshToken);

        // 쿠키 갱신
        ResponseCookie refreshCookie = jwtUtil.createRefreshTokenCookie(newRefreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken)
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body("토큰 재발급 완료");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // 1. 쿠키에서 리프레시 토큰 추출
        String refreshToken = jwtUtil.extractRefreshToken(request);

        // 2. 토큰 검증
        if (refreshToken != null && jwtUtil.validateToken(refreshToken, "refresh")) {
            // 3. 토큰에서 ID 추출 후 Redis에서 삭제
            String memberId = jwtUtil.getId(refreshToken);
            refreshTokenService.deleteRefreshToken(memberId);
        }

        // 4. 클라이언트 쿠키에서 제거
        ResponseCookie deleteCookie = jwtUtil.invalidateRefreshToken();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        // access token 블랙리스트 등록
        String accessToken = jwtUtil.extractAccessToken(request);
        if (accessToken != null && jwtUtil.validateToken(accessToken, "access")) {
            long expiration = jwtUtil.getExpiration(accessToken); // 남은 만료 시간(ms)
            blacklistService.addToBlacklist(accessToken, expiration);
        }

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