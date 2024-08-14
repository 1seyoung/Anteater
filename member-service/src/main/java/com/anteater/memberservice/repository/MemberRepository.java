package com.anteater.memberservice.repository;
import com.anteater.memberservice.entity.SubscriptionStatus;

import com.anteater.memberservice.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);
    Optional<Member> findByEmail(String email);
    Optional<Member> findByUserId(String userId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<Member> findBySubscriptionStatus(SubscriptionStatus status);

}