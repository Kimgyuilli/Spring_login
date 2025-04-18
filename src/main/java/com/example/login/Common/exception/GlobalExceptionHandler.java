package com.example.login.Common.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<?> handleBaseException(BaseException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(e.getErrorCode().getMessage()); // 혹은 공통 응답 형식 사용
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<?> handleInvalidFormat(InvalidFormatException e) {
        // Role 변환 실패만 잡고 싶은 경우
        if (e.getTargetType().isEnum() && e.getTargetType().getSimpleName().equals("Role")) {
            return ResponseEntity
                    .status(ErrorCode.INVALID_ROLE.getStatus())
                    .body(ErrorCode.INVALID_ROLE.getMessage());
        }

        // 그 외는 400으로 일반 처리
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("입력값이 잘못되었습니다.");
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> handleUsernameNotFound(UsernameNotFoundException e) {
        return ResponseEntity
                .status(ErrorCode.MEMBER_NOT_FOUND.getStatus())
                .body(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }
}