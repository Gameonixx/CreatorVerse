package com.creatorverse.collaboration.deliverable.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public class DeliverableSubmissionRequest {

    @NotBlank(message = "Submission URL is required")
    @URL(message = "Must be a valid URL")
    private String submissionUrl;

    public DeliverableSubmissionRequest() {}

    public String getSubmissionUrl() { return submissionUrl; }
    public void setSubmissionUrl(String submissionUrl) { this.submissionUrl = submissionUrl; }
}
