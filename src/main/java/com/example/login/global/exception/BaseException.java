package com.example.login.global.exception;

import com.example.login.global.response.ErrorType.ErrorType;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private final ErrorType errorCode;

    public BaseException(ErrorType errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
