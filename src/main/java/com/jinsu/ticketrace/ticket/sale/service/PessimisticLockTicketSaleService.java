package com.jinsu.ticketrace.ticket.sale.service;

import com.jinsu.ticketrace.global.error.GlobalException;
import com.jinsu.ticketrace.global.exception.MemberErrorCode;
import com.jinsu.ticketrace.global.exception.TicketBoardErrorCode;
import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.member.repository.MemberRepository;
import com.jinsu.ticketrace.ticket.board.domain.entity.GATicketBoard;
import com.jinsu.ticketrace.ticket.board.repository.TicketBoardRepository;
import com.jinsu.ticketrace.ticket.sale.domain.entity.Ticket;
import com.jinsu.ticketrace.ticket.sale.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PessimisticLockTicketSaleService implements TicketSaleUseCase{

    private final TicketRepository ticketRepository;
    private final TicketBoardRepository ticketBoardRepository;
    private final MemberRepository memberRepository;


    @Override
    public void ticketSale(long ticketBoardPk, long memberPk, long amount) {
        Optional<Member> optionalMember = memberRepository.findByIdWithPessimisticLock(memberPk);
        Optional<GATicketBoard> optionalGATicketBoard = ticketBoardRepository.findByWithPessimisticLock(ticketBoardPk);

        Member member = optionalMember
                .orElseThrow(() -> new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND));
        GATicketBoard ticketBoard = optionalGATicketBoard
                .orElseThrow(() -> new GlobalException(TicketBoardErrorCode.TICKET_BOARD_NOT_FOUND));

        ticketBoard.sale();
        member.buy(ticketBoard.getPrice(), amount);
        
        Ticket ticket = Ticket.of(ticketBoard, member, amount);
        ticketRepository.save(ticket);
    }
}
