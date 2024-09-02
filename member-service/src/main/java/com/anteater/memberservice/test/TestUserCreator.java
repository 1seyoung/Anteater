package com.anteater.memberservice.test;

import com.anteater.memberservice.common.entity.Member;
import com.anteater.memberservice.common.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TestUserCreator {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public List<Member> createTestUsers(int count) {
        List<Member> createdUsers = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            Member testUser = new Member(
                    "testuser" + i,
                    "testuser" + i + "@example.com",
                    passwordEncoder.encode("password" + i)
            );
            testUser.activate(); // 이메일 인증 건너뛰기
            testUser.setSubscribed(i % 2 == 0); // 짝수 번호 사용자는 구독 상태로 설정
            testUser.addRole(i % 3 == 0 ? "ROLE_ADMIN" : "ROLE_USER"); // 3의 배수 번호 사용자는 관리자로 설정

            // 프로필 정보 설정
            testUser.updateProfile(
                    "Test User " + i,
                    "This is a bio for test user " + i,
                    "https://example.com/profile" + i + ".jpg"
            );

            Member savedUser = memberRepository.save(testUser);
            createdUsers.add(savedUser);
        }

        return createdUsers;
    }
}