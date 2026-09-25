package com.creatorverse.messaging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MessageSendRequest {
    
    @NotBlank
    @Size(max = 5000)
    private String content;

    public MessageSendRequest() {}

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
