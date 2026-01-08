package com.jinsu.ticketrace.auth.service;

import com.jinsu.ticketrace.auth.jwt.JwtTokenProvider;
import com.jinsu.ticketrace.auth.repository.redis.RefreshTokenStore;
import com.jinsu.ticketrace.auth.repository.redis.AccessTokenBlacklistStore;
import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.member.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final AccessTokenBlacklistStore accessTokenBlacklistStore;

    @Transactional(readOnly = true)
    public TokenResponse signIn(Member member){
        String access = jwtTokenProvider.createAccessToken(member.getMemberPk());
        JwtTokenProvider.RefreshTokenBundle refresh = jwtTokenProvider.createRefreshToken(member.getMemberPk());
        refreshTokenStore.save(member.getMemberPk(), refresh.token, refresh.ttl);

        return new TokenResponse(access, refresh.token);
    }


    public void logout(long memberPk, String accessToken){
        refreshTokenStore.delete(memberPk);
        blacklistAccessToken(accessToken);

    }

    private void blacklistAccessToken(String accessToken) {
        if(accessToken == null || accessToken.isBlank()) return;

        try{
            Claims claims = jwtTokenProvider.parseAndValidate(accessToken);
            Instant expiration = claims.getExpiration().toInstant();
            Instant now = Instant.now();
            if(expiration.isAfter(now)){
                accessTokenBlacklistStore.blacklist(accessToken, Duration.between(now, expiration));
            }
        }catch (Exception ignored){

        }
    }

    public record TokenResponse(String accessToken, String refreshToken) {}
}
