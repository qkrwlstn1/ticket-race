package com.jinsu.ticketrace.ticket.payment;

/**
 * PG 결제 요청 정보.
 *
 * @param memberPk      결제 요청 회원 PK
 * @param amount        결제 금액 (원 단위)
 * @param method        결제 수단 (CARD / VBANK 등)
 * @param idempotencyKey PG 호출 멱등성 키 — 동일 키로 중복 호출 시 PG가 1회만 처리
 */
public record PaymentRequest(
        long memberPk,
        long amount,
        PaymentMethod method,
        String idempotencyKey
) {
    public PaymentRequest {
        if (amount <= 0) {
            throw new IllegalArgumentException("결제 금액은 0보다 커야 합니다.");
        }
    }
}
