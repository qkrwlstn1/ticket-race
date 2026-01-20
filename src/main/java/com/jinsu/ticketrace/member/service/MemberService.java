package com.jinsu.ticketrace.member.service;

import com.jinsu.ticketrace.auth.repository.redis.AccessTokenBlacklistStore;
import com.jinsu.ticketrace.auth.service.AuthService;
import com.jinsu.ticketrace.global.error.GlobalException;
import com.jinsu.ticketrace.global.exception.MemberErrorCode;
import com.jinsu.ticketrace.member.domain.DTO.SignUpDTO;
import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.member.repository.MemberRepository;
import com.jinsu.ticketrace.member.validator.MemberValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberValidator memberValidator;
    private final AuthService authService;

    //회원 가입
    @Transactional
    public long signUp(SignUpDTO.SignUpRequest signUpRequest) {
        String encodedPassword = passwordEncoding(signUpRequest.getPassword());
        Member member = Member.of(signUpRequest, encodedPassword);

        return memberRepository.save(member).getMemberPk();
    }


    public String passwordEncoding(String password) {
        return passwordEncoder.encode(password);
    }

    @Transactional
    public void modifyInfo(String nickname, long memberPk) {
        memberValidator.memberNicknameCheck(nickname);
        Member member = memberRepository.findById(memberPk).orElseThrow(() -> new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND));

        member.modifyNickname(nickname);
    }

    @Transactional
    public void deleteMember(Member member,String accessToken) {
        authService.logout(member.getMemberPk(), accessToken);
        memberRepository.delete(member);
    }
}
