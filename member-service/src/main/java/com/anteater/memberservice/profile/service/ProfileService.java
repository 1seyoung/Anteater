package com.anteater.memberservice.profile.service;

import com.anteater.memberservice.entity.Member;
import com.anteater.memberservice.exception.UserNotFoundException;
import com.anteater.memberservice.profile.dto.ProfileResponse;
import com.anteater.memberservice.repository.MemberRepository;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final MemberRepository memberRepository;

    public ProfileService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public ProfileResponse getProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return new ProfileResponse(
                member.getUsername(),
                member.getEmail(),
                member.getBio(),
                member.getProfileImg(),
                member.getSubscriptionStatus()
        );
    }

    // 새로 추가한 생성자를 이용하여 회원 생성
    public Member createMember(String email, String username, String password, String bio, String profileImg) {
        Member member = new Member(email, username, password, bio, profileImg);
        return memberRepository.save(member);
    }
}