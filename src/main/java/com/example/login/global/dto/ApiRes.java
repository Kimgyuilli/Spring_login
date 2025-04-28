package com.example.login.global.dto;

import com.example.login.global.response.ErrorType.ErrorType;
import com.example.login.global.response.SuccessType.MemberSuccessCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "공통 API 응답")
public class ApiRes<T> {

    @Schema(description = "응답 코드", example = "M001" /* 또는 E001 등 */)
    private final String code;

    @Schema(description = "응답 메시지", example = "회원 정보 조회 성공")
    private final String message;

    @Schema(description = "응답 데이터", nullable = true)
    private final T data;

    // 성공 응답
    public static <T> ApiRes<T> success(MemberSuccessCode successCode, T data) {
        return new ApiRes<>(successCode.getCode(), successCode.getMessage(), data);
    }

    public static ApiRes<Void> success(MemberSuccessCode successCode) {
        return new ApiRes<>(successCode.getCode(), successCode.getMessage(), null);
    }

    // 실패 응답
    public static ApiRes<Void> fail(ErrorType errorType) {
        return new ApiRes<>(errorType.getCode(), errorType.getMessage(), null);
    }

    // 생성자
    private ApiRes(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
