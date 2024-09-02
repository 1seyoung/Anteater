package com.anteater.stockservice.watchlist.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "watchlist_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_name", "stock_isin"})
}) // 유니크 제약조건 추가 (동일한 사용자가 동일한 종목을 중복해서 추가하는 것을 방지)
public class WatchlistItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "stock_isin", nullable = false)
    private String stockIsin;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;


    // WatchlistItem -> Stock의 다대일 관계 매핑 ?
}