package com.anteater.memberservice.auth.service;

import com.anteater.memberservice.auth.exception.InvalidTokenException;
import com.anteater.memberservice.config.JwtConfig;
import io.jsonwebtoken.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {

    private final JwtConfig jwtConfig;
    private final RedisTemplate<String, String> redisTemplate;

    public TokenService(JwtConfig jwtConfig, RedisTemplate<String, String> redisTemplate) {
        this.jwtConfig = jwtConfig;
        this.redisTemplate = redisTemplate;
    }

    public String generateAccessToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getExpiration());

        return Jwts.builder()
                .setSubject(Long.toString(userId))
                .claim("username", username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtConfig.getSecret())
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getRefreshTokenExpiration());

        String refreshToken = Jwts.builder()
                .setSubject(Long.toString(userId))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtConfig.getSecret())
                .compact();

        // Redis에 Refresh Token 저장
        redisTemplate.opsForValue().set(
                "refresh_token:" + userId,
                refreshToken,
                jwtConfig.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );

        return refreshToken;
    }

    public Long validateAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtConfig.getSecret())
                    .parseClaimsJws(token)
                    .getBody();

            return Long.parseLong(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("Invalid JWT token");
        }
    }

    public Long validateRefreshToken(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(jwtConfig.getSecret().getBytes())
                    .build()
                    .parseClaimsJws(token);

            Long userId = Long.parseLong(claimsJws.getBody().getSubject());
            String storedToken = redisTemplate.opsForValue().get("refresh_token:" + userId);

            if (!token.equals(storedToken)) {
                throw new InvalidTokenException("Refresh token not found or not matched");
            }

            return userId;
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("Invalid JWT token");
        }
    }

    public void revokeRefreshToken(String token) {
        Long userId = validateRefreshToken(token);
        redisTemplate.delete("refresh_token:" + userId);
    }

    public void revokeAllRefreshTokens(Long userId) {
        redisTemplate.delete("refresh_token:" + userId);
    }
    public String generateActivationToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 24 * 60 * 60 * 1000); // 24시간 유효

        return Jwts.builder()
                .setSubject(Long.toString(userId))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtConfig.getSecret())
                .compact();
    }

    public Long validateActivationToken(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(jwtConfig.getSecret().getBytes())
                    .build()
                    .parseClaimsJws(token);

            Claims claims = claimsJws.getBody();
            return Long.parseLong(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("Invalid JWT token");
        }
    }

}