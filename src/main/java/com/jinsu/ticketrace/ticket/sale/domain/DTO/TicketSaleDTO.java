package com.jinsu.ticketrace.ticket.sale.domain.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
        @NotBlank
        private long boardPk;

        @NotBlank
        @Min(1)
        private long amount;
    }
}
