package com.creatorverse.campaign.application.service;

import com.creatorverse.campaign.application.dto.ApplicationReviewRequest;
import com.creatorverse.campaign.application.entity.CampaignApplication;
import com.creatorverse.campaign.application.entity.enums.ApplicationStatus;
import com.creatorverse.campaign.application.repository.CampaignApplicationRepository;
import com.creatorverse.campaign.entity.Campaign;
import com.creatorverse.campaign.repository.CampaignRepository;
import com.creatorverse.collaboration.service.CollaborationService;
import com.creatorverse.creator.entity.CreatorProfile;
import com.creatorverse.creator.repository.CreatorProfileRepository;
import com.creatorverse.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.AopTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringJUnitConfig
public class CampaignApplicationServiceIntegrationTest {

    @Configuration
    @EnableTransactionManagement
    static class TestConfig {
        @Bean
        public CampaignApplicationRepository applicationRepository() {
            return mock(CampaignApplicationRepository.class);
        }

        @Bean
        public CampaignRepository campaignRepository() {
            return mock(CampaignRepository.class);
        }

        @Bean
        public CreatorProfileRepository creatorProfileRepository() {
            return mock(CreatorProfileRepository.class);
        }

        @Bean
        public CollaborationService collaborationService() {
            return mock(CollaborationService.class);
        }

        @Bean
        public PlatformTransactionManager transactionManager() {
            return mock(PlatformTransactionManager.class);
        }

        @Bean
        public org.springframework.context.ApplicationEventPublisher applicationEventPublisher() {
            return mock(org.springframework.context.ApplicationEventPublisher.class);
        }

        @Bean
        public CampaignApplicationService campaignApplicationService(
                CampaignApplicationRepository applicationRepository,
                CampaignRepository campaignRepository,
                CreatorProfileRepository creatorProfileRepository,
                CollaborationService collaborationService,
                org.springframework.context.ApplicationEventPublisher eventPublisher) {
            return new CampaignApplicationService(applicationRepository, campaignRepository, creatorProfileRepository, collaborationService, eventPublisher);
        }
    }

    @Autowired
    private CampaignApplicationService applicationService;

    @Autowired
    private CampaignApplicationRepository applicationRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private CollaborationService collaborationService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void testReviewApplication_Accepted_CollaborationFails_RollsBack() {
        // Setup data
        User brandUser = new User();
        brandUser.setId(1L);

        User creatorUser = new User();
        creatorUser.setId(2L);

        Campaign campaign = new Campaign();
        campaign.setId(10L);
        campaign.setBrandUser(brandUser);

        CampaignApplication application = new CampaignApplication();
        application.setId(100L);
        application.setCampaign(campaign);
        application.setCreatorUser(creatorUser);
        application.setStatus(ApplicationStatus.PENDING);

        // Mock repository lookups
        doReturn(Optional.of(campaign)).when(campaignRepository).findById(10L);
        doReturn(Optional.of(application)).when(applicationRepository).findById(100L);
        doAnswer(i -> i.getArgument(0)).when(applicationRepository).save(any(CampaignApplication.class));

        // Mock transaction manager to return a mock status
        TransactionStatus txStatus = mock(TransactionStatus.class);
        PlatformTransactionManager rawTxManager = AopTestUtils.getTargetObject(transactionManager);
        doReturn(txStatus).when(rawTxManager).getTransaction(any());

        // Force collaboration creation to fail
        CollaborationService rawCollabService = AopTestUtils.getTargetObject(collaborationService);
        doThrow(new RuntimeException("Simulated collaboration failure"))
                .when(rawCollabService).createCollaboration(any(CampaignApplication.class));

        ApplicationReviewRequest request = new ApplicationReviewRequest();
        request.setStatus(ApplicationStatus.ACCEPTED);

        // Execute service call which uses the Spring Transactional proxy
        assertThrows(RuntimeException.class, () -> {
            applicationService.reviewApplication(1L, 10L, 100L, request);
        });

        // VERIFY transaction rollback was explicitly called on the transaction manager
        verify(transactionManager, atLeast(1)).rollback(txStatus);

        // VERIFY that commit was never called
        verify(transactionManager, never()).commit(any());
    }
}
