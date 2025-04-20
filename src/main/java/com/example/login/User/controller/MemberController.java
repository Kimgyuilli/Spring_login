package com.example.login.User.controller;

import com.example.login.Common.exception.BaseException;
import com.example.login.Common.jwt.JWTUtil;
import com.example.login.User.domain.Role;
import com.example.login.User.dto.request.MemberLoginReq;
import com.example.login.User.dto.request.MemberSaveReq;
import com.example.login.User.dto.request.MemberUpdateReq;
import com.example.login.User.dto.response.MemberLoginRes;
import com.example.login.User.dto.response.MemberRes;
import com.example.login.User.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final JWTUtil jwtUtil;

    @PostMapping("/member/token/refresh")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtUtil.extractRefreshToken(request);

        if (refreshToken == null || !jwtUtil.validateToken(refreshToken, "refresh")) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body("유효하지 않은 리프레시 토큰");
        }

        String id = jwtUtil.getId(refreshToken);
        String email = jwtUtil.getEmail(refreshToken);
        Role role = jwtUtil.getRole(refreshToken);

        String newAccessToken = jwtUtil.createAccessToken(id, role, email);

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken)
                .build();
    }

    @GetMapping("/member/save")
    public String saveForm(Model model) {
        model.addAttribute("memberSaveReq", new MemberSaveReq());
        return "save";
    }

    @PostMapping("/member/save")
    public String save(@ModelAttribute @Valid MemberSaveReq req,
                       BindingResult bindingResult, Model model) {
        System.out.println("[Controller] save() 요청 들어옴");

        if (bindingResult.hasErrors()) {
            System.out.println("[Controller] 유효성 오류 목록:");
            bindingResult.getAllErrors().forEach(error -> {
                System.out.println(" - " + error.getDefaultMessage());
            });
            model.addAttribute("memberSaveReq", req);
            return "save";
        }


        try {
            System.out.println("[Controller] memberService.save() 호출");
            memberService.save(req);
            model.addAttribute("memberLoginReq", new MemberLoginReq());
            return "login";
        } catch (BaseException e) {
            System.out.println("[Controller] 예외 발생: " + e.getMessage());
            return "save";
        }
    }


    @GetMapping("/member/login-page")
    public String loginForm(Model model) {
        model.addAttribute("memberLoginReq", new MemberLoginReq());
        return "login";
    }

    @PostMapping("/member/login")
    public String login(@ModelAttribute @Valid MemberLoginReq req,
                        BindingResult bindingResult,
                        HttpSession session,
                        Model model) {
        System.out.println("[Controller] login() 요청 들어옴");
        if (bindingResult.hasErrors()) {
            model.addAttribute("memberLoginReq", req);
            return "login"; // 유효성 실패 시 로그인 폼으로
        }

        MemberLoginRes loginResult = memberService.login(req);
        if (loginResult != null) {
            session.setAttribute("loginEmail", loginResult.getMemberEmail());
            model.addAttribute("memberLoginReq", req);
            return "main";
        } else {
            model.addAttribute("loginError", "이메일 또는 비밀번호가 틀렸습니다.");
            return "login";
        }
    }

    @GetMapping("/member/")
    public String findAll(Model model) {
        List<MemberRes> memberDTOList = memberService.findAll();
//        어떠한 html로 가져갈 데이터가 있다면 model 사용
        model.addAttribute("memberList", memberDTOList);
        return "list";
    }

    @GetMapping("/member/{id}")
    public String findById(@PathVariable("id") Long id, Model model) {
        MemberRes memberDTO = memberService.findById(id);
        model.addAttribute("member", memberDTO);
        return "detail";
    }

    @GetMapping("/member/update")
    public String updateForm(Model model, HttpSession session) {
        String myEmail = (String) session.getAttribute("loginEmail");
        MemberRes res = memberService.updateForm(myEmail);

        MemberUpdateReq req = MemberUpdateReq.builder()
                .id(res.getId())
                .memberEmail(res.getMemberEmail())
                .memberName(res.getMemberName())
                .memberPassword(res.getMemberPassword())
                .build();

        model.addAttribute("memberUpdateReq", req);
        return "update";
    }

    @PostMapping("/member/update")
    public String update(@Valid @ModelAttribute MemberUpdateReq req,
                         BindingResult bindingResult,
                         HttpSession session,
                         Model model) {

        if (bindingResult.hasErrors()) {
            // 유효성 오류 시 다시 update.html로 이동 + 기존 값 유지
            model.addAttribute("memberUpdateReq", req);
            return "update";
        }

        // 세션에서 이메일 꺼내서 채우기 (readonly 필드는 form에서 안 넘어올 수 있음)
        String email = (String) session.getAttribute("loginEmail");
        req.setMemberEmail(email);

        memberService.update(req);
        return "redirect:/member/" + req.getId();
    }

    @GetMapping("/member/delete/{id}")
    public String delete(@PathVariable("id") Long id, Model model) {
        memberService.deleteById(id);
        return "redirect:/member/";
    }

    @GetMapping("/member/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "index";
    }

    @PostMapping("/member/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // 쿠키 삭제: refresh token을 무효화된 상태로 다시 쿠키에 덮어씀
        ResponseCookie deleteCookie = jwtUtil.invalidateRefreshToken();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        // (선택) Authorization 헤더 제거
        response.setHeader(HttpHeaders.AUTHORIZATION, "");

        return ResponseEntity.ok("로그아웃 완료");
    }

    @PostMapping("/member/email-check")
    public @ResponseBody String emailCheck(@RequestParam("memberEmail") String memberEmail) {
        System.out.println("memberEmail = " + memberEmail);
        return memberService.emailCheck(memberEmail);
    }

}
