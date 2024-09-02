package com.anteater.stockservice.watchlist.repository;

import com.anteater.stockservice.watchlist.entity.WatchlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface WatchlistRepository extends JpaRepository<WatchlistItem, Long> {
    List<WatchlistItem> findByUserName(String userName);
    Optional<WatchlistItem> findByUserNameAndStockIsin(String userName, String stockIsin);
    void deleteByUserNameAndStockIsin(String userName, String stockIsin);

    @Query("SELECT w FROM WatchlistItem w JOIN FETCH w.stock WHERE w.userName = :userName")
    List<WatchlistItem> findByUserNameWithStock(@Param("userName") String userName);
}