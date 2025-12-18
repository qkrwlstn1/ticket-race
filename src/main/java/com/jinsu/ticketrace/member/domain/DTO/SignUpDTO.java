package com.jinsu.ticketrace.member.domain.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.ToString;

public class SignUpDTO {

    @Getter
    @ToString
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
