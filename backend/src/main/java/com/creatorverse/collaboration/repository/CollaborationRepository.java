package com.creatorverse.collaboration.repository;

import com.creatorverse.campaign.application.entity.CampaignApplication;
import com.creatorverse.campaign.entity.Campaign;
import com.creatorverse.collaboration.entity.Collaboration;
import com.creatorverse.collaboration.entity.enums.CollaborationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CollaborationRepository extends JpaRepository<Collaboration, Long> {

    boolean existsByOriginatingApplication(CampaignApplication application);

    @EntityGraph(attributePaths = {"campaign", "creatorUser", "campaign.brandUser"})
    Page<Collaboration> findByCreatorUser_IdOrCampaign_BrandUser_Id(Long creatorUserId, Long brandUserId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"campaign", "creatorUser", "campaign.brandUser"})
    Optional<Collaboration> findById(Long id);

    boolean existsByCampaignAndStatus(Campaign campaign, CollaborationStatus status);
}
