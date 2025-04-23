package com.example.login.User.controller;

import com.example.login.Common.dto.ApiRes;
import com.example.login.Common.exception.BaseException;
import com.example.login.Common.response.ErrorType.ErrorCode;
import com.example.login.Common.response.SuccessType.MemberSuccessCode;
import com.example.login.User.dto.request.MemberSaveReq;
import com.example.login.User.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/join")
@RequiredArgsConstructor
public class JoinApiController {

    private final MemberService memberService;

    // 회원가입 요청
    @PostMapping
    public ResponseEntity<ApiRes<Void>> join(@RequestBody @Valid MemberSaveReq req) {
        memberService.save(req);
        return ResponseEntity.ok(ApiRes.success(MemberSuccessCode.MEMBER_CREATED));
    }

    // 이메일 중복 체크
    @PostMapping("/email-check")
    public ResponseEntity<ApiRes<Void>> emailCheck(@RequestParam String memberEmail) {
        boolean isAvailable = memberService.emailCheck(memberEmail);
        if (isAvailable) {
            return ResponseEntity.ok(ApiRes.success(MemberSuccessCode.EMAIL_CHECK_OK));
        } else {
            throw new BaseException(ErrorCode.DUPLICATE_EMAIL); // 또는 ErrorCode.DUPLICATE_EMAIL
        }
    }
}