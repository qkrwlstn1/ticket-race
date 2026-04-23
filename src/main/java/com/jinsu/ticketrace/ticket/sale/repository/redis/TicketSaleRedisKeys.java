package com.jinsu.ticketrace.ticket.sale.repository.redis;

public class TicketSaleRedisKeys {
    private TicketSaleRedisKeys() {
        // 유틸리티 클래스 — 인스턴스화 방지
    }

    /**
     * GA 티켓 재고 키
     * 예: ticket:inventory:ga:42:quantity
     */
    public static String inventoryKey(long boardPk) {
        return "ticket:inventory:ga:" + boardPk + ":quantity";
    }

    /**
     * 회원 잔액 키
     * 예: ticket:member:7:account
     */
    public static String accountKey(long memberPk) {
        return "ticket:member:" + memberPk + ":account";
    }
}
