package com.creatorverse.collaboration.entity;

import com.creatorverse.campaign.application.entity.CampaignApplication;
import com.creatorverse.campaign.entity.Campaign;
import com.creatorverse.collaboration.entity.enums.CollaborationStatus;
import com.creatorverse.user.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "collaborations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"originating_application_id"})
})
public class Collaboration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_user_id", nullable = false)
    private User creatorUser;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "originating_application_id", nullable = false, unique = true)
    private CampaignApplication originatingApplication;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CollaborationStatus status = CollaborationStatus.ACTIVE;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Collaboration() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Campaign getCampaign() { return campaign; }
    public void setCampaign(Campaign campaign) { this.campaign = campaign; }

    public User getCreatorUser() { return creatorUser; }
    public void setCreatorUser(User creatorUser) { this.creatorUser = creatorUser; }

    public CampaignApplication getOriginatingApplication() { return originatingApplication; }
    public void setOriginatingApplication(CampaignApplication originatingApplication) { this.originatingApplication = originatingApplication; }

    public CollaborationStatus getStatus() { return status; }
    public void setStatus(CollaborationStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
