package com.example.login.User.controller;

import com.example.login.Common.exception.BaseException;
import com.example.login.User.dto.request.MemberLoginReq;
import com.example.login.User.dto.request.MemberSaveReq;
import com.example.login.User.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class JoinController {

    private final MemberService memberService;

    @GetMapping("/save")
    public String saveForm(Model model) {
        model.addAttribute("memberSaveReq", new MemberSaveReq());
        return "save";
    }

    @PostMapping("/save")
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

    @PostMapping("/email-check")
    public @ResponseBody String emailCheck(@RequestParam("memberEmail") String memberEmail) {
        System.out.println("memberEmail = " + memberEmail);
        return memberService.emailCheck(memberEmail);
    }
}
