package com.jinsu.ticketrace.ticket.sale.controller;

import com.jinsu.ticketrace.ticket.sale.service.RefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 티켓 환불 컨트롤러.
 *
 * POST /ticket/refund/{ticketPk}
 * → 본인 티켓만 환불 가능, 중복 환불 불가
 */
@RestController
@RequestMapping("ticket/refund")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    @PostMapping("{ticketPk}")
    @Operation(
            summary = "티켓 환불",
            description = "구매한 티켓을 환불합니다. 이미 환불된 티켓은 재환불 불가.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "환불 성공"),
                    @ApiResponse(responseCode = "403", description = "본인 티켓이 아님"),
                    @ApiResponse(responseCode = "404", description = "티켓 없음"),
                    @ApiResponse(responseCode = "409", description = "이미 환불된 티켓")
            }
    )
    public ResponseEntity<Void> refund(@PathVariable long ticketPk,
                                       Authentication authentication) {
        long memberPk = Long.parseLong(authentication.getName());
        refundService.refund(ticketPk, memberPk);
        return ResponseEntity.noContent().build();
    }
}