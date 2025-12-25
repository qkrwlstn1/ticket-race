package com.jinsu.ticketrace.auth.service;

import com.jinsu.ticketrace.auth.jwt.JwtTokenProvider;
import com.jinsu.ticketrace.auth.repository.RefreshTokenStore;
import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;

    @Transactional(readOnly = true)
    public TokenResponse signIn(Member member){
        String access = jwtTokenProvider.createAccessToken(member.getMemberPk());
        JwtTokenProvider.RefreshTokenBundle refresh = jwtTokenProvider.createRefreshToken(member.getMemberPk());
        refreshTokenStore.save(member.getMemberPk(), refresh.token, refresh.ttl);

        return new TokenResponse(access, refresh.token);
    }

    public record TokenResponse(String accessToken, String refreshToken) {}
}
