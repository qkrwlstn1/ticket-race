package com.jinsu.ticketrace.ticket.board.validator;

import com.jinsu.ticketrace.global.error.GlobalException;
import com.jinsu.ticketrace.global.exception.TicketBoardErrorCode;
import com.jinsu.ticketrace.ticket.board.domain.entity.GATicketBoard;
import com.jinsu.ticketrace.ticket.board.repository.TicketBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TicketBoardValidator {

    private final TicketBoardRepository ticketBoardRepository;

    public GATicketBoard boardCheck(long boardPk){
        Optional<GATicketBoard> optionalTicketBoard = ticketBoardRepository.findById(boardPk);
        return optionalTicketBoard.orElseThrow(() -> new GlobalException(TicketBoardErrorCode.TICKET_BOARD_NOT_FOUND));
    }

    public void ownerCheck(GATicketBoard board, long memberPk){
        if(board.getMember().getMemberPk() != memberPk)
            throw new GlobalException(TicketBoardErrorCode.TICKET_BOARD_NOT_OWNER);
    }

}
