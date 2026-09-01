package com.creatorverse.user.controller;

import com.creatorverse.user.dto.UserCreateRequest;
import com.creatorverse.user.dto.UserResponse;
import com.creatorverse.user.dto.UserUpdateRequest;
import com.creatorverse.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.creatorverse.auth.security.SecurityUtils;
import com.creatorverse.common.exception.ForbiddenException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        verifyOwnershipOrAdmin(id);
        UserResponse response = userService.getUser(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<UserResponse> getPublicUser(@PathVariable Long id) {
        UserResponse response = userService.getUser(id);
        response.setEmail(null); // Explicitly remove sensitive data for public endpoint
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUser() {
        String currentUsername = SecurityUtils.getCurrentUsername();
        if (currentUsername == null) {
            throw new ForbiddenException("Not authenticated");
        }
        UserResponse response = userService.getUserByUsername(currentUsername);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> responses = userService.getAllUsers();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        verifyOwnershipOrAdmin(id);
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        verifyOwnershipOrAdmin(id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    private void verifyOwnershipOrAdmin(Long id) {
        if (SecurityUtils.hasRole("ADMIN")) return;
        String currentUsername = SecurityUtils.getCurrentUsername();
        if (currentUsername == null) {
            throw new ForbiddenException("Not authenticated");
        }
        UserResponse user = userService.getUser(id);
        if (!user.getUsername().equals(currentUsername)) {
            throw new ForbiddenException("You do not have permission to access this resource");
        }
    }
}
