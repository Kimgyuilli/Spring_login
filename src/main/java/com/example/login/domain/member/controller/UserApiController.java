package com.example.login.domain.member.controller;

import com.example.login.global.dto.ApiRes;
import com.example.login.global.response.SuccessType.MemberSuccessCode;
import com.example.login.domain.member.dto.request.MemberUpdateReq;
import com.example.login.domain.member.dto.response.MemberRes;
import com.example.login.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "회원 관리 API", description = "사용자 조회, 수정, 삭제 관련 API")
public class UserApiController {

    private final MemberService memberService;

    // 전체 사용자 목록 조회
    @Operation(
            summary = "전체 사용자 목록 조회",
            description = "가입된 모든 사용자의 정보를 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<ApiRes<List<MemberRes>>> findAll() {
        List<MemberRes> members = memberService.findAll();
        return ResponseEntity.ok(ApiRes.success(MemberSuccessCode.MEMBER_VIEW, members));
    }

    // 특정 사용자 조회
    @Operation(
            summary = "특정 사용자 조회",
            description = "사용자 ID를 기준으로 회원 정보를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 사용자가 존재하지 않음", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiRes<MemberRes>> findById(@PathVariable Long id) {
        MemberRes member = memberService.findById(id);
        return ResponseEntity.ok(ApiRes.success(MemberSuccessCode.MEMBER_VIEW, member));
    }

    // 사용자 정보 수정

    @Operation(
            summary = "사용자 정보 수정",
            description = "요청 본문의 정보로 사용자 정보를 수정합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 유효성 실패", content = @Content),
            @ApiResponse(responseCode = "404", description = "해당 사용자가 존재하지 않음", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiRes<Void>> update(@PathVariable Long id, @RequestBody MemberUpdateReq req) {
        req.setId(id);
        memberService.update(req);
        return ResponseEntity.ok(ApiRes.success(MemberSuccessCode.MEMBER_UPDATED));
    }

    // 사용자 삭제
    @Operation(
            summary = "사용자 삭제",
            description = "지정한 사용자 ID에 해당하는 회원을 삭제합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "해당 사용자가 존재하지 않음", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiRes<Void>> delete(@PathVariable Long id) {
        memberService.deleteById(id);
        return ResponseEntity.ok(ApiRes.success(MemberSuccessCode.MEMBER_DELETED));
    }
}

