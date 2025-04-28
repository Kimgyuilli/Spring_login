// ErrorCode.java
package com.example.login.global.response.ErrorType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements ErrorType {
    MEMBER_NOT_FOUND("E404", "회원을 찾을 수 없습니다", HttpStatus.NOT_FOUND.value()),
    LOGIN_FAIL("E401", "이메일 또는 비밀번호가 틀렸습니다", HttpStatus.UNAUTHORIZED.value()),
    DUPLICATE_EMAIL("E409", "이미 가입된 이메일입니다", HttpStatus.CONFLICT.value()),
    INVALID_ROLE("E400", "잘못된 역할(Role)입니다", HttpStatus.BAD_REQUEST.value()),
    INVALID_TOKEN("E401", "유효하지 않은 토큰입니다", HttpStatus.UNAUTHORIZED.value()),
    OAUTH2_LOGIN_FAILED("E401", "소셜 로그인에 실패했습니다. 다시 시도해주세요.", HttpStatus.UNAUTHORIZED.value());

    private final String code;
    private final String message;
    private final int status;
}
