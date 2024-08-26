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


import java.util.List;


@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();
            logger.debug("path: {}", path);

            if (config.getPublicPaths().stream().anyMatch(path::startsWith)) {
                // 1. public path 일 경우
                logger.debug("public path : {}", path);
                return chain.filter(exchange);
            }
            String token = extractToken(exchange);
            if (token == null) {
                // 2. 토큰이 없는 경우
                logger.warn("Missing token for path: {}", path);
                return onError(exchange, "Missing token", HttpStatus.UNAUTHORIZED);
            }


            if (path.startsWith("/api/auth/logout")) {
                // 3. 로그아웃 요청인 경우
                logger.debug("Logout request detected");
                return handleLogout(token, exchange, chain);
            }

            //모든 조건을 넘어간 경우 -> 토큰 검증
            return validateAndProcessToken(token, exchange, chain);
        };
    }

    private String extractToken(ServerWebExchange exchange) {
        // Authorization 헤더에서 Bearer Token 추출 -> 토큰이 없으면 null 반환
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        logger.warn("Invalid Authorization header: {}", authHeader);
        return null;
    }

    private Mono<Void> handleLogout(String token, ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        return jwtUtil.blacklistToken(token)
                .then(Mono.fromRunnable(() -> logger.info("Token blacklisted: {}", token)))
                .then(chain.filter(exchange));
    }

    private Mono<Void> validateAndProcessToken(String token, ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        return jwtUtil.isTokenBlacklisted(token)
                .flatMap(isBlacklisted -> {
                    if (isBlacklisted) {
                        logger.warn("Blacklisted token attempted use: {}", token);
                        return onError(exchange, "Token is blacklisted", HttpStatus.UNAUTHORIZED);
                    }
                    if (!jwtUtil.validateToken(token)) {
                        logger.warn("Invalid token: {}", token);
                        return onError(exchange, "Token is invalid or expired", HttpStatus.UNAUTHORIZED);
                    }
                    String username = jwtUtil.getUsernameFromToken(token);
                    if (username == null) {
                        logger.warn("Unable to extract username from token: {}", token);
                        return onError(exchange, "Unable to extract username from token", HttpStatus.UNAUTHORIZED);
                    }
                    logger.debug("Token validated for user: {}", username);
                    ServerHttpRequest request = exchange.getRequest().mutate()
                            .header("X-User-Name", username)
                            .build();
                    return chain.filter(exchange.mutate().request(request).build());
                });
    }


    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        logger.error("Authentication error: {}", err);
        return response.setComplete();
    }

    @Data
    public static class Config {
        private List<String> publicPaths;
    }
}
