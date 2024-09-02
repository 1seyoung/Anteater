package com.anteater.apigateway.filter;

import com.anteater.apigateway.jwt.JwtUtil;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

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
            log.debug("AccessTokenFilter: Processing request to {}", request.getPath());

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                log.warn("AccessTokenFilter: No Authorization header");
                return onError(exchange, "No Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            log.debug("AccessTokenFilter: Token received: {}", token);

            try {
                String username = jwtUtil.getUsernameFromToken(token.substring(7));
                log.debug("AccessTokenFilter: Username extracted: {}", username);

                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-Auth-Username", username)
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } catch (Exception e) {
                log.error("AccessTokenFilter: Error processing token", e);
                return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
            }
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
