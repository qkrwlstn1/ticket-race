package com.jinsu.ticketrace.auth.domain.entity;

import com.jinsu.ticketrace.member.domain.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_token")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_token_pk")
    private long refreshTokenPk;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_pk",nullable = false, unique = true)
    private Member member;

    @Column(name = "token_hash")
    private String tokenHash;

    public void changeTokenHash(String tokenHash){
        this.tokenHash = tokenHash;
    }
}
