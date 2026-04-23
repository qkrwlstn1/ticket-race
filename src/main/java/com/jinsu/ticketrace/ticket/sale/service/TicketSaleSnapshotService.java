package com.jinsu.ticketrace.ticket.sale.service;

import com.jinsu.ticketrace.global.error.GlobalException;
import com.jinsu.ticketrace.global.exception.MemberErrorCode;
import com.jinsu.ticketrace.global.exception.TicketBoardErrorCode;
import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.member.repository.MemberRepository;
import com.jinsu.ticketrace.ticket.board.domain.entity.GATicketBoard;
import com.jinsu.ticketrace.ticket.board.repository.TicketBoardRepository;
import com.jinsu.ticketrace.ticket.sale.repository.redis.TicketSaleRedisKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketSaleSnapshotService {

    private final StringRedisTemplate redisTemplate;
    private final TicketBoardRepository ticketBoardRepository;
    private final MemberRepository memberRepository;

    // ── 재고 스냅샷 ────────────────────────────────────────────────────────────

    /**
     * 게시글 등록 시 재고를 Redis에 세팁니다.
     * 키가 이미 존재하면 덮어씁니다 (새 게시글이므로 항상 DB 값이 정답).
     */
    public void initInventorySnapshot(long boardPk) {
        GATicketBoard board = ticketBoardRepository.findById(boardPk)
                .orElseThrow(() -> new GlobalException(TicketBoardErrorCode.TICKET_BOARD_NOT_FOUND));

        String key = TicketSaleRedisKeys.inventoryKey(boardPk);
        redisTemplate.opsForValue().set(key, String.valueOf(board.getQuantity()));
        log.info("[Snapshot] 재고 초기화 boardPk={}, quantity={}", boardPk, board.getQuantity());
    }

    /**
     * KEY_MISSING 복구용 재고 재구성.
     * SETNX를 사용해 이미 다른 스레드가 세운 키를 덮어쓰지 않습니다.
     *
     * @return 실제로 키를 세웠으면 true, 이미 존재했으면 false
     */
    public boolean rebuildInventorySnapshotIfAbsent(long boardPk) {
        GATicketBoard board = ticketBoardRepository.findById(boardPk)
                .orElseThrow(() -> new GlobalException(TicketBoardErrorCode.TICKET_BOARD_NOT_FOUND));

        String key = TicketSaleRedisKeys.inventoryKey(boardPk);
        Boolean created = redisTemplate.opsForValue()
                .setIfAbsent(key, String.valueOf(board.getQuantity()));

        boolean wasSet = Boolean.TRUE.equals(created);
        if (wasSet) {
            log.info("[Snapshot] 재고 재구성 boardPk={}, quantity={}", boardPk, board.getQuantity());
        }
        return wasSet;
    }

    // ── 잔액 스냅샷 ────────────────────────────────────────────────────────────

    /**
     * KEY_MISSING 복구용 잔액 재구성.
     * SETNX 사용 — 이미 키가 있으면 무시합니다.
     */
    public boolean rebuildAccountSnapshotIfAbsent(long memberPk) {
        Member member = memberRepository.findById(memberPk)
                .orElseThrow(() -> new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND));

        String key = TicketSaleRedisKeys.accountKey(memberPk);
        Boolean created = redisTemplate.opsForValue()
                .setIfAbsent(key, String.valueOf(member.getAccount()));

        boolean wasSet = Boolean.TRUE.equals(created);
        if (wasSet) {
            log.info("[Snapshot] 잔액 재구성 memberPk={}, account={}", memberPk, member.getAccount());
        }
        return wasSet;
    }

    /**
     * 환불 발생 시 Redis 잔액 키가 존재하면 환불액만큼 증가시킵니다.
     * 키가 없으면 건드리지 않습니다 — 다음 구매 시 KEY_MISSING 경로로 DB에서 재구성됩니다.
     */
    public void incrementAccountIfPresent(long memberPk, long amount) {
        String key = TicketSaleRedisKeys.accountKey(memberPk);
        // hasKey + increment 는 원자적이지 않지만, 환불은 저빈도 작업이므로 허용 범위입니다.
        // 높은 정합성이 필요하다면 Lua 스크립트로 교체하세요.
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.opsForValue().increment(key, amount);
            log.info("[Snapshot] 환불 잔액 증가 memberPk={}, +amount={}", memberPk, amount);
        }
    }

    /**
     * 환불 발생 시 Redis 재고 키가 존재하면 수량만큼 증가시킵니다.
     */
    public void incrementInventoryIfPresent(long boardPk, long amount) {
        String key = TicketSaleRedisKeys.inventoryKey(boardPk);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.opsForValue().increment(key, amount);
            log.info("[Snapshot] 환불 재고 증가 boardPk={}, +amount={}", boardPk, amount);
        }
    }
}
