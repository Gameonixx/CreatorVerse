package com.creatorverse.campaign.application.repository;

import com.creatorverse.campaign.application.entity.CampaignApplication;
import com.creatorverse.campaign.entity.Campaign;
import com.creatorverse.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignApplicationRepository extends JpaRepository<CampaignApplication, Long> {
    
    boolean existsByCampaignAndCreatorUser(Campaign campaign, User creatorUser);
    
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"creatorUser", "campaign"})
    Page<CampaignApplication> findByCampaign(Campaign campaign, Pageable pageable);
    
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"creatorUser", "campaign"})
    Page<CampaignApplication> findByCreatorUser(User creatorUser, Pageable pageable);

    long countByCampaign(Campaign campaign);
}
