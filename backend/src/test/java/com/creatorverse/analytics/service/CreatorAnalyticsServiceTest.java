package com.creatorverse.analytics.service;

import com.creatorverse.content.entity.enums.ContentStatus;
import com.creatorverse.content.repository.ContentRepository;
import com.creatorverse.creator.entity.CreatorProfile;
import com.creatorverse.creator.repository.CreatorProfileRepository;
import com.creatorverse.social.repository.CommentRepository;
import com.creatorverse.social.repository.ContentLikeRepository;
import com.creatorverse.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CreatorAnalyticsServiceTest {

    @Mock
    private CreatorProfileRepository creatorProfileRepository;

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private ContentLikeRepository contentLikeRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<CreatorProfile> profileQuery;

    @Mock
    private TypedQuery<Object[]> aggregateQuery;

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

    private void setupEntityManagerMocks(List<CreatorProfile> profiles, long userId, long posts, long likes, long comments) {
        when(entityManager.createQuery(anyString(), eq(CreatorProfile.class))).thenReturn(profileQuery);
        when(profileQuery.setFirstResult(anyInt())).thenReturn(profileQuery);
        when(profileQuery.setMaxResults(anyInt())).thenReturn(profileQuery);

        // Return profiles on first call, empty list on second to break loop
        when(profileQuery.getResultList()).thenReturn(profiles).thenReturn(Collections.emptyList());

        when(entityManager.createQuery(contains("Content c"), eq(Object[].class))).thenReturn(aggregateQuery);
        when(entityManager.createQuery(contains("ContentLike c"), eq(Object[].class))).thenReturn(aggregateQuery);
        when(entityManager.createQuery(contains("Comment c"), eq(Object[].class))).thenReturn(aggregateQuery);

        when(aggregateQuery.setParameter(anyString(), any())).thenReturn(aggregateQuery);

        // Stub aggregate results based on query content
        when(aggregateQuery.getResultList())
            .thenAnswer(invocation -> {
                Object mockObj = invocation.getMock();
                // Return respective counts. In this simple mock, we just return the counts sequentially or based on args if possible.
                // It's simpler to just return them in order since we know the order of queries.
                return null;
            });

        // Actually, to make it robust, we can just stub them individually if we can differentiate,
        // but Mockito will return the last stubbed value if indistinguishable.
        // Let's differentiate by string match in the initial stubbing.

        TypedQuery<Object[]> postQuery = mock(TypedQuery.class);
        TypedQuery<Object[]> likeQuery = mock(TypedQuery.class);
        TypedQuery<Object[]> commentQuery = mock(TypedQuery.class);

        when(entityManager.createQuery(contains("FROM Content c"), eq(Object[].class))).thenReturn(postQuery);
        when(entityManager.createQuery(contains("FROM ContentLike c"), eq(Object[].class))).thenReturn(likeQuery);
        when(entityManager.createQuery(contains("FROM Comment c"), eq(Object[].class))).thenReturn(commentQuery);

        when(postQuery.setParameter(anyString(), any())).thenReturn(postQuery);
        when(likeQuery.setParameter(anyString(), any())).thenReturn(likeQuery);
        when(commentQuery.setParameter(anyString(), any())).thenReturn(commentQuery);

        when(postQuery.getResultList()).thenReturn(posts > 0 ? Collections.singletonList(new Object[]{userId, posts}) : Collections.emptyList());
        when(likeQuery.getResultList()).thenReturn(likes > 0 ? Collections.singletonList(new Object[]{userId, likes}) : Collections.emptyList());
        when(commentQuery.getResultList()).thenReturn(comments > 0 ? Collections.singletonList(new Object[]{userId, comments}) : Collections.emptyList());
    }

    @Test
    void testA_InsufficientFollowers() {
        User user = createMockUser(1L, 2);
        CreatorProfile profile = createMockProfile(1L, user);
        setupEntityManagerMocks(Collections.singletonList(profile), 1L, 1L, 5L, 2L);

        creatorAnalyticsService.calculateEngagementRates();
        verify(creatorProfileRepository).saveAll(profilesCaptor.capture());
        assertNull(profilesCaptor.getValue().get(0).getEngagementRate());
    }

    @Test
    void testB_InsufficientPosts() {
        User user = createMockUser(1L, 150);
        CreatorProfile profile = createMockProfile(1L, user);
        setupEntityManagerMocks(Collections.singletonList(profile), 1L, 2L, 20L, 5L);

        creatorAnalyticsService.calculateEngagementRates();
        verify(creatorProfileRepository).saveAll(profilesCaptor.capture());
        assertNull(profilesCaptor.getValue().get(0).getEngagementRate());
    }

    @Test
    void testC_InsufficientInteractions() {
        User user = createMockUser(1L, 150);
        CreatorProfile profile = createMockProfile(1L, user);
        setupEntityManagerMocks(Collections.singletonList(profile), 1L, 10L, 8L, 2L);

        creatorAnalyticsService.calculateEngagementRates();
        verify(creatorProfileRepository).saveAll(profilesCaptor.capture());
        assertNull(profilesCaptor.getValue().get(0).getEngagementRate());
    }

    @Test
    void testD_ExactlyAtThresholds() {
        User user = createMockUser(1L, 100);
        CreatorProfile profile = createMockProfile(1L, user);
        setupEntityManagerMocks(Collections.singletonList(profile), 1L, 5L, 15L, 5L);

        creatorAnalyticsService.calculateEngagementRates();
        verify(creatorProfileRepository).saveAll(profilesCaptor.capture());
        assertEquals(4.00, profilesCaptor.getValue().get(0).getEngagementRate());
    }

    @Test
    void testE_EligibleCreatorWithZeroEngagement() {
        User user = createMockUser(1L, 150);
        CreatorProfile profile = createMockProfile(1L, user);
        setupEntityManagerMocks(Collections.singletonList(profile), 1L, 5L, 0L, 0L);

        creatorAnalyticsService.calculateEngagementRates();
        verify(creatorProfileRepository).saveAll(profilesCaptor.capture());
        assertEquals(0.00, profilesCaptor.getValue().get(0).getEngagementRate());
    }

    @Test
    void testF_HighRawEngagementBoundTo100() {
        User user = createMockUser(1L, 100);
        CreatorProfile profile = createMockProfile(1L, user);
        setupEntityManagerMocks(Collections.singletonList(profile), 1L, 5L, 4000L, 1000L);

        creatorAnalyticsService.calculateEngagementRates();
        verify(creatorProfileRepository).saveAll(profilesCaptor.capture());
        assertEquals(100.00, profilesCaptor.getValue().get(0).getEngagementRate());
    }

    @Test
    void testG_Rounding() {
        Double rate1 = creatorAnalyticsService.calculateEngagementRate(100, 4526, 0, 1000);
        assertEquals(4.53, rate1);

        Double rate2 = creatorAnalyticsService.calculateEngagementRate(100, 4524, 0, 1000);
        assertEquals(4.52, rate2);
    }

    @Test
    void testH_MultipleCreators() {
        User user1 = createMockUser(1L, 1000);
        CreatorProfile profile1 = createMockProfile(1L, user1);

        User user2 = createMockUser(2L, 50); // ineligible
        CreatorProfile profile2 = createMockProfile(2L, user2);

        when(entityManager.createQuery(anyString(), eq(CreatorProfile.class))).thenReturn(profileQuery);
        when(profileQuery.setFirstResult(anyInt())).thenReturn(profileQuery);
        when(profileQuery.setMaxResults(anyInt())).thenReturn(profileQuery);
        when(profileQuery.getResultList()).thenReturn(Arrays.asList(profile1, profile2)).thenReturn(Collections.emptyList());

        TypedQuery<Object[]> postQuery = mock(TypedQuery.class);
        TypedQuery<Object[]> likeQuery = mock(TypedQuery.class);
        TypedQuery<Object[]> commentQuery = mock(TypedQuery.class);

        when(entityManager.createQuery(contains("FROM Content c"), eq(Object[].class))).thenReturn(postQuery);
        when(entityManager.createQuery(contains("FROM ContentLike c"), eq(Object[].class))).thenReturn(likeQuery);
        when(entityManager.createQuery(contains("FROM Comment c"), eq(Object[].class))).thenReturn(commentQuery);

        when(postQuery.setParameter(anyString(), any())).thenReturn(postQuery);
        when(likeQuery.setParameter(anyString(), any())).thenReturn(likeQuery);
        when(commentQuery.setParameter(anyString(), any())).thenReturn(commentQuery);

        when(postQuery.getResultList()).thenReturn(Arrays.asList(new Object[]{1L, 10L}, new Object[]{2L, 5L}));
        when(likeQuery.getResultList()).thenReturn(Arrays.asList(new Object[]{1L, 400L}, new Object[]{2L, 100L}));
        when(commentQuery.getResultList()).thenReturn(Collections.singletonList(new Object[]{1L, 50L}));

        creatorAnalyticsService.calculateEngagementRates();

        verify(creatorProfileRepository).saveAll(profilesCaptor.capture());
        List<CreatorProfile> saved = profilesCaptor.getValue();

        CreatorProfile saved1 = saved.stream().filter(p -> p.getUser().getId() == 1L).findFirst().get();
        CreatorProfile saved2 = saved.stream().filter(p -> p.getUser().getId() == 2L).findFirst().get();

        assertEquals(4.50, saved1.getEngagementRate());
        assertNull(saved2.getEngagementRate());
    }
}
