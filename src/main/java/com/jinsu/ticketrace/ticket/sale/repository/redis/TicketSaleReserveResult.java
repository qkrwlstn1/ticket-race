package com.jinsu.ticketrace.ticket.sale.repository.redis;

public enum TicketSaleReserveResult {
    SUCCESS(1L),
    SOLD_OUT(-1L),
    KEY_MISSING(-2L),
    INSUFFICIENT_FUNDS(-3L);

    private final long code;

    TicketSaleReserveResult(long code) {
        this.code = code;
    }

    public static TicketSaleReserveResult fromCode(Long code) {
        if (code == null) {
            throw new IllegalStateException("Redis script returned null");
        }

        for (TicketSaleReserveResult value : values()) {
            if (value.code == code) {
                return value;
            }
        }

        throw new IllegalStateException("Unknown reserve result code: " + code);
    }
}