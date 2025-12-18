package com.jinsu.ticketrace.global.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

public class JwtTokenProvider {
    @Value("${jwt.secret-key}")
    private String secretKeyPlain;// 설정 파일로부터 읽어올 비밀 키 (평문 상태)

    private final long TOKEN_VALIDITY = 1000L * 60 * 60 * 12;  // 액세스 토큰 유효 시간: 12시간
    private final long REFRESH_TOKEN_VALIDITY = 1000L * 60 * 60 * 24 * 7;  // 리프레시 토큰 유효 시간: 7일
    private SecretKey secretKey;  // 실제 서명 및 검증에 사용할 인코딩된 비밀 키

    @PostConstruct
    protected void init() {
        // Base64 인코딩된 secretKeyPlain을 디코딩하여 SecretKey 생성
        System.out.println("플레인 시크릿 키 (Base64 인코딩된 값): " + secretKeyPlain);
        byte[] decodedKey = Base64.getDecoder().decode(secretKeyPlain);
        secretKey = Keys.hmacShaKeyFor(decodedKey);  // Base64 디코딩된 바이트 배열로 SecretKey 생성
        System.out.println("시크릿 키 (디코딩 후 SecretKey 객체): " + secretKey);
    }

    // JWT 액세스 토큰 생성 메서드
    public String generateToken(Long userId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + TOKEN_VALIDITY);  // 유효 시간 설정

        return Jwts.builder().subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(validity)  // 만료 시간 설정
                .signWith(secretKey, SignatureAlgorithm.HS256)  // 서명에 SecretKey 사용
                .compact();  // 토큰 생성
    }
}
