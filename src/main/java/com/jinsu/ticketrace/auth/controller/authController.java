package com.jinsu.ticketrace.auth.controller;

import com.jinsu.ticketrace.auth.domain.DTO.SignInDTO;
import com.jinsu.ticketrace.auth.service.AuthService;
import com.jinsu.ticketrace.auth.validator.AuthValidator;
import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.member.validator.MemberValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
@Tag(name = "auth API")
@RequiredArgsConstructor
public class authController {

    private final AuthValidator validator;
    private final AuthService authService;

    @PostMapping("signin")
    @Operation(
            summary = "로그인",
            description = "유효한 회원인지 검증 후 accessToken + refreshToken 발급",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인 성공"),
                    @ApiResponse(responseCode = "401", description = "아이디, 비밀번호 확인 필요")
            }
    )
    public ResponseEntity<AuthService.TokenResponse> signin(@RequestBody SignInDTO.signInRequest user){
        Member member =  validator.memberCheck(user.getId(), user.getPassword());
        return ResponseEntity.ok(authService.signIn(member));
    }

    //토큰 재발급

    //로그아웃

}
