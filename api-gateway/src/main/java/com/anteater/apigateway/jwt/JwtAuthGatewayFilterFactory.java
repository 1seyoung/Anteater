package com.anteater.apigateway.jwt;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;


//특정 라우트에 적용할 GatewayFilter를 정의 -> 특정 라우트 -> ???

/**
 * 특정 라우트에만 적용되는 필터
 * 적용 시점 : 라우팅 규칙이 적용된 후, 해당 서비스로 요청이 전달되기 전에 실행
 *
 * 주요 로직: 특정 라우트에 대해서만 실행
 * 추가적인 토큰 검증이나 요청/응답 수정을 수행할 수 있음
 *
 * /api/auth/login 의 경우 실행되지 않음
 * /api/
 *
 * 특정 라우트(/api/members/**)에 대해서는 JwtAuthGatewayFilterFactory를 통해 추가적인 처리
 */


@Component
public class JwtAuthGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtAuthGatewayFilterFactory.Config> {

    private final JwtUtil jwtUtil;

    public JwtAuthGatewayFilterFactory(JwtUtil jwtUtil) {
        super(Config.class); //
        this.jwtUtil = jwtUtil;
    }


    @Override //
    public GatewayFilter apply(Config config){ //함수 이름이 apply, 반환 타입 GatewayFilter, 인자 Config
        return (exchange, chain) -> {

            String path = exchange.getRequest().getPath().value();
            System.out.println("JwtAuthGatewayFilterFactory processing request for path: " + path);  // 로깅 추가

            // 특정 경로에 대한 예외 처리
            if (path.startsWith("/public/") ||
                    path.startsWith("/api/auth/login") ||
                    path.startsWith("/api/members/register") ||
                    path.startsWith("/api/members/activate")) {
                System.out.println("Skipping token verification for path: " + path);  // 로깅 추가
                return chain.filter(exchange);
            }
            //exchange : ServerWebExchange 타입, HTTP 요청과 응답에 대한 모든정보를 폼하나는 객체, (요청의 헤더, 본문, URI, HTTP 메서드(GET, POST) 포함), 이 객체를 통해 요청 읽고, 응답 작성
            //chain : GatewayFilterChain 타입, 필터 체인을 나타내는 객체(필터 체인 : 여러 개의 필터, 각 필터는 chain.filter(exchange)를 호출하여 다음 필터로 제어 넘겨줌)
            // ->  : 자바 람다 표현식을 정의할때 사용, 익명 함수 정의 : 여기서 목적 --> exchange와 chain을 인자로 받아, 그 인자들을 사용해 ServerWebExchange 객체를 처리하고, 필터 체인에서 다음 필터로 요청을 전달하는 로직을 정의

            //여기에 필터 로직 들어감

            String token = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                if (jwtUtil.validateToken(token)) {
                    return chain.filter(exchange);
                }
            }

            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED); //토큰 검증 실패 -> 401 UNAUTHORIZED 응답
            return exchange.getResponse().setComplete();
            // exchange.getResponse() : 현재 요청에 대한 응답 객체 반환, 응답 객체를 통해 상태 코드 설정, 헤더 추가,응답 본문 작성등을 함
            // setComplete() : 현재 응답이 완료되었음을 의미, 더 이상 추가적인 응답 데이터를 처리하지 않겠다고 선언, 주로 응답을 끝내고 클라이언트에게 전송되는 시점에 호출
            // return Mono<Void> 타입 객체 반환 --> Mono<void> Reactor 라이브러리의 비동기 객체 중 하나, 비동기적인 작업이 완료되었음을 나타냄, 즉, 응답 처리가 완료된 후 아무 작업도 하지 않음을 나타냄
            // 요약 : 주로 특정 상황에서 응답을 조기에 종료하고, 클라이언트에게 즉시 응답을 보내야 할 때 사용 |  인증이 실패하거나, 잘못된 요청이 들어왔을 때 응답을 바로 완료하는 데 사용
        };
    }
    public static class Config {
    }
}
/*
JwtAuthWebFilter(-> WebFilter) vs JwtAuthGatewayFilterFactory(->GatewayFilterFactory)

- 적용 범위
    - WebFilter : 전역적
    - GatewayFilterFactory : 특정 라우트에만 적용
- 실행 순서
    - WebFilter가 먼저 실행되고 그 다음 GatewayFilter 가 실행

- 기타
    - GatewayFilter은 특정 서비스나 엔드 포인트에 대해 더 세밀한 로직 적용
    - 중복 검사 가능성을 신경써야함
            JwtAuthWebFilter에서는 기본적인 인증 검사와 공개 경로 처리를 수행
            JwtAuthGatewayFilterFactory에서는 특정 서비스에 필요한 추가적인 검증이나 요청 수정을 수행
 */