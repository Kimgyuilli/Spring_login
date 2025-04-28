package com.example.login.domain.member.controller;


import com.example.login.domain.member.dto.request.MemberLoginReq;
import com.example.login.domain.member.dto.response.MemberLoginRes;
import com.example.login.global.dto.ApiRes;
import com.example.login.global.response.SuccessType.MemberSuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/docs/auth")
@Tag(name = "인증 (Swagger 전용)", description = "Swagger 문서용 로그인 API 설명")
public class AuthDocsController {

    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호를 입력받아 로그인합니다. 실제 인증은 필터(LoginFilter)에서 처리됩니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "로그인 성공 (AccessToken + RefreshToken 발급)",
            content = @Content(schema = @Schema(implementation = MemberLoginRes.class))
    )
    @PostMapping("/login")
    public ResponseEntity<ApiRes<Void>> login(
            @RequestBody(description = "로그인 요청", required = true,
                    content = @Content(schema = @Schema(implementation = MemberLoginReq.class)))
            MemberLoginReq req
    ) {
        return ResponseEntity.ok(ApiRes.success(MemberSuccessCode.LOGIN_SUCCESS)); // 실제 동작 안함. 문서용임.
    }
}
