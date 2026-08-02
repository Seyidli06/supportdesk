package com.adil.supportdesk.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(
        @NotBlank(message = "Title cannot be empty")
        @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
        String title,

        @NotBlank(message = "Description cannot be empty")
        String description
) {}