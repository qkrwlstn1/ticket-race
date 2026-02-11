package com.jinsu.ticketrace.ticket.sale.domain.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class TicketSaleDTO {

    @Builder
    @Getter
    @RequiredArgsConstructor
    @AllArgsConstructor
    public static class RequestSale{
        long boardPk;
    }
}
