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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ContentRepository contentRepository;

    public CommentService(CommentRepository commentRepository, UserRepository userRepository, ContentRepository contentRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.contentRepository = contentRepository;
    }

    @Transactional
    public CommentResponse createComment(String currentUsername, Long contentId, CommentRequest request) {
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found"));

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setContent(content);
        comment.setText(request.getText());

        comment = commentRepository.save(comment);
        return mapToResponse(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long contentId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found"));

        return commentRepository.findByContentOrderByCreatedAtAsc(content).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(String currentUsername, Long commentId) {
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You cannot delete another user's comment");
        }

        commentRepository.delete(comment);
    }

    private CommentResponse mapToResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setUserId(comment.getUser().getId());
        response.setUsername(comment.getUser().getUsername());
        response.setUserDisplayName(comment.getUser().getDisplayName());
        response.setContentId(comment.getContent().getId());
        response.setText(comment.getText());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        return response;
    }
}
