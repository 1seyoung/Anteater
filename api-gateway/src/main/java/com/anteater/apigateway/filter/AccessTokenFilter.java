package com.anteater.apigateway.filter;

import com.anteater.apigateway.jwt.JwtUtil;
import lombok.Data;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AccessTokenFilter extends AbstractGatewayFilterFactory<AccessTokenFilter.Config> {
    //만료된 AccessToken을 재발급하는 API Gateway Filter
    private final JwtUtil jwtUtil;

    public AccessTokenFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "No Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            } else {
                return onError(exchange, "Invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String username = jwtUtil.getUsernameFromToken(token);
            if (username == null) {
                return onError(exchange, "Unable to extract username from token", HttpStatus.UNAUTHORIZED);
            }

            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-Auth-Username", username)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    @Data
    public static class Config {
        // 필요한 설정이 있다면 여기에 추가
    }
}
