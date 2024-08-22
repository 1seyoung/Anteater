package com.anteater.apigateway.jwt;

import com.anteater.apigateway.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.time.Duration;

/*
JWT 관련 유틸리티
JWT 토큰 검증 및 파싱 로직을 제공
JwtUtil 클래스는 JWT 토큰과 관련된 여러 유틸리티 메서드를 제공함
- 토큰 유효성 검증, 토큰에서 클레임 정보 추출
 */
@Component
public class JwtUtil {

    private final Key key; //JWT 토큰의 서명을 검증하는데 사용되는 비밀 키 저장, HMAC SHA 알고리즘 사용
    private final long expiration;
    private final ReactiveRedisTemplate<String, String> redisTemplate;


    public JwtUtil(
            JwtProperties jwtProperties,
            @Qualifier("apiGatewayRedisTemplate") ReactiveRedisTemplate<String, String> redisTemplate
    ) {
        //@Value 어노테이션을 사용하여 application.properties 파일에 정의된 jwt.secret 값을 주입받음
        // secret : JWT 서명을 생성하고 검증하는 데 사용되는 비밀 키
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
        this.expiration = jwtProperties.getExpiration();
        this.redisTemplate = redisTemplate;

        // secret 문자열을 바이트 배열로 반환하고, 이를 사용하여 HMAC SHA 알고리즘을 사용하는 키 생성, 이 키는 JWT 서명을 생성하고 검증하는데 사용
    }

    // 역할 : 메서드는 주어진 JWT 토큰이 유효한지 검사
    public boolean validateToken(String token) {
        try{
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJwt(token);
            // Jwts.parserBuilder() : JWT 토큰을 파싱하는 빌더 객체 생성
            // setSigningKey(key) : JWT 토큰의 서명을 검증하는데 사용되는 비밀 키 설정
            // build() : 설정된 키를 사용하여 JWT 토큰을 파싱하는 파서 객체 생성
            // parseClaimsJwt(token) : 주어진 JWT 토큰을 파싱하고, 토큰의 클레임 정보를 반환
            // -> 토큰 파싱에 성공하면 true 반환, 실패하면 예외 발생
            return true;
        } catch (Exception e){
            return false;
        }
    }

    // 역할 : 메서드는 주어진 JWT 토큰에서 클레임 정보를 추출
    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        // Jwts.parserBuilder() : JWT 토큰을 파싱하는 빌더 객체 생성
        // setSigningKey(key) : JWT 토큰의 서명을 검증하는데 사용되는 비밀 키 설정
        // build() : 설정된 키를 사용하여 JWT 토큰을 파싱하는 파서 객체 생성
        // parseClaimsJws(token) : 주어진 JWT 토큰을 파싱하고, 토큰의 클레임 정보를 반환
        // .getBody(): 파싱된 토큰의 클레임 정보를 반환
         }


     // Blacklist : Redis에 토큰을 저장하여 만료된 토큰을 관리하는 방식
    public Mono<Boolean> isTokenBlacklisted(String token) {
        return redisTemplate.opsForValue().get("blacklist:" + token)
                .map(value -> true)
                .defaultIfEmpty(false);
    }

    public Mono<Boolean> blacklistToken(String token) {
        return redisTemplate.opsForValue().set("blacklist:" + token, "true", Duration.ofMillis(expiration));

    }


}
