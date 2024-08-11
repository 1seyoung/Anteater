package com.anteater.apigateway.config;



import com.anteater.apigateway.jwt.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;


@Configuration // Configuration class for security
@EnableWebFluxSecurity // WebFlux 애플리케이션에서 Spring Security를 활성화 하는 역할
public class SecurityConfig {



    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http){
        return http
                .csrf().disable()
    }
}

/*
Spring Webflux는 Spring Security를 사용하여 보안을 구현할 수 있음 // Reactive Streams 사양을 기반 //
@EnableWebFluxSecurity 어노테이션을 사용하여 Spring Security를 사용하도록 설정

Spring Webflux 와 Spring Security는 서로 독립적인 기능을 제공하지만, 둘을 함께 사용하는 경우가 많다

Webflux : 리액티브 프로그래밍을 지원하는 웹 프레임워크
Spring Security : 애플리케이션의 보안 담당

Webflux + Spring Security : 리액티브 애플리케이션의 보안 처리

Web flux
- 비동기 논블로킹(Asynchronous Non-blocking) 방식으로 동작
- 웹 요청을 처리하는 데 있어서 블로킹 방식이 아닌 비동기적으로 요청 처리, 특히 데이터 흐름의 제어를 지원하기 위해 Pub/Sub 패턴 이용
- I/O 작업 동안 스레드가 차단 x
- 적은 수의 스레드로 많은 동시 연결을 처리
- 반응형 프로그래밍: : Reactor 라이브러리를 사용, Mono(0 또는 1개 요소)와 Flux(0-N개 요소) 타입을 사용
- 명령형 x 선언적 프로그래밍 스타일 지원
- SecurityWebFilterChain을 통해 보안 필터를 구성
================================================================
        Reactor 핵심 개념:
            Mono: 0 또는 1개의 결과를 나타내는 Publisher
            Flux: 0-N개의 결과를 나타내는 Publisher
            이들은 다양한 연산자(map, flatMap, filter 등)를 제공

         ----------------------------------------------------
         WebFlux 사용 시나리오:
            마이크로서비스 아키텍처
            실시간 데이터 스트리밍
            대규모 동시 연결 처리 (예: 채팅 애플리케이션)
         ----------------------------------------------------
         WebFlux의 주요 컴포넌트:
            a. RouterFunction:
                함수형 엔드포인트 정의를 위해 사용됩니다.
                URL 패턴과 핸들러 함수를 매핑합니다.
            b. HandlerFunction:
                요청을 처리하고 응답을 생성하는 함수입니다.
            c. WebFilter:
                요청과 응답을 가로채고 수정할 수 있는 필터입니다.

=================================================================
Spring Security
- 인증, 인가, 보안 기능 제공하는 Spring 보안 프레임워크
- Webflux와 함께 사용하면 리액티브 애플리케이션의 보안 처리 추가 가능함
- Spring Security 5.0부터 Webflux 지원 ---> 리액티브 요청 파이프라인에서 비동기 인증 및 인가 로직을 적용할 수 있음

리액티브?
- 리액티브 프로그래밍 : 비동기 데이터 스트림 처리를 위한 프로그래밍 패러다임 ///??

리액티브 보안 : Webflux + Spring Security
- Webflux 애플리케이션에서 요청이 발생할 때, 인증가 인가는 비동기적으로 처리되며, 블로킹 작업 없이 사용자 인증 및 권한 검사 수행


 */