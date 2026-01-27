package com.jinsu.ticketrace.auth.controller;

import com.jinsu.ticketrace.auth.domain.DTO.SignInDTO;
import com.jinsu.ticketrace.auth.domain.DTO.TokenReissueDTO;
import com.jinsu.ticketrace.auth.service.AuthService;
import com.jinsu.ticketrace.auth.validator.AuthValidator;
import com.jinsu.ticketrace.member.domain.entity.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
                    @ApiResponse(responseCode = "404", description = "아이디, 비밀번호 확인 필요")
            }
    )
    public ResponseEntity<AuthService.TokenResponse> signin(@RequestBody @Valid SignInDTO.signInRequest user){
        Member member =  validator.memberCheck(user.getId(), user.getPassword());
        return ResponseEntity.ok(authService.signIn(member));
    }

    //토큰 재발급
    @PostMapping("reissue")
    @Operation(
            summary = "토큰 재발급",
            description = "refresh token 검증 후 accessToken + refreshToken 재발급",
            responses = {
                    @ApiResponse(responseCode = "200", description = "재발급 성공"),
                    @ApiResponse(responseCode = "401", description = "refresh token 만료 / 유효하지 않음")
            }
    )
    public ResponseEntity<AuthService.TokenResponse> reissue(@RequestBody @Valid TokenReissueDTO.ReissueRequest request){
        long memberPk = validator.validateRefreshToken(request.getRefreshToken());
        return ResponseEntity.ok(authService.reissue(request.getRefreshToken() ,memberPk));
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
