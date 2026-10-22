package com.feedbackportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class FeedbackDto {

    @NotBlank(message = "Feedback cannot be empty")
    @Size(max = 5000, message = "Feedback must be at most 5000 characters")
    private String feedback;


    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
