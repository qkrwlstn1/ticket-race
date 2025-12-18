package com.jinsu.ticketrace.member.service.uint;

import com.jinsu.ticketrace.member.repository.MemberRepository;
import com.jinsu.ticketrace.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {


    @Mock
    MemberRepository memberRepository; // DB 접근 차단(목 객체)

    PasswordEncoder passwordEncoder;
    MemberService memberService;

    @BeforeEach
    void setUp() {
        // 실제 구현체로 테스트(기본적으로 bcrypt 기반)
        String idForEncode = "bcrypt";

        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put(idForEncode, new BCryptPasswordEncoder(4)); // 테스트 환경에선 cost를 의도적으로 낮춤(최소 4)

        passwordEncoder = new DelegatingPasswordEncoder(idForEncode, encoders);
        memberService = new MemberService(memberRepository, passwordEncoder);
    }


    @Tag("password Encoding")
    @DisplayName("비밀번호는 평문으로 저장되면 안 된다")
    @Test
    void whenPasswordEncoding_than실제_비밀번호와_인코딩된_값이_달라야_함(){
        //given
        String raw = "P@ssw0rd!";

        //when
        String enc = passwordEncoder.encode(raw);

        //then

        // 평문이 그대로 저장되면 안 됨
        assertNotEquals(raw, enc);
    }

    @Tag("password Encoding")
    @DisplayName("같은 비밀번호를 여러 번 인코딩하면 결과 문자열은 서로 달라진다")
    @Test
    void whenPasswordEncoding_then인코딩된_값이_달라야_한다() {
        // given
        String raw = "P@ssw0rd!";

        // when
        String enc1 = memberService.passwordEncoding(raw);
        String enc2 = memberService.passwordEncoding(raw);
        String enc3 = memberService.passwordEncoding(raw);

        // then
        assertAll(
                // 안전한 인코더(bcrypt/argon2 등)라면 보통 매번 결과가 달라짐(랜덤 salt 포함)
                () -> assertNotEquals(enc1, enc2),
                () -> assertNotEquals(enc2, enc3)
        );
    }
    @Tag("password Encoding")
    @DisplayName("인코딩 결과가 매번 달라도 matches(raw, encoded)는 항상 true")
    @Test
    void whenPasswordEncoding_then인코딩_결과가_달라도_실제_mathches_결과는_true() {
        long ts = System.nanoTime();
        // given
        String raw = "P@ssw0rd!";

        // when
        String enc1 = memberService.passwordEncoding(raw);
        String enc2 = memberService.passwordEncoding(raw);

        // then
        assertAll(
                () -> assertTrue(passwordEncoder.matches(raw, enc1)),
                () -> assertTrue(passwordEncoder.matches(raw, enc2)),

                //틀린 비밀번호는 false
                () -> assertFalse(passwordEncoder.matches("WrongP@ss!", enc1))
        );
        long te = System.nanoTime();
        System.out.println("time = " + (te - ts) / 1_000_000);
    }

}