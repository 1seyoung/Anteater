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

/**
 * JWT 토큰 생성 및 검증을 위한 유틸리티 클래스
 * Access Token과 Refresh Token의 생성, 검증, 블랙리스트 관리 등을 수행함
 */
@Component
public class JwtTokenProvider {

    // Logger logger : 로깅을 위한 SLF4J 로거 객체
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final JwtProperties jwtProperties;

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    /**
     * JwtTokenProvider 생성자.
     * @param jwtProperties JWT 관련 설정을 포함하는 객체
     * @param redisTemplate Redis 연산을 위한 리액티브 템플릿 객체
     */
    public JwtTokenProvider(JwtProperties jwtProperties, ReactiveRedisTemplate<String, String> redisTemplate) {
        this.jwtProperties = jwtProperties;
        this.redisTemplate = redisTemplate;
    }

    //SecretKey key : 비밀키를 저장하는 객체, HMA-SHA 알고리즘을 사용하여 생성된 서명 키, JWT 토큰 서명하고 검증한는 것에 사용
    private SecretKey key;

    //@PostConstruct : 객체가 생성된 후 초기화 작업을 수행하는 메서드, bean lifecycle에서 오직 한 번만 수행된다는 것을 보장, 생성자가 필요하다면 @PostConstruct를 사용하면 될 듯?
    // 이 어노테이션이 붙은 메서드는 Spring 컨테이너가 Bean을 초기화할 때 자동으로 호출
    /**
     * 객체 생성 후 SecretKey를 초기화
     */
    @PostConstruct
    protected void init() { //protected : 접근제어자, 같은 패키지 내의 다른 클래스나 이 클래스를 상속받은 클래스에서만 접근 가능
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
        //this.key : 이 클래스의 인스턴스 필드인 key에 값 할당 (this : 객체 자신을 가리키는 참조 변수)
        //Keys -> io.jsonwebtoken.security.Keys JJWT 라이브러리에서 제공하는 유틸리티 클래스, 보안 키를 생성하는데 사용
        //hmacShaKeyFor : 주어진 바잍트 배열을 사용하여 HmacSHA 알고리즘을 사용하는 보안키(비밀키)를 생성
        //secretKey.getBytes() : secretKey 문자열 형태로 저장되어 있는 비밀키(24Line) , getBytes() : 문자열을 바이트 배열로 변환
            // HMAC-SHA 알고리즘은 바이트 배열로 입력받기 때문에, 문자열로 되어 있는 비밀 키를 바이트 배열로 변환해야함
            //hmacShaKeyFor 메서드는 HMAC-SHA256 알고리즘을 위한 서명 키(SecretKey)를 생성하고, 이를 this.key 필드에 할당
        //HMAC-SHA256 : 대칭 키 암호화 알고리즘, 같은 키로 서명과 검증을 모두 수행할 수 있음
    }

    /**
     * 사용자 ID를 기반으로 Access Token을 생성
     * @param userId 사용자 ID
     * @return 생성된 Access Token을 포함한 Mono
     */    public Mono<String> generateAccessToken(String userId) {
        return Mono.just(generateToken(userId, jwtProperties.getExpiration()));
    }
    /**
     * 사용자 ID를 기반으로 Refresh Token을 생성하고 Redis에 저장
     * @param userId 사용자 ID
     * @return 생성된 Refresh Token을 포함한 Mono
     */
    public Mono<String> generateRefreshToken(String userId) {
        String refreshToken = generateToken(userId, jwtProperties.getRefreshTokenExpiration());
        return storeRefreshToken(userId, refreshToken).thenReturn(refreshToken);
    }
    /**
     * 주어진 사용자 ID와 만료 시간으로 JWT 토큰을 생성
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
     * Refresh Token을 Redis에 저장
     * @param userId 사용자 ID
     * @param refreshToken Refresh Token
     * @return 저장 성공 여부를 나타내는 Mono<Boolean>
     */
    private Mono<Boolean> storeRefreshToken(String userId, String refreshToken) {
        return redisTemplate.opsForValue().set(
                "refresh_token:" + userId,
                refreshToken,
                Duration.ofMillis(jwtProperties.getRefreshTokenExpiration())
        );
    }
    /**
     * 주어진 토큰의 유효성을 검사
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
     * 토큰에서 사용자 ID를 추출
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
     * 토큰이 만료되었는지 확인
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
     * Refresh Token의 유효성을 검사
     * @param userId 사용자 ID
     * @param refreshToken 검사할 Refresh Token
     * @return Refresh Token이 유효하면 true, 그렇지 않으면 false를 포함한 Mono
     */
    public Mono<Boolean> validateRefreshToken(String userId, String refreshToken) {
        return redisTemplate.opsForValue().get("refresh_token:" + userId)
                .flatMap(storedToken -> {
                    if (refreshToken.equals(storedToken)) {
                        return validateToken(refreshToken);
                    } else {
                        return Mono.just(false);
                    }
                })
                .defaultIfEmpty(false);
    }
    /**
     * 주어진 토큰을 블랙리스트에 추가
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
     * 주어진 토큰이 블랙리스트에 있는지 확인
     * @param token 확인할 토큰
     * @return 토큰이 블랙리스트에 있으면 true, 그렇지 않으면 false를 포함한 Mono
     */
    public Mono<Boolean> isTokenBlacklisted(String token) {
        return redisTemplate.hasKey("blacklist:" + token);
    }
    /**
     * 토큰의 남은 유효 시간을 밀리초 단위로 반환
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
     * 사용자의 구독 정보를 Redis에 저장
     * @param userId 사용자 ID
     * @param subscriptionInfo 구독 정보
     * @return 저장 성공 여부를 나타내는 Mono<Boolean>
     */
    public Mono<Boolean> storeSubscriptionInfo(String userId, String subscriptionInfo) {
        return redisTemplate.opsForValue().set("subscription:" + userId, subscriptionInfo);
    }
    /**
     * 사용자의 구독 정보를 Redis에서 조회
     * @param userId 사용자 ID
     * @return 구독 정보를 포함한 Mono<String>
     */
    public Mono<String> getSubscriptionInfo(String userId) {
        return redisTemplate.opsForValue().get("subscription:" + userId);
    }
}