package com.anteater.apigateway.config;

import com.anteater.apigateway.jwt.JwtAuthFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {
    private final JwtAuthFilter jwtAuthFilter;

    public RouteConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("member_service", r -> r.path("/api/auth/**", "/api/members/**")
                        .filters(f -> f.filter(jwtAuthFilter.apply(new JwtAuthFilter.Config())))
                        .uri("lb://member-service"))
                // 다른 서비스들에 대한 라우트 추가
                .build();
    }
}
    /*
    uri() 메서드에 정의된 대상으로 요청이 전달
    lb://는 로드 밸런싱을 의미하며, 뒤에 오는 이름은 Eureka에 등록된 서비스 이름
     */