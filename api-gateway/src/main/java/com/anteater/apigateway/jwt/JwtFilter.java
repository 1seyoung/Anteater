package com.anteater.apigateway.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.apache.tomcat.util.http.parser.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * JWT 토큰을 검증하고 사용자 인증을 처리하는 WebFilter 구현체.
 * 이 필터는 특정 경로를 제외한 모든 요청에 대해 JWT 토큰을 검증하고,
 * 유효한 토큰에 대해 사용자 인증 정보를 설정합니다.
 */
@Component
public class JwtFilter implements WebFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    private final JwtTokenProvider tokenProvider;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final String[] EXCLUDED_PATHS = {"/auth/login", "/auth/signup", "/public/**"};

    /**
     * JwtFilter 생성자.
     * @param tokenProvider JWT 토큰 관련 작업을 수행하는 provider
     * @param redisTemplate Redis 작업을 위한 reactive template
     */
    public JwtFilter(JwtTokenProvider tokenProvider, ReactiveRedisTemplate<String, String> redisTemplate) {
        this.tokenProvider = tokenProvider;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        logger.debug("Processing request for path: {}", path);

        // 인증이 필요 없는 경로인 경우 다음 필터로 넘김
        if (isExcludedPath(path)) {
            logger.debug("Path {} is excluded from JWT filtering", path);
            return chain.filter(exchange);
        }

        // Authorization 헤더에서 JWT 토큰 추출
        String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return validateAndProcessToken(token, exchange, chain);
        }

        // 토큰이 없는 경우 다음 필터로 넘김
        logger.debug("No JWT token found in request headers");
        return chain.filter(exchange);
    }

    /**
     * 주어진 경로가 인증이 필요 없는 경로인지 확인
     * @param path 확인할 경로
     * @return 인증이 필요 없는 경로이면 true, 그렇지 않으면 false
     */
    private boolean isExcludedPath(String path) {
        return Arrays.stream(EXCLUDED_PATHS)
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * JWT 토큰을 검증하고 처리
     * @param token 검증할 JWT 토큰
     * @param exchange 현재 요청/응답 교환
     * @param chain 필터 체인
     * @return 처리 결과
     */
    private Mono<Void> validateAndProcessToken(String token, ServerWebExchange exchange, WebFilterChain chain) {
        return Mono.just(token)
                .flatMap(tokenProvider::validateToken)
                .flatMap(valid -> {
                    if (valid) {
                        // 토큰이 유효한 경우
                        return tokenProvider.getUserIdFromToken(token)
                                .flatMap(userId -> attachSubscriptionInfo(exchange, chain, userId));
                    } else {
                        // 토큰이 유효하지 않은 경우
                        return Mono.error(new JwtException("Invalid token"));
                    }
                })
                .onErrorResume(ExpiredJwtException.class, e -> {
                    // 토큰이 만료된 경우
                    logger.warn("JWT token has expired: {}", e.getMessage());
                    return refreshToken(exchange, chain, e.getClaims().getSubject());
                })
                .onErrorResume(JwtException.class, e -> {
                    // 기타 JWT 관련 예외 발생 시
                    logger.error("Invalid JWT token: {}", e.getMessage());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }

    /**
     * 사용자 ID와 구독 정보를 요청 헤더에 첨부
     * @param exchange 현재 요청/응답 교환
     * @param chain 필터 체인
     * @param userId 사용자 ID
     * @return 처리 결과
     */
    private Mono<Void> attachSubscriptionInfo(ServerWebExchange exchange, WebFilterChain chain, String userId) {
        return redisTemplate.opsForValue().get("subscription:" + userId)
                .defaultIfEmpty("") // 구독 정보가 없는 경우 빈 문자열 사용
                .flatMap(subscriptionInfo -> {
                    // 사용자 ID와 구독 정보를 헤더에 추가
                    ServerWebExchange mutatedExchange = exchange.mutate()
                            .request(exchange.getRequest().mutate()
                                    .header("X-User-Id", userId)
                                    .header("X-Subscription-Info", subscriptionInfo)
                                    .build())
                            .build();
                    // 인증 컨텍스트 설정 및 다음 필터로 전달
                    return setAuthenticationContext(mutatedExchange, chain, userId);
                });
    }

    /**
     * 인증 컨텍스트를 설정
     * @param exchange 현재 요청/응답 교환
     * @param chain 필터 체인
     * @param userId 사용자 ID
     * @return 처리 결과
     */
    private Mono<Void> setAuthenticationContext(ServerWebExchange exchange, WebFilterChain chain, String userId) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userId, null, new ArrayList<>()
        );
        return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
    }

    /**
     * 만료된 액세스 토큰을 리프레시 토큰을 사용하여 갱신
     * @param exchange 현재 요청/응답 교환
     * @param chain 필터 체인
     * @param userId 사용자 ID
     * @return 처리 결과
     */
    private Mono<Void> refreshToken(ServerWebExchange exchange, WebFilterChain chain, String userId) {
        return redisTemplate.opsForValue().get("refresh_token:" + userId)
                .flatMap(refreshToken -> {
                    if (refreshToken != null) {
                        return tokenProvider.validateToken(refreshToken)
                                .flatMap(isValid -> {
                                    if (isValid) {
                                        // 리프레시 토큰이 유효한 경우 새 액세스 토큰 발급
                                        return tokenProvider.generateAccessToken(userId)
                                                .flatMap(newAccessToken -> {
                                                    // 새 액세스 토큰을 헤더에 추가
                                                    ServerWebExchange mutatedExchange = exchange.mutate()
                                                            .request(exchange.getRequest().mutate()
                                                                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken)
                                                                    .build())
                                                            .build();
                                                    return attachSubscriptionInfo(mutatedExchange, chain, userId);
                                                });
                                    } else {
                                        // 리프레시 토큰이 유효하지 않은 경우
                                        return Mono.error(new JwtException("Invalid refresh token"));
                                    }
                                });
                    } else {
                        // 리프레시 토큰을 찾을 수 없는 경우
                        return Mono.error(new JwtException("Refresh token not found"));
                    }
                })
                .onErrorResume(JwtException.class, e -> {
                    logger.error("Error refreshing token: {}", e.getMessage());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }
}
/*
Spring Security 는 기본적으로 순서가 있는 Security Filter 들을 제공하고, Spring Security가 제공하는 Filter를 구현한게 아니라면 필터의 순서를 정해줘야 함
 */