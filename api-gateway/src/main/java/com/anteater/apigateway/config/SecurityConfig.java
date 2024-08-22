package com.anteater.apigateway.config;

import com.anteater.apigateway.jwt.JwtAuthWebFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Spring WebFlux와 Spring Security를 사용하여 웹 애플리케이션의 보안 설정을 정의
 */
@Configuration //Spring의 설정 클래스, 클래스에서 정의된 빈(bean)들이 Spring 컨텍스트에 의해 관리되고, 빈들의 설정을 정의
@EnableWebFluxSecurity // Spring WebFlux에서 보안 기능을 활성화하는 어노테이션, 어노테이션을 사용하면 Spring Security의 기본 보안 기능을 사용할 수 있음
public class SecurityConfig {

    private final JwtAuthWebFilter jwtAuthWebFilter; //JWT 기반의 인증 필터입니다. 이 필터는 요청이 들어올 때 JWT 토큰을 검사하여 사용자를 인증하는 역할

    public SecurityConfig(JwtAuthWebFilter jwtAuthWebFilter) { //SecurityConfig 클래스는 JwtAuthWebFilter를 주입받아 필드에 저장합니다. 이 필터는 이후에 보안 필터 체인에 추가됨
        this.jwtAuthWebFilter = jwtAuthWebFilter;
    }

    @Bean //SecurityWebFilterChain 타입의 빈을 생성, Sprign Context에 등록, 이 빈은 애플리케이션의 보안 필터 체인 정의
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http //Spring Security의 HTTP 보안 설정을 구성하는 데 사용
                .csrf(ServerHttpSecurity.CsrfSpec::disable) //CSRF 보호 비활성화, 일반적인 RESTful API 에서는 CSRF 보호가 필요하지 않음 (csrf : Cross-Site Request Forgery)
                .authorizeExchange(exchanges -> exchanges // 요청 권한 설정
                        .pathMatchers("/public/**", "/api/auth/login","/api/members/register","/api/members/activate").permitAll() //
                        .pathMatchers("/api/auth/logout").authenticated() //로그아웃은 인증된 사용자만 가능
                        .anyExchange().authenticated() //나머지 모든 요청은 인증된 사용자만 가능
                )
                .addFilterAt(jwtAuthWebFilter, SecurityWebFiltersOrder.AUTHENTICATION) //addFilterAt() 메서드를 사용하여 JwtAuthWebFilter를 SecurityWebFiltersOrder.AUTHENTICATION 위치에 추가
                .build();
    }
}