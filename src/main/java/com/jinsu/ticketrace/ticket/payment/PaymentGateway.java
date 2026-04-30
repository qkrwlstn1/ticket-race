package com.jinsu.ticketrace.ticket.payment;


public interface PaymentGateway {

    /**
     * 결제를 요청합니다.
     *
     * @param request 결제 요청 (회원 PK, 금액, 결제 수단)
     * @return 결제 결과 — 성공 시 transactionId 포함, 실패 시 사유 포함
     */
    PaymentResult charge(PaymentRequest request);
}
