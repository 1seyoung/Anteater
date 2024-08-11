package com.anteater.apigateway.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * JWT 인증 과정에서 접근이 거부된 경우를 처리하는 핸들러.
 * 이 핸들러는 다양한 접근 거부 상황에 대해 적절한 에러 메시지를 생성하고,
 * JSON 형식의 응답을 클라이언트에게 반환합니다.
 */
@Component
public class JwtAccessDeniedHandler implements ServerAccessDeniedHandler {

    private static final Logger logger = LoggerFactory.getLogger(JwtAccessDeniedHandler.class);
    private final ObjectMapper objectMapper;

    /**
     * JwtAccessDeniedHandler 생성자.
     * @param objectMapper JSON 직렬화를 위한 ObjectMapper
     */
    public JwtAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 접근이 거부된 요청을 처리합니다.
     * @param exchange 현재의 서버 웹 교환
     * @param denied 발생한 AccessDeniedException
     * @return 처리 결과를 나타내는 Mono<Void>
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        return Mono.defer(() -> {
            String path = exchange.getRequest().getPath().value();
            String message = determineErrorMessage(path, denied);

            // 접근 거부 이벤트 로깅
            logger.warn("Access denied for path: {}. Reason: {}", path, message);

            // 응답 상태 코드와 컨텐츠 타입 설정
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

            // JSON 응답 본문 생성
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("status", HttpStatus.FORBIDDEN.value());
            responseBody.put("error", "Forbidden");
            responseBody.put("message", message);
            responseBody.put("path", path);

            try {
                // JSON 응답을 바이트 배열로 변환
                byte[] responseBytes = objectMapper.writeValueAsBytes(responseBody);
                DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(responseBytes);
                // 응답 쓰기
                return exchange.getResponse().writeWith(Mono.just(buffer));
            } catch (Exception e) {
                // JSON 변환 또는 응답 쓰기 중 오류 발생 시 로깅
                logger.error("Error writing response", e);
                return Mono.error(e);
            }
        });
    }

    /**
     * 요청 경로와 예외에 따라 적절한 에러 메시지를 결정합니다.
     * @param path 요청 경로
     * @param denied 발생한 AccessDeniedException
     * @return 결정된 에러 메시지
     */
    private String determineErrorMessage(String path, AccessDeniedException denied) {
        // TODO: API 구현 완료 후 각 경로에 따른 구체적인 메시지 추가
        if (path.startsWith("/api/admin")) {
            return "You do not have administrative privileges to access this resource.";
        } else if (path.startsWith("/api/premium")) {
            // 구독 관련 메시지
            return "This feature requires a premium subscription. Please upgrade your account to access.";
        } else if (denied.getMessage() != null && !denied.getMessage().isEmpty()) {
            // AccessDeniedException에 메시지가 있는 경우 사용
            return denied.getMessage();
        } else {
            // 기본 메시지
            return "Access denied. You do not have permission to access this resource.";
        }
    }
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

--> Redis 를 사용하여 구독 상태를 저장하는 방안으로? 수정 예정? 거의 같은건지는 봐야 알겠음
 */