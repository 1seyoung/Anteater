package com.anteater.memberservice.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * 이메일 설정을 위한 구성 클래스입니다.
 * <p>
 * 이 클래스는 애플리케이션의 설정 파일들(e.g., application.yml, application-secret.yml)에 정의된
 * 이메일 관련 속성들을 Java 필드에 매핑하는 역할을 합니다.
 * 이 속성들은 설정 파일에서 "spring.mail" 접두사로 시작합니다.
 * </p>
 *
 * <p>
 * 설정 예시:
 * <pre>
 * spring:
 *   mail:
 *     host: smtp.gmail.com
 *     port: 587
 *     username: user@example.com
 *     password: secretpassword
 *     properties:
 *       mail:
 *         smtp:
 *           auth: true
 *           starttls:
 *             enable: true
 *             required: true
 *           connectiontimeout: 5000
 *           timeout: 5000
 *           writetimeout: 5000
 *   auth-code-expiration-millis: 1800000
 * </pre>
 * </p>
 */

@Configuration
@ConfigurationProperties(prefix = "spring.mail")
public class EmailConfig {
    /**
     * SMTP 호스트 주소
     */
    private String host;
    /**
     * SMTP 서버 포트
     */
    private int port;
    /**
     * SMTP 서버와 인증할 때 사용하는 사용자 이름(이메일)
     */
    private String username;
    /**
     * SMTP 서버와 인증할 때 사용하는 비밀번호
     */
    private String password;
    /**
     * SMTP 인증, TLS 설정 및 타임아웃 등 추가적인 메일 속성
     */
    private Properties properties = new Properties();
    /**
     * 인증 코드의 만료 시간(밀리초)
     * 일반적으로 코드는 설정된 시간이 지나면 만료
     */
    private long authCodeExpirationMillis;

    /**
     * SMTP 호스트 주소를 반환합니다.
     *
     * @return SMTP 호스트 주소
     */
    public String getHost() {
        return host;
    }

    /**
     * SMTP 호스트 주소를 설정합니다.
     *
     * @param host SMTP 호스트 주소
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * SMTP 서버 포트를 반환합니다.
     *
     * @return SMTP 서버 포트
     */
    public int getPort() {
        return port;
    }

    /**
     * SMTP 서버 포트를 설정합니다.
     *
     * @param port SMTP 서버 포트
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * SMTP 서버와 인증할 때 사용하는 사용자 이름을 반환합니다.
     *
     * @return SMTP 사용자 이름
     */
    public String getUsername() {
        return username;
    }

    /**
     * SMTP 서버와 인증할 때 사용하는 사용자 이름을 설정합니다.
     *
     * @param username SMTP 사용자 이름
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * SMTP 서버와 인증할 때 사용하는 비밀번호를 반환합니다.
     *
     * @return SMTP 비밀번호
     */
    public String getPassword() {
        return password;
    }

    /**
     * SMTP 서버와 인증할 때 사용하는 비밀번호를 설정합니다.
     *
     * @param password SMTP 비밀번호
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 추가적인 메일 속성을 반환합니다.
     *
     * @return 추가적인 메일 속성
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * 추가적인 메일 속성을 설정합니다.
     *
     * @param properties 추가적인 메일 속성
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * 인증 코드의 만료 시간을 밀리초 단위로 반환합니다.
     *
     * @return 만료 시간 (밀리초)
     */
    public long getAuthCodeExpirationMillis() {
        return authCodeExpirationMillis;
    }

    /**
     * 인증 코드의 만료 시간을 밀리초 단위로 설정합니다.
     *
     * @param authCodeExpirationMillis 만료 시간 (밀리초)
     */
    public void setAuthCodeExpirationMillis(long authCodeExpirationMillis) {
        this.authCodeExpirationMillis = authCodeExpirationMillis;
    }
}