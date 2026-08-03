package com.adil.supportdesk.adapter.in.web.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddCommentRequest(

        @NotBlank(
                message = "Comment content cannot be empty"
        )
        @Size(
                max = 2000,
                message = "Comment cannot exceed 2000 characters"
        )
        String content
) {
}