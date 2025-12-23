package com.jinsu.ticketrace.member.validator.Integration;

import com.jinsu.ticketrace.global.error.GlobalException;
import com.jinsu.ticketrace.global.exception.MemberErrorCode;
import com.jinsu.ticketrace.member.domain.DTO.SignUpDTO;
import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.member.repository.MemberRepository;
import com.jinsu.ticketrace.member.validator.MemberValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(MemberValidator.class)
class MemberValidatorTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MemberValidator memberValidator;

    @BeforeEach
    void setUp() {
        SignUpDTO.SignUpRequest existing = signUpRequest("jinsu", "jinsu@naver.com", "jinsu");
        memberRepository.save(Member.of(existing, "password"));
    }

    @Test
    @DisplayName("중복이 없으면 예외가 발생하지 않는다")
    void no_duplicate_should_pass() {
        assertDoesNotThrow(() ->
                memberValidator.memberDuplicateCheck(signUpRequest("park", "park@naver.com", "park"))
        );
    }

    @ParameterizedTest(name = "[{index}] {0} -> {2}")
    @MethodSource("duplicate_cases")
    @DisplayName("중복이면 정책에 맞는 ErrorCode가 발생한다")
    void duplicate_should_throw_by_policy(String scenario,
                                          SignUpDTO.SignUpRequest request,
                                          MemberErrorCode expected) {

        GlobalException e = assertThrows(GlobalException.class,
                () -> memberValidator.memberDuplicateCheck(request));

        assertEquals(expected, e.getErrorCode());
    }


    static Stream<Arguments> duplicate_cases() {
        return Stream.of(
                // 단일 중복
                Arguments.of("ID 중복",       signUpRequest("jinsu", "park@naver.com", "park"), MemberErrorCode.DUPLICATE_ID),
                Arguments.of("EMAIL 중복",    signUpRequest("park", "jinsu@naver.com", "park"), MemberErrorCode.DUPLICATE_EMAIL),
                Arguments.of("NICKNAME 중복", signUpRequest("park", "park@naver.com", "jinsu"), MemberErrorCode.DUPLICATE_NICKNAME),

                // 복수 중복(정책 우선순위 검증용)
                Arguments.of("ID+EMAIL+NICK 중복", signUpRequest("jinsu", "jinsu@naver.com", "jinsu"), MemberErrorCode.DUPLICATE_ID),
                Arguments.of("ID+NICK 중복",      signUpRequest("jinsu", "park@naver.com", "jinsu"), MemberErrorCode.DUPLICATE_ID),
                Arguments.of("ID+EMAIL 중복",     signUpRequest("jinsu", "jinsu@naver.com", "park"), MemberErrorCode.DUPLICATE_ID),
                Arguments.of("EMAIL+NICK 중복", signUpRequest("park", "jinsu@naver.com", "jinsu"), MemberErrorCode.DUPLICATE_EMAIL),
                Arguments.of("EMAIL 중복",      signUpRequest("park", "jinsu@naver.com", "park"), MemberErrorCode.DUPLICATE_EMAIL),
                Arguments.of("NICK 중복",     signUpRequest("park", "park@naver.com", "jinsu"), MemberErrorCode.DUPLICATE_NICKNAME)
        );
    }

    private static SignUpDTO.SignUpRequest signUpRequest(String id, String email, String nickname) {
        return SignUpDTO.SignUpRequest.builder()
                .id(id)
                .email(email)
                .nickname(nickname)
                .password("password")
                .build();
    }

}