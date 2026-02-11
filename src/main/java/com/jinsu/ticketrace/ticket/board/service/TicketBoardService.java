package com.jinsu.ticketrace.ticket.board.service;

import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.ticket.board.domain.DTO.TicketArticleDTO;
import com.jinsu.ticketrace.ticket.board.domain.entity.GATicketBoard;
import com.jinsu.ticketrace.ticket.board.repository.TicketBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TicketBoardService {

    private final TicketBoardRepository ticketBoardRepository;

    public TicketArticleDTO.CreateArticleResponse CreateArticle(Member member,
                                                                TicketArticleDTO.CreateArticleRequest articleRequest){
        GATicketBoard board = GATicketBoard.of(articleRequest, member);
        GATicketBoard savedBoard = ticketBoardRepository.save(board);
        return TicketArticleDTO.CreateArticleResponse.of(savedBoard);
    }

    @Transactional(readOnly = true)
    public TicketArticleDTO.GetArticle getArticle(GATicketBoard board){
        return TicketArticleDTO.GetArticle.of(board);
    }

    public void deleteBoard(GATicketBoard board) {
        ticketBoardRepository.delete(board);
    }

    public TicketArticleDTO.GetArticle modifyBoard(GATicketBoard board, TicketArticleDTO.ModifyArticleRequest article) {
        GATicketBoard modifiedBoard = board.modifyBoard(article);
        return TicketArticleDTO.GetArticle.of(modifiedBoard);
    }
}
