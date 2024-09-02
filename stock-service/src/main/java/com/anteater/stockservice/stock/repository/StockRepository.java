package com.anteater.stockservice.stock.repository;

import com.anteater.stockservice.stock.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, String> {
    List<Stock> findAllByIsinCodeIn(List<String> isinCodes);
}