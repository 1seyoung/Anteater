package com.anteater.memberservice.test;
import com.anteater.memberservice.common.entity.Member;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("dev") // 개발 환경에서만 실행되도록 설정
public class TestUserCreationRunner implements CommandLineRunner {

    @Autowired
    private TestUserCreator testUserCreator;

    @Override
    public void run(String... args) {
        // 20명의 테스트 유저 생성
        List<Member> testUsers = testUserCreator.createTestUsers(20);

        // 생성된 유저 정보 출력
        System.out.println("Created Test Users:");
        for (Member user : testUsers) {
            System.out.println("ID: " + user.getId() +
                    ", Username: " + user.getUsername() +
                    ", Email: " + user.getEmail() +
                    ", Subscribed: " + user.isSubscribed() +
                    ", Role: " + user.getRole() +
                    ", Display Name: " + user.getDisplayName());
        }
    }
}