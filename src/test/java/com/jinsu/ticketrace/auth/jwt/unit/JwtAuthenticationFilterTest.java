package com.jinsu.ticketrace.auth.jwt.unit;

import com.jinsu.ticketrace.auth.jwt.JwtAuthenticationFilter;
import com.jinsu.ticketrace.auth.jwt.JwtTokenProvider;
import com.jinsu.ticketrace.auth.repository.redis.AccessTokenBlacklistStore;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private FilterChain filterChain;

    @Mock
    private AccessTokenBlacklistStore accessTokenBlacklistStore;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private JwtAuthenticationFilter filter;


    @Test
    @DisplayName("Authorization 헤더가 없으면 체인을 통과하고 인증을 세팅하지 않는다")
    void no_auth_header_should_pass_through() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());

    }

    @Test
    @DisplayName("블랙리스트 토큰이면 401을 반환하고 체인을 중단")
    void blacklisted_token_should_return_401() throws ServletException, IOException {
        String accessToken = "blacklistedAccessToken";
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization","Bearer "+accessToken);

        when(accessTokenBlacklistStore.isBlacklisted(accessToken)).thenReturn(true);
        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(filterChain, never()).doFilter(any(), any());
        verify(accessTokenBlacklistStore).isBlacklisted(accessToken);
        verify(tokenProvider, never()).parseAndValidate(any());

    }

    @Test
    @DisplayName("유효하지 않은 토큰이면 401을 반환")
    void invalid_access_token_should_return_401() throws ServletException, IOException {
        String invalidAccessToken = "invalidAccessToken";

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization","Bearer " + invalidAccessToken);

        when(accessTokenBlacklistStore.isBlacklisted(invalidAccessToken)).thenReturn(false);
        when(tokenProvider.parseAndValidate(invalidAccessToken)).thenThrow(new JwtException("invalid"));

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("access 토큰이 아니면 401을 반환")
    void non_access_token_should_return_401() throws ServletException, IOException {
        String refreshToken = "refresh token";

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization","Bearer " + refreshToken);

        Claims claims = Jwts.claims()
                .subject("1")
                .add("typ", "refresh")
                .build();

        when(accessTokenBlacklistStore.isBlacklisted(refreshToken)).thenReturn(false);
        when(tokenProvider.parseAndValidate(refreshToken)).thenReturn(claims);
        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertNull(SecurityContextHolder.getContext().getAuthentication());

    }
    @Test
    @DisplayName("유효한 access 토큰이면 SecurityContext에 인증을 세팅한다")
    void valid_access_token_should_set_authentication() throws ServletException, IOException {
        String accessToken = "access token";

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization","Bearer " + accessToken);

        Claims claims = Jwts.claims()
                .subject("96")
                .add("typ", "access")
                .build();

        when(accessTokenBlacklistStore.isBlacklisted(accessToken)).thenReturn(false);
        when(tokenProvider.parseAndValidate(accessToken)).thenReturn(claims);
        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("96", authentication.getPrincipal());
        assertEquals(accessToken, authentication.getCredentials());
        assertTrue(authentication.getAuthorities().isEmpty());


    }
}