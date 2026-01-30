package com.jinsu.ticketrace.ticket.board.controller;

import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.member.validator.MemberValidator;
import com.jinsu.ticketrace.ticket.board.domain.DTO.TicketArticleDTO;
import com.jinsu.ticketrace.ticket.board.service.TicketBoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("article/{id}")
    @Operation(
            summary = "게시글 조회",
            description = "게시글을 조회합니다",
            responses = {
                    @ApiResponse(responseCode = "200", description = "게시글 조회 성공"),
                    @ApiResponse(responseCode = "404", description = "없는 게시글 조회")
            }
    )
    public ResponseEntity<TicketArticleDTO.GetArticle> getArticle(@PathVariable(name = "id") long boardId){
        TicketArticleDTO.GetArticle article = ticketBoardService.getArticle(boardId);

        return ResponseEntity.ok(article);
    }


}
