package com.jinsu.ticketrace.member.domain.entity;

import com.jinsu.ticketrace.auth.domain.entity.RefreshToken;
import com.jinsu.ticketrace.member.domain.DTO.SignUpDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "member")
@Builder
@NoArgsConstructor
@AllArgsConstructor
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


    @OneToOne(mappedBy = "member", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private RefreshToken refreshToken;

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
}
