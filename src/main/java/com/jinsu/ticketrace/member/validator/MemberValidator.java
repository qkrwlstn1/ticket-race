package com.jinsu.ticketrace.member.validator;

import com.jinsu.ticketrace.global.error.GlobalException;
import com.jinsu.ticketrace.global.exception.MemberErrorCode;
import com.jinsu.ticketrace.member.domain.DTO.SignUpDTO;
import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
@Component
@RequiredArgsConstructor
public class MemberValidator {
    private final MemberRepository memberRepository;
    private final PasswordEncoder encoder;

    //id, email, nickname 중복체크
    public void memberDuplicateCheck(SignUpDTO.SignUpRequest request){
        Optional<Member> member =memberRepository
                .findByMemberIdOrEmailOrNickname(
                        request.getId(), request.getEmail(), request.getNickname()
                );
        if(member.isEmpty()) return;
        Member dupMember = member.get();

        String id = dupMember.getMemberId();
        String email = dupMember.getEmail();
        String nickname = dupMember.getNickname();

        //체크 순서 id -> email -> nickname
        if (id.equals(request.getId())) throw new GlobalException(MemberErrorCode.DUPLICATE_ID);
        if (email.equals(request.getEmail())) throw new GlobalException(MemberErrorCode.DUPLICATE_EMAIL);
        if (nickname.equals(request.getNickname())) throw new GlobalException(MemberErrorCode.DUPLICATE_NICKNAME);


    }

    //비밀번호 체크
    public Member memberPasswordCheck(long memberPk, String password){
        Optional<Member> optionalMember = memberRepository.findById(memberPk);
        Member member = optionalMember.orElseThrow(() -> new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND));
        if(!encoder.matches(password, member.getPassword())) throw new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND);
        return member;
    }

    public Member memberCheck(Authentication authentication){
        long memberPk = Long.parseLong(authentication.getName());
        return memberRepository.findById(memberPk).orElseThrow(() -> new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    public void memberEmailCheck(String email){
        if(memberRepository.existsByEmail(email)) throw new GlobalException(MemberErrorCode.DUPLICATE_EMAIL);
    }
    public void memberIdCheck(String id){
        if(memberRepository.existsByMemberId(id)) throw new GlobalException(MemberErrorCode.DUPLICATE_ID);
    }
    public void memberNicknameCheck(String nickname){
        if(memberRepository.existsByNickname(nickname)) throw new GlobalException(MemberErrorCode.DUPLICATE_NICKNAME);
    }

}
