package com.jinsu.ticketrace.member.repository;

import com.jinsu.ticketrace.member.domain.entity.Member;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Member m where m.memberPk = :id")
    Optional<Member> findByIdWithPessimisticLock(long id);

    @Modifying
    @Query("""
            update Member m 
            set m.account = m.account - :totalPrice 
            where m.memberPk = :memberPk 
                and m.account >= :totalPrice
            """)
    int decreaseAccount(long memberPk, long totalPrice);

    @Modifying
    @Query("""
            update Member m
            set m.account = m.account + :refundAmount
            where m.memberPk = :memberPk
            """)
    int increaseAccount(long memberPk, long refundAmount);
}
