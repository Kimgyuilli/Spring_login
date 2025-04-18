package com.example.login.Common.response;

import com.example.login.Common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class LoginErrorResponse {
    private final int status;
    private final String error;
    private final String message;

    public LoginErrorResponse(ErrorCode errorCode) {
        this.status = errorCode.getStatus().value();
        this.error = errorCode.getStatus().name();
        this.message = errorCode.getMessage();
    }
}
