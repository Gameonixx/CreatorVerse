package com.creatorverse.content.repository;

import com.creatorverse.content.entity.Content;
import com.creatorverse.content.entity.enums.ContentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.creatorverse.content.entity.enums.ContentVisibility;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {
    List<Content> findByCreatorId(Long creatorId);
    List<Content> findByCreatorIdAndStatus(Long creatorId, ContentStatus status);
    Page<Content> findByStatusAndVisibility(ContentStatus status, ContentVisibility visibility, Pageable pageable);
    Page<Content> findByCreatorIdAndStatusAndVisibility(Long creatorId, ContentStatus status, ContentVisibility visibility, Pageable pageable);
}
