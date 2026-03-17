package com.jinsu.ticketrace.ticket.sale.service;

public interface TicketSaleUseCase {
    void ticketSale(long ticketBoardPk, long memberPk, long amount);
}
