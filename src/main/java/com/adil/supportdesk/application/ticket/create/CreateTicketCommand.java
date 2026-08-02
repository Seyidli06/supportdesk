package com.adil.supportdesk.application.ticket.create;

import com.adil.supportdesk.domain.ticket.model.TicketPriority;

import java.util.Objects;

public record CreateTicketCommand(
        String title,
        String description,
        TicketPriority priority
) {

    public CreateTicketCommand {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Title cannot be empty"
            );
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Description cannot be empty"
            );
        }

        Objects.requireNonNull(
                priority,
                "Priority cannot be null"
        );
    }
}