package com.jinsu.ticketrace.global.exception;

import com.jinsu.ticketrace.global.error.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * 티켓 환불 관련 에러 코드.
 *
 * 기존 TicketBoardErrorCode는 게시글 CRUD에 관한 것이고,
 * 환불은 Ticket 엔티티 도메인이므로 별도 enum으로 분리
 */
public enum TicketErrorCode implements ErrorCode {

    TICKET_NOT_FOUND(HttpStatus.NOT_FOUND, "티켓을 찾을 수 없습니다"),
    TICKET_NOT_OWNER(HttpStatus.FORBIDDEN, "본인의 티켓이 아닙니다"),
    ALREADY_REFUNDED(HttpStatus.CONFLICT, "이미 환불된 티켓입니다");

    private final HttpStatus status;
    private final String message;

    TicketErrorCode(HttpStatus status, String message) {
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