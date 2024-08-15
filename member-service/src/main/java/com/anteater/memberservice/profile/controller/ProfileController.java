package com.anteater.memberservice.profile.controller;

import com.anteater.memberservice.member.dto.response.ProfileResponse;
import com.anteater.memberservice.profile.dto.ProfileDTO;
import com.anteater.memberservice.profile.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<ProfileDTO> getProfile(@PathVariable Long memberId) {
        ProfileDTO profile = profileService.getProfile(memberId);
        return ResponseEntity.ok(profile);
    }
}