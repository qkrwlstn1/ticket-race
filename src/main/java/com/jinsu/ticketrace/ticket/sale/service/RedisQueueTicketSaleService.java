package com.jinsu.ticketrace.ticket.sale.service;

import com.jinsu.ticketrace.global.error.GlobalException;
import com.jinsu.ticketrace.global.exception.MemberErrorCode;
import com.jinsu.ticketrace.global.exception.TicketBoardErrorCode;
import com.jinsu.ticketrace.ticket.board.repository.TicketBoardRepository;
import com.jinsu.ticketrace.ticket.sale.repository.redis.TicketSaleRequestStore;
import com.jinsu.ticketrace.ticket.sale.repository.redis.TicketSaleReserveResult;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Primary
@Service
@RequiredArgsConstructor
public class RedisQueueTicketSaleService implements TicketSaleUseCase {

    private final TicketBoardRepository ticketBoardRepository;
    private final TicketSaleRequestStore ticketSaleRequestStore;

    @Override
    public void ticketSale(long ticketBoardPk, long memberPk, long amount) {
        long price = ticketBoardRepository.findPriceByBoardPk(ticketBoardPk)
                .orElseThrow(() -> new GlobalException(TicketBoardErrorCode.TICKET_BOARD_NOT_FOUND));

        TicketSaleCommand command = new TicketSaleCommand(
                UUID.randomUUID().toString(),
                ticketBoardPk,
                memberPk,
                amount,
                price
        );

        TicketSaleReserveResult reserveResult = ticketSaleRequestStore.reserve(command);

        if (reserveResult == TicketSaleReserveResult.SOLD_OUT) {
            throw new GlobalException(TicketBoardErrorCode.TICKET_SOLD_OUT);
        }
        if (reserveResult == TicketSaleReserveResult.INSUFFICIENT_FUNDS) {
            throw new GlobalException(MemberErrorCode.ACCOUNT_INSUFFICIENT_FUNDS);
        }
        if (reserveResult == TicketSaleReserveResult.KEY_MISSING) {
            throw new IllegalStateException("redis sale snapshot missing");
        }
    }
}