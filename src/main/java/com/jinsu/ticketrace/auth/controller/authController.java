package com.jinsu.ticketrace.auth.controller;

import com.jinsu.ticketrace.auth.domain.DTO.SignInDTO;
import com.jinsu.ticketrace.auth.service.AuthService;
import com.jinsu.ticketrace.auth.validator.AuthValidator;
import com.jinsu.ticketrace.member.domain.entity.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/test")
    String test(){
        return "good";
    }



    //로그아웃
    @PostMapping("logout")
    @Operation(
            summary = "로그아웃",
            description = "refresh token 삭제",
            responses = {
                    @ApiResponse(responseCode = "204", description = "로그아웃 성공")
            }
    )
    public ResponseEntity<Void> logout(Authentication authentication){
        long memberPk = Long.parseLong(authentication.getName());
        String accessToken = authentication.getCredentials() instanceof String credential
                ? credential
                : null;
        authService.logout(memberPk, accessToken);
        return ResponseEntity.noContent().build();
    }

}
