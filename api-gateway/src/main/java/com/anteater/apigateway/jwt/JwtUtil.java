package com.anteater.apigateway.jwt;

import com.anteater.apigateway.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.time.Duration;
import java.util.Date;

/*
JWT 관련 유틸리티
JWT 토큰 검증 및 파싱 로직을 제공
JwtUtil 클래스는 JWT 토큰과 관련된 여러 유틸리티 메서드를 제공함
- 토큰 유효성 검증, 토큰에서 클레임 정보 추출
 */
@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private final Key key;
    private final long expiration;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public JwtUtil(
            JwtProperties jwtProperties,
            @Qualifier("apiGatewayRedisTemplate") ReactiveRedisTemplate<String, String> redisTemplate
    ) {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
        this.expiration = jwtProperties.getExpiration();
        this.redisTemplate = redisTemplate;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            // TODO: 추가적인 검증 로직 구현 (예: 특정 role 확인)

            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.warn("Unsupported JWT token: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.warn("Malformed JWT token: {}", e.getMessage());
        } catch (SignatureException e) {
            logger.warn("Invalid JWT signature: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public String getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (JwtException e) {
            logger.error("Error extracting userId from token", e);
            return null;
        }
    }


    //블랙리스트 조회 -> 입력된 블랙리스트에 있는지 확인
    public Mono<Boolean> isTokenBlacklisted(String token) {
        return redisTemplate.opsForValue().get("blacklist:" + token)
                .map(value -> true)
                .defaultIfEmpty(false);
    }


}
