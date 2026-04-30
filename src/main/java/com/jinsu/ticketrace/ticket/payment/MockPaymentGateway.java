package com.jinsu.ticketrace.ticket.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 테스트/개발용 Mock 결제 게이트웨이.
 *
 * 실제 PG 호출 없이 항상 성공 처리합니다.
 */
@Slf4j
@Component
public class MockPaymentGateway implements PaymentGateway {

    private static final String TX_PREFIX = "mock_tx_";

    @Override
    public PaymentResult charge(PaymentRequest request) {
        // 실제 PG 호출 시뮬레이션 — 외부 네트워크 latency 가정 시 Thread.sleep 등 가능
        String transactionId = TX_PREFIX + UUID.randomUUID().toString().replace("-", "");

        log.info("[MockPG] 결제 처리 memberPk={}, amount={}, method={}, txId={}",
                request.memberPk(), request.amount(), request.method(), transactionId);

        return PaymentResult.success(transactionId);
    }
}
