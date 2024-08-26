package com.anteater.apigateway.jwt;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Qualifier;

import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import com.anteater.apigateway.jwt.JwtProperties;



import java.security.Key;
import java.time.Duration;
import java.util.Date;

@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private static final String BLACKLIST_PREFIX = "blacklist:";
    private final Key key;
    private final ReactiveRedisTemplate<String, String> redisTemplate;


    public JwtUtil(JwtProperties jwtProperties,
                   @Qualifier("apiGatewayRedisTemplate") ReactiveRedisTemplate<String, String> redisTemplate) {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
        this.redisTemplate = redisTemplate;
    }

    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (JwtException e) {
            logger.error("Error extracting username from token", e);
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }


    public Mono<Boolean> isTokenBlacklisted(String token) {
        return redisTemplate.opsForValue().get(BLACKLIST_PREFIX + token)
                .map(value -> true)
                .defaultIfEmpty(false);
    }



    public Mono<Boolean> blacklistToken(String token) {
        return Mono.fromCallable(() -> Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token))
                .map(jws -> {
                    Date expiration = jws.getBody().getExpiration();
                    long ttl = Math.max(0, expiration.getTime() - System.currentTimeMillis());
                    logger.info("Blacklisting token with TTL: {} ms", ttl);
                    return redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, "true", Duration.ofMillis(ttl));
                })
                .flatMap(result -> result)
                .onErrorResume(e -> {
                    logger.error("Error blacklisting token", e);
                    return Mono.just(false);
                });
    }


}
