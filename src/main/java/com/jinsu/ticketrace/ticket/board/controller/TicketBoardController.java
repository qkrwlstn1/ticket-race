package com.jinsu.ticketrace.ticket.board.controller;

import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.member.validator.MemberValidator;
import com.jinsu.ticketrace.ticket.board.domain.DTO.TicketArticleDTO;
import com.jinsu.ticketrace.ticket.board.domain.entity.GATicketBoard;
import com.jinsu.ticketrace.ticket.board.service.TicketBoardService;
import com.jinsu.ticketrace.ticket.board.validator.TicketBoardValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("ticket/board")
@RequiredArgsConstructor
public class TicketBoardController {

    private final MemberValidator memberValidator;
    private final TicketBoardService ticketBoardService;
    private final TicketBoardValidator ticketBoardValidator;
    @PostMapping("article")
    @Operation(
            summary = "게시글 작성",
            description = "티켓 관련 상품을 작성합니다",
            responses = {
                    @ApiResponse(responseCode = "201", description = "게시글 작성 성공")
            }
    )
    public ResponseEntity<TicketArticleDTO.CreateArticleResponse> createArticle(
            @Valid @RequestBody TicketArticleDTO.CreateArticleRequest articleRequest,
            Authentication authentication){
        Member member = memberValidator.memberCheck(authentication);
        TicketArticleDTO.CreateArticleResponse createArticleResponse = ticketBoardService.CreateArticle(member, articleRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(createArticleResponse);
    }

    @GetMapping("articles")
    @Operation(
            summary = "게시글 목록 조회",
            description = "게시글 목록을 페이지 단위로 조회합니다",
            responses = {
                    @ApiResponse(responseCode = "200", description = "목록 조회 성공")
            }
    )
    public ResponseEntity<Page<TicketArticleDTO.GetSummaryArticle>> getArticles(
            @PageableDefault(size = 20, sort = "createDateTime", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(ticketBoardService.getArticles(pageable));
    }

    @GetMapping("article/{boardId}")
    @Operation(
            summary = "게시글 조회",
            description = "게시글을 조회합니다",
            responses = {
                    @ApiResponse(responseCode = "200", description = "게시글 조회 성공"),
                    @ApiResponse(responseCode = "404", description = "없는 게시글 조회")
            }
    )
    public ResponseEntity<TicketArticleDTO.GetArticle> getArticle(@PathVariable long boardId){
        GATicketBoard board = ticketBoardValidator.boardCheck(boardId);
        TicketArticleDTO.GetArticle article = ticketBoardService.getArticle(board);

        return ResponseEntity.ok(article);
    }

    @DeleteMapping("article/{boardId}")
    @Operation(
            summary = "게시글 삭제",
            description = "게시글을 삭제합니다",
            responses = {
                    @ApiResponse(responseCode = "204", description = "삭제 성공"),
                    @ApiResponse(responseCode = "403", description = "게시글 소유권이 없음"),
                    @ApiResponse(responseCode = "404", description = "없는 게시글 조회")
            }
    )
    public ResponseEntity<?> deleteArticle(@PathVariable long boardId, Authentication authentication){
        GATicketBoard board = ticketBoardValidator.boardCheck(boardId);
        long memberPk = Long.parseLong(authentication.getName());
        ticketBoardValidator.ownerCheck(board, memberPk);

        ticketBoardService.deleteBoard(board);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("article")
    @Operation(
            summary = "게시글 수정",
            description = "게시글을 수정하고 수정된 내용을 반환",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공"),
                    @ApiResponse(responseCode = "403", description = "게시글 소유권이 없음"),
                    @ApiResponse(responseCode = "404", description = "없는 게시글 조회")
            }
    )
    public ResponseEntity<TicketArticleDTO.GetArticle> modifyBoard(@RequestBody @Valid TicketArticleDTO.ModifyArticleRequest article,
                                                                       Authentication authentication){
        GATicketBoard board = ticketBoardValidator.boardCheck(article.getBoardPk());
        long memberPk = Long.parseLong(authentication.getName());
        ticketBoardValidator.ownerCheck(board, memberPk);

        TicketArticleDTO.GetArticle result = ticketBoardService.modifyBoard(board, article);
        return ResponseEntity.ok(result);
    }

}
