package com.jinsu.ticketrace.global.error;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
public class GlobalException extends RuntimeException{
    private final ErrorCode errorCode;

    public GlobalException(ErrorCode errorCode){
        super(errorCode.message());
        this.errorCode = errorCode;

    }
}
