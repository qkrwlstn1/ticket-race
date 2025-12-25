package com.jinsu.ticketrace.member.service.integration;

import com.jinsu.ticketrace.member.domain.DTO.SignUpDTO;
import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.member.repository.MemberRepository;
import com.jinsu.ticketrace.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberServiceTest {
    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MemberService memberService;



    enum CheckType{ID, EMAIL, NICKNAME}

    @BeforeEach
    void setUp() {
        memberRepository.save(Member.builder()
                .memberId("jinsu")
                .email("jinsu@naver.com")
                .nickname("jinsu")
                .build()
        );
    }

    @Test
    @DisplayName("회원가입을 하면 저장되어야 한다")
    void sign_up_for_member_it_should_be_saved(){
        //given
        SignUpDTO.SignUpRequest dto= SignUpDTO.SignUpRequest.builder()
                .id("id")
                .email("email@naver.com")
                .nickname("nickname")
                .password("password")
                .build();

        //when
        memberService.signUp(dto);
        Optional<Member> result = memberRepository.findByMemberIdOrEmailOrNickname(dto.getId(), dto.getEmail(), dto.getNickname());
        //then
        assertFalse(result.isEmpty());
    }


    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("duplicate_cases")
    @DisplayName("중복이면 false를 반환한다")
    void duplicate_are_false(String scenario, String request, CheckType type){
        boolean result = switch (type){
            case ID -> memberService.checkId(request);
            case EMAIL -> memberService.checkEmail(request);
            case NICKNAME -> memberService.checkNickname(request);
        };

        assertFalse(result);
    }
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("non_duplicate_cases")
    @DisplayName("중복이 아니면 true를 반환한다")
    void non_duplicate_are_true(String scenario, String request, CheckType type){
        boolean result = switch (type){
            case ID -> memberService.checkId(request);
            case EMAIL -> memberService.checkEmail(request);
            case NICKNAME -> memberService.checkNickname(request);
        };

        assertTrue(result);
    }


    static Stream<Arguments> duplicate_cases(){
        return Stream.of(
                Arguments.of("Id 중복", "jinsu", CheckType.ID),
                Arguments.of("email 중복", "jinsu@naver.com", CheckType.EMAIL),
                Arguments.of("nickname 중복", "jinsu", CheckType.NICKNAME)
        );
    }

    public static Stream<Arguments> non_duplicate_cases() {
        return Stream.of(
                Arguments.of("Id 비중복", "park", CheckType.ID),
                Arguments.of("email 비중복", "park@naver.com", CheckType.EMAIL),
                Arguments.of("nickname 비중복", "park", CheckType.NICKNAME)
        );
    }


}