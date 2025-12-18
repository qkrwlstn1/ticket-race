package com.jinsu.ticketrace.global.error;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    HttpStatus httpstatus();
    String message();
}
