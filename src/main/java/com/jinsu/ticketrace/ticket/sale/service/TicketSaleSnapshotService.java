package com.jinsu.ticketrace.ticket.sale.service;

import com.jinsu.ticketrace.global.error.GlobalException;
import com.jinsu.ticketrace.global.exception.MemberErrorCode;
import com.jinsu.ticketrace.global.exception.TicketBoardErrorCode;
import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.member.repository.MemberRepository;
import com.jinsu.ticketrace.ticket.board.domain.entity.GATicketBoard;
import com.jinsu.ticketrace.ticket.board.repository.TicketBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketSaleSnapshotService {

    private final StringRedisTemplate redisTemplate;
    private final TicketBoardRepository ticketBoardRepository;
    private final MemberRepository memberRepository;

    /**
     * 티켓 판매 시작 전 Redis 스냅샷 초기화
     * (게시글 등록 시점 or 관리자 API로 호출)
     */
    public void initInventorySnapshot(long boardPk) {
        GATicketBoard board = ticketBoardRepository.findById(boardPk)
                .orElseThrow(() -> new GlobalException(TicketBoardErrorCode.TICKET_BOARD_NOT_FOUND));
        String key = "ticket:inventory:ga:" + boardPk + ":quantity";
        redisTemplate.opsForValue().set(key, String.valueOf(board.getQuantity()));
    }

    public void initAccountSnapshot(long memberPk) {
        Member member = memberRepository.findById(memberPk)
                .orElseThrow(() -> new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND));
        String key = "ticket:member:" + memberPk + ":account";
        redisTemplate.opsForValue().set(key, String.valueOf(member.getAccount()));
    }
}