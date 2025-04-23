package com.example.login.User.controller;

import com.example.login.Common.dto.ApiRes;
import com.example.login.Common.response.SuccessType.MemberSuccessCode;
import com.example.login.User.dto.request.MemberUpdateReq;
import com.example.login.User.dto.response.MemberRes;
import com.example.login.User.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserApiController {

    private final MemberService memberService;

    // 전체 사용자 목록 조회
    @GetMapping
    public ResponseEntity<ApiRes<List<MemberRes>>> findAll() {
        List<MemberRes> members = memberService.findAll();
        return ResponseEntity.ok(ApiRes.success(MemberSuccessCode.MEMBER_VIEW, members));
    }

    // 특정 사용자 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiRes<MemberRes>> findById(@PathVariable Long id) {
        MemberRes member = memberService.findById(id);
        return ResponseEntity.ok(ApiRes.success(MemberSuccessCode.MEMBER_VIEW, member));
    }

    // 사용자 정보 수정
    @PutMapping("/{id}")
    public ResponseEntity<ApiRes<Void>> update(@PathVariable Long id, @RequestBody MemberUpdateReq req) {
        req.setId(id);
        memberService.update(req);
        return ResponseEntity.ok(ApiRes.success(MemberSuccessCode.MEMBER_UPDATED));
    }

    // 사용자 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiRes<Void>> delete(@PathVariable Long id) {
        memberService.deleteById(id);
        return ResponseEntity.ok(ApiRes.success(MemberSuccessCode.MEMBER_DELETED));
    }
}

