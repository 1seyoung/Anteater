package com.anteater.apigateway.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpHeaders;

import java.util.List;

@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    private final JwtUtil jwtUtil;
    private final List<String> publicPaths;

    public JwtAuthFilter(JwtUtil jwtUtil, @Value("${public.paths}") List<String> publicPaths) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.publicPaths = publicPaths;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // 1. 요청 경로 확인
            String path = exchange.getRequest().getPath().value();
            if (publicPaths.stream().anyMatch(path::startsWith)) {
                // 공개 경로인 경우 인증 없이 통과
                return chain.filter(exchange);
            }

            // 2. 액세스 토큰 추출 및 검증
            String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (token != null && token.startsWith("Bearer ")) {
                final String extractedToken = token.substring(7);
                return jwtUtil.isTokenBlacklisted(token)
                        .flatMap(isBlacklisted -> {
                            if (isBlacklisted) {
                                // 2-1. 블랙리스트에 있는 토큰인 경우
                                return onError(exchange, "Token is blacklisted", HttpStatus.UNAUTHORIZED);
                            } else if (jwtUtil.validateToken(extractedToken)) {
                                String userId = jwtUtil.getUserIdFromToken(extractedToken);
                                ServerHttpRequest request = exchange.getRequest().mutate()
                                        .header("X-User-ID", userId)
                                        .build();
                                // 2-2. 유효한 토큰인 경우
                                return chain.filter(exchange.mutate().request(request).build());
                            } else {
                                // 2-3. 만료된 토큰인 경우
                                return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"));
                            }
                        });
            }

            // 3. 유효한 토큰이 없는 경우
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing token"));
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        return exchange.getResponse().setComplete();
    }

    public static class Config {}
}