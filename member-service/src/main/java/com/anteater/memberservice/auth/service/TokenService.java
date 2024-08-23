package com.anteater.memberservice.auth.service;

import com.anteater.memberservice.auth.dto.TokenPair;
import com.anteater.memberservice.common.config.JwtConfig;
import com.anteater.memberservice.common.entity.Member;
import com.anteater.memberservice.common.exception.InvalidRefreshTokenException;
import com.anteater.memberservice.common.repository.MemberRepository;
import com.anteater.memberservice.common.util.JwtUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {


    //final 을 왜 쓰는건지 생각해볼 것
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final JwtConfig jwtConfig;


    //생성자를 꼭 정의해야하는 이유? 항상?
    public TokenService(RedisTemplate<String, String> redisTemplate, JwtUtil jwtUtil, MemberRepository memberRepository, JwtConfig jwtConfig) {
        this.redisTemplate = redisTemplate;
        this.jwtUtil = jwtUtil;
        this.memberRepository = memberRepository;
        this.jwtConfig = jwtConfig;
    }

    /*
    refreshToken을 생성하는 메소드

    TokenPair -> accessToken, refreshToken 두 가지를 함께 생성한다는 의미
    username -> 사용자 이름
    userId -> 사용자 아이디
    claims -> 토큰에 추가될 정보

    최초 로그인 성공 시 메소드를 호출하여 토큰을 생성 / 토큰 갱신 시에도 사용
     */


    public TokenPair generateTokenPair(String username, Long userId, Map<String, Object> claims)  {
        //TODO : 추후 수정 필요 --> 토큰 버전을 통해 토큰의 유효성을 검증하는 로직 추가 -> 그렇다면 저장없이도..!
        String accessToken = generateAccessToken(username,claims);
        String refreshToken = generateAndStoreRefreshToken(userId);
        return new TokenPair(accessToken, refreshToken);
    }

    //의도 : 리프레시 토큰을 이용해 새로운 엑세스 토큰을 발급할 때 사용
    public String generateAccessToken(String username, Map<String, Object> claims) {
        return jwtUtil.generateToken(username, claims);
    }

    //의도 : 새로운 리프레시 토큰을 생성하고 Redis에 저장 (유효 기간은 7일로) 유저 아이디만 주면 알아서...?
    public String generateAndStoreRefreshToken(Long userId) {
        String refreshToken = UUID.randomUUID().toString();
        storeRefreshToken(userId, refreshToken); //7일
        return refreshToken;
    }

    //의도 : 토큰을 저장하는 부분 -> generateAndStoreRefreshToken 과 연결되어 있음(유효기간을 받아서 저장을 이 메서드가 책임)
    public void storeRefreshToken(Long userId, String refreshToken) {
        String key = "refresh_token:" + refreshToken; //refresh_token:UUID
        String value = userId.toString();

        redisTemplate.opsForValue().set(key, value,jwtUtil.getExpiration(), TimeUnit.MILLISECONDS); //TimeUnit.MILLISECONDS :

    }

    // 의도: 액세스 토큰 만료 시 리프레시 토큰으로 새 액세스 토큰 발급 --> AccessToken을 갱신하는 메소드 (요청은 어디서? 어떻게?)
    public String refreshAccessToken(String refreshToken) throws InvalidRefreshTokenException {
        //validateAndGetUserIdFromRefreshToken - > 리프레시 토큰의 유효성을 검사하고 해당 사용자 ID 반환
        Long userId = validateAndGetUserIdFromRefreshToken(refreshToken); //변수 이름이 너무 길다
        if (userId == null) {
            throw new InvalidRefreshTokenException("Invalid or expired refresh token");
        }

        Member member = memberRepository.findById(userId)
                .orElseThrow(()-> new UsernameNotFoundException("User not found"));; //orElseThrow() -> 값이 없을 때 예외를 던짐

        Map<String, Object> claims = new HashMap<>(); //왜 HashMap을 쓰는지 생각해볼 것?
        claims.put("role", member.getRole());
        claims.put("isSubscribed", member.isSubscribed()); // 담아둬야 나중에 리소스 접근할 때 사용할 수 있음

        return generateAccessToken(member.getUsername(), claims); // 정보를 전달해서 액세스 토큰 만들기

    }

    // 의도: 리프레시 토큰의 유효성을 검사하고 해당 사용자 ID 반환
    private Long validateAndGetUserIdFromRefreshToken(String refreshToken) {
        String key = "refresh_token:" + refreshToken;
        String value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return null;
        }

        String[] parts = value.split(":");
        if (parts.length != 2) {
            return null;
        }

        Long userId = Long.parseLong(parts[0]);
        long expirationTime = Long.parseLong(parts[1]);

        if (System.currentTimeMillis() > expirationTime) {
            redisTemplate.delete(key);
            return null;
        }

        return userId;

    }


    // 리프레시 토큰 폐기 --> TODO : 로그 아웃 기능 구현 시 다시 검토해야함
    // 의도: 로그아웃 시 리프레시 토큰을 무효화
    public void revokeRefreshToken(String refreshToken) {
        String key = "refresh_token:" + refreshToken;
        redisTemplate.delete(key);
    }





    }