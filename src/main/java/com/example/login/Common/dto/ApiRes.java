package com.example.login.Common.dto;

import com.example.login.Common.response.ErrorType.ErrorType;
import com.example.login.Common.response.SuccessType.MemberSuccessCode;
import lombok.Getter;

@Getter
public class ApiRes<T> {

    private final String code;
    private final String message;
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
