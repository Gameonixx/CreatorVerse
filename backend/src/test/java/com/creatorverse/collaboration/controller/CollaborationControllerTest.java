package com.creatorverse.collaboration.controller;

import com.creatorverse.auth.security.SecurityUtils;
import com.creatorverse.collaboration.dto.CollaborationResponse;
import com.creatorverse.collaboration.dto.CollaborationStatusUpdateRequest;
import com.creatorverse.collaboration.entity.enums.CollaborationStatus;
import com.creatorverse.collaboration.service.CollaborationService;
import com.creatorverse.user.entity.User;
import com.creatorverse.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CollaborationControllerTest {

    @Mock
    private CollaborationService collaborationService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CollaborationController collaborationController;

    private MockedStatic<SecurityUtils> securityUtilsMock;
    private User currentUser;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUsername).thenReturn("testuser");

        currentUser = new User();
        currentUser.setId(10L);
        currentUser.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(currentUser));
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    void getMyCollaborations_ReturnsPage() {
        CollaborationResponse response = new CollaborationResponse();
        response.setId(1L);
        Page<CollaborationResponse> page = new PageImpl<>(List.of(response));

        when(collaborationService.getMyCollaborations(eq(10L), any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<CollaborationResponse>> result = collaborationController.getMyCollaborations(0, 10, "createdAt", "desc");

        assertEquals(200, result.getStatusCode().value());
        assertEquals(1, result.getBody().getContent().size());
    }

    @Test
    void getCollaboration_ReturnsCollaboration() {
        CollaborationResponse response = new CollaborationResponse();
        response.setId(1L);

        when(collaborationService.getCollaboration(1L, 10L)).thenReturn(response);

        ResponseEntity<CollaborationResponse> result = collaborationController.getCollaboration(1L);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(1L, result.getBody().getId());
    }

    @Test
    void updateCollaborationStatus_ReturnsUpdated() {
        CollaborationResponse response = new CollaborationResponse();
        response.setId(1L);
        response.setStatus(CollaborationStatus.COMPLETED);

        CollaborationStatusUpdateRequest request = new CollaborationStatusUpdateRequest();
        request.setStatus(CollaborationStatus.COMPLETED);

        when(collaborationService.updateStatus(1L, 10L, CollaborationStatus.COMPLETED)).thenReturn(response);

        ResponseEntity<CollaborationResponse> result = collaborationController.updateCollaborationStatus(1L, request);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(CollaborationStatus.COMPLETED, result.getBody().getStatus());
    }
}
