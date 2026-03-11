package com.jinsu.ticketrace.member.controller;

import com.jinsu.ticketrace.member.domain.DTO.MemberDTO;
import com.jinsu.ticketrace.member.domain.DTO.SignUpDTO;
import com.jinsu.ticketrace.member.domain.entity.Member;
import com.jinsu.ticketrace.member.service.MemberService;
import com.jinsu.ticketrace.member.validator.MemberValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member")
@Tag(name = "User API")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final MemberValidator memberValidator;

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
        memberValidator.memberDuplicateCheck(signUpRequest);
        long result = memberService.signUp(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/id/{id}")
    @Operation(
            summary = "아이디 중복 체크",
            description = "중복을 체크하여 회원가입 가능한 아이디인지를 반환",
            responses ={
                    @ApiResponse(responseCode = "200",description = "사용가능 true"),
                    @ApiResponse(responseCode = "409", description = "중복된 아이디")
            }
        )
    public ResponseEntity<Boolean> idCheck(@PathVariable String id){
        memberValidator.memberIdCheck(id);
        return ResponseEntity.ok(true);
    }
    @GetMapping("/email/{email}")
    @Operation(
            summary = "이메일 중복 체크",
            description = "중복을 체크하여 회원가입 가능한 이메일인지를 반환",
            responses ={
                    @ApiResponse(responseCode = "200",description = "사용가능 true, 사용불가 false"),
                    @ApiResponse(responseCode = "409", description = "중복된 이메일")
            }
        )
    public ResponseEntity<Boolean> emailCheck(@PathVariable String email){
        memberValidator.memberEmailCheck(email);
        return ResponseEntity.ok(true);
    }
    @GetMapping("/nickname/{nickname}")
    @Operation(
            summary = "닉네임 중복 체크",
            description = "중복을 체크하여 회원가입 가능한 닉네임인지를 반환",
            responses ={
                    @ApiResponse(responseCode = "200",description = "사용가능 true, 사용불가 false"),
                    @ApiResponse(responseCode = "409", description = "중복된 닉네임")
            }
        )
    public ResponseEntity<Boolean> nicknameCheck(@PathVariable String nickname){
        memberValidator.memberNicknameCheck(nickname);
        return ResponseEntity.ok(true);
    }


    @PostMapping("me")
    @Operation(
            summary = "내 정보 조회",
            description = "비밀번호를 체크하고 정보를 반환",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "404", description = "비밀번호 틀림")
            }
    )
    public ResponseEntity<MemberDTO.Info> info(@RequestBody MemberDTO.Password password, Authentication authentication){
        long memberPk = Long.parseLong(authentication.getName());
        Member member =memberValidator.memberPasswordCheck(memberPk, password.getPassword());
        MemberDTO.Info result = MemberDTO.Info.of(member);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("me")
    @Operation(
            summary = "내 정보 수정",
            description = "닉네임을 수정할 수 있다",
            responses = {
                    @ApiResponse(responseCode = "204", description = "수정 성공"),
                    @ApiResponse(responseCode = "409", description = "중복된 닉네임")
            }
    )
     public ResponseEntity<?> modifyInfo(@RequestBody MemberDTO.Nickname nickname, Authentication authentication){
        long memberPk = Long.parseLong(authentication.getName());
        memberService.modifyInfo(nickname.getNickname(), memberPk);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("me")
    @Operation(
            summary = "회원 탈퇴",
            description = "비밀번호를 재확인 후 회원 삭제",
            responses = {
                    @ApiResponse(responseCode = "204", description = "삭제 성공"),
                    @ApiResponse(responseCode = "404", description = "비밀번호 틀림")
            }
    )
    public ResponseEntity<?> deleteMember(@RequestBody @Valid MemberDTO.Password password, Authentication authentication){
        long memberPk = Long.parseLong(authentication.getName());
        String accessToken = authentication.getCredentials() instanceof String credential
                ? credential : null;
        Member member = memberValidator.memberPasswordCheck(memberPk, password.getPassword());

        memberService.deleteMember(member, accessToken);
        return ResponseEntity.noContent().build();
    }


}
