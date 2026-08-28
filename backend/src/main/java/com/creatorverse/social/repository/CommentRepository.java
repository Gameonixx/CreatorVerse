package com.creatorverse.social.repository;

import com.creatorverse.content.entity.Content;
import com.creatorverse.social.entity.Comment;
import com.creatorverse.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByContentOrderByCreatedAtAsc(Content content);
    Optional<Comment> findByIdAndUser(Long id, User user);
    long countByContent(Content content);

    @org.springframework.data.jpa.repository.Query("SELECT c.content.id, COUNT(c) FROM Comment c WHERE c.content IN :contents GROUP BY c.content.id")
    List<Object[]> countCommentsForContents(@org.springframework.data.repository.query.Param("contents") List<Content> contents);
}
