package com.jinsu.ticketrace.global.exception;

import com.jinsu.ticketrace.global.error.ErrorCode;
import com.jinsu.ticketrace.global.error.GlobalException;
import org.springframework.http.HttpStatus;

public enum TicketBoardErrorCode implements ErrorCode {

    TICKET_BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "없는 게시글 입니다")
    ;

    private final HttpStatus status;
    private final String message;

    TicketBoardErrorCode(HttpStatus status, String message) {
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
