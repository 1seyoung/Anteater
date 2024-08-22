package com.anteater.memberservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class) //자동으로 생성 시간과 수정 시간을 관리
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(name ="img_url")
    private String profileImage;

    //TEXT는 SQL에서의 데이터 타입 중 하나로, 매우 큰 문자열 데이터를 저장할 수 있는 타입
    @Column(columnDefinition = "TEXT")
    private String bio; //bio 필드에 매우 긴 문자열 저장 가능

    @Getter
    @Column(nullable = false)
    private boolean enabled = false;  // 새로 추가된 필드 -> 활성화 체크용

    @Column(nullable = false)
    private boolean isSubscribed = false; // 구독 여부를 나타내는 필드

    @Column(name = "role")
    private String role = ""; // 기본 역할 설정

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 08.22 리프레시 토큰 저장을 위한 필드 추가
    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "refresh_token_expiry")
    private LocalDateTime refreshTokenExpiry;


    public Member(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.enabled = false;
        this.isSubscribed = false;
        this.initializeCreatedAt();
        this.updateUpdatedAt();
    }

    public boolean isSubscribed() {
        return this.isSubscribed;
    }

    public void setSubscribed(boolean subscribed) {
        if (this.isSubscribed != subscribed) {
            this.isSubscribed = subscribed;
            if (subscribed) {
                addRole("ROLE_SUBSCRIBER");
            } else {
                removeRole("ROLE_SUBSCRIBER");
            }
        }
    }

    public String addRole(String newRole) {
        if (!this.role.equals(newRole)) {
            this.role = newRole;
        }
        return this.role;
    }
    //초기 : addRole 메서드는 void를 반환
    //수정 이유 : ActivationResponse 생성자에서는 이 메서드의 반환값을 String 타입의 매개변수로 전달,  이로 인해 타입 불일치 오류

    public void removeRole(String roleToRemove) {
        if (this.role.equals(roleToRemove)) {
            this.role = "USER"; // 역할 제거 시 기본 역할로 설정
        }
    }

    public boolean hasRole(String roleToCheck) {
        return this.role.equals(roleToCheck);
    }


    public void activate() {
        this.enabled = true;
    }

    public void deactivate() {
        this.enabled = false;
    }
    /*
    계정 활성화 : member.activate();
    계정 비활성화 : member.deactivate();
    상태 확인 (Lombok이 생성한 getter 사용)
    if (member.isEnabled()) {
        // 활성화된 계정에 대한 로직
    }
     */

    //변경 가능한 필드에 대한 메서드 추가

    public void changePassword(String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        this.password = newPassword;
    }//비밀번호 변경

    public void updateProfileImage(String newProfileImage) {
        this.profileImage = newProfileImage;
    }//프로필 이미지 변경

    public void updateBio(String newBio) {
        if (newBio != null && newBio.length() > 500) {  // 예시 제한
            throw new IllegalArgumentException("Bio cannot exceed 500 characters");
        }
        this.bio = newBio;
    }//자기 소개 변경

    // createdAt은 생성 시점에만 설정되도록 합니다.
    protected void initializeCreatedAt() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }


    public void updateUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }

    // 08.22 리프레시 토큰 관련 메서드 추가
    public void setRefreshToken(String refreshToken, LocalDateTime expiry) {
        this.refreshToken = refreshToken;
        this.refreshTokenExpiry = expiry;
        this.updateUpdatedAt();
    }

    public void clearRefreshToken() {
        this.refreshToken = null;
        this.refreshTokenExpiry = null;
        this.updateUpdatedAt();
    }

    public boolean isRefreshTokenValid() {
        return this.refreshToken != null &&
                this.refreshTokenExpiry != null &&
                this.refreshTokenExpiry.isAfter(LocalDateTime.now());
    }

}