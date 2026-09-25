package com.creatorverse.messaging.service;

import com.creatorverse.collaboration.entity.Collaboration;
import com.creatorverse.collaboration.entity.enums.CollaborationStatus;
import com.creatorverse.collaboration.repository.CollaborationRepository;
import com.creatorverse.common.exception.ForbiddenException;
import com.creatorverse.common.exception.ResourceNotFoundException;
import com.creatorverse.messaging.dto.ConversationResponse;
import com.creatorverse.messaging.dto.MessageResponse;
import com.creatorverse.messaging.dto.UnreadCountResponse;
import com.creatorverse.messaging.entity.Conversation;
import com.creatorverse.messaging.entity.Message;
import com.creatorverse.messaging.repository.ConversationRepository;
import com.creatorverse.messaging.repository.MessageRepository;
import com.creatorverse.user.entity.User;
import com.creatorverse.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessagingService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final CollaborationRepository collaborationRepository;
    private final UserRepository userRepository;

    public MessagingService(ConversationRepository conversationRepository,
                            MessageRepository messageRepository,
                            CollaborationRepository collaborationRepository,
                            UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.collaborationRepository = collaborationRepository;
        this.userRepository = userRepository;
    }

    private Collaboration getAuthorizedCollaboration(Long collaborationId, Long currentUserId) {
        Collaboration collaboration = collaborationRepository.findById(collaborationId)
                .orElseThrow(() -> new ResourceNotFoundException("Collaboration not found"));

        if (!collaboration.getCreatorUser().getId().equals(currentUserId) &&
            !collaboration.getCampaign().getBrandUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("Not authorized to access this collaboration's conversation");
        }

        return collaboration;
    }

    @Transactional
    public ConversationResponse getConversation(Long collaborationId, Long currentUserId) {
        Collaboration collaboration = getAuthorizedCollaboration(collaborationId, currentUserId);

        Conversation conversation = conversationRepository.findByCollaborationId(collaborationId)
                .orElseGet(() -> {
                    Conversation newConv = new Conversation();
                    newConv.setCollaboration(collaboration);
                    try {
                        return conversationRepository.saveAndFlush(newConv);
                    } catch (DataIntegrityViolationException e) {
                        return conversationRepository.findByCollaborationId(collaborationId)
                                .orElseThrow(() -> new IllegalStateException("Failed to retrieve conversation after concurrent creation"));
                    }
                });

        long unreadCount = messageRepository.countUnreadMessages(conversation.getId(), currentUserId);

        ConversationResponse response = new ConversationResponse();
        response.setId(conversation.getId());
        response.setCollaborationId(collaborationId);
        response.setUnreadCount(unreadCount);
        response.setCreatedAt(conversation.getCreatedAt());
        response.setUpdatedAt(conversation.getUpdatedAt());

        return response;
    }

    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessages(Long collaborationId, Long currentUserId, Pageable pageable) {
        Collaboration collaboration = getAuthorizedCollaboration(collaborationId, currentUserId);
        
        return conversationRepository.findByCollaborationId(collaborationId)
                .map(conversation -> messageRepository.findByConversationId(conversation.getId(), pageable)
                        .map(this::mapToMessageResponse))
                .orElse(Page.empty(pageable));
    }

    @Transactional
    public MessageResponse sendMessage(Long collaborationId, Long currentUserId, String content) {
        Collaboration collaboration = getAuthorizedCollaboration(collaborationId, currentUserId);

        if (collaboration.getStatus() != CollaborationStatus.ACTIVE) {
            throw new IllegalStateException("Cannot send message in a non-active collaboration");
        }

        Conversation conversation = conversationRepository.findByCollaborationId(collaborationId)
                .orElseGet(() -> {
                    Conversation newConv = new Conversation();
                    newConv.setCollaboration(collaboration);
                    try {
                        return conversationRepository.saveAndFlush(newConv);
                    } catch (DataIntegrityViolationException e) {
                        return conversationRepository.findByCollaborationId(collaborationId)
                                .orElseThrow(() -> new IllegalStateException("Failed to retrieve conversation after concurrent creation"));
                    }
                });

        User sender = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Message message = new Message();
        message.setConversation(conversation);
        message.setSenderUser(sender);
        message.setContent(content.trim());

        Message saved = messageRepository.save(message);

        return mapToMessageResponse(saved);
    }

    @Transactional
    public UnreadCountResponse markAsRead(Long collaborationId, Long currentUserId) {
        Collaboration collaboration = getAuthorizedCollaboration(collaborationId, currentUserId);
        
        conversationRepository.findByCollaborationId(collaborationId).ifPresent(conversation -> {
            messageRepository.markMessagesAsRead(conversation.getId(), currentUserId);
        });
        
        return new UnreadCountResponse(0);
    }

    private MessageResponse mapToMessageResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setConversationId(message.getConversation().getId());
        response.setSenderUserId(message.getSenderUser().getId());
        response.setSenderName(message.getSenderUser().getDisplayName());
        response.setSenderAvatarUrl(message.getSenderUser().getAvatarUrl());
        response.setContent(message.getContent());
        response.setCreatedAt(message.getCreatedAt());
        response.setReadAt(message.getReadAt());
        return response;
    }
}
