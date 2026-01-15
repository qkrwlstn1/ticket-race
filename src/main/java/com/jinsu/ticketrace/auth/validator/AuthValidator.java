package com.jinsu.ticketrace.auth.validator;

import com.jinsu.ticketrace.auth.jwt.JwtTokenProvider;
import com.jinsu.ticketrace.auth.repository.redis.RefreshTokenStore;
import com.jinsu.ticketrace.global.error.GlobalException;
import com.jinsu.ticketrace.global.exception.AuthErrorCode;
import com.jinsu.ticketrace.global.exception.MemberErrorCode;
import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.member.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthValidator {
    private final MemberRepository memberRepository;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;

    public Member memberCheck(String id, String password){
        Member member = memberRepository.findByMemberId(id)
                .orElseThrow(() -> new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND));
        if(!encoder.matches(password, member.getPassword())) throw new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND);

        return member;
    }

    public long validateRefreshToken(String refreshToken){
        if(refreshToken == null || refreshToken.isBlank()) throw new GlobalException(AuthErrorCode.REFRESH_TOKEN_REQUIRED);

        Claims claims;
        try{
            claims = jwtTokenProvider.parseAndValidate(refreshToken);
        }catch (Exception e){
            throw new GlobalException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        String typ = claims.get("typ", String.class);
        if(!"refresh".equals(typ)) throw new GlobalException(AuthErrorCode.INVALID_REFRESH_TOKEN);

        long memberPk = Long.parseLong(claims.getSubject());
        String storedToken = refreshTokenStore.find(memberPk)
                .orElseThrow(() -> new GlobalException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        if (!storedToken.equals(refreshToken)) throw new GlobalException(AuthErrorCode.INVALID_REFRESH_TOKEN);

        return memberPk;

    }


}
