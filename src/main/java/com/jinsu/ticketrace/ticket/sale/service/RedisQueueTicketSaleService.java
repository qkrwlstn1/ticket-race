package com.jinsu.ticketrace.ticket.sale.service;

import com.jinsu.ticketrace.global.error.GlobalException;
import com.jinsu.ticketrace.global.exception.MemberErrorCode;
import com.jinsu.ticketrace.global.exception.TicketBoardErrorCode;
import com.jinsu.ticketrace.member.repository.MemberRepository;
import com.jinsu.ticketrace.ticket.board.repository.TicketBoardRepository;
import com.jinsu.ticketrace.ticket.sale.repository.redis.TicketSaleRequestStore;
import com.jinsu.ticketrace.ticket.sale.repository.redis.TicketSaleReserveResult;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Primary
@Service
@RequiredArgsConstructor
public class RedisQueueTicketSaleService implements TicketSaleUseCase {

    private final TicketBoardRepository ticketBoardRepository;
    private final MemberRepository memberRepository;
    private final TicketSaleRequestStore ticketSaleRequestStore;
    private final StringRedisTemplate redisTemplate;

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

        TicketSaleReserveResult reserveResult = tryReserveWithSnapshotRecovery(command);
        if (reserveResult == TicketSaleReserveResult.KEY_MISSING) {
            rebuildSnapshot(ticketBoardPk, memberPk);
            reserveResult = ticketSaleRequestStore.reserve(command);
        }


        if (reserveResult == TicketSaleReserveResult.SOLD_OUT) {
            throw new GlobalException(TicketBoardErrorCode.TICKET_SOLD_OUT);
        }
        if (reserveResult == TicketSaleReserveResult.INSUFFICIENT_FUNDS) {
            throw new GlobalException(MemberErrorCode.ACCOUNT_INSUFFICIENT_FUNDS);
        }

        if (reserveResult == TicketSaleReserveResult.KEY_MISSING) {
//            snapshotInitializer.initSnapshot(ticketBoardPk, memberPk); // DB → Redis 동기화
            reserveResult = ticketSaleRequestStore.reserve(command);   // 재시도
            if (reserveResult == TicketSaleReserveResult.KEY_MISSING) {
                throw new IllegalStateException("redis snapshot rebuild 후에도 키 없음");
            }
        }
    }

    private TicketSaleReserveResult tryReserveWithSnapshotRecovery(TicketSaleCommand command) {
        TicketSaleReserveResult reserveResult = ticketSaleRequestStore.reserve(command);

        int retries = 0;
        while (reserveResult == TicketSaleReserveResult.KEY_MISSING && retries < 3) {
            rebuildSnapshot(command.boardPk(), command.memberPk());
            reserveResult = ticketSaleRequestStore.reserve(command);
            retries++;
        }

        return reserveResult;
    }


    private void rebuildSnapshot(long boardPk, long memberPk) {
        long quantity = ticketBoardRepository.findById(boardPk)
                .orElseThrow(() -> new GlobalException(TicketBoardErrorCode.TICKET_BOARD_NOT_FOUND))
                .getQuantity();

        long account = memberRepository.findById(memberPk)
                .orElseThrow(() -> new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND))
                .getAccount();

        redisTemplate.opsForValue().setIfAbsent(quantityKey(boardPk), String.valueOf(quantity));
        redisTemplate.opsForValue().setIfAbsent(accountKey(memberPk), String.valueOf(account));
    }

    private String quantityKey(long boardPk) {
        return "ticket:inventory:ga:" + boardPk + ":quantity";
    }

    private String accountKey(long memberPk) {
        return "ticket:member:" + memberPk + ":account";
    }

}