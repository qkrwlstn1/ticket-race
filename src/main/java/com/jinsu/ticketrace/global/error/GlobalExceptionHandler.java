package com.jinsu.ticketrace.global.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<String> handleBusiness(
            GlobalException e,
            HttpServletRequest request
    ){
        ErrorCode errorCode = e.getErrorCode();
        log.error("{}, {}, {}",
                errorCode,
                errorCode.message(),
                errorCode.httpstatus()
        );

        return ResponseEntity.status(
                errorCode.httpstatus()
        ).body(errorCode.message());
    }
}
