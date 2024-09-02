package com.anteater.newsfeedservice.service;

import com.anteater.newsfeedservice.entity.NewsfeedItem;
import com.anteater.newsfeedservice.kafka.event.LikeEvent;
import com.anteater.newsfeedservice.kafka.event.PostEvent;
import com.anteater.newsfeedservice.kafka.event.WatchlistEvent;
import com.anteater.newsfeedservice.repository.NewsfeedItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.events.CommentEvent;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NewsfeedService {

    private final NewsfeedItemRepository newsfeedItemRepository;

    @Autowired
    public NewsfeedService(NewsfeedItemRepository newsfeedItemRepository) {
        this.newsfeedItemRepository = newsfeedItemRepository;
    }

    @Transactional
    @KafkaListener(topics = "post-events", groupId = "newsfeed-group")
    public void consumePostEvent(PostEvent event) {
        NewsfeedItem item = new NewsfeedItem();
        item.setUserName(event.getAuthorId().toString());
        item.setContent("Posted about stock " + event.getStockIsin() + ": " + event.getContent());
        item.setEventType("POST");
        item.setCreatedAt(LocalDateTime.now());
        item.setRelatedId(event.getPostId());
        item.setStockIsin(event.getStockIsin());
        newsfeedItemRepository.save(item);
    }

    @Transactional
    @KafkaListener(topics = "comment-events", groupId = "newsfeed-group")
    public void consumeCommentEvent(CommentEvent event) {
        NewsfeedItem item = new NewsfeedItem();
        item.setUserName(event.getUserId());
        item.setContent("Commented: " + event.getContent());
        item.setEventType("COMMENT");
        item.setCreatedAt(LocalDateTime.now());
        item.setRelatedId(event.getCommentId());
        item.setParentId(event.getPostId());
        newsfeedItemRepository.save(item);
    }

    @Transactional
    @KafkaListener(topics = "like-events", groupId = "newsfeed-group")
    public void consumeLikeEvent(LikeEvent event) {
        NewsfeedItem item = new NewsfeedItem();
        item.setUserName(event.getUserId().toString());
        item.setContent("Liked a " + event.getTargetType().toLowerCase());
        item.setEventType("LIKE");
        item.setCreatedAt(LocalDateTime.now());
        item.setRelatedId(event.getTargetId());
        item.setParentId(event.getPostId());  // 이벤트에 포스트 ID가 포함되어야 함
        newsfeedItemRepository.save(item);
    }

    @Transactional
    @KafkaListener(topics = "watchlist-events", groupId = "newsfeed-group")
    public void consumeWatchlistEvent(WatchlistEvent event) {
        NewsfeedItem item = new NewsfeedItem();
        item.setUserName(event.getUserName());
        item.setContent(event.getEventType().equals("ADDED") ?
                "Added stock " + event.getStockIsin() + " to watchlist" :
                "Removed stock " + event.getStockIsin() + " from watchlist");
        item.setEventType("WATCHLIST");
        item.setCreatedAt(LocalDateTime.now());
        item.setStockIsin(event.getStockIsin());
        newsfeedItemRepository.save(item);
    }

    public Page<NewsfeedItem> getNewsfeedForUser(String userName, Pageable pageable) {
        return newsfeedItemRepository.findByUserNameOrderByCreatedAtDesc(userName, pageable);
    }

    public Page<NewsfeedItem> getNewsfeedForUserByStock(String userName, String stockIsin, Pageable pageable) {
        return newsfeedItemRepository.findByUserNameAndStockIsinOrderByCreatedAtDesc(userName, stockIsin, pageable);
    }
}