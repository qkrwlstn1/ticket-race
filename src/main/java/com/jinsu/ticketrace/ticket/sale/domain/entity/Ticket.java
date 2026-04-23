package com.jinsu.ticketrace.ticket.sale.domain.entity;

import com.jinsu.ticketrace.global.error.GlobalException;
import com.jinsu.ticketrace.global.exception.TicketErrorCode;
import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.ticket.board.domain.entity.GATicketBoard;
import com.jinsu.ticketrace.ticket.sale.domain.entity.status.RefundStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EntityListeners(AuditingEntityListener.class)
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_pk")
    private long ticketPk;

    @CreatedDate
    @Column(name = "sale_date_time")
    private LocalDateTime saleDateTime;

    @Column(name = "buy_request_id", nullable = false, unique = true, updatable = false)
    private String buyRequestId;

    @Column(name = "amount")
    private long amount;

    @Column(name = "refunded_price")// 구매/환불 금액
    private long refundedPrice;

    @Column(name = "refund_status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RefundStatus refundStatus = RefundStatus.NONE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ga_ticket_board_pk")
    private GATicketBoard GATicketBoard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_pk")
    private Member member;


    public static Ticket of(String purchaseRequestId, GATicketBoard ticketBoard, Member member, long amount, long refundedPrice){
        return Ticket.builder()
                .buyRequestId(purchaseRequestId)
                .amount(amount)
                .refundedPrice(refundedPrice)
                .GATicketBoard(ticketBoard)
                .member(member)
                .build();
    }
    // ── 환불 도메인 메서드 ──────────────────────────────────────────────────────

    /**
     * 환불 상태를 PROCESSED로 변경합니다.
     *
     * 도메인 규칙:
     * - 이미 환불된 티켓은 중복 환불 불가
     * - 소유권 검사는 서비스 레이어에서 수행
     *
     * @throws GlobalException ALREADY_REFUNDED — 이미 환불된 경우
     */
    public void refund() {
        if (this.refundStatus != RefundStatus.NONE) {
            throw new GlobalException(TicketErrorCode.ALREADY_REFUNDED);
        }
        this.refundStatus = RefundStatus.PROCESSED;
    }

    /**
     * 환불이 가능한 상태인지 확인합니다.
     * refund()를 호출하기 전 사전 검사에 활용할 수 있습니다.
     */
    public boolean isRefundable() {
        return this.refundStatus == RefundStatus.NONE;
    }

}
