package com.anteater.memberservice.common.repository;

import com.anteater.memberservice.common.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);
    Optional<Member> findByEmail(String email);
    Optional<Member> findByRefreshToken(String refreshToken); // 08.22 리프레시 토큰 저장용 추가
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);


    @Query("SELECT m FROM Member m WHERE m.refreshToken = :token AND m.refreshTokenExpiry > CURRENT_TIMESTAMP")
    Optional<Member> findByValidRefreshToken(@Param("token") String token);

    @Query("SELECT m FROM Member m WHERE m.email = :identifier OR m.username = :identifier")
    Optional<Member> findByEmailOrUsername(@Param("identifier") String identifier);
}