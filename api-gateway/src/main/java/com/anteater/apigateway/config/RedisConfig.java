package com.anteater.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ClusterTopology;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Arrays;

/**
 * Configuration class for Redis
 */
@Configuration //spring의 구성 클래스 : 하나 이상의 @Bean 메소드를 정의하고,메서드들이 Spring 컨테이너에서 관리되는 빈(bean)으로 등록
public class RedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        //ClusterTopology 설정 필요 (Lettuce 사용시) --> TODO : 도커에 올려서 테스트할 때는 다시 확인하기
        RedisClusterConfiguration clusterConfiguration = new RedisClusterConfiguration(
                Arrays.asList("localhost:6379", "localhost:6380", "localhost:6381")
        );
        /*
        @Bean
        public RedisConnectionFactory redisConnectionFactory() {
            return new LettuceConnectionFactory(redisProperties.getHost(), redisProperties.getPort());
            }
         */
        return new LettuceConnectionFactory(clusterConfiguration);
    }

    @Bean // Spring IoC 컨테이너가 관리하는 빈으로 등록
    public RedisTemplate<String, Object> redisTemplate() {
        // RedisTemplate 객체 생성. 키는 String, 값은 모든 Object 타입 가능
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        // 앞서 정의한 redisConnectionFactory() 빈을 주입받아 연결 설정
        template.setConnectionFactory(redisConnectionFactory());

        // 키 직렬화 방식을 StringRedisSerializer로 설정
        template.setKeySerializer(new StringRedisSerializer());

        // 값 직렬화 방식을 GenericJackson2JsonRedisSerializer로 설정
        // 복잡한 객체도 JSON 형태로 저장되어 범용성이 높음
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template; // 구성이 완료된 RedisTemplate 객체 반환
    }
}
/*레디스 세부 설정 적용 기준
- 데이터 직렬화 (키, 값 직렬화)
    - 기본 직렬화(JdkSerializationRedisSerializer)
        - Java 객체를 직렬화하여 Redis에 저장, 다른 언어와의 호환성 낮고 복잡할 경우 직렬화된 데이터가 크다(성능 이슈)
    - JSON 직렬화(GenericJackson2JsonRedisSerializer 또는 Jackson2JsonRedisSerializer)
        - 객체 데이터를 직렬화할 때 JSON 형식으로 변환하여 저장, 호환성이 높고 직관적
        - 가독성이 높고, 다른 시스템(API)와 통합 쉬움, 인텍싱, 검색, 분석을 위한 도구와 호환성이 좋음
    - String 직렬화 (StringRedisSerializer)
        - 키가 항상 문자열 형태로 저장되는 경우 사용, 가독성이 높고 호환성 좋음
        - 키가 문자열이 아닌 경우 사용 불가
        - 직렬화된 데이터가 단순한 문자열이라 Redis 내에서 데이터 조회 및 관리 용이
- Redis 클라이언트 선택
    - Jedis
        - 동기 방식의 Redis 클라이언트로, 비교적 간단하게 설정하고 사용
        - 간단한 Redis 연동이 필요하거나, 기존 시스템에서 Jedis를 사용 중인 경우 적합
        - 설정과 사용이 간단하며, Spring과의 통합도 원활합니다. 다만, 멀티스레드 환경에서 Lettuce보다 성능이 떨어질 수 있음
    - Lettuce (더 추천한다 -> 비동기 처리 가능// ClusterTopology 꼭 설정해야함)
        - 비동기, 동기, 반응형(reactive) 모드를 지원하는 고성능 Redis 클라이언트  -> 확장성과 성능이 뛰어나며, 멀티스레드 환경에서 안정적
        - 비동기 모드를 사용하면 더 빠른 응답 시간과 더 높은 처리량을 얻을 수 있음
- 캐시 설정 및 TTL(Time to Live)
    - TTL(Time to Live)
        - Redis는 캐시 용도로 많이 사용됨, TTL은 데이터의 생명 주기를 정하는 설정이다. 캐시 데이터는 일정 시간이 지나면 자동으로 만료되어야 함
        - 캐시 데이터를 일정 시간 후 자동으로 제거하는 경우에 사용
        - 오래된 데이터 자동 정리
- 보안 설정
    - Redis는 기본적으로 인증을 사용하지 않기 때문에, 외부에서 접근할 수 있는 경우 보안에 취약
    - Redis에 접근할 수 있는 IP를 제한하거나(SSL/TLS), 비밀번호를 설정하여 보안을 강화

- 클러스터링 및 샤딩
    - Redis 클러스터
        - 데이터가 여러 노드에 걸쳐 분산 저장되는 구조 제공, 가용성, 확장성
        - 대규모 데이터, 트래픽 처리
    - 샤딩
        - 데이터를 여러 노드에 분산 저장하는 방식, 데이터 양이 많아지면서 단일 노드에서 처리하기 어려울 때 사용 -> 클러스터에서 자동 지원
        - 메모리 사용 최적화, 병목 현상 줄이기
 */