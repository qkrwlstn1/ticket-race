package com.jinsu.ticketrace.auth.validator.unit;

import com.jinsu.ticketrace.auth.jwt.JwtTokenProvider;
import com.jinsu.ticketrace.auth.repository.redis.RefreshTokenStore;
import com.jinsu.ticketrace.auth.validator.AuthValidator;
import com.jinsu.ticketrace.global.error.GlobalException;
import com.jinsu.ticketrace.global.exception.AuthErrorCode;
import com.jinsu.ticketrace.global.exception.MemberErrorCode;
import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.member.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthValidatorTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PasswordEncoder encoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private RefreshTokenStore refreshTokenStore;

    @InjectMocks
    private AuthValidator validator;

    @Test
    @DisplayName("아이디/비밀번호가 일치하면 Member를 반환한다")
    void member_PasswordMatches_ReturnMember() {
        Member member = Member.builder()
                .memberId("user")
                .password("hashed")
                .build();

        when(memberRepository.findByMemberId("user")).thenReturn(Optional.of(member));
        when(encoder.matches("pass","hashed")).thenReturn(true);

        Member result = validator.memberCheck("user", "pass");

        assertSame(result, member);

    }

    @Test
    @DisplayName("비밀번호가 다르면 MEMBER_NOT_FOUND 예외를 반환한다")
    void member_PasswordMismatch_Throw() {
        Member member = Member.builder()
                .memberId("user")
                .password("hashed")
                .build();
        when(memberRepository.findByMemberId("user")).thenReturn(Optional.of(member));
        when(encoder.matches("pass", "hashed")).thenReturn(false);

        GlobalException passMiss = assertThrows(GlobalException.class, () -> validator.memberCheck("user", "pass"));


        assertEquals(MemberErrorCode.MEMBER_NOT_FOUND, passMiss.getErrorCode());
    }

    @Test
    @DisplayName("아이디가 다르면 MEMBER_NOT_FOUND 예외를 반환한다")
    void missingMember_Check_Throw() {
        when(memberRepository.findByMemberId("user1")).thenReturn(Optional.empty());

        GlobalException passMiss = assertThrows(GlobalException.class, () -> validator.memberCheck("user1", "pass"));


        assertEquals(MemberErrorCode.MEMBER_NOT_FOUND, passMiss.getErrorCode());
    }
    @Test
    @DisplayName("refresh token이 비어 있거나 null인 경우 REFRESH_TOKEN_REQUIRED 예외를 반환한다")
    void emptyRefreshToken_Validate_ThrowRequired() {
        GlobalException nullException = assertThrows(GlobalException.class, () -> validator.validateRefreshToken(null));
        GlobalException emptyException = assertThrows(GlobalException.class, () -> validator.validateRefreshToken(""));

        assertEquals(AuthErrorCode.REFRESH_TOKEN_REQUIRED, nullException.getErrorCode());
        assertEquals(AuthErrorCode.REFRESH_TOKEN_REQUIRED, emptyException.getErrorCode());

    }

    @Test
    @DisplayName("refresh token 파싱 실패 시 INVALID_REFRESH_TOKEN 예외를 반환한다")
    void invalidRefreshToken_Parse_Throw() {
        String badToken = "bad.token";
        when(jwtTokenProvider.parseAndValidate(badToken)).thenThrow(new IllegalArgumentException("bad"));

        GlobalException e = assertThrows(GlobalException.class, () -> validator.validateRefreshToken(badToken));

        assertEquals(AuthErrorCode.INVALID_REFRESH_TOKEN, e.getErrorCode());

    }

    @Test
    @DisplayName("refresh 타입이 아니면 INVALID_REFRESH_TOKEN 예외를 반환한다")
    void nonRefreshToken_Validate_Throw() {
        String accessToken = "accessToken";
        Claims claims = Jwts.claims()
                .subject("1")
                .add("typ", "access")
                .build();
        when(jwtTokenProvider.parseAndValidate(accessToken)).thenReturn(claims);

        GlobalException e = assertThrows(GlobalException.class, () -> validator.validateRefreshToken(accessToken));
        assertEquals(AuthErrorCode.INVALID_REFRESH_TOKEN, e.getErrorCode());
    }

    @Test
    @DisplayName("저장된 refresh token과 다르면 INVALID_REFRESH_TOKEN 예외를 반환한다")
    void mismatchedRefreshToken_Validate_Throw() {
        String inputToken = "refresh token1";
        String returnToken = "refresh token2";
        Claims claims = Jwts.claims()
                .subject("1")
                .add("typ", "refresh")
                .build();
        when(jwtTokenProvider.parseAndValidate(inputToken)).thenReturn(claims);
        when(refreshTokenStore.find(1L)).thenReturn(Optional.of(returnToken));

        GlobalException e = assertThrows(GlobalException.class, () -> validator.validateRefreshToken(inputToken));

        assertEquals(AuthErrorCode.INVALID_REFRESH_TOKEN, e.getErrorCode());
    }

    @Test
    @DisplayName("정상 refresh token이면 memberPk를 반환한다")
    void validRefreshToken_Validate_ReturnMemberPk() {
        String inputToken = "refresh token";
        String returnToken = "refresh token";
        Claims claims = Jwts.claims()
                .subject("1")
                .add("typ", "refresh")
                .build();
        when(jwtTokenProvider.parseAndValidate(inputToken)).thenReturn(claims);
        when(refreshTokenStore.find(1L)).thenReturn(Optional.of(returnToken));

        assertEquals(1L, validator.validateRefreshToken(inputToken));
    }

}
