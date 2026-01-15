package com.jinsu.ticketrace.auth.service.unit;

import com.jinsu.ticketrace.auth.jwt.JwtTokenProvider;
import com.jinsu.ticketrace.auth.repository.redis.AccessTokenBlacklistStore;
import com.jinsu.ticketrace.auth.repository.redis.RefreshTokenStore;
import com.jinsu.ticketrace.auth.service.AuthService;
import com.jinsu.ticketrace.member.domain.entity.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private RefreshTokenStore refreshTokenStore;
    @Mock
    private AccessTokenBlacklistStore accessTokenBlacklistStore;

    @InjectMocks
    private AuthService authService;


    @Test
    @DisplayName("로그인 후 토큰을 반환하고 refresh token을 저장해야 한다")
    void signin_should_return_tokens_and_store_refresh_token(){
        Member member = Member.builder()
                .memberPk(1L)
                .build();
        JwtTokenProvider.RefreshTokenBundle refreshTokenBundle =
                new JwtTokenProvider.RefreshTokenBundle("refresh-token", Duration.ofDays(1));


        when(jwtTokenProvider.createRefreshToken(1L)).thenReturn(refreshTokenBundle);
        when(jwtTokenProvider.createAccessToken(1L)).thenReturn("access-token");

        AuthService.TokenResponse response = authService.signIn(member);
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        verify(refreshTokenStore).save(1L, "refresh-token", Duration.ofDays(1));


    }

    @Test
    @DisplayName("로그아웃은 만료될 때까지 액세스 토큰을 블랙리스트에 올려야 한다")
    void logout_should_blacklist_access_token_until_expiry(){
        Date expiration = Date.from(Instant.now().plusSeconds(300));

        Claims claims = Jwts.claims().expiration(expiration).build();

        when(jwtTokenProvider.parseAndValidate("access-token")).thenReturn(claims);

        authService.logout(1L, "access-token");

        verify(refreshTokenStore).delete(1L);
        ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(accessTokenBlacklistStore).blacklist(eq("access-token"), durationCaptor.capture());
        assertTrue(durationCaptor.getValue().compareTo(Duration.ZERO) > 0);
    }

}