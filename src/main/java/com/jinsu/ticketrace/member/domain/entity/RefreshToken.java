package com.jinsu.ticketrace.member.domain.entity;

import jakarta.persistence.*;
import lombok.Builder;

@Entity
@Table(name = "refresh_token")
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_token_pk")
    private long refreshTokenPk;

    @OneToOne
    @JoinColumn(name = "member_pk")
    private Member member;

    @Column(name = "token_hash")
    private String tokenHash;
}
