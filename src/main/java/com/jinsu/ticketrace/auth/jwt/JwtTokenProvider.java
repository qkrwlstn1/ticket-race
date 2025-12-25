package com.jinsu.ticketrace.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {
    private final String issuer;        //발급자
    private final SecretKey secretKey;  // 실제 서명 및 검증에 사용할 인코딩된 비밀 키
    private final Duration accessTtl;   //Access 만료
    private final Duration refreshTtl;  //refresh 만료

    public String createAccessToken(long memberPk){
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expiration = Date.from(now.plus(accessTtl));
        String token =Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(memberPk))
                .issuedAt(issuedAt)
                .expiration(expiration)
                .claim("typ","access")
                .signWith(secretKey)
                .compact();
        log.info("로그인 access token : {}", token);

        return  token;
    }

    public RefreshTokenBundle createRefreshToken(long memberPk){
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expiration = Date.from(now.plus(refreshTtl));
        String token = Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(memberPk))
                .issuedAt(issuedAt)
                .expiration(expiration)
                .claim("typ", "refresh")
                .signWith(secretKey)
                .compact();
        log.info("로그인 refresh token : {}", token);
        return new RefreshTokenBundle(token, refreshTtl);
    }

    public Claims parseAndValidate(String jwt){
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();

    }

    public static final class RefreshTokenBundle {
        public final String token;
        public final Duration ttl;

        public RefreshTokenBundle(String token, Duration ttl) {
            this.token = token;
            this.ttl = ttl;
        }
    }


}
