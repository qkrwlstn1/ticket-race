package com.jinsu.ticketrace.ticket.sale.repository.redis;

import com.jinsu.ticketrace.ticket.sale.service.TicketSaleCommand;

import java.util.List;

public interface TicketSaleRequestStore {
    TicketSaleReserveResult reserve(TicketSaleCommand command);
    List<String> claimUpTo(int batchSize);
    void acknowledgeAll(List<String> payloads);
    long requeueAll(List<String> payloads);
}
