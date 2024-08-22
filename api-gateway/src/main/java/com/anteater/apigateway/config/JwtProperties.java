package com.anteater.apigateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 관련 설정을 관리하는 클래스
 * application.yml 또는 application.properties 파일의 'jwt' 접두사를 가진 속성들을 자동으로 매핑
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT 서명에 사용되는 비밀 키
     * application.yml에서 'jwt.secret' 속성으로 설정
     */
    private String secret;

    /**
     * 액세스 토큰의 만료 시간(밀리초)
     * application.yml에서 'jwt.expiration' 속성으로 설정
     */
    private long expiration;


    // Getter와 Setter 메서드
    // 이 메서드들을 사용하여 application.yml의 값을 자동으로 주입


    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

}
