package com.anteater.memberservice.auth.service;

import com.anteater.memberservice.auth.dto.request.LoginRequest;
import com.anteater.memberservice.auth.dto.request.LogoutRequest;
import com.anteater.memberservice.auth.dto.request.RegisterRequest;
import com.anteater.memberservice.auth.dto.response.*;
import com.anteater.memberservice.auth.entity.User;
import com.anteater.memberservice.auth.exception.*;
import com.anteater.memberservice.auth.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Value; // 설정 파일에서 값을 가져오기 위한 어노테이션 line146(8.13)


/**
 * AuthService는 사용자 인증과 관련된 로직을 처리하는 서비스 클래스입니다.
 * 사용자 등록, 로그인, 토큰 갱신 등의 기능을 제공합니다.
 */
@Service
@Transactional // 모든 메서드에 트랜잭션을 적용
public class AuthService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder; // 비밀번호 암호화를 처리하는 PasswordEncoder -> Security Config 없이 했다가 오류 났었음
    private final TokenService tokenService;

    /**
     * AuthService 생성자.
     *
     * @param userRepository   사용자 정보를 관리하는 UserRepository
     * @param emailService     이메일 전송을 처리하는 EmailService
     * @param passwordEncoder  비밀번호 암호화를 처리하는 PasswordEncoder
     * @param tokenService     토큰 생성 및 검증을 처리하는 TokenService
     */
    public AuthService(UserRepository userRepository, EmailService emailService,
                       PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    /**
     * 사용자 등록을 처리합니다.
     * 중복된 사용자명이나 이메일이 존재하는 경우 예외를 발생시킵니다.
     * 사용자가 성공적으로 등록되면, 계정 활성화를 위한 이메일을 발송합니다.
     *
     * @param request 사용자 등록 요청 객체
     * @return RegisterResponse 등록 성공 메시지와 사용자 정보가 포함된 응답 객체
     * @throws UserAlreadyExistsException 사용자명 또는 이메일이 이미 존재하는 경우 발생
     */
    public RegisterResponse register(RegisterRequest request) {
        // 사용자명 중복 검사
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }
        // 이메일 중복 검사
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        // 새로운 사용자 생성
        User newUser = User.createUser(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getName(),
                request.getBirthdate()
        );

        // 사용자 저장
        User savedUser = userRepository.save(newUser);

        // 계정 활성화를 위한 토큰 생성 및 이메일 발송
        String activationToken = tokenService.generateActivationToken(savedUser.getId());
        String activationLink = activationBaseUrl + activationToken;
        emailService.sendActivationEmail(savedUser.getEmail(), activationLink);


        // 등록 성공 응답 반환
        return new RegisterResponse("Registration successful. Please check your email to activate your account.",
                savedUser.getId().toString(),
                savedUser.getSubscriptionStatus().toString(),
                true);
    }
    /**
     * 사용자의 로그인 요청을 처리합니다.
     * 사용자명과 비밀번호를 확인하고, 계정이 활성화된 경우 액세스 및 리프레시 토큰을 반환합니다.
     *
     * @param request 로그인 요청 객체
     * @return LoginResponse 로그인 성공 시 액세스 토큰, 리프레시 토큰 및 구독 상태를 포함한 응답 객체
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우 발생
     * @throws InvalidCredentialsException 비밀번호가 일치하지 않는 경우 발생
     * @throws AccountNotActivatedException 계정이 활성화되지 않은 경우 발생
     */
    public LoginResponse login(LoginRequest request) {
        // 사용자명으로 사용자 검색
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        // 계정 활성화 여부 확인
        if (!user.isActivated()) {
            throw new AccountNotActivatedException("Account is not activated");
        }

        // 액세스 및 리프레시 토큰 생성
        String accessToken = tokenService.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = tokenService.generateRefreshToken(user.getId());

        // 로그인 성공 응답 반환
        return new LoginResponse(accessToken, refreshToken, user.getSubscriptionStatus().toString());
    }


    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰을 생성합니다.
     *
     * @param refreshToken 유효한 리프레시 토큰
     * @return TokenResponse 새로운 액세스 토큰을 포함한 응답 객체
     * @throws InvalidTokenException 리프레시 토큰이 유효하지 않은 경우 발생
     * @throws UserNotFoundException 사용자 정보를 찾을 수 없는 경우 발생
     */
    public TokenResponse refreshToken(String refreshToken) {
        // 리프레시 토큰 검증 및 사용자 ID 추출
        Long userId = tokenService.validateRefreshToken(refreshToken);
        if (userId == null) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        // 사용자 정보 검색
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // 새로운 액세스 토큰 생성
        String newAccessToken = tokenService.generateAccessToken(user.getId(), user.getUsername());
        return new TokenResponse(newAccessToken);
    }

    @Value("${app.activation-base-url}")
    private String activationBaseUrl;


    public ActivationResponse activateAccount(String token) {
        Long userId = tokenService.validateActivationToken(token);
        if (userId == null) {
            throw new InvalidTokenException("Invalid or expired activation token");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.isActivated()) {
            user.activate();
            //userRepository.save(user); -> @Transactional 어노테이션으로 인해 자동으로 저장됨
        }

        return new ActivationResponse(
                "Account successfully activated",
                user.getId().toString(),
                user.getSubscriptionStatus().name()
        );

    }

    /**
     * 로그아웃을 처리합니다.
     *
     * @param request 로그아웃 요청 객체
     * @return LogoutResponse 로그아웃 결과를 포함한 응답 객체
     */
    public LogoutResponse logout(LogoutRequest request) {
        if (request.allDevices()) {
            tokenService.revokeAllRefreshTokens(request.userId());
        } else {
            tokenService.revokeRefreshToken(request.refreshToken());
        }

        return new LogoutResponse("Successfully logged out");
    }

    /**
     * 계정 활성화 이메일을 재전송합니다.
     *
     * @param email 사용자 이메일
     * @return ResendActivationResponse 이메일 재전송 결과를 포함한 응답 객체
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우 발생
     * @throws AccountAlreadyActivatedException 계정이 이미 활성화된 경우 발생
     */
    public ResendActivationResponse resendActivationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.isActivated()) {
            throw new AccountAlreadyActivatedException("Account is already activated");
        }

        String activationToken = tokenService.generateActivationToken(user.getId());
        String activationLink = "http://yourdomain.com/activate?token=" + activationToken;
        emailService.sendActivationEmail(user.getEmail(), activationLink);

        return new ResendActivationResponse("Activation email has been resent", true);
    }
}
