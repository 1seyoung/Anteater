package com.anteater.apigateway.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;

    // Getter
    public String getSecret() {
        return secret;
    }

    // Setter
    public void setSecret(String secret) {
        this.secret = secret;
    }
}