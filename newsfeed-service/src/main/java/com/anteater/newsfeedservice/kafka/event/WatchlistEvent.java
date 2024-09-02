package com.anteater.newsfeedservice.kafka.event;

import lombok.Data;

@Data
public class WatchlistEvent {
    private String userName;
    private String stockIsin;
    private String eventType;
}