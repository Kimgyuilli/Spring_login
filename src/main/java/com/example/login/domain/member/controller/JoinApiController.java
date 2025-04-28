package com.example.login.domain.member.controller;

import com.example.login.global.dto.ApiRes;
import com.example.login.global.exception.BaseException;
import com.example.login.global.response.ErrorType.ErrorCode;
import com.example.login.global.response.SuccessType.MemberSuccessCode;
import com.example.login.domain.member.dto.request.MemberSaveReq;
import com.example.login.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/join")
@RequiredArgsConstructor
@Tag(name = "회원가입 API", description = "회원 등록 및 이메일 중복 확인 API")
public class JoinApiController {

    private final MemberService memberService;

    // 회원가입 요청
    @Operation(
            summary = "회원가입",
            description = "새로운 회원을 등록합니다. 이메일 중복은 허용되지 않으며, 유효성 검사 실패 시 예외를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력값", content = @Content),
            @ApiResponse(responseCode = "409", description = "이메일 중복", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ApiRes<Void>> join(@RequestBody @Valid MemberSaveReq req) {
        memberService.save(req);
        return ResponseEntity.ok(ApiRes.success(MemberSuccessCode.MEMBER_CREATED));
    }

    // 이메일 중복 체크
    @Operation(
            summary = "이메일 중복 확인",
            description = "회원가입 전에 이메일 중복 여부를 확인합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이메일 사용 가능"),
            @ApiResponse(responseCode = "409", description = "이메일 중복")
    })
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