package com.anteater.memberservice.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * User 엔티티 클래스.
 * 이 클래스는 사용자의 정보를 표현하며, 데이터베이스의 'users' 테이블과 매핑
 * Setter 사용을 제한하여 엔티티의 불변성을 보장
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String name;

    private LocalDate birthdate;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus subscriptionStatus = SubscriptionStatus.UNACTIVATED;

    private boolean activated = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @Builder // 빌더 패턴을 사용하여 객체 생성
    private User(String username, String email, String password, String name, LocalDate birthdate) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.name = name;
        this.birthdate = birthdate;
        this.activated = false;
    }


    /**
     * 사용자 생성을 위한 정적 팩토리 메서드.
     */
    public static User createUser(String username, String email, String password, String name, LocalDate birthdate) {
        return User.builder()
                .username(username)
                .email(email)
                .password(password)
                .name(name)
                .birthdate(birthdate)
                .build();
    }

    /**
     * 사용자 계정을 활성화합니다.
     */
    public void activate() {
        this.activated = true;
        this.subscriptionStatus = SubscriptionStatus.BASIC;
    }

    public boolean isSubscriptionActive() {
        return this.subscriptionStatus != SubscriptionStatus.UNACTIVATED;
    }

    /**
     * 사용자의 구독 상태를 변경합니다.
     */
    public void changeSubscription(SubscriptionStatus newStatus) {
        this.subscriptionStatus = newStatus;
    }

    /**
     * 사용자의 비밀번호를 변경합니다.
     */
    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    //grea
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    //createdAt과 updatedAt
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}



/*
@Entity : JPA 엔티티임을 나타냄, 클래스는 데이터베이스의 테이블과 연결되어 있으며, 데이터베이스 테이블의 행과 클래스의 객체 매핑됨
@Table : 엔티티가 매핑되는 데이터베이스 테이블을 지정
@Id : 필드가 엔티티의 기본 키(PK : Primary Key)임을 나타냄, 데이터베이스의 고유 행
@GenerativeValue(strategy = GenerationType.IDENTITY) : 기본 키 생성 전략, 자동으로 생성해줌,
@Column : 필드가 데이터베이스의 열(Column)과 매핑됨을 나타냄, Unique= true , 이 열의 값이 고유해야함을 의미하고,
            nullable = false, 이 열의 값이 null이 아니어야 함을 의미
@Enumerated(EnumType.STRING) : Enum(-> 열거형) 필드를 데이터베이스에 문자열로 저장하도록 지정
@PrePersist : 엔티티가 처음으로 저장되기 전에 실행될 메서드임을 나타냄
@PreUpate : 엔티티가 업데이트되기 전에 실행될 메서드임을 나타냄
@Getter, @NoArgsConstructor(access = AccessLevel.PROTECTED): Lombok 라이브러리를 사용하여 자동으로 게터 메서드와 기본 생성자를 생성
        기본 생성자는 외부에서 직접 호출할 수 없도록 PROTECTED로 제한

@Setter 안 쓰는 이유
- 엔티티의 불변성, 데이터의 일관성을 유지하고 예기치 않은 변경을 방지할 수 있음,
Setter 메서드를 제한하여, 생성 시점에만 값을 설정하도록 제한함으로써 불변성 유지

@PrePersist, @PreUpdate : JPA 생명 주기 관련
==============================
팩토리 메서드 : 객체 생성의 책임을 메서드로 캡슐화한 디자인 패턴이자 기법
객체 생성 로직을 클래스 내부에서 통제하고 외부에서는 메서드를 호출하여 객체를 생성하도록 함
- 객체 생성 로직을 캡슐화하여 객체 생성 과정을 단순화하고, 객체 생성 방식을 유연하게 변경할 수 있음
 */