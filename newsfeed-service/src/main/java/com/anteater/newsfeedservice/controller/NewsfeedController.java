package com.anteater.newsfeedservice.controller;

import com.anteater.newsfeedservice.entity.NewsfeedItem;
import com.anteater.newsfeedservice.service.NewsfeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/newsfeed")
public class NewsfeedController {

    @Autowired
    private NewsfeedService newsfeedService;

    @GetMapping
    public ResponseEntity<Page<NewsfeedItem>> getNewsfeed(
            @RequestHeader("X-Auth-Username") String username,
            Pageable pageable) {
        Page<NewsfeedItem> newsfeed = newsfeedService.getNewsfeedForUser(username, pageable);
        return ResponseEntity.ok(newsfeed);
    }

    @GetMapping("/stock/{stockIsin}")
    public ResponseEntity<Page<NewsfeedItem>> getNewsfeedByStock(
            @RequestHeader("X-Auth-Username") String username,
            @PathVariable String stockIsin,
            Pageable pageable) {
        Page<NewsfeedItem> newsfeed = newsfeedService.getNewsfeedForUserByStock(username, stockIsin, pageable);
        return ResponseEntity.ok(newsfeed);
    }
}