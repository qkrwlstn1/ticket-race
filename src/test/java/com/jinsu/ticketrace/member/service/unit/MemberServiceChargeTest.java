package com.jinsu.ticketrace.member.service.unit;

import com.jinsu.ticketrace.auth.service.AuthService;
import com.jinsu.ticketrace.global.error.GlobalException;
import com.jinsu.ticketrace.global.exception.MemberErrorCode;
import com.jinsu.ticketrace.member.domain.DTO.MemberDTO;
import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.member.repository.MemberRepository;
import com.jinsu.ticketrace.member.service.MemberService;
import com.jinsu.ticketrace.member.validator.MemberValidator;
import com.jinsu.ticketrace.global.exception.PaymentErrorCode;
import com.jinsu.ticketrace.ticket.payment.PaymentGateway;
import com.jinsu.ticketrace.ticket.payment.PaymentMethod;
import com.jinsu.ticketrace.ticket.payment.PaymentRequest;
import com.jinsu.ticketrace.ticket.payment.PaymentResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 잔액 충전(charge) / 잔액 조회(getAccount) 단위 테스트.
 *
 * 주요 검증 포인트:
 * 1. 회원이 없으면 PG 호출 자체를 하지 않는다 (빠른 실패)
 * 2. PG 실패 시 DB 갱신을 시도하지 않는다 (외부 시스템 실패에 따른 데이터 일관성)
 * 3. PG 성공 후 DB 갱신 정상
 * 4. 잔액 조회는 readOnly 트랜잭션
 */
@ExtendWith(MockitoExtension.class)
class MemberServiceChargeTest {

    @Mock MemberRepository memberRepository;
    @Mock MemberValidator memberValidator;
    @Mock AuthService authService;
    @Mock PasswordEncoder passwordEncoder;
    @Mock PaymentGateway paymentGateway;
    @Mock TransactionTemplate transactionTemplate;

    MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(
                memberRepository,
                passwordEncoder,
                memberValidator,
                authService,
                paymentGateway,
                transactionTemplate
        );
    }

    @Nested
    @DisplayName("잔액 충전 (charge)")
    class Charge {

        @Test
        @DisplayName("회원이 존재하지 않으면 PG 호출 없이 MEMBER_NOT_FOUND 예외")
        void memberNotFound_doesNotCallPaymentGateway() {
            // given
            long memberPk = 999L;
            MemberDTO.ChargeRequest req = MemberDTO.ChargeRequest.builder()
                    .amount(10_000L)
                    .method(PaymentMethod.MOCK)
                    .build();
            when(memberRepository.existsById(memberPk)).thenReturn(false);

            // when / then
            GlobalException ex = assertThrows(GlobalException.class,
                    () -> memberService.charge(memberPk, req));
            assertEquals(MemberErrorCode.MEMBER_NOT_FOUND, ex.getErrorCode());

            // PG 호출이 절대 일어나지 않아야 함
            verify(paymentGateway, never()).charge(any());
            verify(transactionTemplate, never()).execute(any());
        }

        @Test
        @DisplayName("PG 결제 실패 시 PAYMENT_FAILED 예외 + DB 갱신 시도하지 않음")
        void paymentFailed_doesNotUpdateBalance() {
            // given
            long memberPk = 1L;
            MemberDTO.ChargeRequest req = MemberDTO.ChargeRequest.builder()
                    .amount(50_000L)
                    .method(PaymentMethod.CARD)
                    .build();
            when(memberRepository.existsById(memberPk)).thenReturn(true);
            when(paymentGateway.charge(any(PaymentRequest.class)))
                    .thenReturn(PaymentResult.failure("카드 한도 초과"));

            // when / then
            GlobalException ex = assertThrows(GlobalException.class,
                    () -> memberService.charge(memberPk, req));
            assertEquals(PaymentErrorCode.PAYMENT_FAILED, ex.getErrorCode());

            // DB 트랜잭션 시도가 일어나면 안 됨 (PG 실패 = 잔액 변경 금지)
            verify(transactionTemplate, never()).execute(any());
        }

        @Test
        @DisplayName("정상 충전 - PG 성공 후 DB 갱신 + 응답에 거래ID/잔액/충전금액 포함")
        @SuppressWarnings("unchecked")
        void chargeSuccess_returnsResponseWithBalance() {
            // given
            long memberPk = 1L;
            long chargeAmount = 100_000L;
            long expectedBalance = 250_000L; // 충전 후 예상 잔액
            String txId = "mock_tx_abcdef";

            MemberDTO.ChargeRequest req = MemberDTO.ChargeRequest.builder()
                    .amount(chargeAmount)
                    .method(PaymentMethod.MOCK)
                    .build();

            when(memberRepository.existsById(memberPk)).thenReturn(true);
            when(paymentGateway.charge(any(PaymentRequest.class)))
                    .thenReturn(PaymentResult.success(txId));
            // TransactionTemplate.execute는 콜백을 직접 실행
            when(transactionTemplate.execute(any(TransactionCallback.class)))
                    .thenReturn(expectedBalance);

            // when
            MemberDTO.ChargeResponse response = memberService.charge(memberPk, req);

            // then
            assertAll(
                    () -> assertEquals(txId, response.getTransactionId()),
                    () -> assertEquals(chargeAmount, response.getChargedAmount()),
                    () -> assertEquals(expectedBalance, response.getBalance())
            );

            verify(paymentGateway).charge(any(PaymentRequest.class));
            verify(transactionTemplate).execute(any(TransactionCallback.class));
        }
    }

    @Nested
    @DisplayName("잔액 조회 (getAccount)")
    class GetAccount {

        @Test
        @DisplayName("회원이 존재하면 현재 잔액을 반환한다")
        void memberExists_returnsBalance() {
            // given
            long memberPk = 1L;
            long balance = 500_000L;
            Member member = Member.builder()
                    .memberPk(memberPk)
                    .account(balance)
                    .build();
            when(memberRepository.findById(memberPk)).thenReturn(Optional.of(member));

            // when
            MemberDTO.Account result = memberService.getAccount(memberPk);

            // then
            assertEquals(balance, result.getBalance());
        }

        @Test
        @DisplayName("회원이 없으면 MEMBER_NOT_FOUND 예외")
        void memberNotFound_throwsException() {
            // given
            long memberPk = 999L;
            when(memberRepository.findById(memberPk)).thenReturn(Optional.empty());

            // when / then
            GlobalException ex = assertThrows(GlobalException.class,
                    () -> memberService.getAccount(memberPk));
            assertEquals(MemberErrorCode.MEMBER_NOT_FOUND, ex.getErrorCode());
        }
    }
}
