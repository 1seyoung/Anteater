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
import java.util.HashSet;
import java.util.Set;

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
    private boolean enabled = false;  // 새로 추가된 필드

    @Column(nullable = false)
    private boolean isSubscribed = false; // 구독 여부를 나타내는 필드

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Member(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.enabled = false;
        this.isSubscribed = false;
    }

    public boolean isSubscribed() {
        return this.isSubscribed;
    }

    public void setSubscribed(boolean subscribed) {
        if (this.isSubscribed != subscribed) {
            this.isSubscribed = subscribed;
            if (subscribed) {
                this.roles.add("ROLE_SUBSCRIBER");
            } else {
                this.roles.remove("ROLE_SUBSCRIBER");
            }
        }
    }  // 구독에 따른 권한 설정(구독자인 경우 ROLE_SUBSCRIBER 권한을 부여)

    public void addRole(String role) {
        this.roles.add(role);
    } // 권한 추가

    public void removeRole(String role) {
        this.roles.remove(role);
    } // 권한 제거

    public boolean hasRole(String role) {
        return this.roles.contains(role);
    } // 특정 권한을 가지고 있는지 확인


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

}