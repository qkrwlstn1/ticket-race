package com.jinsu.ticketrace.member.controller;

import com.jinsu.ticketrace.member.domain.DTO.SignUpDTO;
import com.jinsu.ticketrace.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member")
@Tag(name = "User API")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    @PostMapping(value = "signup")
    @Operation(
            summary = "회원가입",
            description = "회원가입 및 회원가입 성공 여부를 반환",
            responses = {
                    @ApiResponse(responseCode = "201", description = "회원가입 성공."),
                    @ApiResponse(responseCode = "409", description = "중복된 이메일, 닉네임, 아이디로 인한 회원가입 실패.")
            }
    )
    public ResponseEntity<Long> signUp(@RequestBody @Valid SignUpDTO.SignUpRequest signUpRequest){

        long result = memberService.signUp(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

}
