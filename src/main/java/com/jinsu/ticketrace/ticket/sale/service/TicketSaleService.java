package com.jinsu.ticketrace.ticket.sale.service;

import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.ticket.board.domain.entity.GATicketBoard;
import com.jinsu.ticketrace.ticket.sale.domain.entity.Ticket;
import com.jinsu.ticketrace.ticket.sale.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketSaleService {

    private final TicketRepository ticketRepository;

    public void ticketSale(GATicketBoard ticketBoard, Member member) {
        Ticket ticket = Ticket.of(ticketBoard,member);
        ticketRepository.save(ticket);
    }
}
