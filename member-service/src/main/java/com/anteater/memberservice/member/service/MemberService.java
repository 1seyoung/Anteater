package com.anteater.memberservice.member.service;

import com.anteater.memberservice.exception.*;
import com.anteater.memberservice.member.dto.request.PasswordChangeRequest;
import com.anteater.memberservice.member.dto.request.UpdateProfileRequest;
import com.anteater.memberservice.member.dto.response.ActivationResponse;
import com.anteater.memberservice.entity.Member;
import com.anteater.memberservice.member.dto.request.RegisterRequest;
import com.anteater.memberservice.member.dto.response.PasswordChangeResponse;
import com.anteater.memberservice.member.dto.response.RegisterResponse;
import com.anteater.memberservice.member.dto.response.ProfileResponse;
import com.anteater.memberservice.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value; // 설정 파일에서 값을 가져오기 위한 어노테이션 line146(8.13)
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

@Service
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TransactionTemplate transactionTemplate;
    private final TempStorageService tempStorageService;

    @Value("${app.activation-base-url}")
    private String activationBaseUrl;


    //의존성 주입을 통해 필요한 서비스들(회원 저장소, 비밀번호 인코더, 이메일 서비스)을 초기화
    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder,
                         EmailService emailService, TransactionTemplate transactionTemplate, TempStorageService tempStorageService) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.transactionTemplate = transactionTemplate;
        this.tempStorageService = tempStorageService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // 1. 중복 확인
        if (memberRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException("Username already exists");
        }
        if (memberRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        return transactionTemplate.execute(status -> {
            try {
                // 2. 새 회원 객체 생성
                Member newMember = new Member(
                        request.username(),
                        request.email(),
                        passwordEncoder.encode(request.password())
                );
                newMember.deactivate();;  // 이메일 인증 전이므로 비활성화 상태로 설정

                // 3. 이메일 인증 토큰 생성
                String activationToken = generateActivationToken();

                // 4. 임시 저장소에 등록 정보 저장
                tempStorageService.saveRegistrationInfo(activationToken, request);

                // 5. 활성화 링크 생성
                String activationLink = activationBaseUrl + activationToken;

                // 6. 데이터베이스에 회원 정보 저장
                Member savedMember = memberRepository.save(newMember);

                // 7. 활성화 링크 이메일 전송
                emailService.sendActivationEmail(savedMember.getEmail(), activationLink);

                // 8. 응답 생성 및 반환
                return new RegisterResponse(
                        "Registration successful. Please check your email to activate your account.",
                        savedMember.getId().toString(),
                        savedMember.isSubscribed(),  // 구독 상태
                        savedMember.isEnabled(),     // 계정 활성화 상태
                        true  // 활성화 이메일이 성공적으로 전송되었음을 나타냄
                );
            } catch (Exception e) {
                // 예외 발생 시 트랜잭션 롤백
                status.setRollbackOnly();
                throw new RegistrationFailedException("Registration failed: " + e.getMessage(), e);
            }
        });
    }



    @Transactional
    public ActivationResponse activateAccount(String token) {
        RegisterRequest registrationInfo = tempStorageService.getRegistrationInfo(token);
        if (registrationInfo == null) {
            throw new InvalidTokenException("Invalid or expired activation token");
        }

        Member member = memberRepository.findByEmail(registrationInfo.email())
                .orElseThrow(() -> new MemberNotFoundException("Member not found"));

        if (!member.isEnabled()) {
            member.activate();
            memberRepository.save(member);
            tempStorageService.removeRegistrationInfo(token);
        }

        return new ActivationResponse(
                "Account successfully activated",
                member.getId().toString(),
                member.isSubscribed()
        );
    }

    @Transactional
    public PasswordChangeResponse changePassword(Long memberId, PasswordChangeRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found"));

        if (!passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }

        member.changePassword(passwordEncoder.encode(request.newPassword()));
        memberRepository.save(member);

        return new PasswordChangeResponse("Password changed successfully");
    }


    @Transactional
    public ProfileResponse updateProfile(Long memberId, UpdateProfileRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found"));

        if (request.bio() != null) {
            member.updateBio(request.bio());
        }
        if (request.profileImage() != null) {
            member.updateProfileImage(request.profileImage());
        }

        Member updatedMember = memberRepository.save(member);

        return new ProfileResponse(
                updatedMember.getUsername(),
                updatedMember.getEmail(),
                updatedMember.getBio(),
                updatedMember.getProfileImage(),
                updatedMember.isSubscribed()
        );
    }



    private String generateActivationToken() {
        return UUID.randomUUID().toString();
    }
}