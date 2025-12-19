package com.jinsu.ticketrace.member.repository;

import com.jinsu.ticketrace.member.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByIdOrEmailOrNickname(String id, String Email, String nickname);
}
