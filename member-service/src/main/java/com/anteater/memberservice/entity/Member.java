package com.anteater.memberservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String username;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(name = "profile_img")
    private String profileImg;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus subscriptionStatus = SubscriptionStatus.UNACTIVATED;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Member(String email, String username, String password, String bio, String profileImg) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.subscriptionStatus = SubscriptionStatus.UNACTIVATED;
        this.bio = bio;
        this.profileImg = profileImg;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return subscriptionStatus != SubscriptionStatus.UNACTIVATED;
    }

    public boolean isSubscriptionActive() {
        return subscriptionStatus == SubscriptionStatus.BASIC
                || subscriptionStatus == SubscriptionStatus.PREMIUM;
    }

    public void updateSubscriptionStatus(SubscriptionStatus newStatus) {
        this.subscriptionStatus = newStatus;
    }


    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public void updateEmail(String newEmail) {
        this.email = newEmail;
    }

    public void updateUsername(String newUsername) {
        this.username = newUsername;
    }

    public void updateProfileImg(String newProfileImg) {
        this.profileImg = newProfileImg;
    }

    public void updateBio(String newBio) {
        this.bio = newBio;
    }
}