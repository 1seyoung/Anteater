package com.anteater.stockservice.watchlist.service;

import com.anteater.stockservice.common.kafka.WatchlistEventProducer;
import com.anteater.stockservice.watchlist.entity.WatchlistItem;
import com.anteater.stockservice.watchlist.repository.WatchlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final WatchlistEventProducer watchlistEventProducer;

    @Autowired
    public WatchlistService(WatchlistRepository watchlistRepository, WatchlistEventProducer watchlistEventProducer) {
        this.watchlistRepository = watchlistRepository;
        this.watchlistEventProducer = watchlistEventProducer;
    }

    @Transactional
    public void addToWatchlist(String userName, String stockIsin) {
        if (watchlistRepository.findByUserNameAndStockIsin(userName, stockIsin).isPresent()) {
            throw new RuntimeException("Stock already in watchlist");
        }

        WatchlistItem item = new WatchlistItem();
        item.setUserName(userName);
        item.setStockIsin(stockIsin);
        item.setAddedAt(LocalDateTime.now());

        watchlistRepository.save(item);
        watchlistEventProducer.sendWatchlistAddedEvent(userName, stockIsin);
    }

    @Transactional
    public void removeFromWatchlist(String userName, String stockIsin) {
        watchlistRepository.deleteByUserNameAndStockIsin(userName, stockIsin);
        watchlistEventProducer.sendWatchlistRemovedEvent(userName, stockIsin);
    }

    public List<WatchlistItem> getWatchlistItems(String userName) {
        return watchlistRepository.findByUserName(userName);
    }
}