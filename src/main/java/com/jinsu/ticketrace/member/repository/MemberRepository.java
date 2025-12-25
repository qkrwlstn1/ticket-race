package com.jinsu.ticketrace.member.repository;

import com.jinsu.ticketrace.member.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    //id, email, nickname 중복 체크
    Optional<Member> findByMemberIdOrEmailOrNickname(String memberId, String Email, String nickname);

    Optional<Member> findByMemberId(String memberId);

    boolean existsByMemberId(String memberId);
    //email 중복 체크
    boolean existsByEmail(String email);
    //nickname 중복 체크
    boolean existsByNickname(String nickname);

}
