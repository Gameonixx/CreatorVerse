package com.creatorverse.collaboration.deliverable.repository;

import com.creatorverse.collaboration.deliverable.entity.Deliverable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliverableRepository extends JpaRepository<Deliverable, Long> {

    List<Deliverable> findByCollaboration_IdOrderByCreatedAtAsc(Long collaborationId);
    
}
