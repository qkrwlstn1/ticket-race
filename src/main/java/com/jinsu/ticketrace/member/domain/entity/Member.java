package com.jinsu.ticketrace.member.domain.entity;

import com.jinsu.ticketrace.auth.domain.entity.RefreshToken;
import com.jinsu.ticketrace.member.domain.DTO.SignUpDTO;
import com.jinsu.ticketrace.ticket.board.domain.entity.GATicketBoard;
import com.jinsu.ticketrace.ticket.sale.domain.entity.Ticket;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.util.List;

@Entity
@Getter
@Table(name = "member")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_pk", nullable = false)
    private long memberPk;

    @Column(name = "member_id", unique = true)
    private String memberId;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password_hash")
    private String password;

    @Column(name = "nickname", unique = true)
    private String nickname;

    @Column(name = "account")
    private long account;



    @OneToOne(mappedBy = "member", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private RefreshToken refreshToken;

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<GATicketBoard> GATicketBoard;

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<Ticket> tickets;

    public static Member of(
            SignUpDTO.SignUpRequest signUpRequest
            , String encodedPassword
    ){
        return Member.builder()
                .memberId(signUpRequest.getId())
                .password(encodedPassword)
                .email(signUpRequest.getEmail())
                .nickname(signUpRequest.getNickname())
                .build();
    }
    public void modifyNickname(String nickname){
        this.nickname = nickname;
    }
}
