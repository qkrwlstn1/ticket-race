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
    private final TicketSaleSnapshotService snapshotService; // 추가

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

        TicketSaleReserveResult result = ticketSaleRequestStore.reserve(command);

        // KEY_MISSING: DB에서 스냅샷을 재구성하고 단 1회 재시도
        if (result == TicketSaleReserveResult.KEY_MISSING) {
            result = rebuildAndRetry(command, ticketBoardPk, memberPk);
        }

        handleResult(result);
    }

    /**
     * DB에서 재고·잔액을 읽어 Redis 키를 재구성한 뒤 reserve를 재시도합니다.
     * SETNX를 사용하므로 동시에 여러 스레드가 진입해도 중복 세팅 없이 안전합니다.
     */
    private TicketSaleReserveResult rebuildAndRetry(
            TicketSaleCommand command, long boardPk, long memberPk) {

        snapshotService.rebuildInventorySnapshotIfAbsent(boardPk);
        snapshotService.rebuildAccountSnapshotIfAbsent(memberPk);

        TicketSaleReserveResult retryResult = ticketSaleRequestStore.reserve(command);

        if (retryResult == TicketSaleReserveResult.KEY_MISSING) {
            // 재구성 후에도 키가 없으면 시스템 이상 — IllegalStateException으로 상위에 알림
            throw new IllegalStateException(
                    "스냅샷 재구성 후에도 Redis 키 없음. boardPk=" + boardPk + ", memberPk=" + memberPk);
        }
        return retryResult;
    }

    private void handleResult(TicketSaleReserveResult result) {
        if (result == TicketSaleReserveResult.SOLD_OUT) {
            throw new GlobalException(TicketBoardErrorCode.TICKET_SOLD_OUT);
        }
        if (result == TicketSaleReserveResult.INSUFFICIENT_FUNDS) {
            throw new GlobalException(MemberErrorCode.ACCOUNT_INSUFFICIENT_FUNDS);
        }
        // SUCCESS(1L) — 정상 처리
    }


}