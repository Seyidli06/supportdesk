package com.adil.supportdesk.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AssignTicketRequest(

        @NotBlank(message = "Agent id cannot be empty")
        String agentId
) {
}