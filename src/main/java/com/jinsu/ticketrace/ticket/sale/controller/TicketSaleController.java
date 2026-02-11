package com.jinsu.ticketrace.ticket.sale.controller;

import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.member.validator.MemberValidator;
import com.jinsu.ticketrace.ticket.board.domain.entity.GATicketBoard;
import com.jinsu.ticketrace.ticket.board.validator.TicketBoardValidator;
import com.jinsu.ticketrace.ticket.sale.domain.DTO.TicketSaleDTO;
import com.jinsu.ticketrace.ticket.sale.service.TicketSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("ticket/sale")
public class TicketSaleController {

    private final MemberValidator memberValidator;
    private final TicketBoardValidator ticketBoardValidator;
    private final TicketSaleService ticketSaleService;

    @PostMapping()
    public ResponseEntity<?> ticketSale(@RequestBody TicketSaleDTO.RequestSale requestSale, Authentication authentication){

        Member member = memberValidator.memberCheck(authentication);
        GATicketBoard ticketBoard = ticketBoardValidator.boardCheck(requestSale.getBoardPk());
        ticketSaleService.ticketSale(ticketBoard, member);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


}
