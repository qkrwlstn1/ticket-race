package com.jinsu.ticketrace.ticket.board.service.unit;

import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.ticket.board.domain.DTO.TicketArticleDTO;
import com.jinsu.ticketrace.ticket.board.domain.entity.TicketBoard;
import com.jinsu.ticketrace.ticket.board.repository.TicketBoardRepository;
import com.jinsu.ticketrace.ticket.board.service.TicketBoardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketBoardServiceTest {

    @Mock
    private TicketBoardRepository ticketBoardRepository;

    @InjectMocks
    private TicketBoardService ticketBoardService;

    @Test
    @DisplayName("게시글 생성 요청 시 저장 후 응답 DTO를 반환한다")
    void createRequest_CreateArticle_ReturnResponse() {
        String title = "title";
        String content = "content";
        LocalDateTime createDate = LocalDateTime.now();
        LocalDateTime deadline = LocalDateTime.now().plusDays(10);

        Member member = Member.builder()
                .memberPk(1L)
                .nickname("member")
                .build();
        TicketArticleDTO.CreateArticleRequest articleRequest = TicketArticleDTO.CreateArticleRequest.builder()
                .title(title)
                .content(content)
                .deadline(deadline)
                .build();
        
        TicketBoard savedBoard = TicketBoard.builder()
                .boardPk(3L)
                .title(title)
                .content(content)
                .member(member)
                .createDateTime(createDate)
                .deadlineDateTime(deadline)
                .build();

        when(ticketBoardRepository.save(any(TicketBoard.class))).thenReturn(savedBoard);
        TicketArticleDTO.CreateArticleResponse articleResponse = ticketBoardService.CreateArticle(member, articleRequest);

        assertEquals(title, articleResponse.getTitle());
        assertEquals(content, articleResponse.getContent());
        assertEquals(3L, articleResponse.getBoardPk());
        assertEquals(deadline, articleResponse.getDeadlineDate());
        assertEquals(createDate, articleResponse.getCreateDate());
        assertEquals("member", articleResponse.getMemberNickname());
        verify(ticketBoardRepository).save(any());
    }

    @Test
    @DisplayName("게시글 조회 요청 시 DTO를 반환한다")
    void board_GetArticle_ReturnDto() {
        Member member = Member.builder()
                .memberPk(2L)
                .nickname("nick")
                .build();
        TicketBoard board = TicketBoard.builder()
                .boardPk(1L)
                .title("title")
                .content("content")
                .deadlineDateTime(LocalDateTime.now().plusDays(30))
                .modifyDateTime(LocalDateTime.now().minusHours(3))
                .createDateTime(LocalDateTime.now().minusDays(10))
                .member(member)
                .build();

        TicketArticleDTO.GetArticle article = ticketBoardService.getArticle(board);

        assertEquals(board.getBoardPk(), article.getBoardPk());
        assertEquals(board.getTitle(), article.getTitle());
        assertEquals(board.getContent(), article.getContent());
        assertEquals(board.getDeadlineDateTime(), article.getDeadlineDate());
        assertEquals(board.getModifyDateTime(), article.getModifyDate());
        assertEquals(board.getCreateDateTime(), article.getCreateDate());
        assertEquals(board.getMember().getNickname(), article.getMemberNickname());

    }
}
