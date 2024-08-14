package com.anteater.memberservice.member.controller;


import com.anteater.memberservice.entity.Member;
import com.anteater.memberservice.member.dto.request.*;
import com.anteater.memberservice.member.dto.response.ActivationResponse;
import com.anteater.memberservice.member.dto.response.PasswordChangeResponse;
import com.anteater.memberservice.member.dto.response.RegisterResponse;
import com.anteater.memberservice.member.dto.response.ResendActivationResponse;
import com.anteater.memberservice.member.service.MemberService;
import com.anteater.memberservice.profile.dto.ProfileResponse;
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
    }
    @GetMapping("/activate")
    public ResponseEntity<ActivationResponse> activateAccount(@RequestParam String token) {
        ActivationResponse response = memberService.activateAccount(token);
        return ResponseEntity.ok(response);
    }
    // 필요한 경우, 이메일 재전송을 위한 엔드포인트 추가
    @PostMapping("/resend-activation")
    public ResponseEntity<ResendActivationResponse> resendActivation(@RequestBody ResendActivationRequest request) {
        ResendActivationResponse response = memberService.resendActivationEmail(request.email());
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 정보를 업데이트합니다.
     *
     * @param memberId 업데이트할 사용자의 ID
     * @param request  사용자 정보 업데이트 요청 객체
     * @return 업데이트 성공 시 응답 객체를 반환
     */
    @PutMapping("/{memberId}")
    public ResponseEntity<Member> updateMember(@PathVariable Long memberId,
                                               @Valid @RequestBody MemberUpdateRequest request) {
        Member response = memberService.updateMemberInfo(memberId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 비밀번호 변경 요청을 처리합니다.
     * @param memberId 비밀번호를 변경할 사용자의 ID
     * @param request 비밀번호 변경 요청 객체
     * @return 변경 성공 시 응답 객체를 반환
     */
    @PostMapping("/{memberId}/change-password")
    public ResponseEntity<PasswordChangeResponse> changePassword(@PathVariable Long memberId,
                                                                 @Valid @RequestBody PasswordChangeRequest request) {
        PasswordChangeResponse response = memberService.changePassword(memberId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{memberId}/profile")
    public ResponseEntity<ProfileResponse> updateProfile(@PathVariable Long memberId,
                                                         @Valid @RequestBody ProfileUpdateRequest request) {
        Member updatedMember = memberService.updateProfile(memberId, request);

        ProfileResponse response = new ProfileResponse(
                updatedMember.getUsername(),
                updatedMember.getEmail(),
                updatedMember.getBio(),
                updatedMember.getProfileImg(),
                updatedMember.getSubscriptionStatus()
        );

        return ResponseEntity.ok(response);
    }


}
