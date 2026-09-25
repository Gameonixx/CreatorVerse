package com.creatorverse.messaging.service;

import com.creatorverse.campaign.entity.Campaign;
import com.creatorverse.collaboration.entity.Collaboration;
import com.creatorverse.collaboration.entity.enums.CollaborationStatus;
import com.creatorverse.collaboration.repository.CollaborationRepository;
import com.creatorverse.common.exception.ForbiddenException;
import com.creatorverse.messaging.dto.ConversationResponse;
import com.creatorverse.messaging.dto.MessageResponse;
import com.creatorverse.messaging.entity.Conversation;
import com.creatorverse.messaging.entity.Message;
import com.creatorverse.messaging.repository.ConversationRepository;
import com.creatorverse.messaging.repository.MessageRepository;
import com.creatorverse.user.entity.User;
import com.creatorverse.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MessagingServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private CollaborationRepository collaborationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MessagingService messagingService;

    private User brandUser;
    private User creatorUser;
    private User otherUser;
    private Campaign campaign;
    private Collaboration collaboration;
    private Conversation conversation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        brandUser = new User();
        brandUser.setId(1L);
        brandUser.setDisplayName("Brand");

        creatorUser = new User();
        creatorUser.setId(2L);
        creatorUser.setDisplayName("Creator");

        otherUser = new User();
        otherUser.setId(3L);

        campaign = new Campaign();
        campaign.setId(10L);
        campaign.setBrandUser(brandUser);

        collaboration = new Collaboration();
        collaboration.setId(100L);
        collaboration.setCampaign(campaign);
        collaboration.setCreatorUser(creatorUser);
        collaboration.setStatus(CollaborationStatus.ACTIVE);

        conversation = new Conversation();
        conversation.setId(1000L);
        conversation.setCollaboration(collaboration);
    }

    @Test
    void getConversation_asBrand_success() {
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));
        when(conversationRepository.findByCollaborationId(100L)).thenReturn(Optional.of(conversation));
        when(messageRepository.countUnreadMessages(1000L, 1L)).thenReturn(5L);

        ConversationResponse response = messagingService.getConversation(100L, 1L);

        assertNotNull(response);
        assertEquals(1000L, response.getId());
        assertEquals(5L, response.getUnreadCount());
    }

    @Test
    void getConversation_asCreator_success() {
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));
        when(conversationRepository.findByCollaborationId(100L)).thenReturn(Optional.of(conversation));
        when(messageRepository.countUnreadMessages(1000L, 2L)).thenReturn(0L);

        ConversationResponse response = messagingService.getConversation(100L, 2L);

        assertNotNull(response);
        assertEquals(0L, response.getUnreadCount());
    }

    @Test
    void getConversation_asOtherUser_throwsForbidden() {
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));

        assertThrows(ForbiddenException.class, () -> messagingService.getConversation(100L, 3L));
    }

    @Test
    void getMessages_asOtherUser_throwsForbidden() {
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));

        assertThrows(ForbiddenException.class, () -> messagingService.getMessages(100L, 3L, Pageable.unpaged()));
    }

    @Test
    void sendMessage_asBrand_success() {
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));
        when(conversationRepository.findByCollaborationId(100L)).thenReturn(Optional.of(conversation));
        when(userRepository.findById(1L)).thenReturn(Optional.of(brandUser));
        
        Message message = new Message();
        message.setId(500L);
        message.setConversation(conversation);
        message.setSenderUser(brandUser);
        message.setContent("Hello Creator!");

        when(messageRepository.save(any(Message.class))).thenReturn(message);

        MessageResponse response = messagingService.sendMessage(100L, 1L, "Hello Creator!");

        assertNotNull(response);
        assertEquals("Hello Creator!", response.getContent());
        assertEquals(1L, response.getSenderUserId());
    }

    @Test
    void sendMessage_asCreator_success() {
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));
        when(conversationRepository.findByCollaborationId(100L)).thenReturn(Optional.of(conversation));
        when(userRepository.findById(2L)).thenReturn(Optional.of(creatorUser));
        
        Message message = new Message();
        message.setId(501L);
        message.setConversation(conversation);
        message.setSenderUser(creatorUser);
        message.setContent("Hello Brand!");

        when(messageRepository.save(any(Message.class))).thenReturn(message);

        MessageResponse response = messagingService.sendMessage(100L, 2L, "Hello Brand!");

        assertNotNull(response);
        assertEquals("Hello Brand!", response.getContent());
        assertEquals(2L, response.getSenderUserId());
    }

    @Test
    void sendMessage_notActive_throwsException() {
        collaboration.setStatus(CollaborationStatus.COMPLETED);
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));

        assertThrows(IllegalStateException.class, () -> messagingService.sendMessage(100L, 1L, "Should fail"));
    }

    @Test
    void getConversation_createsIfNotFound() {
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));
        when(conversationRepository.findByCollaborationId(100L)).thenReturn(Optional.empty());
        when(conversationRepository.saveAndFlush(any(Conversation.class))).thenReturn(conversation);
        when(messageRepository.countUnreadMessages(anyLong(), anyLong())).thenReturn(0L);

        ConversationResponse response = messagingService.getConversation(100L, 1L);

        assertNotNull(response);
        verify(conversationRepository, times(1)).saveAndFlush(any(Conversation.class));
    }

    @Test
    void markAsRead_callsRepository() {
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));
        when(conversationRepository.findByCollaborationId(100L)).thenReturn(Optional.of(conversation));

        messagingService.markAsRead(100L, 1L);

        verify(messageRepository, times(1)).markMessagesAsRead(1000L, 1L);
    }
}
