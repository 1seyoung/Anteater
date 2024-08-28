package com.anteater.memberservice.common.redis;

import com.anteater.memberservice.member.dto.request.RegisterRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.password}")
    private String password;


    //모든 Redis 템플릿은 Redis와의 연결을 위해 이 팩토리를 사용
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setPassword(password);
        return new LettuceConnectionFactory(configuration);
    }

    @Bean(name = "registerRequestRedisTemplate")
    public RedisTemplate<String, RegisterRequest> registerRequestRedisTemplate() {
        RedisTemplate<String, RegisterRequest> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    @Bean(name = "myStringRedisTemplate")
    @Primary
    public RedisTemplate<String, String> stringRedisTemplate() {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    @Bean(name = "redisTempStorageService")
    public RedisTempStorageService<RegisterRequest> registerRequestStorage(
            @Qualifier("registerRequestRedisTemplate") RedisTemplate<String, RegisterRequest> redisTemplate) {
        return new RedisTempStorageService<>(redisTemplate, "reg:");
    }

    @Bean(name = "refreshTokenStorageService")
    public RedisTempStorageService<String> refreshTokenStorage(
            @Qualifier("myStringRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        return new RedisTempStorageService<>(redisTemplate, "");
    }
}