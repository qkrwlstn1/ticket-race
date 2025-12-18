package com.jinsu.ticketrace.member.service;

import com.jinsu.ticketrace.member.domain.DTO.SignUpDTO;
import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    //회원 가입
    @Transactional
    public long signUp(SignUpDTO.SignUpRequest signUpRequest){
        String encodedPassword = passwordEncoding(signUpRequest.getPassword());
        Member member = Member.of(signUpRequest, encodedPassword);

        return memberRepository.save(member).getMemberPk();
    }

    public String passwordEncoding(String password){
        return passwordEncoder.encode(password);
    }

}
