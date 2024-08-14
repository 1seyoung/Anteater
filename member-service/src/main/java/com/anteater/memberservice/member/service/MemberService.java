package com.anteater.memberservice.member.service;

import com.anteater.memberservice.member.dto.request.PasswordChangeRequest;
import com.anteater.memberservice.member.dto.request.ProfileUpdateRequest;
import com.anteater.memberservice.member.dto.response.ActivationResponse;
import com.anteater.memberservice.entity.Member;
import com.anteater.memberservice.entity.SubscriptionStatus;
import com.anteater.memberservice.exception.AccountAlreadyActivatedException;
import com.anteater.memberservice.exception.InvalidTokenException;
import com.anteater.memberservice.exception.UserAlreadyExistsException;
import com.anteater.memberservice.exception.UserNotFoundException;
import com.anteater.memberservice.member.dto.request.RegisterRequest;
import com.anteater.memberservice.member.dto.response.PasswordChangeResponse;
import com.anteater.memberservice.member.dto.response.RegisterResponse;
import com.anteater.memberservice.member.dto.response.ResendActivationResponse;
import com.anteater.memberservice.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value; // 설정 파일에서 값을 가져오기 위한 어노테이션 line146(8.13)

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.activation-base-url}")
    private String activationBaseUrl;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder,
                         EmailService emailService) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public RegisterResponse register(RegisterRequest request) {
        if (memberRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException("Username already exists");
        }
        if (memberRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        Member newMember = new Member(
                request.email(),
                request.username(),
                passwordEncoder.encode(request.password())
        );

        String activationToken = generateActivationToken();
        newMember.setActivationToken(activationToken);
        newMember.setTokenExpiryDate(LocalDateTime.now().plusHours(24)); // 24시간 유효

        Member savedMember = memberRepository.save(newMember);

        String activationLink = activationBaseUrl + activationToken;
        emailService.sendActivationEmail(savedMember.getEmail(), activationLink);

        return new RegisterResponse("Registration successful. Please check your email to activate your account.",
                savedMember.getId().toString(),
                savedMember.getSubscriptionStatus().toString(),
                true);
    }

    public ActivationResponse activateAccount(String token) {
        Member member = memberRepository.findByActivationToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid activation token"));

        if (member.getTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Activation token has expired");
        }

        if (member.getSubscriptionStatus() == SubscriptionStatus.UNACTIVATED) {
            member.setSubscriptionStatus(SubscriptionStatus.BASIC);
            member.setActivationToken(null);
            member.setTokenExpiryDate(null);
            memberRepository.save(member);
        }

        return new ActivationResponse(
                "Account successfully activated",
                member.getId().toString(),
                member.getSubscriptionStatus().name()
        );
    }

    public ResendActivationResponse resendActivationEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (member.getSubscriptionStatus() != SubscriptionStatus.UNACTIVATED) {
            throw new AccountAlreadyActivatedException("Account is already activated");
        }

        String activationToken = generateActivationToken();
        member.setActivationToken(activationToken);
        member.setTokenExpiryDate(LocalDateTime.now().plusHours(24));
        memberRepository.save(member);

        String activationLink = activationBaseUrl + "?token=" + activationToken;
        emailService.sendActivationEmail(member.getEmail(), activationLink);

        return new ResendActivationResponse("Activation email has been resent", true);
    }

    public Member updateMemberInfo(Long memberId, MemberUpdateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (request.username() != null) {
            member.setUsername(request.username());
        }
        if (request.email() != null) {
            member.setEmail(request.email());
        }
        // Add more fields as needed

        return memberRepository.save(member);
    }

    public PasswordChangeResponse changePassword(Long memberId, PasswordChangeRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.oldPassword(), member.getPassword())) {
            throw new InvalidCredentialsException("Invalid old password");
        }

        member.setPassword(passwordEncoder.encode(request.newPassword()));
        memberRepository.save(member);

        return new PasswordChangeResponse("Password changed successfully");
    }

    public ProfileResponse updateProfile(Long memberId, ProfileUpdateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (request.bio() != null) {
            member.setBio(request.bio());
        }
        if (request.profileImg() != null) {
            member.setProfileImg(request.profileImg());
        }
        // Add more profile-specific fields as needed

        Member updatedMember = memberRepository.save(member);

        return new ProfileResponse(
                updatedMember.getUsername(),
                updatedMember.getEmail(),
                updatedMember.getBio(),
                updatedMember.getProfileImg(),
                updatedMember.getSubscriptionStatus()
        );
    }

    private String generateActivationToken() {
        return UUID.randomUUID().toString();
    }
}