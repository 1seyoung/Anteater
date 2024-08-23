package com.anteater.memberservice.common.config;


import com.anteater.memberservice.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//JwtUtil 빈을 생성하기 위한 설정 클래스
//JwtConfig 클래스는 JWT 설정을 관리하고, 이를 통해 JwtUtil 인스턴스를 생성
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil(secret, expiration);
    }
}