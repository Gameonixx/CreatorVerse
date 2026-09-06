package com.creatorverse.analytics.service;

import com.creatorverse.content.entity.enums.ContentStatus;
import com.creatorverse.content.repository.ContentRepository;
import com.creatorverse.creator.entity.CreatorProfile;
import com.creatorverse.creator.repository.CreatorProfileRepository;
import com.creatorverse.social.repository.CommentRepository;
import com.creatorverse.social.repository.ContentLikeRepository;
import com.creatorverse.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreatorAnalyticsServiceTest {

    @Mock
    private CreatorProfileRepository creatorProfileRepository;

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private ContentLikeRepository contentLikeRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CreatorAnalyticsService creatorAnalyticsService;

    @Captor
    private ArgumentCaptor<List<CreatorProfile>> profilesCaptor;

    private User createMockUser(Long id, int followerCount) {
        User user = new User();
        user.setId(id);
        user.setFollowerCount(followerCount);
        return user;
    }

    private CreatorProfile createMockProfile(Long id, User user) {
        CreatorProfile profile = new CreatorProfile();
        profile.setId(id);
        profile.setUser(user);
        profile.setEngagementRate(0.0);
        return profile;
    }

    private void mockAggregates(long userId, long posts, long likes, long comments) {
        when(contentRepository.countPostsPerCreatorByStatus(ContentStatus.PUBLISHED))
                .thenReturn(posts > 0 ? Collections.singletonList(new Object[]{userId, posts}) : Collections.emptyList());
        when(contentLikeRepository.countLikesOnPostsPerCreatorByStatus(ContentStatus.PUBLISHED))
                .thenReturn(likes > 0 ? Collections.singletonList(new Object[]{userId, likes}) : Collections.emptyList());
        when(commentRepository.countCommentsOnPostsPerCreatorByStatus(ContentStatus.PUBLISHED))
                .thenReturn(comments > 0 ? Collections.singletonList(new Object[]{userId, comments}) : Collections.emptyList());
    }

    @Test
    void testA_InsufficientFollowers() {
        User user = createMockUser(1L, 2);
        CreatorProfile profile = createMockProfile(1L, user);
        when(creatorProfileRepository.findAll()).thenReturn(Collections.singletonList(profile));
        mockAggregates(1L, 1L, 5L, 2L);
        creatorAnalyticsService.calculateEngagementRates();
        verify(creatorProfileRepository).saveAll(profilesCaptor.capture());
        assertNull(profilesCaptor.getValue().get(0).getEngagementRate());
    }

    @Test
    void testB_InsufficientPosts() {
        User user = createMockUser(1L, 150);
        CreatorProfile profile = createMockProfile(1L, user);
        when(creatorProfileRepository.findAll()).thenReturn(Collections.singletonList(profile));
        mockAggregates(1L, 2L, 20L, 5L);
        creatorAnalyticsService.calculateEngagementRates();
        verify(creatorProfileRepository).saveAll(profilesCaptor.capture());
        assertNull(profilesCaptor.getValue().get(0).getEngagementRate());
    }

    @Test
    void testC_InsufficientInteractions() {
        User user = createMockUser(1L, 150);
        CreatorProfile profile = createMockProfile(1L, user);
        when(creatorProfileRepository.findAll()).thenReturn(Collections.singletonList(profile));
        mockAggregates(1L, 10L, 8L, 2L);
        creatorAnalyticsService.calculateEngagementRates();
        verify(creatorProfileRepository).saveAll(profilesCaptor.capture());
        assertNull(profilesCaptor.getValue().get(0).getEngagementRate());
    }

    @Test
    void testD_ExactlyAtThresholds() {
        User user = createMockUser(1L, 100);
        CreatorProfile profile = createMockProfile(1L, user);
        when(creatorProfileRepository.findAll()).thenReturn(Collections.singletonList(profile));
        mockAggregates(1L, 5L, 15L, 5L);
        creatorAnalyticsService.calculateEngagementRates();
        verify(creatorProfileRepository).saveAll(profilesCaptor.capture());
        assertEquals(4.00, profilesCaptor.getValue().get(0).getEngagementRate());
    }

    @Test
    void testE_EligibleCreatorWithZeroEngagement() {
        User user = createMockUser(1L, 150);
        CreatorProfile profile = createMockProfile(1L, user);
        when(creatorProfileRepository.findAll()).thenReturn(Collections.singletonList(profile));
        mockAggregates(1L, 5L, 0L, 0L);
        creatorAnalyticsService.calculateEngagementRates();
        verify(creatorProfileRepository).saveAll(profilesCaptor.capture());
        assertEquals(0.00, profilesCaptor.getValue().get(0).getEngagementRate());
    }

    @Test
    void testF_HighRawEngagementBoundTo100() {
        // followers = 100, posts = 5, interactions = 5000
        // average = 1000 per post. 1000 / 100 = 10 -> 1000.0% -> capped to 100.00%
        User user = createMockUser(1L, 100);
        CreatorProfile profile = createMockProfile(1L, user);
        when(creatorProfileRepository.findAll()).thenReturn(Collections.singletonList(profile));
        mockAggregates(1L, 5L, 4000L, 1000L);
        creatorAnalyticsService.calculateEngagementRates();
        verify(creatorProfileRepository).saveAll(profilesCaptor.capture());
        assertEquals(100.00, profilesCaptor.getValue().get(0).getEngagementRate());
    }

    @Test
    void testG_Rounding() {
        // Rate calculated to 4.526 -> 4.53
        Double rate1 = creatorAnalyticsService.calculateEngagementRate(100, 4526, 0, 1000);
        assertEquals(4.53, rate1);

        // Rate calculated to 4.524 -> 4.52
        Double rate2 = creatorAnalyticsService.calculateEngagementRate(100, 4524, 0, 1000);
        assertEquals(4.52, rate2);
    }

    @Test
    void testH_MultipleCreators() {
        User user1 = createMockUser(1L, 1000);
        CreatorProfile profile1 = createMockProfile(1L, user1);

        User user2 = createMockUser(2L, 50); // ineligible
        CreatorProfile profile2 = createMockProfile(2L, user2);

        when(creatorProfileRepository.findAll()).thenReturn(Arrays.asList(profile1, profile2));
        when(contentRepository.countPostsPerCreatorByStatus(ContentStatus.PUBLISHED))
                .thenReturn(Arrays.asList(new Object[]{1L, 10L}, new Object[]{2L, 5L}));
        when(contentLikeRepository.countLikesOnPostsPerCreatorByStatus(ContentStatus.PUBLISHED))
                .thenReturn(Arrays.asList(new Object[]{1L, 400L}, new Object[]{2L, 100L}));
        when(commentRepository.countCommentsOnPostsPerCreatorByStatus(ContentStatus.PUBLISHED))
                .thenReturn(Collections.singletonList(new Object[]{1L, 50L}));

        creatorAnalyticsService.calculateEngagementRates();

        verify(creatorProfileRepository).saveAll(profilesCaptor.capture());
        List<CreatorProfile> saved = profilesCaptor.getValue();

        CreatorProfile saved1 = saved.stream().filter(p -> p.getUser().getId() == 1L).findFirst().get();
        CreatorProfile saved2 = saved.stream().filter(p -> p.getUser().getId() == 2L).findFirst().get();

        assertEquals(4.50, saved1.getEngagementRate());
        assertNull(saved2.getEngagementRate());
    }
}
