package com.anteater.apigateway.config;

import com.anteater.apigateway.jwt.JwtAuthGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration //Spring의 설정 클래스
public class RouteConfig {

    private  final JwtAuthGatewayFilterFactory jwtAuthGatewayFilterFactory;

    public RouteConfig(JwtAuthGatewayFilterFactory jwtAuthGatewayFilterFactory) {
        this.jwtAuthGatewayFilterFactory = jwtAuthGatewayFilterFactory;
    }


    /*
    uri() 메서드에 정의된 대상으로 요청이 전달
    lb://는 로드 밸런싱을 의미하며, 뒤에 오는 이름은 Eureka에 등록된 서비스 이름
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("member_service", r -> r.path("/api/auth/**", "/api/members/**") //member_service 라우트의 경우, JwtAuthGatewayFilterFactory가 적용
                        .filters(f -> f.filter(jwtAuthGatewayFilterFactory.apply(new JwtAuthGatewayFilterFactory.Config())))
                        .uri("lb://member-service"))
                // 다른 서비스들에 대한 라우트 추가

                /* 다른 서비스 예시
                .route("product_service", r -> r.path("/api/products/**")
                    .filters(f -> f.filter(jwtAuthGatewayFilterFactory.apply(new JwtAuthGatewayFilterFactory.Config())))
                    .uri("lb://product-service"))
                .route("order_service", r -> r.path("/api/orders/**")
                    .filters(f -> f.filter(jwtAuthGatewayFilterFactory.apply(new JwtAuthGatewayFilterFactory.Config())))
                    .uri("lb://order-service"))
                 */
                .build();
    }

}
