package com.anteater.stockservice.watchlist.controller;

import com.anteater.stockservice.watchlist.entity.WatchlistItem;
import com.anteater.stockservice.watchlist.service.WatchlistService;
import com.anteater.stockservice.stock.dto.StockDTO;
import com.anteater.stockservice.stock.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/stock/watchlist")
public class WatchlistController {

    private final WatchlistService watchlistService;
    private final StockService stockService;

    @Autowired
    public WatchlistController(WatchlistService watchlistService, StockService stockService) {
        this.watchlistService = watchlistService;
        this.stockService = stockService;
    }

    @PostMapping("/{stockIsin}")
    public ResponseEntity<Void> addToWatchlist(@RequestHeader("X-Auth-Username") String username, @PathVariable String stockIsin) {
        watchlistService.addToWatchlist(username, stockIsin);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{stockIsin}")
    public ResponseEntity<Void> removeFromWatchlist(@RequestHeader("X-Auth-Username") String username, @PathVariable String stockIsin) {
        watchlistService.removeFromWatchlist(username, stockIsin);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<StockDTO>> getWatchlist(@RequestHeader("X-Auth-Username") String username) {
        List<WatchlistItem> watchlistItems = watchlistService.getWatchlistItems(username);
        List<String> isinCodes = watchlistItems.stream()
                .map(WatchlistItem::getStockIsin)
                .collect(Collectors.toList());

        Map<String, StockDTO> stockMap = stockService.getStocksByIsinCodes(isinCodes);

        List<StockDTO> watchlist = watchlistItems.stream()
                .map(item -> stockMap.get(item.getStockIsin()))
                .filter(stock -> stock != null)
                .collect(Collectors.toList());

        return ResponseEntity.ok(watchlist);
    }
}