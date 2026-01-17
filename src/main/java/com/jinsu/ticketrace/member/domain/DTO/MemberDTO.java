package com.jinsu.ticketrace.member.domain.DTO;

import com.jinsu.ticketrace.member.domain.entity.Member;
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

        public static Info of(Member member){
            return Info.builder()
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
    public static class password{
        private String password;
    }
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class nickname{
        private String nickname;
    }
}
