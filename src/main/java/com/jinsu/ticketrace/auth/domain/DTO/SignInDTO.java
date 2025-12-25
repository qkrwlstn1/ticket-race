package com.jinsu.ticketrace.auth.domain.DTO;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

public class SignInDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class signInRequest{

        @NotEmpty
        private String id;
        @NotEmpty
        private String password;
    }
}
