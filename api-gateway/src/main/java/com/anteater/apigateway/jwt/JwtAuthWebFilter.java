package com.anteater.apigateway.jwt;

//전역 인증을 위한 WebFilter

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 주요 로직:
 * 1. 경로 확인: /public/이나 /api/auth/login으로 시작하는 경로는 인증 없이 통과
 * 2. 그 외의 모든 경로에 대해 토큰 검증을 수행
 *
 * 토큰 검증 위치 : 필터 내에서 직접 수행
 * 모든 요청에 대해 일관된 인증 로직 적용
 * 모든 요청에 대한 기본적인 JWT 인증은 JwtAuthWebFilter에서 처리
 */
@Component
public class JwtAuthWebFilter implements WebFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthWebFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain){
        String path = exchange.getRequest().getPath().value();
        //TODO : 링크 관리 추후 수정 필요
        if (path.startsWith("/public/") || path.startsWith("/api/auth/login") || path.startsWith("/api/members/register") || path.startsWith("/api/members/activate") ) { //-> member service 에서 사용하는 api
            return chain.filter(exchange); // 토큰 검증을 수행하지 않음 -> public 이면 토큰 검증을 수행하지 않음, 로그인 api도 토큰 검증을 수행하지 않음 -> 왜? 로그인 api는 토큰이 없는 상태에서 호출되어야 하기 때문
        }

        String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); //왜 7번째부터? -> "Bearer " 를 제외한 토큰 값만 추출하기 위함
            String finalToken = token;
            return jwtUtil.isTokenBlacklisted(token)
                    .flatMap(isBlacklisted -> {
                        if (isBlacklisted) {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        } else if (jwtUtil.validateToken(finalToken)){
                            return chain.filter(exchange);
                        } else {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }
                    });
        }

        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED); //토큰 검증 실패 -> 401 UNAUTHORIZED 응답
        return exchange.getResponse().setComplete(); //토큰 검증 실패 -> 요청 처리 중단 -> 중단하면 뭐가되는거지 ?? -> 401 UNAUTHORIZED 응답을 클라이언트에게 전송

    }
}

/*
JwtAuthWebFilter vs JwtAuthGatewayFilterFactory
결정 기준
    범위: 전체 애플리케이션에 적용될 규칙인가, 특정 서비스에만 적용될 규칙인가?
    일관성: 모든 요청에 대해 동일하게 적용되어야 하는가?
    유연성: 서비스별로 다른 규칙이 필요한가?

 */