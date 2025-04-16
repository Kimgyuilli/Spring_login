package com.example.login.User.controller;

import com.example.login.User.dto.request.MemberLoginReq;
import com.example.login.User.dto.request.MemberSaveReq;
import com.example.login.User.dto.request.MemberUpdateReq;
import com.example.login.User.dto.response.MemberLoginRes;
import com.example.login.User.dto.response.MemberRes;
import com.example.login.User.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/member/save")
    public String saveForm() {
        return "save";
    }

    @PostMapping("/member/save")
    public String save(@ModelAttribute MemberSaveReq req, Model model) {
        try {
            memberService.save(req);
            return "login";
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "save"; // 회원가입 폼으로 다시 이동
        }
    }

    @GetMapping("/member/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/member/login")
    public String login(@ModelAttribute MemberLoginReq req, HttpSession session) {
        MemberLoginRes loginResult = memberService.login(req);
        if (loginResult != null) {
            // 로그인 성공
            session.setAttribute("loginEmail", loginResult.getMemberEmail());
            return "main";
        } else {
            // 로그인 실패
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
        model.addAttribute("updateMember", res);
        return "update";
    }

    @PostMapping("/member/update")
    public String update(@ModelAttribute MemberUpdateReq req) {
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
