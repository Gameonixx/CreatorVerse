package com.creatorverse.analytics.scheduler;

import com.creatorverse.analytics.service.CreatorAnalyticsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CreatorAnalyticsSchedulerTest {

    @Mock
    private CreatorAnalyticsService creatorAnalyticsService;

    @InjectMocks
    private CreatorAnalyticsScheduler creatorAnalyticsScheduler;

    @Test
    void testScheduleEngagementRateCalculationDelegatesToService() {
        creatorAnalyticsScheduler.scheduleEngagementRateCalculation();
        verify(creatorAnalyticsService).calculateEngagementRates();
    }
}
