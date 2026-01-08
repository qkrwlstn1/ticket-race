package com.jinsu.ticketrace.auth.jwt;

import com.jinsu.ticketrace.auth.repository.redis.AccessTokenBlacklistStore;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final AccessTokenBlacklistStore accessTokenBlacklistStore;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String auth = request.getHeader("Authorization");

        if(auth == null || !auth.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }
        String token = auth.substring("Bearer ".length());
        log.info("access token = {}",token);
        try {
            if(accessTokenBlacklistStore.isBlacklisted(token)) throw new JwtException("blacklisted access token");

            Claims claims = tokenProvider.parseAndValidate(token);

            String typ = claims.get("typ", String.class);
            if (!"access".equals(typ)) throw new JwtException("not access token");
            // subject = memberPk
            String memberPk = claims.getSubject();
            log.info("memberPk = {}", memberPk);
            log.info(request.getRequestURI());
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(memberPk, token, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        }catch (JwtException e) {
            SecurityContextHolder.clearContext();
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write("{\"code\":\"JWT_INVALID\",\"message\":\"invalid or expired access token\"}");
        }

    }
}
