package com.creatorverse.social.service;

import com.creatorverse.common.exception.ForbiddenException;
import com.creatorverse.common.exception.ResourceNotFoundException;
import com.creatorverse.content.entity.Content;
import com.creatorverse.content.repository.ContentRepository;
import com.creatorverse.social.dto.CommentRequest;
import com.creatorverse.social.dto.CommentResponse;
import com.creatorverse.social.entity.Comment;
import com.creatorverse.social.repository.CommentRepository;
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

class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ContentRepository contentRepository;

    @InjectMocks
    private CommentService commentService;

    private User user;
    private User otherUser;
    private Content content;
    private Comment comment;
    private CommentRequest commentRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setDisplayName("Test User");

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");

        content = new Content();
        content.setId(10L);

        comment = new Comment();
        comment.setId(100L);
        comment.setUser(user);
        comment.setContent(content);
        comment.setText("Great content!");
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        commentRequest = new CommentRequest();
        commentRequest.setText("Great content!");
    }

    @Test
    void createComment_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(contentRepository.findById(10L)).thenReturn(Optional.of(content));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentResponse response = commentService.createComment("testuser", 10L, commentRequest);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("Great content!", response.getText());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createComment_UserNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.createComment("testuser", 10L, commentRequest));
    }

    @Test
    void createComment_ContentNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(contentRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.createComment("testuser", 10L, commentRequest));
    }

    @Test
    void getComments_Success() {
        when(contentRepository.findById(10L)).thenReturn(Optional.of(content));
        when(commentRepository.findByContentOrderByCreatedAtAsc(content)).thenReturn(List.of(comment));

        List<CommentResponse> responses = commentService.getComments(10L);

        assertEquals(1, responses.size());
        assertEquals(100L, responses.get(0).getId());
    }

    @Test
    void getComments_ContentNotFound() {
        when(contentRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.getComments(10L));
    }

    @Test
    void deleteComment_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));

        commentService.deleteComment("testuser", 100L);

        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_CommentNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(commentRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.deleteComment("testuser", 100L));
    }

    @Test
    void deleteComment_Forbidden() {
        when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));
        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment)); // belongs to 'user'

        assertThrows(ForbiddenException.class, () -> commentService.deleteComment("otheruser", 100L));
    }
}
