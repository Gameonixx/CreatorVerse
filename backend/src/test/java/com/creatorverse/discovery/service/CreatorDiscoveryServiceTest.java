package com.creatorverse.discovery.service;

import com.creatorverse.creator.entity.CreatorProfile;
import com.creatorverse.creator.repository.CreatorProfileRepository;
import com.creatorverse.discovery.dto.CreatorCardResponse;
import com.creatorverse.user.entity.Role;
import com.creatorverse.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreatorDiscoveryServiceTest {

    @Mock
    private CreatorProfileRepository creatorProfileRepository;

    @InjectMocks
    private CreatorDiscoveryService creatorDiscoveryService;

    private CreatorProfile creatorProfile;

    @BeforeEach
    void setUp() {
        User user = new User("creator1", "test@test.com", "Creator One", Role.USER);
        user.setId(2L);
        user.setFollowerCount(1000);
        
        creatorProfile = new CreatorProfile();
        creatorProfile.setUser(user);
        creatorProfile.setNiche("Fashion");
        creatorProfile.setEngagementRate(5.0);
    }

    @Test
    void testDiscoverCreators_MapsCorrectly() {
        Page<CreatorProfile> page = new PageImpl<>(List.of(creatorProfile));
        when(creatorProfileRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        Page<CreatorCardResponse> response = creatorDiscoveryService.discoverCreators("creator", "Fashion", 500, null, PageRequest.of(0, 10));

        assertEquals(1, response.getContent().size());
        CreatorCardResponse card = response.getContent().get(0);
        assertEquals(2L, card.getUserId());
        assertEquals("creator1", card.getUsername());
        assertEquals("Creator One", card.getDisplayName());
        assertEquals("Fashion", card.getNiche());
        assertEquals(1000, card.getFollowerCount());
    }
}
