package com.jinsu.ticketrace.member.controller;

import com.jinsu.ticketrace.member.domain.DTO.SignUpDTO;
import com.jinsu.ticketrace.member.service.MemberService;
import com.jinsu.ticketrace.member.validator.MemberValidator;
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
    private final MemberValidator validator;

    @PostMapping("signup")
    @Operation(
            summary = "회원가입",
            description = "회원가입 및 회원가입 성공 여부를 반환",
            responses = {
                    @ApiResponse(responseCode = "201", description = "회원가입 성공."),
                    @ApiResponse(responseCode = "409", description = "중복된 이메일, 닉네임, 아이디로 인한 회원가입 실패.")
            }
    )
    public ResponseEntity<Long> signUp(@RequestBody @Valid SignUpDTO.SignUpRequest signUpRequest){
        validator.memberDuplicateCheck(signUpRequest);
        long result = memberService.signUp(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/id/{id}")
    @Operation(
            summary = "아이디 중복 체크",
            description = "중복을 체크하여 회원가입 가능한 아이디인지를 반환",
            responses ={
                    @ApiResponse(responseCode = "200",description = "사용가능 true, 사용불가 false")//중복 조회이기 때문에 409보다는 200이 합리적이라 느낌
            }
        )
    public ResponseEntity<Boolean> idCheck(@PathVariable String id){

        return ResponseEntity.ok(memberService.checkId(id));
    }
    @GetMapping("/email/{email}")
    @Operation(
            summary = "이메일 중복 체크",
            description = "중복을 체크하여 회원가입 가능한 이메일인지를 반환",
            responses ={
                    @ApiResponse(responseCode = "200",description = "사용가능 true, 사용불가 false")
            }
        )
    public ResponseEntity<Boolean> emailCheck(@PathVariable String email){

        return ResponseEntity.ok(memberService.checkEmail(email));
    }
    @GetMapping("/nickname/{nickname}")
    @Operation(
            summary = "닉네임 중복 체크",
            description = "중복을 체크하여 회원가입 가능한 닉네임인지를 반환",
            responses ={
                    @ApiResponse(responseCode = "200",description = "사용가능 true, 사용불가 false")
            }
        )
    public ResponseEntity<Boolean> nicknameCheck(@PathVariable String nickname){

        return ResponseEntity.ok(memberService.checkNickname(nickname));
    }



}
