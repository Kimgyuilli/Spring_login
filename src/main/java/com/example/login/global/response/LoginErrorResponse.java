package com.example.login.global.response;

import com.example.login.global.response.ErrorType.ErrorType;
import lombok.Getter;

@Getter
public class LoginErrorResponse {
    private final int status;
    private final String error;
    private final String message;

    public LoginErrorResponse(ErrorType errorType) {
        this.status = errorType.getStatus();
        this.error = errorType.getCode();
        this.message = errorType.getMessage();
    }
}