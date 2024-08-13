package com.anteater.apigateway.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtFilter implements WebFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    private final JwtTokenProvider tokenProvider;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final String[] EXCLUDED_PATHS = {"/auth/login", "/auth/signup", "/public/**"};

    public JwtFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (isExcludedPath(path)) {
            return chain.filter(exchange);
        }

        String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return validateAndProcessToken(token, exchange, chain);
        }

        return chain.filter(exchange);
    }

    private Mono<Void> validateAndProcessToken(String token, ServerWebExchange exchange, WebFilterChain chain) {
        return Mono.just(token)
                .flatMap(tokenProvider::validateToken)
                .flatMap(valid -> {
                    if (valid) {
                        return tokenProvider.getUserIdFromToken(token)
                                .flatMap(userId -> attachUserInfo(exchange, chain, userId, token));
                    } else {
                        return Mono.error(new JwtException("Invalid token"));
                    }
                })
                .onErrorResume(ExpiredJwtException.class, e -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                })
                .onErrorResume(JwtException.class, e -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }

    private Mono<Void> attachUserInfo(ServerWebExchange exchange, WebFilterChain chain, String userId, String token) {
        return tokenProvider.getSubscriptionFromToken(token)
                .flatMap(subscriptionInfo -> {
                    ServerWebExchange mutatedExchange = exchange.mutate()
                            .request(exchange.getRequest().mutate()
                                    .header("X-User-Id", userId)
                                    .header("X-Subscription-Info", subscriptionInfo)
                                    .build())
                            .build();

                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    if ("PREMIUM".equals(subscriptionInfo)) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_PREMIUM"));
                    }

                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            userId, null, authorities
                    );

                    return chain.filter(mutatedExchange)
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
                });
    }

    private boolean isExcludedPath(String path) {
        return Arrays.stream(EXCLUDED_PATHS).anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}
/*
Spring Security 는 기본적으로 순서가 있는 Security Filter 들을 제공하고, Spring Security가 제공하는 Filter를 구현한게 아니라면 필터의 순서를 정해줘야 함
 */