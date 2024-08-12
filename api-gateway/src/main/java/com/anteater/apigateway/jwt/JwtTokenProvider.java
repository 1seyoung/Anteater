package com.anteater.apigateway.jwt;

import com.anteater.apigateway.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 토큰 생성, 검증 및 관리를 위한 유틸리티 클래스.
 * Access Token과 Refresh Token의 생성, 검증, 블랙리스트 관리 등을 수행하며,
 * Redis를 사용하여 토큰 및 사용자 관련 정보를 저장 및 관리합니다.
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final JwtProperties jwtProperties;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private SecretKey key;

    /**
     * JwtTokenProvider 생성자.
     * @param jwtProperties JWT 관련 설정을 포함하는 객체
     * @param redisTemplate Redis 연산을 위한 리액티브 템플릿 객체
     */
    public JwtTokenProvider(JwtProperties jwtProperties, ReactiveRedisTemplate<String, String> redisTemplate) {
        this.jwtProperties = jwtProperties;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 객체 생성 후 SecretKey를 초기화합니다.
     */
    @PostConstruct
    protected void init() {
        // HMAC-SHA256 알고리즘을 위한 서명 키 생성
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    /**
     * 사용자 ID를 기반으로 Access Token을 생성합니다.
     * @param userId 사용자 ID
     * @return 생성된 Access Token을 포함한 Mono
     */
    public Mono<String> generateAccessToken(String userId) {
        return Mono.just(generateToken(userId, jwtProperties.getExpiration()));
    }

    /**
     * 사용자 ID를 기반으로 Refresh Token을 생성하고 Redis에 저장합니다.
     * @param userId 사용자 ID
     * @return 생성된 Refresh Token을 포함한 Mono
     */
    public Mono<String> generateRefreshToken(String userId) {
        String refreshToken = generateToken(userId, jwtProperties.getRefreshTokenExpiration());
        return storeTokens(userId, refreshToken, null).thenReturn(refreshToken);
    }

    /**
     * 주어진 사용자 ID와 만료 시간으로 JWT 토큰을 생성합니다.
     * @param userId 사용자 ID
     * @param expirationTime 토큰 만료 시간 (밀리초)
     * @return 생성된 JWT 토큰
     */
    private String generateToken(String userId, long expirationTime) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 사용자의 토큰 정보를 Redis에 저장합니다.
     * @param userId 사용자 ID
     * @param refreshToken Refresh Token (null일 수 있음)
     * @param accessToken Access Token (null일 수 있음)
     * @return 저장 작업의 완료를 나타내는 Mono<Void>
     */
    public Mono<Void> storeTokens(String userId, String refreshToken, String accessToken) {
        Map<String, String> tokenData = new HashMap<>();
        if (refreshToken != null) {
            tokenData.put("refreshToken", refreshToken);
        }
        if (accessToken != null) {
            tokenData.put("accessToken", accessToken);
        }
        return redisTemplate.opsForHash().putAll("user:" + userId, tokenData).then();
    }

    /**
     * 주어진 토큰의 유효성을 검사합니다.
     * @param token 검사할 토큰
     * @return 토큰이 유효하면 true, 그렇지 않으면 false를 포함한 Mono
     */
    public Mono<Boolean> validateToken(String token) {
        return Mono.fromCallable(() -> {
            try {
                Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
                return true;
            } catch (JwtException | IllegalArgumentException e) {
                logger.error("Invalid JWT token: {}", e.getMessage());
                return false;
            }
        });
    }

    /**
     * 토큰에서 사용자 ID를 추출합니다.
     * @param token JWT 토큰
     * @return 토큰에서 추출한 사용자 ID를 포함한 Mono
     */
    public Mono<String> getUserIdFromToken(String token) {
        return Mono.fromCallable(() ->
                Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody()
                        .getSubject()
        );
    }

    /**
     * 토큰이 만료되었는지 확인합니다.
     * @param token 검사할 토큰
     * @return 토큰이 만료되었으면 true, 그렇지 않으면 false를 포함한 Mono
     */
    public Mono<Boolean> isTokenExpired(String token) {
        return Mono.fromCallable(() -> {
            try {
                Date expiration = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody()
                        .getExpiration();
                return expiration.before(new Date());
            } catch (ExpiredJwtException e) {
                return true;
            }
        });
    }

    /**
     * Refresh Token의 유효성을 검사합니다.
     * @param userId 사용자 ID
     * @param refreshToken 검사할 Refresh Token
     * @return Refresh Token이 유효하면 true, 그렇지 않으면 false를 포함한 Mono
     */
    public Mono<Boolean> validateRefreshToken(String userId, String refreshToken) {
        return redisTemplate.opsForHash().get("user:" + userId, "refreshToken")
                .map(storedToken -> refreshToken.equals(storedToken))
                .flatMap(isEqual -> isEqual ? validateToken(refreshToken) : Mono.just(false))
                .defaultIfEmpty(false);
    }

    /**
     * 주어진 토큰을 블랙리스트에 추가합니다.
     * @param token 블랙리스트에 추가할 토큰
     * @return 작업 완료를 나타내는 Mono<Void>
     */
    public Mono<Void> blacklistToken(String token) {
        return getUserIdFromToken(token)
                .flatMap(userId -> redisTemplate.opsForValue().set(
                        "blacklist:" + token,
                        userId,
                        Duration.ofMillis(getTokenRemainingTimeInMillis(token))
                ))
                .then();
    }

    /**
     * 주어진 토큰이 블랙리스트에 있는지 확인합니다.
     * @param token 확인할 토큰
     * @return 토큰이 블랙리스트에 있으면 true, 그렇지 않으면 false를 포함한 Mono
     */
    public Mono<Boolean> isTokenBlacklisted(String token) {
        return redisTemplate.hasKey("blacklist:" + token);
    }

    /**
     * 토큰의 남은 유효 시간을 밀리초 단위로 반환합니다.
     * @param token 계산할 토큰
     * @return 토큰의 남은 유효 시간 (밀리초)
     */
    private long getTokenRemainingTimeInMillis(String token) {
        try {
            Date expiration = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
            return Math.max(0, expiration.getTime() - System.currentTimeMillis());
        } catch (JwtException e) {
            return 0;
        }
    }

    /**
     * 사용자의 구독 정보를 Redis에 저장합니다.
     * @param userId 사용자 ID
     * @param subscriptionInfo 구독 정보
     * @return 저장 성공 여부를 나타내는 Mono<Boolean>
     */
    public Mono<Boolean> storeSubscriptionInfo(String userId, String subscriptionInfo) {
        return redisTemplate.opsForHash().put("user:" + userId, "subscription", subscriptionInfo);
    }

    /**
     * 사용자의 구독 정보를 Redis에서 조회합니다.
     * @param userId 사용자 ID
     * @return 구독 정보를 포함한 Mono<String>
     */
    public Mono<String> getSubscriptionInfo(String userId) {
        return redisTemplate.opsForHash().get("user:" + userId, "subscription")
                .map(Object::toString);
    }
}