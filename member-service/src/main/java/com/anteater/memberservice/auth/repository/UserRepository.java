package com.anteater.memberservice.auth.repository;

import com.anteater.memberservice.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 사용자명으로 사용자를 찾습니다.
     *
     * @param username 찾고자 하는 사용자의 사용자명
     * @return 찾은 사용자의 Optional 객체
     */
    Optional<User> findByUsername(String username);

    /**
     * 이메일로 사용자를 찾습니다.
     *
     * @param email 찾고자 하는 사용자의 이메일
     * @return 찾은 사용자의 Optional 객체
     */
    Optional<User> findByEmail(String email);

    /**
     * 주어진 사용자명이 존재하는지 확인합니다.
     *
     * @param username 확인하고자 하는 사용자명
     * @return 사용자명이 존재하면 true, 그렇지 않으면 false
     */
    boolean existsByUsername(String username);

    /**
     * 주어진 이메일이 존재하는지 확인합니다.
     *
     * @param email 확인하고자 하는 이메일
     * @return 이메일이 존재하면 true, 그렇지 않으면 false
     */
    boolean existsByEmail(String email);
}