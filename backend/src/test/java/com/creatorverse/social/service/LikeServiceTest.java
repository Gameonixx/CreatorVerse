package com.creatorverse.social.service;

import com.creatorverse.common.exception.DuplicateResourceException;
import com.creatorverse.common.exception.ResourceNotFoundException;
import com.creatorverse.content.entity.Content;
import com.creatorverse.content.repository.ContentRepository;
import com.creatorverse.social.dto.LikeResponse;
import com.creatorverse.social.entity.ContentLike;
import com.creatorverse.social.repository.ContentLikeRepository;
import com.creatorverse.user.entity.User;
import com.creatorverse.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LikeServiceTest {

    @Mock
    private ContentLikeRepository contentLikeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ContentRepository contentRepository;

    @InjectMocks
    private LikeService likeService;

    private User user;
    private Content content;
    private ContentLike contentLike;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        content = new Content();
        content.setId(10L);

        contentLike = new ContentLike();
        contentLike.setId(100L);
        contentLike.setUser(user);
        contentLike.setContent(content);
        contentLike.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void likeContent_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(contentRepository.findById(10L)).thenReturn(Optional.of(content));
        when(contentLikeRepository.existsByUserAndContent(user, content)).thenReturn(false);
        when(contentLikeRepository.save(any(ContentLike.class))).thenReturn(contentLike);

        LikeResponse response = likeService.likeContent("testuser", 10L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(1L, response.getUserId());
        assertEquals(10L, response.getContentId());
        verify(contentLikeRepository).save(any(ContentLike.class));
    }

    @Test
    void likeContent_UserNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> likeService.likeContent("testuser", 10L));
    }

    @Test
    void likeContent_ContentNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(contentRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> likeService.likeContent("testuser", 10L));
    }

    @Test
    void likeContent_DuplicateLike() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(contentRepository.findById(10L)).thenReturn(Optional.of(content));
        when(contentLikeRepository.existsByUserAndContent(user, content)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> likeService.likeContent("testuser", 10L));
    }

    @Test
    void unlikeContent_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(contentRepository.findById(10L)).thenReturn(Optional.of(content));
        when(contentLikeRepository.findByUserAndContent(user, content)).thenReturn(Optional.of(contentLike));

        likeService.unlikeContent("testuser", 10L);

        verify(contentLikeRepository).delete(contentLike);
    }

    @Test
    void unlikeContent_NotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(contentRepository.findById(10L)).thenReturn(Optional.of(content));
        when(contentLikeRepository.findByUserAndContent(user, content)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> likeService.unlikeContent("testuser", 10L));
    }

    @Test
    void getLikes_Success() {
        when(contentRepository.findById(10L)).thenReturn(Optional.of(content));
        when(contentLikeRepository.findByContent(content)).thenReturn(List.of(contentLike));

        List<LikeResponse> likes = likeService.getLikes(10L);

        assertEquals(1, likes.size());
        assertEquals(100L, likes.get(0).getId());
    }

    @Test
    void getLikes_ContentNotFound() {
        when(contentRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> likeService.getLikes(10L));
    }
}
