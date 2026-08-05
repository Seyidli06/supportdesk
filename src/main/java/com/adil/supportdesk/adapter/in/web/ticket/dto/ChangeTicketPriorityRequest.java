package com.adil.supportdesk.adapter.in.web.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangeTicketPriorityRequest(

        @NotBlank(
                message = "Priority cannot be empty"
        )
        @Pattern(
                regexp = "(?i)^(LOW|MEDIUM|HIGH|URGENT)$",
                message =
                        "Priority must be LOW, MEDIUM, "
                                + "HIGH or URGENT"
        )
        String priority
) {
}