package com.anteater.memberservice.member.controller;


import com.anteater.memberservice.entity.Member;
import com.anteater.memberservice.member.dto.request.*;
import com.anteater.memberservice.member.dto.response.ActivationResponse;
import com.anteater.memberservice.member.dto.response.PasswordChangeResponse;
import com.anteater.memberservice.member.dto.response.RegisterResponse;
import com.anteater.memberservice.member.service.MemberService;
import com.anteater.memberservice.member.dto.response.ProfileResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 회원 등록, 활성화 이메일 재전송, 회원 정보 업데이트, 비밀번호 변경 등 회원 관리 관련 엔드포인트를 담당하는 컨트롤러
 */
@RestController
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = memberService.register(request);
        return ResponseEntity.ok(response);
    }//[v]
    @GetMapping("/activate")
    public ResponseEntity<ActivationResponse> activateAccount(@RequestParam String token) {
        ActivationResponse response = memberService.activateAccount(token);
        return ResponseEntity.ok(response);
    }//[v]



    @PostMapping("/{memberId}/change-password")
    public ResponseEntity<PasswordChangeResponse> changePassword(
            @PathVariable Long memberId,
            @Valid @RequestBody PasswordChangeRequest request
    ) {
        PasswordChangeResponse response = memberService.changePassword(memberId, request);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{memberId}/profile")
    public ResponseEntity<ProfileResponse> updateProfile(
            @PathVariable Long memberId,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        ProfileResponse response = memberService.updateProfile(memberId, request);
        return ResponseEntity.ok(response);
    }

}
