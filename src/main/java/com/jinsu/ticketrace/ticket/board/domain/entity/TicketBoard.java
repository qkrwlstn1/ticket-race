package com.jinsu.ticketrace.ticket.board.domain.entity;

import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.ticket.board.domain.DTO.TicketArticleDTO;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_board")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EntityListeners(AuditingEntityListener.class)
public class TicketBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_board_pk")
    private long boardPk;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", length = 5000, nullable = false)
    private String content;

    @CreatedDate
    @Column(name = "create_date_time", nullable = false, updatable = false)
    private LocalDateTime createDateTime;

    @LastModifiedDate
    @Column(name = "modify_Date_Time")
    private LocalDateTime modifyDateTime;

    @Column(name = "deadlineDateTime", nullable = false)
    private LocalDateTime deadlineDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_pk")
    private Member member;


    public static TicketBoard of(TicketArticleDTO.CreateArticleRequest articleRequest, Member member){

        return TicketBoard.builder()
                .title(articleRequest.getTitle())
                .content(articleRequest.getContent())
                .deadlineDateTime(articleRequest.getDeadline())
                .member(member)
                .build();
    }

    public TicketBoard modifyBoard(TicketArticleDTO.ModifyArticleRequest article){
        this.title = article.getTitle();
        this.content = article.getContent();
        this.deadlineDateTime = article.getDeadlineDateTime();

        return this;
    }


}
