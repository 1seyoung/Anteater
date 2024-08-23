package com.anteater.memberservice.auth.service;

import com.anteater.memberservice.common.entity.Member;
import com.anteater.memberservice.common.entity.SecurityUser;
import com.anteater.memberservice.common.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Autowired
    public CustomUserDetailsService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmailOrUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + username));

        return new SecurityUser(member);
    }
}
