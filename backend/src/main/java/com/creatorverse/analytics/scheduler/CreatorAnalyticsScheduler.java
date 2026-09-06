package com.creatorverse.analytics.scheduler;

import com.creatorverse.analytics.service.CreatorAnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CreatorAnalyticsScheduler {

    private static final Logger logger = LoggerFactory.getLogger(CreatorAnalyticsScheduler.class);
    private final CreatorAnalyticsService analyticsService;

    public CreatorAnalyticsScheduler(CreatorAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    // Run every hour (3600000 milliseconds)
    @Scheduled(fixedRate = 3600000)
    public void scheduleEngagementRateCalculation() {
        logger.info("Starting hourly creator engagement rate calculation...");
        long startTime = System.currentTimeMillis();

        try {
            analyticsService.calculateEngagementRates();
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Successfully completed creator engagement rate calculation in {} ms.", duration);
        } catch (Exception e) {
            logger.error("Failed to calculate creator engagement rates", e);
        }
    }
}
