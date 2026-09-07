package com.creatorverse.campaign.repository;

import com.creatorverse.campaign.entity.Campaign;
import com.creatorverse.campaign.entity.enums.CampaignStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long>, JpaSpecificationExecutor<Campaign> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"brandUser"})
    Page<Campaign> findByBrandUser_Id(Long userId, Pageable pageable);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"brandUser"})
    Page<Campaign> findByStatus(CampaignStatus status, Pageable pageable);
    
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"brandUser"})
    Page<Campaign> findAll(Specification<Campaign> spec, Pageable pageable);
}
