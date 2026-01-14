package com.jinsu.ticketrace.auth.validator.unit;

import com.jinsu.ticketrace.auth.validator.AuthValidator;
import com.jinsu.ticketrace.global.error.GlobalException;
import com.jinsu.ticketrace.global.exception.MemberErrorCode;
import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthValidatorTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private AuthValidator validator;

    @Test
    @DisplayName("아이디와 비밀번호가 일치하면 Member를 반환한다.")
    void member_check_returns_member_when_password_matches(){
        Member member = Member.builder()
                .memberId("user")
                .password("hashed")
                .build();

        when(memberRepository.findByMemberId("user")).thenReturn(Optional.of(member));
        when(encoder.matches("pass","hashed")).thenReturn(true);

        Member result = validator.memberCheck("user", "pass");

        assertSame(result,member);

    }

    @Test
    @DisplayName("비밀번호가 다르면 MEMBER_NOT_FOUND 예외를 반환한다")
    void member_check_throws_when_password_mismatch(){
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
    void member_check_throws_when_id_mismatch(){
        Member member = Member.builder()
                .memberId("user")
                .password("hashed")
                .build();
        when(memberRepository.findByMemberId("user1")).thenReturn(Optional.empty());

        GlobalException passMiss = assertThrows(GlobalException.class, () -> validator.memberCheck("user1", "pass"));


        assertEquals(MemberErrorCode.MEMBER_NOT_FOUND, passMiss.getErrorCode());
    }

}