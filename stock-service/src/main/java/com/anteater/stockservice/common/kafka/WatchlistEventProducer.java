package com.anteater.stockservice.common.kafka;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class WatchlistEventProducer {

    private static final String TOPIC = "watchlist-events";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendWatchlistAddedEvent(String userName, String stockIsin) {
        WatchlistEvent event = new WatchlistEvent("ADDED", userName, stockIsin);
        kafkaTemplate.send(TOPIC, userName, event);
    }

    public void sendWatchlistRemovedEvent(String userName, String stockIsin) {
        WatchlistEvent event = new WatchlistEvent("REMOVED", userName, stockIsin);
        kafkaTemplate.send(TOPIC, userName, event);
    }

    private static class WatchlistEvent {
        private String eventType;
        private String userName;
        private String stockIsin;

        public WatchlistEvent(String eventType, String userName, String stockIsin) {
            this.eventType = eventType;
            this.userName = userName;
            this.stockIsin = stockIsin;
        }

        // Getters and setters
    }
}