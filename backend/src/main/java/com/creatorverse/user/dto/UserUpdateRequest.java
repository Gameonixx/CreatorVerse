package com.creatorverse.user.dto;

import jakarta.validation.constraints.Size;

public class UserUpdateRequest {
    @Size(max = 100, message = "Display name cannot exceed 100 characters")
    private String displayName;

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}
