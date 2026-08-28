package com.creatorverse.social.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CommentRequest {

    @NotNull(message = "Comment text cannot be null")
    @NotBlank(message = "Comment text cannot be blank")
    private String text;

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
