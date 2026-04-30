package com.jinsu.ticketrace.ticket.payment;

/**
 * PG 결제 결과.
 *
 * @param success         결제 성공 여부
 * @param transactionId   PG 발급 거래 ID — 영수증/조회/취소 시 사용
 * @param failureReason   실패 사유 (success=false 일 때만)
 */
public record PaymentResult(
        boolean success,
        String transactionId,
        String failureReason
) {
    public static PaymentResult success(String transactionId) {
        return new PaymentResult(true, transactionId, null);
    }

    public static PaymentResult failure(String reason) {
        return new PaymentResult(false, null, reason);
    }
}
