package com.jinsu.ticketrace.member.domain.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

public class SignUpDTO {

    @Getter
    @ToString
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignUpRequest{

        @NotEmpty
        private String id;

        @Email
        private String email;

        @NotEmpty
        private String password;

        @NotEmpty
        private String nickname;

    }
}
