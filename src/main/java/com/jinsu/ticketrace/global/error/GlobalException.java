package com.jinsu.ticketrace.global.error;

import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException{
    private final ErrorCode errorCode;

    public GlobalException(ErrorCode errorCode){
        super(errorCode.message());
        this.errorCode = errorCode;

    }
}
