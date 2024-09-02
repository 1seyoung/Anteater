package com.anteater.newsfeedservice.repository;


import com.anteater.newsfeedservice.entity.NewsfeedItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;


import java.util.List;

@Repository
public interface NewsfeedItemRepository extends JpaRepository<NewsfeedItem, Long> {
    Page<NewsfeedItem> findByUserNameAndParentIdIsNullOrderByCreatedAtDesc(String userName, Pageable pageable);

    @Query("SELECT n FROM NewsfeedItem n WHERE n.parentId = :parentId ORDER BY n.createdAt ASC")
    List<NewsfeedItem> findRelatedItems(@Param("parentId") Long parentId);

    Page<NewsfeedItem> findByUserNameAndStockIsinOrderByCreatedAtDesc(String userName, String stockIsin, Pageable pageable);

    Page<NewsfeedItem> findByUserNameOrderByCreatedAtDesc(String userName, Pageable pageable);
}