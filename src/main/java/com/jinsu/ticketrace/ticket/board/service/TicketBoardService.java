package com.jinsu.ticketrace.ticket.board.service;

import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.ticket.board.domain.DTO.TicketArticleDTO;
import com.jinsu.ticketrace.ticket.board.domain.entity.GATicketBoard;
import com.jinsu.ticketrace.ticket.board.repository.TicketBoardRepository;
import com.jinsu.ticketrace.ticket.sale.service.TicketSaleSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TicketBoardService {

    private final TicketBoardRepository ticketBoardRepository;
    private final TicketSaleSnapshotService snapshotService;

    /**
     * 게시글 등록 + Redis 재고 스냅샷 초기화.
     *
     * 게시글이 저장된 직후 Redis 재고 키를 세웁니다.
     * 이렇게 하면 첫 구매 요청이 들어왔을 때 KEY_MISSING 없이 바로 reserve가 성공합니다.
     * (트랜잭션 커밋 후 Redis에 쓰므로, 극히 짧은 시간에 KEY_MISSING이 발생할 수는 있습니다.
     *  완벽한 원자성이 필요하다면 TransactionalEventListener를 사용하세요.)
     */
    public TicketArticleDTO.CreateArticleResponse CreateArticle(Member member,
                                                                TicketArticleDTO.CreateArticleRequest articleRequest) {
        GATicketBoard board = GATicketBoard.of(articleRequest, member);
        GATicketBoard savedBoard = ticketBoardRepository.save(board);

        // 게시글 등록 즉시 Redis 재고 키 초기화
        snapshotService.initInventorySnapshot(savedBoard.getBoardPk());

        return TicketArticleDTO.CreateArticleResponse.of(savedBoard);
    }

    @Transactional(readOnly = true)
    public TicketArticleDTO.GetArticle getArticle(GATicketBoard board) {
        return TicketArticleDTO.GetArticle.of(board);
    }

    /**
     * 게시글 목록 조회 (페이지네이션).
     *
     * N+1 방지를 위해 member를 JOIN FETCH 합니다.
     * JPA 페이지네이션 + fetch join 시 count 쿼리가 별도로 나가는 점에 주의하세요.
     * 필요하면 countQuery를 별도로 정의할 수 있습니다.
     */
    @Transactional(readOnly = true)
    public Page<TicketArticleDTO.GetSummaryArticle> getArticles(Pageable pageable) {
        return ticketBoardRepository.findAllWithMember(pageable)
                .map(TicketArticleDTO.GetSummaryArticle::of);
    }

    public void deleteBoard(GATicketBoard board) {
        ticketBoardRepository.delete(board);
    }

    public TicketArticleDTO.GetArticle modifyBoard(GATicketBoard board, TicketArticleDTO.ModifyArticleRequest article) {
        GATicketBoard modifiedBoard = board.modifyBoard(article);
        return TicketArticleDTO.GetArticle.of(modifiedBoard);
    }
}
