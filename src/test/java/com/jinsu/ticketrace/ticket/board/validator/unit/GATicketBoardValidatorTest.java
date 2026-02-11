package com.jinsu.ticketrace.ticket.board.validator.unit;

import com.jinsu.ticketrace.global.error.GlobalException;
import com.jinsu.ticketrace.global.exception.TicketBoardErrorCode;
import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.ticket.board.domain.entity.GATicketBoard;
import com.jinsu.ticketrace.ticket.board.repository.TicketBoardRepository;
import com.jinsu.ticketrace.ticket.board.validator.TicketBoardValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GATicketBoardValidatorTest {
    @Mock
    private TicketBoardRepository ticketBoardRepository;

    @InjectMocks
    private TicketBoardValidator ticketBoardValidator;

    @Test
    @DisplayName("저장되지 않은 TicketBoard를 요청하면 TICKET_BOARD_NOT_FOUND를 반환한다")
    void missingBoard_Check_Throw() {
        long boardPk = 105L;

        when(ticketBoardRepository.findById(boardPk)).thenReturn(Optional.empty());
        GlobalException e = assertThrows(GlobalException.class, () -> ticketBoardValidator.boardCheck(boardPk));

        assertEquals(TicketBoardErrorCode.TICKET_BOARD_NOT_FOUND, e.getErrorCode());

    }

    @Test
    @DisplayName("저장된 ticketBoardPk를 요청하면 ticketBoard를 반환한다")
    void existingBoard_Check_ReturnBoard() {
        long boardPk = 19L;
        GATicketBoard board = GATicketBoard.builder()
                .boardPk(boardPk)
                .build();
        when(ticketBoardRepository.findById(boardPk)).thenReturn(Optional.of(board));

        GATicketBoard result = ticketBoardValidator.boardCheck(boardPk);
        assertEquals(board, result);

    }

    @Test
    @DisplayName("게시글의 memberPk와 입력받은 memberPk가 다르면 TICKET_BOARD_NOT_OWNER를 반환한다")
    void nonOwner_Check_Throw() {
        long memberPk = 123L;
        long inputMemberPk = 321L;
        Member member = Member.builder()
                .memberPk(memberPk)
                .build();
        GATicketBoard board = GATicketBoard.builder()
                .member(member)
                .build();

        GlobalException e = assertThrows(GlobalException.class, () -> ticketBoardValidator.ownerCheck(board, inputMemberPk));
        assertEquals(TicketBoardErrorCode.TICKET_BOARD_NOT_OWNER, e.getErrorCode());

    }
}
