package com.jinsu.ticketrace.member.domain.DTO;

import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.ticket.payment.PaymentMethod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MemberDTO {


    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Info{

        private String id;
        private String nickname;
        private String email;
        private long account;

        public static Info of(Member member){
            return Info.builder()
                    .account(member.getAccount())
                    .id(member.getMemberId())
                    .nickname(member.getNickname())
                    .email(member.getEmail())
                    .build();
        }

    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Password {
        @NotBlank
        private String password;
    }
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Nickname{
        @NotBlank
        private String nickname;
    }

    /**
     * 잔액 충전 요청.
     * - amount: 충전 금액 (1원 이상)
     * - method: 결제 수단 (CARD / VBANK / MOCK)
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChargeRequest {
        @NotNull
        @Min(value = 1, message = "충전 금액은 1원 이상이어야 합니다.")
        private Long amount;

        @NotNull
        private PaymentMethod method;
    }

    /**
     * 잔액 충전 응답.
     * - transactionId: PG 거래 ID
     * - chargedAmount: 이번에 충전된 금액
     * - balance: 충전 후 최종 잔액
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChargeResponse {
        private String transactionId;
        private long chargedAmount;
        private long balance;
    }

    /**
     * 잔액 조회 응답.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Account {
        private long balance;

        public static Account of(long balance) {
            return Account.builder().balance(balance).build();
        }
    }
}
