package com.jinsu.ticketrace.global.exception;

import com.jinsu.ticketrace.global.error.ErrorCode;
import org.springframework.http.HttpStatus;


public enum PaymentErrorCode implements ErrorCode {

    PAYMENT_FAILED(HttpStatus.BAD_GATEWAY, "결제 처리에 실패했습니다."),
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "결제 금액이 유효하지 않습니다."),
    PAYMENT_GATEWAY_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "결제 게이트웨이 응답 시간이 초과되었습니다.")
    ;

    private final HttpStatus status;
    private final String message;

    PaymentErrorCode(HttpStatus status, String message) {
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
