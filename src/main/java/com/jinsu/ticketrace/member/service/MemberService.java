package com.jinsu.ticketrace.member.service;

import com.jinsu.ticketrace.auth.service.AuthService;
import com.jinsu.ticketrace.global.error.GlobalException;
import com.jinsu.ticketrace.global.exception.MemberErrorCode;
import com.jinsu.ticketrace.member.domain.DTO.MemberDTO;
import com.jinsu.ticketrace.member.domain.DTO.SignUpDTO;
import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.member.repository.MemberRepository;
import com.jinsu.ticketrace.member.validator.MemberValidator;
import com.jinsu.ticketrace.global.exception.PaymentErrorCode;
import com.jinsu.ticketrace.ticket.payment.PaymentGateway;
import com.jinsu.ticketrace.ticket.payment.PaymentRequest;
import com.jinsu.ticketrace.ticket.payment.PaymentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberValidator memberValidator;
    private final AuthService authService;
    private final PaymentGateway paymentGateway;
    private final TransactionTemplate transactionTemplate;

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

    /**
     * 잔액 충전.
     *
     * 트랜잭션 경계 설계:
     * 1) PG 호출은 외부 네트워크 I/O이므로 DB 트랜잭션 외부에서 수행 (DB 커넥션을 점유한 채 외부 호출 금지)
     * 2) PG 성공 후에만 별도 트랜잭션으로 DB 잔액 갱신
     * 3) 멱등성 키(idempotencyKey)는 PG 측 중복 호출 방지를 위해 매 요청마다 새로 발급
     *    (Phase 4에서 클라이언트 Idempotency-Key 헤더로 확장 예정)
     *
     * @return 충전 후 최신 잔액 + PG 거래 ID
     */
    public MemberDTO.ChargeResponse charge(long memberPk, MemberDTO.ChargeRequest request) {
        // 1) 회원 존재 검증 (PG 호출 전 빠른 실패)
        if (!memberRepository.existsById(memberPk)) {
            throw new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND);
        }

        // 2) PG 호출 (트랜잭션 외부)
        String idempotencyKey = "charge_" + memberPk + "_" + UUID.randomUUID();
        PaymentResult pgResult = paymentGateway.charge(new PaymentRequest(
                memberPk,
                request.getAmount(),
                request.getMethod(),
                idempotencyKey
        ));

        if (!pgResult.success()) {
            log.warn("[Charge] PG 결제 실패 memberPk={}, reason={}", memberPk, pgResult.failureReason());
            throw new GlobalException(PaymentErrorCode.PAYMENT_FAILED);
        }

        // 3) DB 잔액 갱신 (별도 트랜잭션)
        long newBalance = transactionTemplate.execute(status -> {
            int updated = memberRepository.increaseAccount(memberPk, request.getAmount());
            if (updated != 1) {
                throw new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND);
            }
            // 갱신된 잔액 재조회 — 같은 트랜잭션이므로 본인이 쓴 값을 읽음
            return memberRepository.findById(memberPk)
                    .orElseThrow(() -> new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND))
                    .getAccount();
        });

        log.info("[Charge] 충전 완료 memberPk={}, amount={}, balance={}, txId={}",
                memberPk, request.getAmount(), newBalance, pgResult.transactionId());

        return MemberDTO.ChargeResponse.builder()
                .transactionId(pgResult.transactionId())
                .chargedAmount(request.getAmount())
                .balance(newBalance)
                .build();
    }

    /**
     * 현재 잔액 조회.
     */
    @Transactional(readOnly = true)
    public MemberDTO.Account getAccount(long memberPk) {
        Member member = memberRepository.findById(memberPk)
                .orElseThrow(() -> new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND));
        return MemberDTO.Account.of(member.getAccount());
    }

}
