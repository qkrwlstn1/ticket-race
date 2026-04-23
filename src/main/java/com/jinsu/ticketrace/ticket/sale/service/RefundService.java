package com.jinsu.ticketrace.ticket.sale.service;

import com.jinsu.ticketrace.global.error.GlobalException;
import com.jinsu.ticketrace.global.exception.TicketErrorCode;
import com.jinsu.ticketrace.member.repository.MemberRepository;
import com.jinsu.ticketrace.ticket.board.repository.TicketBoardRepository;
import com.jinsu.ticketrace.ticket.sale.domain.entity.Ticket;
import com.jinsu.ticketrace.ticket.sale.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 티켓 환불 서비스.
 *
 * 환불 처리 순서:
 *   티켓 조회 및 소유권 검증
 *   중복 환불 방지 (Ticket.refund()에서 검사)
 *   DB 재고 / 잔액 복구 (@Modifying UPDATE)
 *   티켓 상태 PROCESSED로 변경
 *   Redis 재고 / 잔액 조건부 증가 (키가 있을 때만)
 *
 * DB → Redis 순서인 이유:
 * DB 트랜잭션이 커밋된 뒤 Redis를 갱신합니다.
 * 만약 Redis 갱신 중 예외가 나면 DB는 이미 복구된 상태이므로,
 * 다음 구매 요청에서 KEY_MISSING 경로로 DB를 읽어 정합성이 맞춰집니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefundService {

    private final TicketRepository ticketRepository;
    private final TicketBoardRepository ticketBoardRepository;
    private final MemberRepository memberRepository;
    private final TicketSaleSnapshotService snapshotService;

    /**
     * 티켓 환불을 처리합니다.
     *
     * @param ticketPk  환불할 티켓 PK
     * @param memberPk  요청자 회원 PK (소유권 검증에 사용)
     */
    @Transactional
    public void refund(long ticketPk, long memberPk) {
        // 1) 티켓 조회
        Ticket ticket = ticketRepository.findById(ticketPk)
                .orElseThrow(() -> new GlobalException(TicketErrorCode.TICKET_NOT_FOUND));

        // 2) 소유권 검증
        if (ticket.getMember().getMemberPk() != memberPk) {
            throw new GlobalException(TicketErrorCode.TICKET_NOT_OWNER);
        }

        // 3) 중복 환불 방지 — 도메인 메서드 내에서 RefundStatus 검사
        ticket.refund();

        long boardPk = ticket.getGATicketBoard().getBoardPk();
        long refundAmount = ticket.getRefundedPrice(); // 실제 지불 금액
        long quantity = ticket.getAmount();

        // 4) DB 재고 복구
        int boardUpdated = ticketBoardRepository.increaseQuantity(boardPk, quantity);
        if (boardUpdated != 1) {
            log.warn("[Refund] 재고 복구 실패. boardPk={}", boardPk);
            throw new IllegalStateException("재고 복구 실패. boardPk=" + boardPk);
        }

        // 5) DB 잔액 복구
        int memberUpdated = memberRepository.increaseAccount(memberPk, refundAmount);
        if (memberUpdated != 1) {
            log.warn("[Refund] 잔액 복구 실패. memberPk={}", memberPk);
            throw new IllegalStateException("잔액 복구 실패. memberPk=" + memberPk);
        }

        log.info("[Refund] DB 복구 완료. ticketPk={}, boardPk={}, memberPk={}, refundAmount={}",
                ticketPk, boardPk, memberPk, refundAmount);

        // 6) Redis 조건부 갱신 (트랜잭션 외부에서 실행되어도 무방)
        //    키가 없으면 건드리지 않음 — 다음 구매 시 KEY_MISSING으로 DB에서 재구성
        syncRedisAfterRefund(boardPk, memberPk, quantity, refundAmount);
    }

    /**
     * DB 커밋 후 Redis를 조건부로 갱신합니다.
     * Redis 갱신 실패는 데이터 유실로 이어지지 않으므로 예외를 전파하지 않습니다.
     */
    private void syncRedisAfterRefund(long boardPk, long memberPk, long quantity, long refundAmount) {
        try {
            snapshotService.incrementInventoryIfPresent(boardPk, quantity);
            snapshotService.incrementAccountIfPresent(memberPk, refundAmount);
        } catch (Exception e) {
            // Redis 갱신 실패 → 로그만 남기고 환불 자체는 성공 처리
            // 다음 구매 시 KEY_MISSING 경로로 DB에서 자동 복구됩니다.
            log.error("[Refund] Redis 갱신 실패 (DB는 정상). boardPk={}, memberPk={}", boardPk, memberPk, e);
        }
    }
}