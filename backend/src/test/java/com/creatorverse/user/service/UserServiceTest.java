package com.creatorverse.user.service;

import com.creatorverse.common.exception.DuplicateResourceException;
import com.creatorverse.user.dto.UserCreateRequest;
import com.creatorverse.user.entity.Role;
import com.creatorverse.user.entity.User;
import com.creatorverse.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createUser_DuplicateUsername() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("existing");
        
        when(userRepository.existsByUsername("existing")).thenReturn(true);
        
        assertThrows(DuplicateResourceException.class, () -> userService.createUser(request));
    }

    @Test
    void createUser_DuplicateEmail() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newuser");
        request.setEmail("existing@example.com");
        
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);
        
        assertThrows(DuplicateResourceException.class, () -> userService.createUser(request));
    }
}
