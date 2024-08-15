package com.anteater.memberservice.entity;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


// SecurityUser 클래스
@Getter
public class SecurityUser implements UserDetails {
    // Member 객체에 대한 getter
    private final Member member;

    public SecurityUser(Member memeber) {
        this.member = memeber;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Member의 권한 정보를 반환하는 로직 구현
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getEmail(); // 또는 user.getUsername()
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
        return member.isEnabled();
    }





}