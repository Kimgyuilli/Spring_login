package com.example.login.Common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "이미 사용 중인 이메일입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원 정보를 찾을 수 없습니다."),
    LOGIN_FAIL(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 틀렸습니다."),
    INVALID_ROLE(HttpStatus.BAD_REQUEST, "역할은 USER 또는 ADMIN 중 하나여야 합니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
