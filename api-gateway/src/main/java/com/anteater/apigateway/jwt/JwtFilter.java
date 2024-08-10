package com.anteater.apigateway.jwt;

import org.springframework.stereotype.Component;


@Component
public class JwtFilter {
    /*
    TODO auth 2: JwtFilter 구현
    - WebFilter를 구현한 JwtFilter 클래스를 작성하여 모든 요청에서 JWT 토큰을 검증하도록 한다.
    - JWT 토큰이 유효한지 확인하고, 유효한 경우 ReactiveSecurityContextHolder에 사용자 인증 정보를 설정한다.
    - 구독 상태를 확인하여 구독자 전용 리소스에 접근할 수 있는지 판단하도록 로직을 추가한다.

     */


}
/*
Spring Security 는 기본적으로 순서가 있는 Security Filter 들을 제공하고, Spring Security가 제공하는 Filter를 구현한게 아니라면 필터의 순서를 정해줘야 함
 */