package com.jinsu.ticketrace.auth.validator;

import com.jinsu.ticketrace.global.error.GlobalException;
import com.jinsu.ticketrace.global.exception.MemberErrorCode;
import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthValidator {
    private final MemberRepository memberRepository;
    private final PasswordEncoder encoder;

    public Member memberCheck(String id, String password){
        System.out.println("id : "+id +"\npass : "+password);
        Member member = memberRepository.findByMemberId(id)
                .orElseThrow(() -> new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND));
        System.out.println("id : " + member.getMemberId() + "\npass : " + member.getPassword());
        if(!encoder.matches(password, member.getPassword())) throw new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND);

        return member;
    }

}
