package com.creatorverse.analytics.service;

import com.creatorverse.content.entity.enums.ContentStatus;
import com.creatorverse.content.repository.ContentRepository;
import com.creatorverse.creator.entity.CreatorProfile;
import com.creatorverse.creator.repository.CreatorProfileRepository;
import com.creatorverse.social.repository.CommentRepository;
import com.creatorverse.social.repository.ContentLikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CreatorAnalyticsService {

    private final CreatorProfileRepository creatorProfileRepository;
    private final ContentRepository contentRepository;
    private final ContentLikeRepository contentLikeRepository;
    private final CommentRepository commentRepository;

    public static final long MINIMUM_FOLLOWERS = 100;
    public static final long MINIMUM_PUBLISHED_POSTS = 5;
    public static final long MINIMUM_INTERACTIONS = 20;

    public CreatorAnalyticsService(CreatorProfileRepository creatorProfileRepository,
                                   ContentRepository contentRepository,
                                   ContentLikeRepository contentLikeRepository,
                                   CommentRepository commentRepository) {
        this.creatorProfileRepository = creatorProfileRepository;
        this.contentRepository = contentRepository;
        this.contentLikeRepository = contentLikeRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional
    public void calculateEngagementRates() {
        // Fetch all aggregates
        List<Object[]> postCountsRaw = contentRepository.countPostsPerCreatorByStatus(ContentStatus.PUBLISHED);
        List<Object[]> likeCountsRaw = contentLikeRepository.countLikesOnPostsPerCreatorByStatus(ContentStatus.PUBLISHED);
        List<Object[]> commentCountsRaw = commentRepository.countCommentsOnPostsPerCreatorByStatus(ContentStatus.PUBLISHED);

        Map<Long, Long> postCounts = convertToMap(postCountsRaw);
        Map<Long, Long> likeCounts = convertToMap(likeCountsRaw);
        Map<Long, Long> commentCounts = convertToMap(commentCountsRaw);

        List<CreatorProfile> allProfiles = creatorProfileRepository.findAll();

        for (CreatorProfile profile : allProfiles) {
            Long creatorId = profile.getUser().getId();
            long totalPosts = postCounts.getOrDefault(creatorId, 0L);
            long totalLikes = likeCounts.getOrDefault(creatorId, 0L);
            long totalComments = commentCounts.getOrDefault(creatorId, 0L);
            long followers = profile.getUser().getFollowerCount() != null ? profile.getUser().getFollowerCount() : 0L;

            Double engagementRate = calculateEngagementRate(totalPosts, totalLikes, totalComments, followers);
            profile.setEngagementRate(engagementRate);
        }

        creatorProfileRepository.saveAll(allProfiles);
    }

    private Map<Long, Long> convertToMap(List<Object[]> rawList) {
        return rawList.stream()
                .collect(Collectors.toMap(
                        arr -> ((Number) arr[0]).longValue(),
                        arr -> ((Number) arr[1]).longValue()
                ));
    }

    public Double calculateEngagementRate(long totalPosts, long totalLikes, long totalComments, long followers) {
        long totalInteractions = totalLikes + totalComments;

        boolean hasMinimumInteractions = totalInteractions >= MINIMUM_INTERACTIONS || totalInteractions == 0;

        if (followers < MINIMUM_FOLLOWERS || totalPosts < MINIMUM_PUBLISHED_POSTS || !hasMinimumInteractions) {
            return null;
        }

        if (totalInteractions == 0) {
            return 0.00;
        }

        long safeFollowers = Math.max(1, followers);
        double averageInteractionsPerPost = (double) totalInteractions / totalPosts;

        double rawEngagementRate = (averageInteractionsPerPost / safeFollowers) * 100.0;
        double displayEngagementRate = Math.min(rawEngagementRate, 100.00);

        return BigDecimal.valueOf(displayEngagementRate)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
