package com.anteater.apigateway.handler;

/**
 * Handler for JWT access denied
 * 구독 모델에 적용
 * 프리미엄 콘텐츠에 대한 접근 권한 문제 해결
 */
public class JwtAccessDeniedHandler {
    /*
    TODO auth4: JwtAccessDeniedHandler 구현
    - 사용자가 구독 상태에 따라 권한이 없을 때 호출되는 접근 거부 처리 핸들러를 작성한다.
    - ServerAccessDeniedHandler를 구현하여 사용자가 권한이 없을 때 HTTP 403 상태 코드를 반환하도록 한다.

     */

    // TODO 5: JwtAuthenticationEntryPoint 구현
    // - JWT 토큰이 없거나 유효하지 않을 때 인증 실패 처리를 위한 핸들러를 작성한다.
    // - ServerAuthenticationEntryPoint를 구현하여 인증이 필요한 요청에 대해 HTTP 401 상태 코드를 반환하도록 한다.

    // TODO 6: 구독 테이블과의 연동
    // - 구독 관련 테이블을 사용해 구독 상태를 관리하도록 한다.
    // - 사용자가 로그인할 때 구독 테이블에서 구독 상태를 조회하여 JWT 토큰에 포함시키도록 한다.
    // - 구독 상태가 변경될 때 JWT 토큰을 갱신하거나, 다음 로그인 시 반영되도록 한다.

    // TODO 7: Refresh Token 관리
    // - Redis를 사용하여 Refresh Token을 관리하는 로직을 구현한다.
    // - JWT 토큰이 만료된 경우, Refresh Token을 사용해 새로운 Access Token을 발급하는 메커니즘을 구현한다.
    // - 사용자가 로그아웃하거나 구독을 취소할 때 Redis에서 Refresh Token을 제거하는 로직을 추가한다.

    // TODO 8: Caching 및 성능 최적화
    // - 구독 상태 조회 작업의 성능을 최적화하기 위해 Redis를 사용하여 캐싱 로직을 추가한다.
    // - 필요한 경우 구독 상태와 관련된 정보를 Redis에 저장하고, 캐시를 통해 빠르게 조회할 수 있도록 한다.

    // TODO 9: 테스트 작성
    // - 단위 테스트: JwtTokenProvider, JwtFilter, JwtAccessDeniedHandler, JwtAuthenticationEntryPoint 등의 단위 테스트를 작성하여 올바르게 동작하는지 확인한다.
    // - 통합 테스트: 전체 인증 및 인가 흐름을 통합적으로 테스트하여 모든 시나리오에서 올바르게 작동하는지 검증한다.

}


/*
JWT(Json Web Token)
JWT는 JSON 객체를 사용하여 정보를 안전하게 전송하는 방식, 인증과 권한 부여에 사용

구성요소
    - Header: 토큰의 유형과 해싱 알고리즘 정보
    - Payload: 클레임(claim) 정보 포함 ---> 사용자 정보나 구독 상태 데이터 저장
    - Signature: 헤더와 페이로드의 인코딩 값, 시크릿 키를 사용하여 생성

토큰 검증 : JWT의 유효성을 검증하는 방법(서명 검증, 마뇰 시간 확인 등)
클레임 추가 및 검증 : 사용자 구동 상태를 JWT에 클레임으로 추가하고 검증하는 방법

Spring Security Filter Chain
- 필터 체인 : Spring Security는 요청을 처리하기 전에 여러 개의 필터 거침, 각 필터는 특정 보안 작업 수행
- WebFilter : WebFlux에서 사용되는 필터, 요청과 응답을 가로채고 수정할 수 있는 필터

Redis
- 인메모리 데이터베이스, 캐시, 세션 저장소
- Spring Data Redis : Redis와 상호작용 위한 모듈
- Refresh Token : Refresh Token을 사용하여 Access Token을 갱신? Redis 를 이용하는 방법을 알아야함

인증(Authentication): 사용자가 올바른 자격 증명을 통해 시스템에 접근할 수 있는지 확인하는 과정
인가(Authorization): 사용자가 특정 리소스에 접근할 수 있는 권한이 있는지 확인하는 과정입니다. 구독 상태에 따라 인가를 처리

---> 회원가입을 완료함 --> 베이직 : 기본 기능 모두 사용 // 프리미엄 : 프리미엄 콘텐츠 사용 가능 ---> 로그인 안했으면 인가 거부



구독 모델과의 연계?
- 상태 관리 고민 : 활성, 비활성, 만료
- JWT와 구독 상태: JWT 토큰에 구독 상태를 추가하고, 이를 기반으로 접근 권한을 제어하는 방법을 설계
- 구독 기간 및 만료 처리: 구독 기간을 설정하고, 만료된 구독에 대한 처리 방법(예: 자동 갱신, 만료 알림 등)을 고려

MySQL에 구독 테이블 설계해둠 ( 요구사항에 따라 구독 상태를 저장하는 테이블을 설계해둠 )
시나리오
- 사용자가 로그인 할 때 : 인증과정에서 사용자 ID를 기반으로 구독 테이블에서 현재 구독 상태 조회
- 구독 상태에 따라 JWT 토큰에 클레임 추가
- 인가 처리 : 특정 리소스에 접글 할 때, JWT 토큰에서 구독 상태를 확인하여 접근을 허용하거나 거부
- JWT 토큰이 만료되면 Refresh Token을 사용하여 갱신, 만료된 구독에 대한 처리 방법을 고려
- 업데이트된 구독 상태는 다음 로그인 시 JWT 토큰에 반영되어, 실시간으로 구독 상태에 따른 접근 권한이 조정


 */