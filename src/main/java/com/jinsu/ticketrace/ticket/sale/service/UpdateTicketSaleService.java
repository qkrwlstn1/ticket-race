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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.UUID;


@RequiredArgsConstructor
@Transactional
@Service
public class UpdateTicketSaleService implements TicketSaleUseCase{

    private final MemberRepository memberRepository;
    private final TicketBoardRepository ticketBoardRepository;
    private final TicketRepository ticketRepository;
    @Override
    public void ticketSale(long ticketBoardPk, long memberPk, long amount) {
        GATicketBoard ticketBoard = ticketBoardRepository.findById(ticketBoardPk)
                .orElseThrow(() -> new GlobalException(TicketBoardErrorCode.TICKET_BOARD_NOT_FOUND));
        int quantityUpdated = ticketBoardRepository.decreaseQuantity(ticketBoardPk, amount);

        if (quantityUpdated != 1) throw new GlobalException(TicketBoardErrorCode.TICKET_SOLD_OUT);

        Member member = memberRepository.findById(memberPk)
                .orElseThrow(() -> new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND));
        int accountUpdated = memberRepository.decreaseAccount(memberPk, ticketBoard.getPrice() * amount);
        if (accountUpdated != 1) throw new GlobalException(MemberErrorCode.ACCOUNT_INSUFFICIENT_FUNDS);

        Ticket ticket = Ticket.of(UUID.randomUUID().toString(), ticketBoard, member, amount, ticketBoard.getPrice() * amount);
        ticketRepository.save(ticket);
    }
}
