package com.anteater.stockservice.stock.controller;

import com.anteater.stockservice.stock.dto.StockDTO;
import com.anteater.stockservice.stock.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final StockService stockService;

    @Autowired
    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/{isinCode}")
    public ResponseEntity<StockDTO> getStockByIsinCode(@PathVariable String isinCode) {
        return stockService.getStockByIsinCode(isinCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}