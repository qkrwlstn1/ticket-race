package com.jinsu.ticketrace.ticket.board.domain.entity;

import com.jinsu.ticketrace.global.error.GlobalException;
import com.jinsu.ticketrace.global.exception.TicketBoardErrorCode;
import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.ticket.board.domain.DTO.TicketArticleDTO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "general_admission_ticket_board")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EntityListeners(AuditingEntityListener.class)
@Check(constraints = "quantity >= 0")
public class GATicketBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ga_ticket_board_pk")
    private long boardPk;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", length = 5000, nullable = false)
    private String content;

    @Column(name = "quantity", nullable = false)
    private long quantity;

    @Column(name = "price")
    private long price;

    @CreatedDate
    @Column(name = "create_date_time", nullable = false, updatable = false)
    private LocalDateTime createDateTime;

    @LastModifiedDate
    @Column(name = "modify_Date_Time")
    private LocalDateTime modifyDateTime;

    @Column(name = "deadline_Date_Time", nullable = false)
    private LocalDateTime deadlineDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_pk")
    private Member member;


    public static GATicketBoard of(TicketArticleDTO.CreateArticleRequest articleRequest, Member member){

        return GATicketBoard.builder()
                .title(articleRequest.getTitle())
                .content(articleRequest.getContent())
                .deadlineDateTime(articleRequest.getDeadline())
                .member(member)
                .price(articleRequest.getPrice())
                .quantity(articleRequest.getQuantity())
                .build();
    }

    public GATicketBoard modifyBoard(TicketArticleDTO.ModifyArticleRequest article){
        this.title = article.getTitle();
        this.content = article.getContent();
        this.quantity = article.getQuantity();
        this.price = article.getPrice();
        this.deadlineDateTime = article.getDeadlineDateTime();

        return this;
    }

    public void sale(){
        if(quantity <= 0){
            throw new GlobalException(TicketBoardErrorCode.TICKET_SOLD_OUT);
        }
        quantity -= 1;
    }

}
