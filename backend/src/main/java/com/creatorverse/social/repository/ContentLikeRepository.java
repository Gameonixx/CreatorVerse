package com.creatorverse.social.repository;

import com.creatorverse.content.entity.Content;
import com.creatorverse.social.entity.ContentLike;
import com.creatorverse.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContentLikeRepository extends JpaRepository<ContentLike, Long> {
    boolean existsByUserAndContent(User user, Content content);
    Optional<ContentLike> findByUserAndContent(User user, Content content);
    List<ContentLike> findByContent(Content content);
    long countByContent(Content content);

    @org.springframework.data.jpa.repository.Query("SELECT c.content.id, COUNT(c) FROM ContentLike c WHERE c.content IN :contents GROUP BY c.content.id")
    List<Object[]> countLikesForContents(@org.springframework.data.repository.query.Param("contents") List<Content> contents);

    @org.springframework.data.jpa.repository.Query("SELECT c.content.id FROM ContentLike c WHERE c.content IN :contents AND c.user = :user")
    List<Long> findLikedContentIds(@org.springframework.data.repository.query.Param("contents") List<Content> contents, @org.springframework.data.repository.query.Param("user") User user);
}
