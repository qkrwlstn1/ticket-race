package com.jinsu.ticketrace.global.exception;

import com.jinsu.ticketrace.global.error.ErrorCode;
import org.springframework.http.HttpStatus;

public enum AuthErrorCode implements ErrorCode {
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "refresh token이 유효하지 않습니다"),
    REFRESH_TOKEN_REQUIRED(HttpStatus.UNAUTHORIZED, "refresh token이 필요합니다")
    ;

    private final HttpStatus status;
    private final String message;
    AuthErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public HttpStatus httpstatus() {
        return status;
    }

    @Override
    public String message() {
        return message;
    }
}
