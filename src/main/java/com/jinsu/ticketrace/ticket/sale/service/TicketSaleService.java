package com.jinsu.ticketrace.ticket.sale.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketSaleService {

    private final TicketSaleUseCase ticketSaleUseCase;

    public void ticketSale(long boardPk, long memberPk, long amount){
        ticketSaleUseCase.ticketSale(boardPk, memberPk, amount);
    }

}
