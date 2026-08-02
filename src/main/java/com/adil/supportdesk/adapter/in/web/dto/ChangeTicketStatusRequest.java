package com.adil.supportdesk.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangeTicketStatusRequest(

        @NotBlank(message = "Status cannot be empty")
        @Pattern(
                regexp =
                        "(?i)OPEN|IN_PROGRESS|WAITING_CUSTOMER|RESOLVED|CLOSED",
                message =
                        "Status must be OPEN, IN_PROGRESS, "
                                + "WAITING_CUSTOMER, RESOLVED or CLOSED"
        )
        String status
) {
}