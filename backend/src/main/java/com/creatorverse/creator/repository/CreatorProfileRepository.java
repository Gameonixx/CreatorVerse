package com.creatorverse.creator.repository;

import com.creatorverse.creator.entity.CreatorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface CreatorProfileRepository extends JpaRepository<CreatorProfile, Long>, JpaSpecificationExecutor<CreatorProfile> {
    Optional<CreatorProfile> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
