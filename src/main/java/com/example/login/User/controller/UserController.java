package com.example.login.User.controller;

import com.example.login.User.dto.request.MemberUpdateReq;
import com.example.login.User.dto.response.MemberRes;
import com.example.login.User.service.MemberService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.List;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class UserController {

    private final MemberService memberService;

    @GetMapping("/")
    public String findAll(Model model) {
        List<MemberRes> memberDTOList = memberService.findAll();
//        어떠한 html로 가져갈 데이터가 있다면 model 사용
        model.addAttribute("memberList", memberDTOList);
        return "list";
    }

    @GetMapping("/{id}")
    public String findById(@PathVariable("id") Long id, Model model) {
        MemberRes memberDTO = memberService.findById(id);
        model.addAttribute("member", memberDTO);
        return "detail";
    }

    @GetMapping("/update")
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

    @PostMapping("/update")
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

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, Model model) {
        memberService.deleteById(id);
        return "redirect:/member/";
    }
}
