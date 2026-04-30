package com.jinsu.ticketrace.ticket.payment;

/**
 * 결제 수단.
 * 실제 PG 연동 시 PG 별 코드와 매핑됩니다.
 */
public enum PaymentMethod {
    CARD,        // 신용/체크카드
    VBANK,       // 가상계좌
    MOCK         // 테스트/Mock 전용
}
