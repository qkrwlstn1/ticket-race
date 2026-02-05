package com.jinsu.ticketrace.member.service.unit;

import com.jinsu.ticketrace.auth.service.AuthService;
import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.member.repository.MemberRepository;
import com.jinsu.ticketrace.member.service.MemberService;
import com.jinsu.ticketrace.member.validator.MemberValidator;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    @Mock
    MemberRepository memberRepository; // DB 접근 차단(목 객체)
    @Mock
    MemberValidator memberValidator;
    @Mock
    AuthService authService;

    PasswordEncoder passwordEncoder;
    MemberService memberService;

    @BeforeEach
    void setUp() {
        // 실제 구현체로 테스트(기본적으로 bcrypt 기반)
        String idForEncode = "bcrypt";

        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put(idForEncode, new BCryptPasswordEncoder(4)); // 테스트 환경에선 cost를 의도적으로 낮춤(최소 4)

        passwordEncoder = new DelegatingPasswordEncoder(idForEncode, encoders);
        memberService = new MemberService(memberRepository, passwordEncoder, memberValidator, authService);
    }

    @Tag("password Encoding")
    @DisplayName("비밀번호는 평문으로 저장되지 않는다")
    @Test
    void rawPassword_Encode_NotEqual() {
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
    void samePassword_EncodedMultipleTimes_HashesDiffer() {
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
    @DisplayName("인코딩 결과가 매번 달라도 matches(raw, encoded)는 항상 true다")
    @Test
    void rawPassword_Matches_TrueForEachEncoded() {
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
    }

    @DisplayName("닉네임 변경 시 중복 검증 후 엔티티가 업데이트된다")
    @Test
    void member_ModifyInfo_ValidateNickname() {
        String newNickname = "newNickname";

        Member member = Member.builder()
                .memberPk(1L)
                .nickname("oldNickname")
                .build();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        memberService.modifyInfo(newNickname, 1L);

        verify(memberValidator).memberNicknameCheck(newNickname);

    }

    @DisplayName("회원 탈퇴 시 로그아웃 처리 후 삭제된다")
    @Test
    void member_Delete_LogoutAndDelete() {
        Member member = Member.builder()
                .memberPk(1L)
                .build();
        String accessToken = "accessToken";
        memberService.deleteMember(member, accessToken);

        verify(authService).logout(member.getMemberPk(), accessToken);
        verify(memberRepository).delete(member);

    }

}
