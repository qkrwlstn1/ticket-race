package com.jinsu.ticketrace.ticket.board.service;

import com.jinsu.ticketrace.global.error.GlobalException;
import com.jinsu.ticketrace.global.exception.TicketBoardErrorCode;
import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.ticket.board.domain.DTO.TicketArticleDTO;
import com.jinsu.ticketrace.ticket.board.domain.entity.TicketBoard;
import com.jinsu.ticketrace.ticket.board.repository.TicketBoardRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class TicketBoardService {

    private final TicketBoardRepository ticketBoardRepository;

    public TicketArticleDTO.CreateArticleResponse CreateArticle(Member member,
                                                                TicketArticleDTO.CreateArticleRequest articleRequest){
        TicketBoard board = TicketBoard.of(articleRequest, member);
        TicketBoard savedBoard = ticketBoardRepository.save(board);
        return TicketArticleDTO.CreateArticleResponse.of(savedBoard);
    }

    @Transactional(readOnly = true)
    public TicketArticleDTO.GetArticle getArticle(TicketBoard board){
        return TicketArticleDTO.GetArticle.of(board);
    }

    public void deleteBoard(TicketBoard board) {
        ticketBoardRepository.delete(board);
    }

    public TicketArticleDTO.GetArticle modifyBoard(TicketBoard board, TicketArticleDTO.ModifyArticleRequest article) {
        TicketBoard modifiedBoard = board.modifyBoard(article);
        return TicketArticleDTO.GetArticle.of(modifiedBoard);
    }
}
