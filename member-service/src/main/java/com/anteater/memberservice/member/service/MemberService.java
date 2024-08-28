package com.anteater.memberservice.member.service;

import com.anteater.memberservice.common.exception.*;
import com.anteater.memberservice.common.redis.RedisTempStorageService;
import com.anteater.memberservice.common.util.EmailUtil;
import com.anteater.memberservice.common.util.JwtUtil;
import com.anteater.memberservice.member.dto.request.PasswordChangeRequest;
import com.anteater.memberservice.member.dto.request.ProfileRequestUpdateDTO;
import com.anteater.memberservice.member.dto.response.ActivationResponse;
import com.anteater.memberservice.common.entity.Member;
import com.anteater.memberservice.member.dto.request.RegisterRequest;
import com.anteater.memberservice.member.dto.response.PasswordChangeResponse;
import com.anteater.memberservice.member.dto.response.ProfileUpdateResponseDTO;
import com.anteater.memberservice.member.dto.response.RegisterResponse;
import com.anteater.memberservice.common.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final JwtUtil jwtUtil;

    @Autowired
    private EmailUtil emailUtil;
    private final TransactionTemplate transactionTemplate;
    private final RedisTempStorageService<RegisterRequest> redisTempStorageService;

    @Value("${app.activation-base-url}")
    private String activationBaseUrl;


    //의존성 주입을 통해 필요한 서비스들(회원 저장소, 비밀번호 인코더, 이메일 서비스)을 초기화
    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder,
                         EmailUtil emailUtil, TransactionTemplate transactionTemplate, RedisTempStorageService<RegisterRequest> redisTempStorageService , JwtUtil jwtUtil) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailUtil = emailUtil;
        this.transactionTemplate = transactionTemplate;
        this.redisTempStorageService = redisTempStorageService;
        this.jwtUtil = jwtUtil;
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
                newMember.deactivate();  // 이메일 인증 전이므로 비활성화 상태로 설정

                // 3. 이메일 인증 토큰 생성
                String activationToken = generateActivationToken();

                // 4. 임시 저장소에 이메일과 토큰 저장
                redisTempStorageService.save(activationToken, request, 60 * 60 * 24); // 24시간 유효

                // 5. 활성화 링크 생성
                String activationLink = activationBaseUrl + activationToken;

                // 6. 데이터베이스에 회원 정보 저장
                Member savedMember = memberRepository.save(newMember);

                // 7. 활성화 링크 이메일 전송
                emailUtil.sendActivationEmail(savedMember.getEmail(), activationLink);

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


    //public 접근제어자-> 외부접근 가능, ActivationResponse 반환
    @Transactional // 메서드가 트랜잭션 내에서 실행되어야 함을 의미 , 예외 발생 시 롤백
    public ActivationResponse activateAccount(String token) {

        // 1. 임시 저장소에서 토큰으로 회원 정보 조회
        RegisterRequest registrationInfo = redisTempStorageService.get(token);
        if (registrationInfo == null) {
            throw new InvalidTokenException("Invalid or expired activation token");
        }

        Member member = memberRepository.findByEmail(registrationInfo.email())
                .orElseThrow(() -> new MemberNotFoundException("Member not found"));
                // 조건이 만족 되지 않으면 지정된 예외 던짐
        if (!member.isEnabled()) { // 계정이 활성화 되지 않았다면
            member.activate(); // 활성화 시키고
            memberRepository.save(member); // DB 업데이트

            // 임시 저장소에서 토큰 삭제
            redisTempStorageService.remove(token);
        }

        return new ActivationResponse(
                "Account successfully activated",
                member.getId().toString(),
                member.isSubscribed(),
                member.addRole("USER")
        );
    }

    @Transactional
    public PasswordChangeResponse changePassword(String token, PasswordChangeRequest request) {
        String username = extractUsernameFromToken(token);
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberNotFoundException("Member not found"));

        if (!passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }

        member.changePassword(passwordEncoder.encode(request.newPassword()));

        return new PasswordChangeResponse("Password changed successfully");
    }

    public ProfileUpdateResponseDTO updateProfile(String token, ProfileRequestUpdateDTO updateDTO) {
        String username = extractUsernameFromToken(token);
        Member member = memberRepository.findByUsername(username)

                .orElseThrow(() -> new MemberNotFoundException("Member not found"));

        member.updateProfile(
                updateDTO.displayName(),
                updateDTO.bio(),
                updateDTO.profileImage()
        );

        return new ProfileUpdateResponseDTO(
                member.getUsername(),
                member.getDisplayName(),
                member.getEmail(),
                member.getBio(),
                member.getProfileImage(),
                member.getUpdatedAt()
        );
    }


    private String extractUsernameFromToken(String token) {
        String bearerToken = token.replace("Bearer ", "");
        return jwtUtil.extractUsername(bearerToken);
    }
    private String generateActivationToken() {
        return UUID.randomUUID().toString();
    }


}