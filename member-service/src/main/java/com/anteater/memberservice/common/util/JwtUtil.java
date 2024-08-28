package com.anteater.memberservice.common.util;


import com.anteater.memberservice.common.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;


import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

import java.util.Date;


public class JwtUtil {

    private final Key secretKey;
    @Getter
    private final Long expiration;



    public JwtUtil(String secret, Long expiration) {
        this.secretKey = new SecretKeySpec(secret.getBytes(), SignatureAlgorithm.HS512.getJcaName());
        this.expiration = expiration;
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username) //ID
                .setIssuedAt(new Date(System.currentTimeMillis())) //발행일자
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // 만료일자 (1000을 곱하지 않음)
                .signWith(secretKey)
                .compact();
    }

    public String extractUsername(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException e) {
            throw new InvalidTokenException("Failed to extract username from token");
        }
    }

    // validateToken 메서드 수정
    public boolean validateToken(String token) {
        try {
            extractUsername(token);
            return true;
        } catch (InvalidTokenException e) {
            return false;
        }
    }
    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}