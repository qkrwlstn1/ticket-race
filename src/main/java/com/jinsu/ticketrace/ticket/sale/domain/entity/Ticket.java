package com.jinsu.ticketrace.ticket.sale.domain.entity;

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

    @Column(name = "amount")
    private long amount;

    @Column(name = "refunded_price")// 구매/환불 금액
    private long refundedPrice;

    @Column(name = "refund_status")
    @Builder.Default
    private RefundStatus refundStatus = RefundStatus.NONE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ga_ticket_board_pk")
    private GATicketBoard GATicketBoard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_pk")
    private Member member;


    public static Ticket of(GATicketBoard ticketBoard, Member member, long amount){
        return Ticket.builder()
                .amount(amount)
                .refundedPrice(ticketBoard.getPrice())
                .GATicketBoard(ticketBoard)
                .member(member)
                .build();
    }

}
