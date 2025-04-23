package com.example.login.Common.exception;

import com.example.login.Common.response.ErrorType.ErrorType;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private final ErrorType errorCode;

    public BaseException(ErrorType errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
