package com.example.login.User.controller;

import com.example.login.Common.exception.BaseException;
import com.example.login.User.dto.request.MemberLoginReq;
import com.example.login.User.dto.request.MemberSaveReq;
import com.example.login.User.dto.request.MemberUpdateReq;
import com.example.login.User.dto.response.MemberLoginRes;
import com.example.login.User.dto.response.MemberRes;
import com.example.login.User.service.MemberService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/member/save")
    public String saveForm(Model model) {
        model.addAttribute("memberSaveReq", new MemberSaveReq());
        return "save";
    }

    @PostMapping("/member/save")
    public String save(@ModelAttribute @Valid MemberSaveReq req,
                       BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("memberSaveReq", req);
            return "save"; // 유효성 에러가 있으면 회원가입 페이지로 다시
        }

        try {
            memberService.save(req);
            model.addAttribute("memberLoginReq", new MemberLoginReq());
            return "login";
        } catch (BaseException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "save"; // 회원가입 폼으로 다시 이동
        }
    }


    @GetMapping("/member/login")
    public String loginForm(Model model) {
        model.addAttribute("memberLoginReq", new MemberLoginReq());
        return "login";
    }

    @PostMapping("/member/login")
    public String login(@ModelAttribute @Valid MemberLoginReq req,
                        BindingResult bindingResult,
                        HttpSession session,
                        Model model) {
        if (bindingResult.hasErrors()) {
            return "login"; // 유효성 실패 시 로그인 폼으로
        }

        MemberLoginRes loginResult = memberService.login(req);
        if (loginResult != null) {
            session.setAttribute("loginEmail", loginResult.getMemberEmail());
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

    @PostMapping("/member/email-check")
    public @ResponseBody String emailCheck(@RequestParam("memberEmail") String memberEmail) {
        System.out.println("memberEmail = " + memberEmail);
        return memberService.emailCheck(memberEmail);
    }

}
