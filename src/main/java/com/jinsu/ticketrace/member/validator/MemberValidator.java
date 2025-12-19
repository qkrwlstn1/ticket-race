package com.jinsu.ticketrace.member.validator;

import com.jinsu.ticketrace.global.error.GlobalException;
import com.jinsu.ticketrace.global.exception.MemberErrorCode;
import com.jinsu.ticketrace.member.domain.DTO.SignUpDTO;
import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Component
@RequiredArgsConstructor
public class MemberValidator {
    private final MemberRepository memberRepository;


    //id, email, nickname 중복체크
    public void memberDuplicateCheck(SignUpDTO.SignUpRequest request){
        Optional<Member> member =memberRepository
                .findByIdOrEmailOrNickname(
                        request.getId(), request.getEmail(), request.getNickname()
                );
        if(member.isEmpty()) return;
        Member dupMember = member.get();

        String id = dupMember.getId();
        String email = dupMember.getEmail();
        String nickname = dupMember.getNickname();

        //체크 순서 id -> email -> nickname
        if (id.equals(request.getId())) throw new GlobalException(MemberErrorCode.DUPLICATE_ID);
        if (email.equals(request.getEmail())) throw new GlobalException(MemberErrorCode.DUPLICATE_EMAIL);
        if (nickname.equals(request.getNickname())) throw new GlobalException(MemberErrorCode.DUPLICATE_NICKNAME);


    }

    //비밀번호 체크

}
