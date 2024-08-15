package com.anteater.memberservice.profile.service;

import com.anteater.memberservice.entity.Member;
import com.anteater.memberservice.exception.MemberNotFoundException;
import com.anteater.memberservice.exception.UserNotFoundException;
import com.anteater.memberservice.member.dto.response.ProfileResponse;
import com.anteater.memberservice.profile.dto.ProfileDTO;
import com.anteater.memberservice.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final MemberRepository memberRepository;

    public ProfileService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public ProfileDTO getProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found"));

        return new ProfileDTO(
                member.getUsername(),
                member.getEmail(),
                member.getBio(),
                member.getProfileImage(),
                member.isSubscribed()
        );
    }
}